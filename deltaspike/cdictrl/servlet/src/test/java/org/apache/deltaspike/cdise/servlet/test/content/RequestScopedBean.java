/*******************************************************************************
 * Copyright (c) 2013 - 2014 Sparta Systems, Inc.
 ******************************************************************************/

package org.apache.deltaspike.cdise.servlet.test.content;

import javax.enterprise.context.RequestScoped;

/**
 * a simple request scoped object.
 */
@RequestScoped
public class RequestScopedBean
{
    public String greet()
    {
        return "Hello, world!";
    }
}
