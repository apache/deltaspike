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
package org.apache.deltaspike.core.impl.message;

import org.apache.deltaspike.core.spi.activation.Deactivatable;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;
import java.lang.reflect.Method;

@Dependent
@Typed(NamedMessageBundleInvocationHandler.class)
public class NamedMessageBundleInvocationHandler extends MessageBundleInvocationHandler implements Deactivatable
{
    private static final long serialVersionUID = -7089857581799104783L;

    private Class<?> targetType;

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        int argCount = 0;
        boolean nullValueFound = false;
        String methodName = new RuntimeException().getStackTrace()[1].getMethodName();

        if (args != null)
        {
            argCount = args.length;
        }

        Class<?>[] paramTypes = new Class[argCount];

        for (int i = 0; i < argCount; i++)
        {
            if (args[i] != null)
            {
                paramTypes[i] = args[i].getClass();
            }
            else
            {
                nullValueFound = true;
            }
        }

        Method targetMethod = null;

        if (!nullValueFound)
        {
            targetMethod = this.targetType.getMethod(methodName, paramTypes);
        }
        else
        {
            //TODO improve it
            for (Method currentMethod : this.targetType.getDeclaredMethods())
            {
                if (currentMethod.getParameterTypes().length == argCount && currentMethod.getName().equals(methodName))
                {
                    if (targetMethod != null)
                    {
                        throw new IllegalStateException("Two methods with the same name and parameter-count found. " +
                            "It isn't possible to select the correct one, because one argument is 'null'.");
                    }
                    targetMethod = currentMethod;
                }
            }
        }

        if (targetMethod == null)
        {
            throw new IllegalStateException(methodName + " can't be found on " + this.targetType);
        }

        return super.invoke(proxy, targetMethod, args);
    }

    public void setTargetType(Class<?> targetType)
    {
        this.targetType = targetType;
    }
}
