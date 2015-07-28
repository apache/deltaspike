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
package org.apache.deltaspike.jsf.util;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A filtered stream that evaluates value expressions in the original stream while reading from it.
 */
public class ValueExpressionEvaluationInputStream extends InputStream
{

    /**
     * Logger for this class.
     */
    private static final Logger log = Logger.getLogger(ValueExpressionEvaluationInputStream.class.getName());

    private FacesContext facesContext;
    private PushbackInputStream wrapped;
    private String currentValue;
    private int currentValueIndex = -1;

    public ValueExpressionEvaluationInputStream(FacesContext facesContext, InputStream inputStream)
    {
        this.facesContext = facesContext;
        this.wrapped = new PushbackInputStream(inputStream, 512);
    }

    /**
     * Reads a byte from the original stream and checks for value expression occurrences.
     * A value expression has the following format: #{xxx}
     * If a value expression is found, its occurrence in the stream will replaced with
     * the evaluated value of the expression.
     *
     * @return
     * @throws java.io.IOException
     */
    @Override
    public int read() throws IOException
    {
        // check for a current value
        if (currentValueIndex != -1)
        {
            if (currentValueIndex < currentValue.length())
            {
                return currentValue.charAt(currentValueIndex++);
            }
            else
            {
                // current value exhausted, reset index
                currentValueIndex = -1;
            }
        }

        // read byte and check for value expression begin
        int c1 = wrapped.read();
        if (c1 != '#')
        {
            return c1;  // can't be a value expression, just return the character
        }
        else
        {
            // could be a value expression, next character must be '{'
            int c2 = wrapped.read();
            if (c2 != '{')
            {
                wrapped.unread(c2);  // we did not find a value expression, unread byte that we read too much
                return c1;   // return original character
            }
            else
            {
                // read until '}', '\n' or eof occurs (end of value expression or data)
                List<Integer> possibleValueExpression = new LinkedList<Integer>();
                int c = wrapped.read();
                boolean insideString = (c == '\'');  // a '}' inside a string must not terminate the expression string
                while (c != -1 && c != '\n' && (insideString || c != '}'))
                {
                    possibleValueExpression.add(c);
                    c = wrapped.read();
                    if (c == '\'')
                    {
                        insideString = !insideString;
                    }
                }

                if (c != '}')
                {
                    // we did not find a value expression, unread bytes that we read too much (in reverse order)
                    if (c != -1)  // we can't unread eof
                    {
                        wrapped.unread(c);
                    }
                    ListIterator<Integer> it = possibleValueExpression.listIterator(possibleValueExpression.size());
                    while (it.hasPrevious())
                    {
                        wrapped.unread(it.previous());
                    }
                    wrapped.unread(c2);
                    return c1; // return original character
                }
                else
                {
                    // we found a value expression #{xxx} (xxx is stored in possibleValueExpression)
                    // create the expression string
                    String expressionString = createExpressionString(possibleValueExpression);

                    // evaluate it
                    String expressionValue = facesContext.getApplication()
                            .evaluateExpressionGet(facesContext, expressionString, String.class);

                    if (expressionValue == null)
                    {
                        if (log.isLoggable(Level.WARNING))
                        {
                            log.warning("ValueExpression " + expressionString + " evaluated to null.");
                        }

                        expressionValue = "null";  // fallback value for null
                    }

                    // do NOT unread the evaluated value, but rather store it in an internal buffer,
                    // because otherwise we could recursively evaluate value expressions (a value expression
                    // that resolves to a string containing "#{...}" would be re-evaluated).
                    this.currentValue = expressionValue;

                    // return first character of currentValue, if exists (not an empty string)
                    if (currentValue.length() != 0)
                    {
                        this.currentValueIndex = 0;
                        return currentValue.charAt(currentValueIndex++);
                    }
                    else  // currentValue is an empty string
                    {
                        // in this case we must recursively start a new read (incl. checks for a new value expression)
                        this.currentValueIndex = -1;
                        return read();
                    }
                }
            }
        }
    }

    private String createExpressionString(List<Integer> expressionList)
    {
        char[] expressionChars = new char[expressionList.size() + 3];  // #{expressionList}
        int i = 0;

        expressionChars[i++] = '#';
        expressionChars[i++] = '{';
        for (Integer c : expressionList)
        {
            expressionChars[i++] = (char) c.intValue();
        }
        expressionChars[i] = '}';

        return String.valueOf(expressionChars);
    }

}
