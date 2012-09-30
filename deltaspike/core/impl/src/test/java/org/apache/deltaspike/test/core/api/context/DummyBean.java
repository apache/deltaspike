package org.apache.deltaspike.test.core.api.context;

import java.io.Serializable;

import org.apache.deltaspike.test.core.api.context.DummyScoped;

/**
 */
@DummyScoped
public class DummyBean implements Serializable
{
    private int i = 4711;

    public int getI()
    {
        return i;
    }

    public void setI(int i)
    {
        this.i = i;
    }
}
