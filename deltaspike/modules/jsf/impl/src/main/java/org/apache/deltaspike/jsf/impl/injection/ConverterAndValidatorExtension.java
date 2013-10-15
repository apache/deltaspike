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
package org.apache.deltaspike.jsf.impl.injection;

import org.apache.deltaspike.core.api.literal.RequestScopedLiteral;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.jsf.api.literal.ViewScopedLiteral;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.faces.bean.ViewScoped;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.inject.Scope;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Dependent scoped converters and validators need to have a restricted lifetime.
 * With upgrading them automatically, no manual handling is needed.
 */
public class ConverterAndValidatorExtension implements Extension, Deactivatable
{
    private boolean isActivated = true;

    private final Logger logger = Logger.getLogger(ConverterAndValidatorExtension.class.getName());

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    protected void upgradeDependentScopedConvertersAndValidators(@Observes ProcessAnnotatedType processAnnotatedType)
    {
        if (!isActivated)
        {
            return;
        }

        Class beanClass = processAnnotatedType.getAnnotatedType().getJavaClass();

        if ((Converter.class.isAssignableFrom(beanClass) || Validator.class.isAssignableFrom(beanClass)) &&
                isDependentScoped(processAnnotatedType.getAnnotatedType().getAnnotations()))
        {
            processAnnotatedType.setAnnotatedType(convertBean(processAnnotatedType.getAnnotatedType()));
        }
    }

    private boolean isDependentScoped(Set<Annotation> annotations)
    {
        for (Annotation annotation : annotations)
        {
            //TODO discuss support of jsf scope-annotations (since they get mapped to cdi annotations automatically)
            if (annotation.annotationType().isAnnotationPresent(Scope.class) ||
                    annotation.annotationType().isAnnotationPresent(NormalScope.class))
            {
                return Dependent.class.equals(annotation.annotationType());
            }
        }

        //no scope annotation found -> dependent-scoped per convention
        return true;
    }

    private AnnotatedType convertBean(AnnotatedType annotatedType)
    {
        AnnotatedTypeBuilder annotatedTypeBuilder = new AnnotatedTypeBuilder()
                .readFromType(annotatedType)
                .removeFromClass(Dependent.class);

        if (Serializable.class.isAssignableFrom(annotatedType.getJavaClass()))
        {
            logConvertedBean(annotatedType, ViewScoped.class);
            annotatedTypeBuilder.addToClass(new ViewScopedLiteral());
        }
        else
        {
            logConvertedBean(annotatedType, RequestScoped.class);
            annotatedTypeBuilder.addToClass(new RequestScopedLiteral());
        }

        return annotatedTypeBuilder.create();
    }

    private void logConvertedBean(AnnotatedType annotatedType, Class<? extends Annotation> scope)
    {
        ProjectStage projectStage = ProjectStageProducer.getInstance().getProjectStage();

        if (projectStage == ProjectStage.Development)
        {
            logger.info("scope of " + annotatedType.getJavaClass().getName() + " was upgraded to @" + scope.getName());
        }
    }
}
