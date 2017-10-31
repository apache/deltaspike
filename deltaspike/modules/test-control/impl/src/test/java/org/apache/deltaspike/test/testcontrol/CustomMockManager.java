package org.apache.deltaspike.test.testcontrol;

import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.apache.deltaspike.testcontrol.impl.mock.SimpleMockManager;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;

@Alternative

@RequestScoped
@Typed(DynamicMockManager.class)
public class CustomMockManager extends SimpleMockManager
{
    private static boolean isCalled;

    @Override
    public <T> T getMock(Class<T> beanClass, Annotation... qualifiers)
    {
        isCalled = true;

        return super.getMock(beanClass, qualifiers);
    }

    public static boolean isIsCalled()
    {
        return isCalled;
    }

    public static void resetInternals()
    {
        isCalled = false;
    }
}
