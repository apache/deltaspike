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
package org.apache.deltaspike.data.impl.param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Query;

import org.apache.deltaspike.data.api.FirstResult;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

/**
 * Convenience class to manage method and query parameters.
 */
public final class Parameters
{

    private static final Logger LOG = Logger.getLogger(Parameters.class.getName());

    private static final int DEFAULT_MAX = 0;
    private static final int DEFAULT_FIRST = -1;

    private final List<Parameter> parameterList;
    private final int max;
    private final int firstResult;

    private Parameters(List<Parameter> parameters, int max, int firstResult)
    {
        this.parameterList = parameters;
        this.max = max;
        this.firstResult = firstResult;
    }

    public static Parameters createEmpty()
    {
        List<Parameter> empty = Collections.emptyList();
        return new Parameters(empty, DEFAULT_MAX, DEFAULT_FIRST);
    }

    public static Parameters create(Method method, Object[] parameters, RepositoryMethodMetadata repositoryMethod)
    {
        int max = extractSizeRestriction(method, repositoryMethod);
        int first = DEFAULT_FIRST;
        List<Parameter> result = new ArrayList<Parameter>(parameters.length);
        int paramIndex = 1;
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++)
        {
            if (isParameter(method.getParameterAnnotations()[i]))
            {
                QueryParam qpAnnotation = extractFrom(annotations[i], QueryParam.class);
                if (qpAnnotation != null)
                {
                    result.add(new NamedParameter(qpAnnotation.value(), parameters[i]));
                }
                else
                {
                    result.add(new IndexedParameter(paramIndex++, parameters[i]));
                }
            }
            else
            {
                max = extractInt(parameters[i], annotations[i], MaxResults.class, max);
                first = extractInt(parameters[i], annotations[i], FirstResult.class, first);
            }
        }
        return new Parameters(result, max, first);
    }

    public void applyMapper(QueryInOutMapper<?> mapper)
    {
        for (Parameter param : parameterList)
        {
            param.applyMapper(mapper);
        }
    }

    public void updateValues(List<ParameterUpdate> updates)
    {
        for (ParameterUpdate update : updates)
        {
            for (Parameter param : parameterList)
            {
                if (param.is(update.forParamWithId()))
                {
                    param.updateValue(update.newParamValue(param.queryValue()));
                }
            }
        }
    }

    public Query applyTo(Query query)
    {
        for (Parameter param : parameterList)
        {
            param.apply(query);
        }
        return query;
    }

    public boolean hasSizeRestriction()
    {
        return max > DEFAULT_MAX;
    }

    public int getSizeRestriciton()
    {
        return max;
    }

    public boolean hasFirstResult()
    {
        return firstResult > DEFAULT_FIRST;
    }

    public int getFirstResult()
    {
        return firstResult;
    }

    private static int extractSizeRestriction(Method method, RepositoryMethodMetadata repositoryMethod)
    {
        if (repositoryMethod.getQuery() != null)
        {
            return repositoryMethod.getQuery().max();
        }
        return repositoryMethod.getMethodPrefix().getDefinedMaxResults();
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A extractFrom(Annotation[] annotations, Class<A> target)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().isAssignableFrom(target))
            {
                return (A) annotation;
            }
        }
        return null;
    }

    private static <A extends Annotation> int extractInt(Object parameter, Annotation[] annotations,
            Class<A> target, int defaultVal)
    {
        if (parameter != null)
        {
            A result = extractFrom(annotations, target);
            if (result != null)
            {
                if (parameter instanceof Integer)
                {
                    return (Integer) parameter;
                }
                else
                {
                    LOG.log(Level.WARNING, "Method parameter extraction: " +
                            "Param type must be int: {0}->is:{1}",
                            new Object[] { target, parameter.getClass() });
                }
            }
        }
        return defaultVal;
    }

    private static boolean isParameter(Annotation[] annotations)
    {
        return extractFrom(annotations, MaxResults.class) == null &&
                extractFrom(annotations, FirstResult.class) == null;
    }

}
