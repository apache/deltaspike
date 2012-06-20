package org.apache.deltaspike.example.securityconsole.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.deltaspike.security.api.permission.annotations.ACLIdentifier;
import org.apache.deltaspike.security.api.permission.annotations.ACLPermission;
import org.apache.deltaspike.security.api.permission.annotations.ACLRecipient;
import org.apache.deltaspike.security.api.permission.annotations.ACLStore;

@ACLStore(Customer.class)
@Entity
public class CustomerPermission implements Serializable
{
    private static final long serialVersionUID = 372174826909042844L;
    
    @Id @GeneratedValue
    private Long id;
    
    @ACLIdentifier
    private Long identifier;
    
    @ACLRecipient
    private String recipient;
    
    @ACLPermission
    private String permission;
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getIdentifier()
    {
        return identifier;
    }
    
    public void setIdentifier(Long identifier)
    {
        this.identifier = identifier;
    }
    
    public String getRecipient()
    {
        return recipient;
    }
    
    public void setRecipient(String recipient)
    {
        this.recipient = recipient;
    }
    
    public String getPermission()
    {
        return permission;
    }
    
    public void setPermission(String permission)
    {
        this.permission = permission;
    }
}
