package org.apache.deltaspike.data.test.ee7.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.data.test.ee7.domain.Simple;

@ApplicationScoped
@javax.transaction.Transactional
public class SimpleClientTx
{
    @Inject
    private SimpleHolderTx simpleHolder;
    
    @Inject
    private JtaTransactionalRepositoryAbstract repo;
    
    public Simple getSimple()
    {
        return simpleHolder.getSimple();
    }
    
    public Simple createSimple(String name)
    {
        Simple simple = new Simple(name);
        simpleHolder.setSimple(simple);
        return repo.saveOnMatchTx(simple);
    }

}
