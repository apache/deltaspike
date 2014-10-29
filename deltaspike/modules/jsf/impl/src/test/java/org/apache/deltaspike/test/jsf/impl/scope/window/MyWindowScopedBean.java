package org.apache.deltaspike.test.jsf.impl.scope.window;

import java.io.Serializable;

import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;

@Named
@WindowScoped
public class MyWindowScopedBean implements Serializable
{

    private static final long serialVersionUID = 1L;

    private int value = 0;

    public int getValue()
    {
        return value;
    }

    public void count()
    {
        value++;
    }

}
