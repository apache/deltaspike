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
package org.apache.deltaspike.core.api.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Represents a subgroup of a conversation group. Useful for closing a subset of {@code @GroupConversationScoped} beans
 * in a {@code ConversationGroup}.
 *
 * <pre>
 * public class MyGroup{}
 *
 * &#064;ConversationScoped
 * &#064;ConversationGroup(MyGroup.class)
 * public class BeanA {}
 *
 * &#064;ConversationScoped
 * &#064;ConversationGroup(MyGroup.class)
 * public class BeanB {}
 *
 * &#064;ConversationScoped
 * &#064;ConversationGroup(MyGroup.class)
 * public class BeanC {}
 *
 * &#064;ConversationSubGroup(of = MyGroup.class, subGroup = {BeanA.class, BeanB.class})
 * public class MySubGroup {}
 * </pre> or
 * <pre>
 * &#064;ConversationSubGroup(subGroup = {BeanA.class, BeanB.class})
 * public class MySubGroup extends MyGroup {}
 *
 * //...
 * this.groupedConversationManager.closeConversation(MySubGroup.class)
 * </pre> or it's possible to use implicit subgroups (point to the interface instead of the bean class itself):
 * <pre>
 * public interface MyUseCase {}
 *
 * &#064;ConversationSubGroup(of = MyGroup.class, subGroup = MyUseCase.class)
 * public class ImplicitSubGroup {}
 *
 * &#064;Named("myController")
 * &#064;ConversationScoped
 * &#064;ConversationGroup(MyGroup.class)
 * public class MyController implements Serializable, MyUseCase
 * {
 *    //...
 * }
 * //...
 * this.groupedConversationManager.closeConversation(ImplicitSubGroup.class)
 * </pre>
 * 
 * @see ConversationGroup
 * @see GroupedConversationScoped
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface ConversationSubGroup
{
    /**
     * Optionally defines the base conversation group.
     *
     * @return base conversation group or ConversationSubGroup if the subgroup inherits from the base conversation group
     */
    Class<?> of() default ConversationSubGroup.class;

    /**
     * Members of the subgroup.
     *
     * @return beans to include in the subgroup
     */
    Class<?>[] subGroup();
}
