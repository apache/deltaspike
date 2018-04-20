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
package org.apache.deltaspike.core.impl.exclude.extension;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.spi.filter.ClassFilter;
import org.apache.deltaspike.core.api.interpreter.ExpressionInterpreter;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.impl.exclude.CustomProjectStageBeanFilter;
import org.apache.deltaspike.core.impl.exclude.GlobalAlternative;
import org.apache.deltaspike.core.impl.interpreter.PropertyExpressionInterpreter;
import org.apache.deltaspike.core.impl.util.AnnotationInstanceUtils;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.alternative.AlternativeBeanClassProvider;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This class implements the logic for handling
 * {@link org.apache.deltaspike.core.api.exclude.Exclude} annotations.</p>
 * <p/>
 * <p>Further details see {@link org.apache.deltaspike.core.api.exclude.Exclude}</p>
 */
public class ExcludeExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(ExcludeExtension.class.getName());

    private boolean isActivated = true;
    private boolean isGlobalAlternativeActivated = true;
    private boolean isCustomProjectStageBeanFilterActivated = true;

    /**
     * Contains the globalAlternatives which should get used
     * KEY=Interface class name
     * VALUE=Implementation class name
     */
    private Map<String, String> globalAlternatives = new HashMap<String, String>();
    private Annotation priorityAnnotationInstance;

    //overruling the filter is supported via config-ordinal - for now only one is supported to keep it simple
    //a custom filter can always delegate to multiple filters
    //(e.g. in combination with ServiceUtils or querying all config-sources explicitly)
    private ClassFilter classFilter;

    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager)
    {
        isActivated =
                ClassDeactivationUtils.isActivated(getClass());

        isCustomProjectStageBeanFilterActivated =
                ClassDeactivationUtils.isActivated(CustomProjectStageBeanFilter.class);

        isGlobalAlternativeActivated =
                ClassDeactivationUtils.isActivated(GlobalAlternative.class);
        if (isGlobalAlternativeActivated)
        {
            loadGlobalAlternativeConfigs();

            if (globalAlternatives.isEmpty())
            {
                isGlobalAlternativeActivated = false;
            }

            if (isGlobalAlternativeActivated)
            {
                int priorityValue = CoreBaseConfig.InterceptorCustomization.PRIORITY;
                priorityAnnotationInstance = AnnotationInstanceUtils.getPriorityAnnotationInstance(priorityValue);
            }
        }

        boolean isClassFilterActivated = ClassDeactivationUtils.isActivated(ClassFilter.class);

        if (isClassFilterActivated)
        {
            String classFilterClassName = ClassFilter.class.getName();
            String activeClassFilterName =
                ConfigResolver.getProjectStageAwarePropertyValue(classFilterClassName, classFilterClassName);

            if (!classFilterClassName.equals(activeClassFilterName))
            {
                classFilter = ClassUtils.tryToInstantiateClassForName(activeClassFilterName, ClassFilter.class);
            }
        }
    }

    private void loadGlobalAlternativeConfigs()
    {
        List<AlternativeBeanClassProvider> alternativeBeanClassProviders =
            new ArrayList<AlternativeBeanClassProvider>();

        //add the default implementation first (if enabled), to give custom implementations a higher priority
        //(they can replace mappings defined by the default implementation)
        if (ClassDeactivationUtils.isActivated(LabelAwareGlobalAlternativeBeanClassProvider.class))
        {
            alternativeBeanClassProviders.add(new LabelAwareGlobalAlternativeBeanClassProvider());
        }

        alternativeBeanClassProviders.addAll(
            ServiceUtils.loadServiceImplementations(AlternativeBeanClassProvider.class));

        for (AlternativeBeanClassProvider currentProvider : alternativeBeanClassProviders)
        {
            Map<String, String> alternativeBeanMappings = currentProvider.getAlternativeMapping();
            globalAlternatives.putAll(alternativeBeanMappings);
        }
    }

    /**
     * triggers initialization in any case
     * @param afterDeploymentValidation observed event
     */
    @SuppressWarnings("UnusedDeclaration")
    protected void initProjectStage(@Observes AfterDeploymentValidation afterDeploymentValidation)
    {
        ProjectStageProducer.getInstance();
    }

    /**
     * Observer which is vetoing beans based on {@link Exclude}
     * @param processAnnotatedType observed event
     */
    @SuppressWarnings("UnusedDeclaration")
    protected void vetoBeans(@Observes ProcessAnnotatedType processAnnotatedType, BeanManager beanManager)
    {
        //we need to do it before the exclude logic to keep the @Exclude support for global alternatives
        if (isGlobalAlternativeActivated)
        {
            activateGlobalAlternatives(processAnnotatedType, beanManager);
        }

        if (isCustomProjectStageBeanFilterActivated)
        {
            vetoCustomProjectStageBeans(processAnnotatedType);
        }

        if (!isActivated)
        {
            return;
        }

        if (classFilter != null)
        {
            Class<?> beanClass = processAnnotatedType.getAnnotatedType().getJavaClass();

            if (classFilter.isFiltered(beanClass))
            {
                veto(processAnnotatedType, classFilter.getClass().getName());
                return;
            }
        }

        //TODO needs further discussions for a different feature CodiStartupBroadcaster.broadcastStartup();

        //also forces deterministic project-stage initialization
        ProjectStage projectStage = ProjectStageProducer.getInstance().getProjectStage();

        Exclude exclude = extractExcludeAnnotation(processAnnotatedType.getAnnotatedType().getJavaClass());

        if (exclude == null)
        {
            return;
        }

        if (!evalExcludeWithoutCondition(processAnnotatedType, exclude))
        {
            return; //veto called already
        }

        if (!evalExcludeInProjectStage(processAnnotatedType, exclude, projectStage))
        {
            return; //veto called already
        }

        if (!evalExcludeNotInProjectStage(processAnnotatedType, exclude, projectStage))
        {
            return; //veto called already
        }

        evalExcludeWithExpression(processAnnotatedType, exclude);
    }

    //only support the physical usage and inheritance if @Exclude comes from an abstract class
    //TODO re-visit the impact of java.lang.annotation.Inherited (for @Exclude) for the available use-cases
    protected Exclude extractExcludeAnnotation(Class<?> currentClass)
    {
        Exclude result = currentClass.getAnnotation(Exclude.class);

        if (result != null)
        {
            return result;
        }

        currentClass = currentClass.getSuperclass();

        while (!Object.class.equals(currentClass) && currentClass != null)
        {
            if (Modifier.isAbstract(currentClass.getModifiers()))
            {
                result = currentClass.getAnnotation(Exclude.class);
            }

            if (result != null)
            {
                return result;
            }

            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    protected void vetoCustomProjectStageBeans(ProcessAnnotatedType processAnnotatedType)
    {
        //currently there is a veto for all project-stage implementations,
        //but we still need @Typed() for the provided implementations in case of the deactivation of this behaviour
        if (ProjectStage.class.isAssignableFrom(processAnnotatedType.getAnnotatedType().getJavaClass()))
        {
            processAnnotatedType.veto();
        }
    }



    private void activateGlobalAlternatives(ProcessAnnotatedType processAnnotatedType,
                                            BeanManager beanManager)
    {
        Class<Object> currentBean = processAnnotatedType.getAnnotatedType().getJavaClass();

        if (currentBean.isInterface())
        {
            return;
        }

        Set<Class> beanBaseTypes = resolveBeanTypes(currentBean);

        boolean isAlternativeBeanImplementation = currentBean.isAnnotationPresent(Alternative.class);

        List<Annotation> qualifiersOfCurrentBean =
                resolveQualifiers(processAnnotatedType.getAnnotatedType().getAnnotations(), beanManager);

        String configuredBeanName;
        List<Annotation> qualifiersOfConfiguredBean;
        Class<Object> alternativeBeanClass;
        Set<Annotation> alternativeBeanAnnotations;

        for (Class currentType : beanBaseTypes)
        {
            alternativeBeanAnnotations = new HashSet<Annotation>();

            configuredBeanName = globalAlternatives.get(currentType.getName());
            if (configuredBeanName != null && configuredBeanName.length() > 0)
            {
                alternativeBeanClass = ClassUtils.tryToLoadClassForName(configuredBeanName);

                if (alternativeBeanClass == null)
                {
                    throw new IllegalStateException("Can't find class " + configuredBeanName + " which is configured" +
                            " for " + currentType.getName());
                }

                //check that the configured class is an alternative
                if (!alternativeBeanClass.isAnnotationPresent(Alternative.class))
                {
                    //we have to continue because other classes can be configured as well
                    continue;
                }

                alternativeBeanAnnotations.addAll(Arrays.asList(alternativeBeanClass.getAnnotations()));
                qualifiersOfConfiguredBean = resolveQualifiers(alternativeBeanAnnotations, beanManager);
            }
            else
            {
                continue;
            }

            if (!doQualifiersMatch(qualifiersOfCurrentBean, qualifiersOfConfiguredBean))
            {
                continue;
            }

            //current bean is annotated with @Alternative and of the same type as the configured bean
            if (isAlternativeBeanImplementation && alternativeBeanClass.equals(currentBean))
            {
                LOG.info(processAnnotatedType.getAnnotatedType().getJavaClass().getName() +
                    " is configured as global-alternative");

                //cdi 1.0
                if (priorityAnnotationInstance == null)
                {
                    AnnotatedTypeBuilder<Object> annotatedTypeBuilder
                        = new AnnotatedTypeBuilder<Object>().readFromType(processAnnotatedType.getAnnotatedType());

                    annotatedTypeBuilder.removeFromClass(Alternative.class);
                    processAnnotatedType.setAnnotatedType(annotatedTypeBuilder.create());
                    return;
                }
                //cdi 1.1+
                else
                {
                    AnnotatedTypeBuilder<Object> annotatedTypeBuilder
                        = new AnnotatedTypeBuilder<Object>().readFromType(processAnnotatedType.getAnnotatedType());

                    annotatedTypeBuilder.addToClass(priorityAnnotationInstance);
                    processAnnotatedType.setAnnotatedType(annotatedTypeBuilder.create());
                    return;
                }
            }
            else //current bean is the original implementation
            {
                //cdi 1.0 (no change needed with cdi 1.1+)
                if (priorityAnnotationInstance == null)
                {
                    //veto this original implementation because the alternative will be added
                    processAnnotatedType.veto();
                    return;
                }
            }
        }
    }

    private boolean doQualifiersMatch(List<Annotation> qualifiersOfCurrentBean,
                                      List<Annotation> qualifiersOfConfiguredBean)
    {
        if (qualifiersOfCurrentBean.size() != qualifiersOfConfiguredBean.size())
        {
            return false;
        }

        int matchingQualifiers = 0;
        for (Annotation currentQualifier : qualifiersOfCurrentBean)
        {
            for (Annotation qualifierConfiguredBean : qualifiersOfConfiguredBean)
            {
                if (doesQualifierMatch(currentQualifier, qualifierConfiguredBean))
                {
                    matchingQualifiers++;
                    break;
                }
            }
        }
        return qualifiersOfConfiguredBean.size() == matchingQualifiers;
    }

    private boolean doesQualifierMatch(Annotation currentQualifier, Annotation qualifierConfiguredBean)
    {
        if (!currentQualifier.annotationType().equals(qualifierConfiguredBean.annotationType()))
        {
            return false;
        }

        Object currentValue;
        Object valueOfQualifierConfiguredBean;
        for (Method currentMethod : currentQualifier.annotationType().getDeclaredMethods())
        {
            if (currentMethod.isAnnotationPresent(Nonbinding.class))
            {
                continue;
            }

            try
            {
                currentMethod.setAccessible(true);
                currentValue = currentMethod.invoke(currentQualifier);
                valueOfQualifierConfiguredBean = currentMethod.invoke(qualifierConfiguredBean);

                if (!currentValue.equals(valueOfQualifierConfiguredBean))
                {
                    return false;
                }
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Can't compare " + currentQualifier.annotationType().getName() +
                    " with " + qualifierConfiguredBean.annotationType().getName(), e);
            }
        }
        return true;
    }

    private List<Annotation> resolveQualifiers(Set<Annotation> annotations, BeanManager beanManager)
    {
        List<Annotation> result = new ArrayList<Annotation>();

        for (Annotation annotation : annotations)
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                result.add(annotation);
            }
        }
        return result;
    }

    private Set<Class> resolveBeanTypes(Class beanClass)
    {
        Set<Class> result = new HashSet<Class>();

        Class<?> currentClass = beanClass;
        while (currentClass != null && !Object.class.getName().equals(currentClass.getName()))
        {
            result.add(currentClass);

            for (Class interfaceClass : currentClass.getInterfaces())
            {
                if (interfaceClass.getName().startsWith("java.") || interfaceClass.getName().startsWith("javax."))
                {
                    continue;
                }
                result.addAll(resolveBeanTypes(interfaceClass));
            }

            currentClass = currentClass.getSuperclass();
        }

        return result;
    }

    private boolean evalExcludeWithoutCondition(ProcessAnnotatedType processAnnotatedType, Exclude exclude)
    {
        if (exclude.ifProjectStage().length == 0 && exclude.exceptIfProjectStage().length == 0 &&
                "".equals(exclude.onExpression()))
        {
            veto(processAnnotatedType, "Stateless");
            return false;
        }
        return true;
    }

    private boolean evalExcludeInProjectStage(ProcessAnnotatedType processAnnotatedType, Exclude exclude,
        ProjectStage currentlyConfiguredProjectStage)
    {
        Class<? extends ProjectStage>[] activatedIn = exclude.ifProjectStage();

        if (activatedIn.length == 0)
        {
            return true;
        }

        if (isInProjectStage(activatedIn, currentlyConfiguredProjectStage))
        {
            veto(processAnnotatedType, "IfProjectState");
            return false;
        }
        return true;
    }

    private boolean evalExcludeNotInProjectStage(ProcessAnnotatedType processAnnotatedType, Exclude exclude,
        ProjectStage currentlyConfiguredProjectStage)
    {
        Class<? extends ProjectStage>[] notIn = exclude.exceptIfProjectStage();

        if (notIn.length == 0)
        {
            return true;
        }

        if (!isInProjectStage(notIn, currentlyConfiguredProjectStage))
        {
            veto(processAnnotatedType, "ExceptIfProjectState");
            return false;
        }
        return true;
    }

    private void evalExcludeWithExpression(ProcessAnnotatedType processAnnotatedType, Exclude exclude)
    {
        if ("".equals(exclude.onExpression()))
        {
            return;
        }

        if (isDeactivated(exclude, PropertyExpressionInterpreter.class))
        {
            veto(processAnnotatedType, "Expression");
        }
    }

    private boolean isInProjectStage(Class<? extends ProjectStage>[] activatedIn,
        ProjectStage currentlyConfiguredProjectStage)
    {
        if (activatedIn != null && activatedIn.length > 0)
        {
            for (Class<? extends ProjectStage> activated : activatedIn)
            {
                if (currentlyConfiguredProjectStage.getClass().equals(activated))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isDeactivated(Exclude exclude, Class defaultExpressionInterpreterClass)
    {
        String expressions = exclude.onExpression();

        Class<? extends ExpressionInterpreter> interpreterClass = exclude.interpretedBy();

        if (interpreterClass.equals(ExpressionInterpreter.class))
        {
            interpreterClass = defaultExpressionInterpreterClass;
        }

        ExpressionInterpreter<String, Boolean> expressionInterpreter =
                ClassUtils.tryToInstantiateClass(interpreterClass);

        if (expressionInterpreter == null)
        {
            if (LOG.isLoggable(Level.WARNING))
            {
                LOG.warning("can't instantiate " + interpreterClass.getClass().getName());
            }
            return true;
        }

        return expressionInterpreter.evaluate(expressions);
    }

    private void veto(ProcessAnnotatedType processAnnotatedType, String vetoType)
    {
        processAnnotatedType.veto();
        LOG.finer(vetoType + " based veto for bean with type: " +
                processAnnotatedType.getAnnotatedType().getJavaClass());
    }

    private static String getJarVersion(Class targetClass)
    {
        String manifestFileLocation = getManifestFileLocationOfClass(targetClass);

        try
        {
            return new Manifest(new URL(manifestFileLocation).openStream())
                    //weld doesn't use IMPLEMENTATION_VERSION
                    .getMainAttributes().getValue(Attributes.Name.SPECIFICATION_VERSION);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static String getManifestFileLocationOfClass(Class targetClass)
    {
        String manifestFileLocation;

        try
        {
            manifestFileLocation = getManifestLocation(targetClass);
        }
        catch (Exception e)
        {
            //in this case we have a proxy
            manifestFileLocation = getManifestLocation(targetClass.getSuperclass());
        }
        return manifestFileLocation;
    }

    private static String getManifestLocation(Class targetClass)
    {
        String classFilePath = targetClass.getCanonicalName().replace('.', '/') + ".class";
        String manifestFilePath = "/META-INF/MANIFEST.MF";

        String classLocation = targetClass.getResource(targetClass.getSimpleName() + ".class").toString();
        return classLocation.substring(0, classLocation.indexOf(classFilePath) - 1) + manifestFilePath;
    }
}
