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
package org.apache.deltaspike.test.core.api.interpreter;

import org.apache.deltaspike.core.api.interpreter.ExpressionInterpreter;
import org.apache.deltaspike.core.impl.interpreter.PropertyExpressionInterpreter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link org.apache.deltaspike.core.impl.interpreter.PropertyExpressionInterpreter}
 */
public class PropertyExpressionInterpreterTest
{
    @Test
    public void testSimplePropertyExpressions()
    {
        ExpressionInterpreter<String, Boolean> interpreter = new PropertyExpressionInterpreter(){};

        System.setProperty("k.1", "v1");
        Assert.assertEquals(interpreter.evaluate("k.1==v1"), Boolean.TRUE);
        Assert.assertEquals(interpreter.evaluate("k.1==v2"), Boolean.FALSE);

        Assert.assertEquals(interpreter.evaluate("k.1!=v1"), Boolean.FALSE);
        Assert.assertEquals(interpreter.evaluate("k.1!=v2"), Boolean.TRUE);

        try
        {
            Assert.assertEquals(interpreter.evaluate("k.1=v1"), Boolean.TRUE);
        }
        catch (IllegalStateException e)
        {
            return;
        }

        Assert.fail();
    }

    @Test
    public void testSimpleAndRequiredPropertyExpressions()
    {
        ExpressionInterpreter<String, Boolean> interpreter = new PropertyExpressionInterpreter(){};

        System.setProperty("k.1", "v1");
        Assert.assertEquals(interpreter.evaluate("k.1==v1;k.1==*"), Boolean.TRUE);
        Assert.assertEquals(interpreter.evaluate("ik.1==*"), Boolean.FALSE);

        Assert.assertEquals(interpreter.evaluate("k.1!=v2;k.1==*"), Boolean.TRUE);
        Assert.assertEquals(interpreter.evaluate("ik.1!=v2;ik.1==*"), Boolean.FALSE);
    }

    @Test
    public void testMultiplePropertyExpressions()
    {
        ExpressionInterpreter<String, Boolean> interpreter = new PropertyExpressionInterpreter(){};

        System.setProperty("k.1", "v1");
        System.setProperty("k.2", "v2");
        Assert.assertEquals(interpreter.evaluate("k.1==v1;k.2==v2"), Boolean.TRUE);
        Assert.assertEquals(interpreter.evaluate("k.1==v1;k.2==v1"), Boolean.FALSE);
        Assert.assertEquals(interpreter.evaluate("k.1==v2;k.2==v2"), Boolean.FALSE);
    }
}
