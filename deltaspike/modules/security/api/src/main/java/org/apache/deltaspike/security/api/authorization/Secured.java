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
package org.apache.deltaspike.security.api.authorization;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ExecutableCallbackDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.DefaultCallback;
import org.apache.deltaspike.core.api.config.view.metadata.ViewMetaData;
import org.apache.deltaspike.core.spi.config.view.ConfigPreProcessor;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Interceptor for securing beans.
 * It's also possible to use it as meta-annotation for type-safe view-configs.
 */
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented

//cdi annotations
@SecurityBindingType

//don't use @Aggregated(true) - we need to support different error-pages (per folder/page)
@ViewMetaData(preProcessor = Secured.AnnotationPreProcessor.class)
public @interface Secured
{
    /**
     * {@link AccessDecisionVoter}s which will be invoked before accessing the intercepted instance or in case of
     * view-configs before a view gets used.
     *
     * @return the configured access-decision-voters which should be used for the voting process
     */
    @Nonbinding
    Class<? extends AccessDecisionVoter>[] value();

    /**
     * Optional inline error-view if it is required to show an error-page
     * which is different from the default error page.
     * @return type-safe view-config of the page which should be used as error-view
     */
    @Nonbinding
    Class<? extends ViewConfig> errorView() default DefaultErrorView.class;

    class AnnotationPreProcessor implements ConfigPreProcessor<Secured>
    {
        @Override
        public Secured beforeAddToConfig(Secured metaData, ViewConfigNode viewConfigNode)
        {
            viewConfigNode.registerCallbackDescriptors(Secured.class, new Descriptor(metaData.value()));
            return metaData; //no change needed
        }
    }

    //can be used from outside to get a typed result
    static class Descriptor extends ExecutableCallbackDescriptor<Set<SecurityViolation>>
    {
        public Descriptor(Class<? extends AccessDecisionVoter>[] accessDecisionVoterBeanClasses)
        {
            super(accessDecisionVoterBeanClasses, DefaultCallback.class);
        }

        public List<Set<SecurityViolation>> execute(AccessDecisionVoterContext accessDecisionVoterContext)
        {
            return super.execute(accessDecisionVoterContext);
        }
    }
}
