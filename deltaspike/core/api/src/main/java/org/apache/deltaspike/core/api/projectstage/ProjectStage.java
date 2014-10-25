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
package org.apache.deltaspike.core.api.projectstage;


import org.apache.deltaspike.core.util.ServiceUtils;

import javax.enterprise.inject.Typed;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is the base of all ProjectStages. A ProjectStage identifies the environment the application currently runs
 * in. It provides the same functionality as the JSF2 ProjectStage but has a few additional benefits:
 * <ul>
 * <li>it works for JSF 1.0, JSF 1.1 and JSF 1.2 applications</li>
 * <li>it works in pure backends and unit tests without any JSF API</li>
 * <li>it is dynamic. Everyone can add their own ProjectStages!</li>
 * </ul>
 *
 * <p>
 * Technically this is kind of a 'dynamic enum'.</p>
 *
 * <p>
 * The following ProjectStages are provided by default:</p>
 * <ul>
 * <li>UnitTest</li>
 * <li>Development</li>
 * <li>SystemTest</li>
 * <li>IntegrationTest</li>
 * <li>Staging</li>
 * <li>Production</li>
 * </ul>
 *
 * <p>
 * The following resolution mechanism is used to determine the current ProjectStage:</p>
 * <ul>
 * <li>TODO specify!</li>
 * </ul>
 *
 * <p>
 * New ProjectStages can be added via the {@link java.util.ServiceLoader} mechanism. A class deriving from
 * {@link ProjectStage} must be provided and used for creating a single static instance of it.</p>
 *
 * <p>
 * Custom ProjectStages can be implemented by writing anonymous ProjectStage members into a registered
 * {@link ProjectStageHolder} as shown in the following example:</p>
 *
 * <pre>
 * package org.apache.deltaspike.test.core.api.projectstage;
 * public class TestProjectStages implements ProjectStageHolder {
 *     public static final class MyOwnProjectStage extends ProjectStage {};
 *     public static final MyOwnProjectStage MyOwnProjectStage = new MyOwnProjectStage();
 *
 *     public static final class MyOtherProjectStage extends ProjectStage {};
 *     public static final MyOtherProjectStage MyOtherProjectStage = new MyOtherProjectStage();
 * }
 * </pre>
 *
 * <p>
 * To activate those ProjectStages, you have to register the ProjectStageHolder class to get picked up via the
 * ServiceLoader mechanism. Simply create a file
 * <pre>
 * META-INF/services/org.apache.deltaspike.core.api.projectstage.ProjectStageHolder
 * </pre> which contains the fully qualified class name of custom ProjectStageHolder implementation:
 * <pre>
 * # this class now gets picked up by java.util.ServiceLoader
 * org.apache.deltaspike.test.core.api.projectstage.TestProjectStages
 * </pre>
 * </p>
 *
 * <p>
 * You can use your own ProjectStages exactly the same way as all the ones provided by the system:
 * <pre>
 * ProjectStage myOwnPs = ProjectStage.valueOf("MyOwnProjectStage");
 * if (myOwnPs.equals(MyOwnProjectStage.MyOwnProjectStage)) ...
 * </pre>
 * </p>
 *
 * <p>
 * <b>Note:</b> DeltaSpike will only find {@link ProjectStageHolder}s which are accessible by this very class. If you
 * deploy the deltaspike-core jar to a shared EAR classloader, it will e.g. <i>not</i> be able to register ProjectStages
 * defined in a web application's WEB-INF/classes directory!
 * </p>
 *
 */
@Typed()
public abstract class ProjectStage implements Serializable
{
    private static final long serialVersionUID = -1210639662598734888L;

    /**
     * This map contains a static map with all registered projectStages.
     *
     * We don't need to use a ConcurrentHashMap because writing to it will
     * only be performed in the static initializer block which is guaranteed
     * to be atomic by the VM spec.
     */
    private static Map<String, ProjectStage> projectStages = new HashMap<String, ProjectStage>();

    /**
     * All the registered ProjectStage values.
     * We don't need to make this volatile because of the classloader guarantees of
     * the VM.
     */
    private static ProjectStage[] values = null;

