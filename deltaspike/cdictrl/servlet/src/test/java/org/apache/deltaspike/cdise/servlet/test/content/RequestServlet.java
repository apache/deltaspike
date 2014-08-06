/*******************************************************************************
 * Copyright (c) 2013 - 2014 Sparta Systems, Inc.
 ******************************************************************************/

package org.apache.deltaspike.cdise.servlet.test.content;

import org.apache.deltaspike.cdise.api.CdiContainerLoader;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A simple servlet that calls a request scoped object.
 */
public class RequestServlet extends HttpServlet
{
    private final Logger LOG = Logger.getLogger(RequestServlet.class.getName());
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        LOG.warning("incoming request "+Thread.currentThread().getName());
        PrintWriter out = resp.getWriter();
        RequestScopedBean bean = getRequestScopedBean();
        out.write(bean.greet());
        out.close();
    }

    private RequestScopedBean getRequestScopedBean()
    {
        BeanManager beanManager = CdiContainerLoader.getCdiContainer().getBeanManager();

        if (beanManager == null)
        {
            return null;
        }
        Set<Bean<?>> beans = beanManager.getBeans(RequestScopedBean.class);
        Bean<RequestScopedBean> reqScpdBean = (Bean<RequestScopedBean>) beanManager.resolve(beans);

        CreationalContext<RequestScopedBean> reqScpdCC =
                beanManager.createCreationalContext(reqScpdBean);

        RequestScopedBean instance = (RequestScopedBean)
                beanManager.getReference(reqScpdBean, RequestScopedBean.class, reqScpdCC);
        return instance;
    }
}
