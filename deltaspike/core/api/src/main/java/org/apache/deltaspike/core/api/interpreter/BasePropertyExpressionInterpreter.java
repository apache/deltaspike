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
package org.apache.deltaspike.core.api.interpreter;

/**
 * Base implementation for simple (property) expressions.
 *
 * Supported operations:<p/>
 * <ul>
 *     <li>[key]==[value]</li>
 *     <li>[key]!=[value]</li>
 *     <li>[key]==* (a value is required)</li>
 *     <li>; (separator)</li>
 * </ul>
 */
public abstract class BasePropertyExpressionInterpreter implements ExpressionInterpreter<String, Boolean>
{
    private static final String ASTERISK = "*";

    @Override
    public final Boolean evaluate(String expressions)
    {
        boolean result = false;
        String[] foundExpressions = expressions.split(";");

        SimpleOperationEnum operation;
        for (String expression : foundExpressions)
        {
            result = false;
            if (expression.contains(SimpleOperationEnum.IS.getValue()))
            {
                operation = SimpleOperationEnum.IS;
            }
            else if (expression.contains(SimpleOperationEnum.NOT.getValue()))
            {
                operation = SimpleOperationEnum.NOT;
            }
            else
            {
                throw new IllegalStateException("expression: " + expression + " isn't supported by " +
                        getClass().getName() + " supported operations: " + SimpleOperationEnum.getOperations() +
                        "separator: ';'");
            }

            String[] keyValue = expression.split(operation.getValue());

            String configuredValue = getConfiguredValue(keyValue[0]);

            if (configuredValue != null)
            {
                configuredValue = configuredValue.trim();
            }
            else
            {
                configuredValue = "";
            }

            if (!ASTERISK.equals(keyValue[1]) && "".equals(configuredValue))
            {
                continue;
            }

            if (ASTERISK.equals(keyValue[1]) && !"".equals(configuredValue))
            {
                result = true;
                continue;
            }

            if (SimpleOperationEnum.IS.equals(operation) && !keyValue[1].equalsIgnoreCase(configuredValue))
            {
                return false;
            }
            else if (SimpleOperationEnum.NOT.equals(operation) && keyValue[1].equalsIgnoreCase(configuredValue))
            {
                return false;
            }
            result = true;
        }

        return result;
    }

    protected abstract String getConfiguredValue(String key);
}
