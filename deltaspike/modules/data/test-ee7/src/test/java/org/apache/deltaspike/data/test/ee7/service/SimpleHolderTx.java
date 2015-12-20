package org.apache.deltaspike.data.test.ee7.service;

import java.io.Serializable;

import org.apache.deltaspike.data.test.ee7.domain.Simple;

@javax.transaction.TransactionScoped
public class SimpleHolderTx implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Simple simple;

    
    public Simple getSimple()
    {
        return simple;
    }

    
    public void setSimple(Simple simple)
    {
        this.simple = simple;
    }
}
