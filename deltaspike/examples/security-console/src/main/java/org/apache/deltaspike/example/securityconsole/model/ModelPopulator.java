package org.apache.deltaspike.example.securityconsole.model;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Populates the database with default values
 *
 */
@Stateless
public @Named class ModelPopulator 
{
    @PersistenceContext
    private EntityManager em;
    
    public void populate()
    {
        Customer c = new Customer();
        c.setFirstName("Shane");
        c.setLastName("Bryzak");
        em.persist(c);
        
        c = new Customer();
        c.setFirstName("John");
        c.setLastName("Smith");
        em.persist(c);
    }
}
