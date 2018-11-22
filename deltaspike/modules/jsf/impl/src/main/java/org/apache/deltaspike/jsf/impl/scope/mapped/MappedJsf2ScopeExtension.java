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
package org.apache.deltaspike.jsf.impl.scope.mapped;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.faces.bean.ManagedBean;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Maps JSF2 scopes to CDI scopes
 */
public class MappedJsf2ScopeExtension implements Extension, Deactivatable
{
    private boolean isActivated = true;

    private final Logger logger = Logger.getLogger(MappedJsf2ScopeExtension.class.getName());

    private Map<Class<? extends Annotation>, Class<? extends Annotation>> mappedJsfScopes
        = new HashMap<Class<? extends Annotation>, Class<? extends Annotation>>();

    /**
     * Default constructor which initializes the scope mapping
     */
    public MappedJsf2ScopeExtension()
    {
        // skip on JSF3.x
        if (ClassUtils.tryToLoadClassForName("javax.faces.bean.ApplicationScoped") != null)
        {
            this.mappedJsfScopes.put(javax.faces.bean.ApplicationScoped.class,
                    javax.enterprise.context.ApplicationScoped.class);
            this.mappedJsfScopes.put(javax.faces.bean.SessionScoped.class,
                    javax.enterprise.context.SessionScoped.class);
            this.mappedJsfScopes.put(javax.faces.bean.RequestScoped.class,
                    javax.enterprise.context.RequestScoped.class);

            if (JsfUtils.isViewScopeDelegationEnabled())
            {
                this.mappedJsfScopes.put(javax.faces.bean.ViewScoped.class,
                    ClassUtils.tryToLoadClassForName("javax.faces.view.ViewScoped"));
            }
        }
    }

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    protected void convertJsf2Scopes(@Observes ProcessAnnotatedType processAnnotatedType)
    {
        if (!isActivated)
        {
            return;
        }

        //TODO
        //CodiStartupBroadcaster.broadcastStartup();

        Class<? extends Annotation> jsf2ScopeAnnotation = getJsf2ScopeAnnotation(processAnnotatedType);

        if (jsf2ScopeAnnotation != null && !isBeanWithManagedBeanAnnotation(processAnnotatedType))
        {
            processAnnotatedType.setAnnotatedType(
                    convertBean(processAnnotatedType.getAnnotatedType(), jsf2ScopeAnnotation));
        }
    }

    private Class<? extends Annotation> getJsf2ScopeAnnotation(ProcessAnnotatedType processAnnotatedType)
    {
        for (Class<? extends Annotation> currentJsfScope : this.mappedJsfScopes.keySet())
        {
            if (processAnnotatedType.getAnnotatedType().getJavaClass().isAnnotationPresent(currentJsfScope))
            {
                return currentJsfScope;
            }
        }
        return null;
    }

    private boolean isBeanWithManagedBeanAnnotation(ProcessAnnotatedType processAnnotatedType)
    {
        Class<?> beanClass = processAnnotatedType.getAnnotatedType().getJavaClass();

        return beanClass.isAnnotationPresent(ManagedBean.class);
    }

    private AnnotatedType convertBean(AnnotatedType annotatedType, Class<? extends Annotation> jsf2ScopeAnnotation)
    {
        logConvertedBean(annotatedType, jsf2ScopeAnnotation);

        return new Jsf2BeanWrapper(annotatedType, this.mappedJsfScopes.get(jsf2ScopeAnnotation), jsf2ScopeAnnotation);
    }

    private void logConvertedBean(AnnotatedType annotatedType, Class<? extends Annotation> jsf2ScopeAnnotation)
    {
        ProjectStage projectStage = ProjectStageProducer.getInstance().getProjectStage();

        if (projectStage == ProjectStage.Development)
        {
            logger.info("JSF2 bean was converted to a CDI bean. Type: " + annotatedType.getJavaClass().getName() +
                    " original scope: " + jsf2ScopeAnnotation.getName());
        }
    }
}
