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

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.faces.context.FacesContext;
import java.io.ByteArrayInputStream;

/**
 * Tests for {@link ValueExpressionEvaluationInputStream}.
 */
public class ValueExpressionEvaluationInputStreamTest extends AbstractJsfTestCase
{

    @Test
    public void testStreamWithoutExpression_mustBeUnmodified() throws Exception
    {
        final String data = "aa\nbbbb\ncccc\ndddd\n\n";
        byte[] dataArray = data.getBytes();

        ValueExpressionEvaluationInputStream inputStream = new ValueExpressionEvaluationInputStream(
                FacesContext.getCurrentInstance(), new ByteArrayInputStream(dataArray));

        byte[] inputStreamDataArray = new byte[dataArray.length];
        inputStream.read(inputStreamDataArray);

        // checks
        Assert.assertArrayEquals(dataArray, inputStreamDataArray);  // data arrays must match
        Assert.assertEquals(-1, inputStream.read()); // stream must be at eof
    }

    @Test
    public void testStreamWithSimpleExpression_mustBeEvaluated() throws Exception
    {
        final String data = "aa\nbbbb\ncc#{requestScope.test}cc\ndddd\n\n";
        final String evaluatedData = "aa\nbbbb\ncctest-valuecc\ndddd\n\n";
        byte[] dataArray = data.getBytes();
        byte[] evaluatedDataArray = evaluatedData.getBytes();

        // put test value into scope, so that expression can evaluate to this value
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test", "test-value");

        ValueExpressionEvaluationInputStream inputStream = new ValueExpressionEvaluationInputStream(
                FacesContext.getCurrentInstance(), new ByteArrayInputStream(dataArray));

        byte[] inputStreamDataArray = new byte[evaluatedDataArray.length];
        inputStream.read(inputStreamDataArray);

        // checks
        Assert.assertArrayEquals(evaluatedDataArray, inputStreamDataArray);  // evaluated data arrays must match
        Assert.assertEquals(-1, inputStream.read()); // stream must be at eof
    }

    @Test
    public void testStreamWithHalfExpressionAtEnd_mustBeUnmodified() throws Exception
    {
        final String data = "aa\nbbbb\ncccc\ndddd\n\n#{requestScope.test"; // } is missing at the end
        byte[] dataArray = data.getBytes();

        // put test value into scope, so that expression could evaluate to this value
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test", "test-value");

        ValueExpressionEvaluationInputStream inputStream = new ValueExpressionEvaluationInputStream(
                FacesContext.getCurrentInstance(), new ByteArrayInputStream(dataArray));

        byte[] inputStreamDataArray = new byte[dataArray.length];
        inputStream.read(inputStreamDataArray);

        // checks
        Assert.assertArrayEquals(dataArray, inputStreamDataArray);  // data arrays must match
        Assert.assertEquals(-1, inputStream.read()); // stream must be at eof
    }

    @Test
    public void testStreamWithHalfExpressionAtLineEnd_mustBeUnmodified() throws Exception
    {
        final String data = "aa\nbb#{requestScope.test\n}bb\ncccc\ndddd\n\n"; // } is missing at the end
        byte[] dataArray = data.getBytes();

        // put test value into scope, so that expression could evaluate to this value
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test", "test-value");

        ValueExpressionEvaluationInputStream inputStream = new ValueExpressionEvaluationInputStream(
                FacesContext.getCurrentInstance(), new ByteArrayInputStream(dataArray));

        byte[] inputStreamDataArray = new byte[dataArray.length];
        inputStream.read(inputStreamDataArray);

        // checks
        Assert.assertArrayEquals(dataArray, inputStreamDataArray);  // data arrays must match
        Assert.assertEquals(-1, inputStream.read()); // stream must be at eof
    }

    @Test
    public void testStreamWithExpressionEvaluatingToExpressionString_mustOnlyEvaluateFirstExpression() throws Exception
    {
        final String data = "aa\nbbbb\ncc#{requestScope.test}cc\ndddd\n\n";
        final String evaluatedData = "aa\nbbbb\ncc#{requestScope.test2}cc\ndddd\n\n";
        byte[] dataArray = data.getBytes();
        byte[] evaluatedDataArray = evaluatedData.getBytes();

        // put test value into scope, so that expression can evaluate to this value
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test", "#{requestScope.test2}");
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test2", "test-value");

        ValueExpressionEvaluationInputStream inputStream = new ValueExpressionEvaluationInputStream(
                FacesContext.getCurrentInstance(), new ByteArrayInputStream(dataArray));

        byte[] inputStreamDataArray = new byte[evaluatedDataArray.length];
        inputStream.read(inputStreamDataArray);

        // checks
        Assert.assertArrayEquals(evaluatedDataArray, inputStreamDataArray);  // evaluated data arrays must match
        Assert.assertEquals(-1, inputStream.read()); // stream must be at eof
    }

    @Test
    public void testStreamThatOnlyConsistsOfExpression_mustEvaluateExpression() throws Exception
    {
        final String data = "#{requestScope.test}";
        final String evaluatedData = "test-value";
        byte[] dataArray = data.getBytes();
        byte[] evaluatedDataArray = evaluatedData.getBytes();

        // put test value into scope, so that expression can evaluate to this value
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("test", "test-value");

        ValueExpressionEvaluationInputStream inputStream = new ValueExpressionEvaluationInputStream(
                FacesContext.getCurrentInstance(), new ByteArrayInputStream(dataArray));

        byte[] inputStreamDataArray = new byte[evaluatedDataArray.length];
        inputStream.read(inputStreamDataArray);

        // checks
        Assert.assertArrayEquals(evaluatedDataArray, inputStreamDataArray);  // evaluated data arrays must match
        Assert.assertEquals(-1, inputStream.read()); // stream must be at eof
    }

    @Test
    public void testStreamWithNullExpression_mustEvaluateToEmptyString() throws Exception
    {
        final String data = "aa\nbbbb\ncc#{requestScope.test}cc\ndddd\n\n";
        final String evaluatedData = "aa\nbbbb\ncccc\ndddd\n\n";
        byte[] dataArray = data.getBytes();
        byte[] evaluatedDataArray = evaluatedData.getBytes();

        // make sure there is no value
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().remove("test");

        ValueExpressionEvaluationInputStream inputStream = new ValueExpressionEvaluationInputStream(
                FacesContext.getCurrentInstance(), new ByteArrayInputStream(dataArray));

        byte[] inputStreamDataArray = new byte[evaluatedDataArray.length];
        inputStream.read(inputStreamDataArray);

        // checks
        Assert.assertArrayEquals(evaluatedDataArray, inputStreamDataArray);  // evaluated data arrays must match
        Assert.assertEquals(-1, inputStream.read()); // stream must be at eof
    }

}
