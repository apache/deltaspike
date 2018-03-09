/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.jpa.impl.transaction;

import org.apache.deltaspike.core.util.AnnotationUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerMetadata;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper which provides utility methods for any
 * {@link org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy}.
 */
@Dependent
public class TransactionStrategyHelper implements Serializable
{
    private static final long serialVersionUID = -6272327391611428125L;

    @Inject
    private BeanManager beanManager;

    /**
     * <p>This method uses the InvocationContext to scan the &#064;Transactional
     * interceptor for a manually specified Qualifier.</p>
     *
     * <p>If none is given (defaults to &#04;Any.class) then we scan the intercepted
     * instance and resolve the Qualifiers of all it's injected EntityManagers.</p>
     *
     * <p>Please note that we will only pickup the first Qualifier on the
     * injected EntityManager. We also do <b>not</b> parse for binding or
     * &h#064;NonBinding values. A &#064;Qualifier should not have any parameter at all.</p>
     * @param entityManagerMetadata the metadata to locate the entity manager
     * @param interceptedTargetClass the Class of the intercepted target
     */
    public Set<Class<? extends Annotation>> resolveEntityManagerQualifiers(EntityManagerMetadata entityManagerMetadata,
                                                                           Class interceptedTargetClass)
    {
        Set<Class<? extends Annotation>> emQualifiers = new HashSet<>();
        Class<? extends Annotation>[] qualifierClasses = entityManagerMetadata.getQualifiers();

        if (qualifierClasses == null || qualifierClasses.length == 1 && Any.class.equals(qualifierClasses[0]) )
        {
            // this means we have no special EntityManager configured in the interceptor
            // thus we should scan all the EntityManagers ourselfs from the intercepted class
            collectEntityManagerQualifiersOnClass(emQualifiers, interceptedTargetClass);
        }
        else
        {
            // take the qualifierKeys from the qualifierClasses
            Collections.addAll(emQualifiers, qualifierClasses);
        }

        //see DELTASPIKE-320
        if (emQualifiers.isEmpty())
        {
            emQualifiers.add(Default.class);
        }
        return emQualifiers;
    }

    /**
     * Scan the given class and return all the injected EntityManager fields.
     * <p>Attention: we do only pick up EntityManagers which use &#064;Inject!</p>
     */
    private void collectEntityManagerQualifiersOnClass(Set<Class<? extends Annotation>> emQualifiers,Class target)
    {
        // first scan all declared fields
        Field[] fields = target.getDeclaredFields();

        for (Field field : fields)
        {
            if (EntityManager.class.equals(field.getType()))
            {
                // also check if this is an injected EM
                if (field.getAnnotation(Inject.class) != null)
                {
                    boolean qualifierFound = false;
                    Class<? extends Annotation> qualifier = getFirstQualifierAnnotation(field.getAnnotations());
                    if (qualifier != null)
                    {
                        emQualifiers.add(qualifier);
                        qualifierFound = true;
                    }

                    if (!qualifierFound)
                    {
                        // according to the CDI injection rules @Default is assumed
                        emQualifiers.add(Default.class);
                    }
                }
            }
        }

        // finally recurse into the superclasses
        Class superClass = target.getSuperclass();
        if (!Object.class.equals(superClass))
        {
            collectEntityManagerQualifiersOnClass(emQualifiers, superClass);
        }
    }

    /**
     * Extract the first CDI-Qualifier Annotation from the given annotations array
     */
    private Class<? extends Annotation> getFirstQualifierAnnotation(Annotation[] annotations)
    {
        for (Annotation ann : annotations)
        {
            if (beanManager.isQualifier(ann.annotationType()))
            {
                return ann.annotationType();
            }
        }

        return null;
    }

    EntityManagerMetadata createEntityManagerMetadata(InvocationContext context)
    {
        EntityManagerMetadata metadata = new EntityManagerMetadata();
        metadata.readFrom(context.getMethod().getDeclaringClass(), beanManager);
        metadata.readFrom(context.getMethod(), beanManager);
        return metadata;
    }

    /**
     * @return the &#064;Transactional annotation from either the method or class
     *         or <code>null</code> if none present.
     */
    protected Transactional extractTransactionalAnnotation(InvocationContext context)
    {
        Class targetClass = context.getTarget() != null ? context.getTarget().getClass() :
            context.getMethod().getDeclaringClass();
        return AnnotationUtils
            .extractAnnotationFromMethodOrClass(beanManager, context.getMethod(), targetClass, Transactional.class);
    }
}
