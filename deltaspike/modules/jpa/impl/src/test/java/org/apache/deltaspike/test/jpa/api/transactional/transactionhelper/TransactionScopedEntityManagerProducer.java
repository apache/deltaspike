package org.apache.deltaspike.test.jpa.api.transactional.transactionhelper;

import org.apache.deltaspike.jpa.api.TransactionScoped;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

/**
 * This class produces and closes the EntityManager
 * for our {@link TransactionHelperTest}
 */
@Dependent
public class TransactionScopedEntityManagerProducer
{

    @Produces
    @TransactionScoped
    public EntityManager createEntityManager()
    {
        return new TestEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager em)
    {
        em.close();
    }
}
