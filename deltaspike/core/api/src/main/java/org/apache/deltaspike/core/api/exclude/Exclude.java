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
package org.apache.deltaspike.core.api.exclude;

import org.apache.deltaspike.core.api.interpreter.ExpressionInterpreter;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Supported usages:
 * <pre>
 * &#064;Exclude
 * &#064;Exclude(ifProjectStage=Production.class)
 * &#064;Exclude(exceptIfProjectStage=UnitTest.class)
 * &#064;Exclude(onExpression="myProperty==myValue")
 * &#064;Exclude(onExpression="[my custom expression syntax]", interpretedBy=CustomExpressionInterpreter.class)
 * </pre>
 *
 * <b>Examples:</b>
 * <br/>
 * <ul>
 * <li>
 * The following bean gets excluded in any case
 * <pre>
 * &#064;Exclude
 * public class NoBean {}
 * </pre>
 * </li>
 *
 * <li>
 * The following bean gets excluded when the ProjectStage is 'Development'
 * <pre>
 * &#064;Exclude(ifProjectStage = ProjectStage.Development.class)
 * public class ProductionBean {}
 * </pre>
 * </li>
 *
 * <li>
 * The following bean gets excluded in every case except when then ProjectStage is 'Development'
 * <pre>
 * &#064;Exclude(exceptIfProjectStage = ProjectStage.Development.class)
 * public class DevBean {}
 * </pre>
 * </li>
 *
 * <li>
 * The following bean gets excluded if the expression evaluates to true, which means there is a configured property
 * called 'myProperty' with the value 'myValue'
 * <pre>
 * &#064;Exclude(onExpression="myProperty==myValue")
 * public class ProductionBean {}
 * </pre>
 * </li>
 *
 * <li>The following bean gets excluded if the expression evaluates to true
 * <pre>
 * &#064;Exclude(onExpression="[my custom expression syntax]", interpretedBy=CustomExpressionInterpreter.class)
 * public class ProductionBean {}
 * </pre>
 * </li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Exclude
{
    /**
     * The {@link org.apache.deltaspike.core.api.projectstage.ProjectStage}s which lead to deactivating this bean. If
     * the current ProjectStage is in this list, the bean will get vetoed.
     *
     * @return 1-n project-stages which are not allowed for the annotated artifact
     */
    Class<? extends ProjectStage>[] ifProjectStage() default { };

    /**
     * The {@link org.apache.deltaspike.core.api.projectstage.ProjectStage}s which lead to activating this bean. If the
     * current ProjectStage is not in this list, the bean will get vetoed.
     *
     * @return 1-n project-stages which are allowed for the annotated artifact
     */
    Class<? extends ProjectStage>[] exceptIfProjectStage() default { };

    /**
     * Expression which signals if the annotated bean should be deactivated or not.
     *
     * @return expression-string which will be interpreted
     */
    String onExpression() default "";

    /**
     * @return class of the interpreter which should be used (default leads to a simple config-property interpreter)
     */
    @SuppressWarnings("rawtypes")
    Class<? extends ExpressionInterpreter> interpretedBy() default ExpressionInterpreter.class;
}
