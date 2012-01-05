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
package org.apache.deltaspike.core.impl.exclude;

import org.apache.deltaspike.core.api.activation.Deactivatable;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.interpreter.ExpressionInterpreter;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.util.ClassUtils;
import org.apache.deltaspike.core.impl.interpreter.PropertyExpressionInterpreter;
import org.apache.deltaspike.core.impl.projectstage.ProjectStageProducer;
import org.apache.deltaspike.core.impl.util.ClassDeactivation;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
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
    protected void vetoBeans(@Observes ProcessAnnotatedType<Object> processAnnotatedType)
    {
        if (!isActivated() ||
                !processAnnotatedType.getAnnotatedType().getJavaClass().isAnnotationPresent(Exclude.class))
        {
            return;
        }

        //TODO needs further discussions for a different feature CodiStartupBroadcaster.broadcastStartup();

        Exclude exclude = processAnnotatedType.getAnnotatedType().getJavaClass().getAnnotation(Exclude.class);

        if (!evalExcludeWithoutCondition(processAnnotatedType, exclude))
        {
            return; //veto called already
        }

        if (!evalExcludeInProjectStage(processAnnotatedType, exclude))
        {
            return; //veto called already
        }

        if (!evalExcludeNotInProjectStage(processAnnotatedType, exclude))
        {
            return; //veto called already
        }

        evalExcludeWithExpression(processAnnotatedType, exclude);
    }

    private boolean evalExcludeWithoutCondition(ProcessAnnotatedType<Object> processAnnotatedType, Exclude exclude)
    {
        if (exclude.ifProjectStage().length == 0 && exclude.exceptIfProjectStage().length == 0 &&
                "".equals(exclude.onExpression()))
        {
            veto(processAnnotatedType, "Stateless");
            return false;
        }
        return true;
    }

    private boolean evalExcludeInProjectStage(ProcessAnnotatedType<Object> processAnnotatedType, Exclude exclude)
    {
        Class<? extends ProjectStage>[] activatedIn = exclude.ifProjectStage();

        if (activatedIn.length == 0)
        {
            return true;
        }

        if (isInProjectStage(activatedIn))
        {
            veto(processAnnotatedType, "IfProjectState");
            return false;
        }
        return true;
    }

    private boolean evalExcludeNotInProjectStage(ProcessAnnotatedType<Object> processAnnotatedType, Exclude exclude)
    {
        Class<? extends ProjectStage>[] notIn = exclude.exceptIfProjectStage();

        if (notIn.length == 0)
        {
            return true;
        }

        if (!isInProjectStage(notIn))
        {
            veto(processAnnotatedType, "ExceptIfProjectState");
            return false;
        }
        return true;
    }

    private void evalExcludeWithExpression(ProcessAnnotatedType<Object> processAnnotatedType, Exclude exclude)
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

    private boolean isInProjectStage(Class<? extends ProjectStage>[] activatedIn)
    {
        if (activatedIn != null && activatedIn.length > 0)
        {
            ProjectStage ps = ProjectStageProducer.getInstance().getProjectStage();
            for (Class<? extends ProjectStage> activated : activatedIn)
            {
                if (ps.getClass().equals(activated))
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

    private void veto(ProcessAnnotatedType<?> processAnnotatedType, String vetoType)
    {
        processAnnotatedType.veto();
        LOG.finer(vetoType + " based veto for bean with type: " +
                processAnnotatedType.getAnnotatedType().getJavaClass());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActivated()
    {
        return ClassDeactivation.isClassActivated(getClass());
    }
}
