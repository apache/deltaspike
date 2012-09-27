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
package org.apache.deltaspike.jsf.impl.scope.view;


import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.faces.bean.ViewScoped;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;


/**
 * <p>This CDI extension enables support for the new JSF-2 &#064;ViewScoped annotation.</p>
 *
 * <p>2 steps are necessary:</p>
 * <ol>
 * <li>We have to manually add the &#064;ViewScoped annotation
 * as a passivating {@link javax.enterprise.context.NormalScope},
 * since this annotation is neither a JSR-330 annotation
 * nor a JSR-299 annotation and therefor doesn't get picked up automatically. This has to be
 * before the bean scanning starts.</li>
 * <li>After the bean scanning succeeded, we register the
 *  {@link javax.enterprise.context.spi.Context} for the
 * ViewScoped. The {@link ViewScopedContext} is responsible for actually storing all our
 * &#064;ViewScoped contextual instances in the JSF ViewMap.</li>
 * </ol>
 *
 * <p>The extension automatically detects if we are in the
 * {@link ProjectStage#UnitTest} and uses
 * </p>
 */
public class ViewScopedExtension implements Extension, Deactivatable
{
    private boolean isActivated = true;

    public void addViewScoped(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());

        if (!isActivated)
        {
            return;
        }

        beforeBeanDiscovery.addScope(ViewScoped.class, true, true);
    }

    public void registerViewContext(@Observes AfterBeanDiscovery afterBeanDiscovery)
    {
        if (!isActivated)
        {
            return;
        }

        //X TODO check whether we still need this in EE6: CodiStartupBroadcaster.broadcastStartup();
        
        // we need to do this manually because there is no dependency injection in place
        // at this time
        ProjectStageProducer psp = ProjectStageProducer.getInstance();

        ProjectStage projectStage = psp.getProjectStage();

        if (projectStage == ProjectStage.UnitTest)
        {
            // for unit tests, we use the mock context
            afterBeanDiscovery.addContext(new MockViewScopedContext());
        }
        else
        {
            // otherwise we use the real JSF ViewMap context
            afterBeanDiscovery.addContext(new ViewScopedContext());
        }
    }

}
