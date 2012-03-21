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
package org.apache.deltaspike.test.api.util;

import org.apache.deltaspike.core.util.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;

public class ExceptionUtilsTest
{
    private static final String FIELD_DOES_NOT_EXIST = "field does not exist - custom message";

    @Test
    public void changeMessageOfCustomException()
    {
        try
        {
            ExceptionUtils.changeAndThrowException(
                    new CustomException("old", new Exception("original")), "new");
        }
        catch (RuntimeException e)
        {
            Assert.fail();
        }
        catch (Exception e)
        {
            Assert.assertEquals("original", e.getCause().getMessage());
            Assert.assertEquals("new", e.getMessage());
            Assert.assertEquals(CustomException.class, e.getClass());
            return;
        }
        Assert.fail();
    }

    @Test
    public void changeMessageOfInvalidCustomException()
    {
        try
        {
            ExceptionUtils.changeAndThrowException(
                    new IncompatibleCustomException("old", new Exception("original"), 1), "new");
        }
        catch (RuntimeException e)
        {
            Assert.fail();
        }
        catch (Exception e)
        {
            Assert.assertEquals("original", e.getCause().getMessage());
            Assert.assertEquals("old", e.getMessage());
            Assert.assertEquals(IncompatibleCustomException.class, e.getClass());
            Assert.assertEquals(1, ((IncompatibleCustomException) e).getCustomParameter());
            return;
        }
        Assert.fail();
    }

    @Test
    public void rethrowCheckedException()
    {
        try
        {
            invalidOperation();
        }
        catch (NoSuchFieldException e)
        {
            Assert.assertEquals(FIELD_DOES_NOT_EXIST, e.getMessage());
            Assert.assertEquals(null, e.getCause());
            return;
        }
        Assert.fail();
    }

    @Test
    public void rethrowUncheckedException()
    {
        try
        {
            ExceptionUtils.changeAndThrowException(new ClassCastException(), "custom");
        }
        catch (ClassCastException e)
        {
            Assert.assertEquals("custom", e.getMessage());
            Assert.assertEquals(null, e.getCause());
            return;
        }
        Assert.fail();
    }

    private void invalidOperation() throws NoSuchFieldException
    {
        try
        {
            getClass().getDeclaredField("virtualField");
        }
        catch (NoSuchFieldException e)
        {
            ExceptionUtils.changeAndThrowException(e, FIELD_DOES_NOT_EXIST);
        }
    }
}
