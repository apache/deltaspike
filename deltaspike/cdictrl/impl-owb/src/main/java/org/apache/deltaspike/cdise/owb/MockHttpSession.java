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
package org.apache.deltaspike.cdise.owb;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * A simple mock HttpSession
 */
public class MockHttpSession implements HttpSession
{
    private String sessionId;

    public MockHttpSession(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public long getCreationTime()
    {
        return 0;
    }

    public String getId()
    {
        return sessionId;
    }

    public long getLastAccessedTime()
    {
        return 0;
    }

    public ServletContext getServletContext()
    {
        return null;
    }

    public void setMaxInactiveInterval(int interval)
    {
    }

    public int getMaxInactiveInterval()
    {
        return 0;
    }

    public HttpSessionContext getSessionContext()
    {
        return null;
    }

    public Object getAttribute(String name)
    {
        return null;
    }

    public Object getValue(String name)
    {
        return null;
    }

    public Enumeration getAttributeNames()
    {
        return null;
    }

    public String[] getValueNames()
    {
        return new String[0];
    }

    public void setAttribute(String name, Object value)
    {
    }

    public void putValue(String name, Object value)
    {
    }

    public void removeAttribute(String name)
    {
    }

    public void removeValue(String name)
    {
    }

    public void invalidate()
    {
    }

    public boolean isNew()
    {
        return false;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MockHttpSession that = (MockHttpSession) o;

        if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}
