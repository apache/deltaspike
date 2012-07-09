package org.apache.deltaspike.example.securityconsole.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ejb.Stateful;
import javax.inject.Inject;

import org.apache.deltaspike.example.securityconsole.model.Customer;
import org.apache.deltaspike.security.api.permission.Permission;
import org.apache.deltaspike.security.api.permission.PermissionManager;
import org.apache.deltaspike.security.api.permission.PermissionQuery;

@Stateful
@Model
public class PermissionSearch 
{
    private static final List<String> ENTITY_TYPES = Arrays.asList(new String[] {"Customer", "Project"});
    
    private String entityType = ENTITY_TYPES.get(0);
    
    @PersistenceContext
    private EntityManager em;
    
    private Object resource;
    private List<Permission> permissions;
    
    @Inject
    private PermissionManager permissionManager;
    
    public String getEntityType()
    {
        return entityType;
    }
    
    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }
    
    public List<String> getEntityTypes()
    {
        return ENTITY_TYPES;
    }
    
    public List<Customer> getCustomers()
    {
        return em.createQuery("select C from Customer C").getResultList();
    }
    
    public Object getResource()
    {
        return resource;
    }
    
    public void setResource(Object resource)
    {
        this.resource = resource;
    }
    
    public List<Permission> getPermissions()
    {
        if (permissions == null && resource != null)
        {
            permissions = permissionManager.createPermissionQuery()
                    .setResource(resource)
                    .getResultList();
            
        }
        return permissions;
    }
}