    /**
     * logger for the ProjectStage
     */
    private static final Logger LOG = Logger.getLogger(ProjectStage.class.getName());


    /**
     * The static initializer block will register all custom ProjectStages
     * by simply touching their classes due loading it with the.
     * {@link java.util.ServiceLoader}.
     */
    static
    {
        List<ProjectStageHolder> projectStageHolders =
            ServiceUtils.loadServiceImplementations(ProjectStageHolder.class);

        for (ProjectStageHolder projectStageHolder : projectStageHolders)
        {
            LOG.fine("registering ProjectStages from ProjectStageHolder " + projectStageHolder.getClass().getName());
        }
    }


    /** the name of the ProjectStage*/
    private String psName;

    /**
     * The protected constructor will register the given ProjectStage via its name.
     * The name is returned by the {@link #toString()} method of the ProjectStage.
     */
    protected ProjectStage()
    {
        String projectStageClassName = getClass().getSimpleName();
        psName = projectStageClassName;

        init(projectStageClassName, this);
    }

    /**
     * This function exists to prevent findbugs from complaining about
     * setting a static member from a non-static function.
     *
     * @param projectStageClassName name of the project-stage
     * @param projectStage instance of the project-stage
     */
    private static void init(String projectStageClassName, ProjectStage projectStage)
    {
        if (!projectStages.containsKey(projectStageClassName))
        {
            projectStages.put(projectStageClassName, projectStage);
        }
        else
        {
            throw new IllegalArgumentException("ProjectStage with name " + projectStageClassName + " already exists!");
        }

        // we cannot do this in the static block since it's not really deterministic
        // when all ProjectStages got resolved.
        values = projectStages.values().toArray(new ProjectStage[ projectStages.size() ]);
    }

    /**
     * @param projectStageClassName the name of the ProjectStage
     * @return the ProjectStage which is identified by it's name
     */
    public static ProjectStage valueOf(String projectStageClassName)
    {
        return projectStages.get(projectStageClassName);
    }

    /**
     * Exposes all registered {@link ProjectStage} implementations.
     *
     * @return provided and custom ProjectStage implementations
     */
    public static ProjectStage[] values()
    {
        ProjectStage[] result = new ProjectStage[values.length];
        System.arraycopy(values, 0, result, 0, values.length);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return psName;
    }

    // CHECKSTYLE:OFF

    /**
     * Project-stage for unit-tests
     */
    @Typed()
    public static final class UnitTest extends ProjectStage implements TestStage
    {
        private static final long serialVersionUID = -7910349894182034559L;
    }

    /**
     * Type-safe {@link ProjectStage}
     */
    public static final UnitTest UnitTest = new UnitTest();

    /**
     * Project-stage for development
     */
    @Typed()
    public static final class Development extends ProjectStage
    {
        private static final long serialVersionUID = 1977308277341527250L;
    }

    /**
     * Type-safe {@link ProjectStage}
     */
    public static final Development Development = new Development();

    /**
     * Project-stage for system-tests
     */
    @Typed()
    public static final class SystemTest extends ProjectStage implements TestStage
    {
        private static final long serialVersionUID = -7444003351466372539L;
    }

    /**
     * Type-safe {@link ProjectStage}
     */
    public static final SystemTest SystemTest = new SystemTest();

    /**
     * Project-stage for integration-tests
     */
    @Typed()
    public static final class IntegrationTest extends ProjectStage implements TestStage
    {
        private static final long serialVersionUID = 2034474361615347127L;
    }

    /**
     * Type-safe {@link ProjectStage}
     */
    public static final IntegrationTest IntegrationTest = new IntegrationTest();

    /**
     * Project-stage for staging
     */
    @Typed()
    public static final class Staging extends ProjectStage
    {
        private static final long serialVersionUID = -8426149532860809553L;
    }

    /**
     * Type-safe {@link ProjectStage}
     */
    public static final Staging Staging = new Staging();

    /**
     * Default project-stage for production
     */
    @Typed()
    public static final class Production extends ProjectStage
    {
        private static final long serialVersionUID = -4030601958667812084L;
    }

    /**
     * Type-safe {@link ProjectStage}
     */
    public static final Production Production = new Production();

    // CHECKSTYLE:ON

}
