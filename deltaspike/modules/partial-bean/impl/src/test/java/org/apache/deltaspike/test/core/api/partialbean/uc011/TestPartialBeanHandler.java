package org.apache.deltaspike.test.core.api.partialbean.uc011;

import org.apache.deltaspike.test.core.api.partialbean.shared.TestPartialBeanBinding;

import javax.enterprise.context.Dependent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@TestPartialBeanBinding
@Dependent
public class TestPartialBeanHandler implements InvocationHandler
{
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        return null;
    }

}
