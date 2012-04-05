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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.deltaspike.core.api.message.Cause;
import org.apache.deltaspike.core.api.message.Message;

class MessageBundleInvocationHandler implements InvocationHandler
{

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(final Object proxy, final Method method,
            final Object[] args) throws Throwable
    {
        final Message message = method.getAnnotation(Message.class);
        if (message == null)
        {
            // nothing to do...
            return null;
        }
        final Annotation[][] parameterAnnotations = method
                .getParameterAnnotations();
        ArrayList<Object> newArgs = new ArrayList<Object>();
        Throwable cause = extractCause(parameterAnnotations, args, newArgs);
        String result;
        switch (message.format())
        {
            case PRINTF:
            {
                result = String.format(message.value(), newArgs.toArray());
                break;
            }
            case MESSAGE_FORMAT:
            {
                result = MessageFormat.format(message.value(),
                        newArgs.toArray());
                break;
            }
            default:
                throw new IllegalStateException();
        }
        final Class<?> returnType = method.getReturnType();
        if (Throwable.class.isAssignableFrom(returnType))
        {
            // the return type is an exception
            if (cause != null)
            {
                final Constructor<?> constructor = returnType.getConstructor(
                        String.class, Throwable.class);
                return constructor.newInstance(result, cause);
            }
            else
            {
                final Constructor<?> constructor = returnType
                        .getConstructor(String.class);
                return constructor.newInstance(result);
            }
        }
        else
        {
            return result;
        }
    }

    protected static Throwable extractCause(
            final Annotation[][] parameterAnnotations, final Object[] args,
            final List<Object> newArgs)
    {
        Throwable cause = null;
        for (int i = 0; i < parameterAnnotations.length; i++)
        {
            Annotation[] annotations = parameterAnnotations[i];
            boolean found = false;
            for (Annotation annotation : annotations)
            {
                if (annotation instanceof Cause)
                {
                    if (cause == null)
                    {
                        cause = (Throwable) args[i];
                    }
                    found = true;
                }
            }
            if (!found)
            {
                newArgs.add(args[i]);
            }
        }
        return cause;
    }

}
