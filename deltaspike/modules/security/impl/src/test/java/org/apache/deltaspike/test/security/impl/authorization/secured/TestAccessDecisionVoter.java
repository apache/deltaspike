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
package org.apache.deltaspike.test.security.impl.authorization.secured;

import org.apache.deltaspike.security.api.authorization.AccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@ApplicationScoped
public class TestAccessDecisionVoter implements AccessDecisionVoter
{
    private static final long serialVersionUID = 1331427301357439804L;

    @Override
    public Set<SecurityViolation> checkPermission(AccessDecisionVoterContext accessDecisionVoterContext)
    {
        Method method = accessDecisionVoterContext.<InvocationContext>getSource().getMethod();

        if (!method.getName().contains("Blocked"))
        {
            return Collections.emptySet();
        }
        return new HashSet<SecurityViolation>() {
            private static final long serialVersionUID = 9033579102593906350L;

            {
                add(new SecurityViolation()
                {
                    private static final long serialVersionUID = -7990980050100180829L;

                    @Override
                    public String getReason()
                    {
                        return "blocked";
                    }
                });
            }
        };
    }
}
