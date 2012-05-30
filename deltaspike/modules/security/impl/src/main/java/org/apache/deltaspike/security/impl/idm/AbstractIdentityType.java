package org.apache.deltaspike.security.impl.idm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.deltaspike.security.api.idm.IdentityType;

/**
 * Abstract base class for IdentityType implementations 
 */
public abstract class AbstractIdentityType implements IdentityType
{
    private String key;
    private boolean enabled = true;
    private Date creationDate = null;
    private Date expirationDate = null;
    private Map<String,String[]> attributes = new HashMap<String,String[]>();
    
    @Override
    public String getKey()
    {
        return this.key;
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @Override
    public Date getExpirationDate()
    {
        return this.expirationDate;
    }

    @Override
    public Date getCreationDate()
    {
        return this.creationDate;
    }

    @Override
    public void setAttribute(String name, String value)
    {
        attributes.put(name, new String[]{value});        
    }

    @Override
    public void setAttribute(String name, String[] values)
    {
        attributes.put(name,  values);
    }

    @Override
    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    @Override
    public String getAttribute(String name)
    {
        String[] vals = attributes.get(name);
        return null == vals ? null : ((vals.length != 0) ? vals[0] : null);
    }

    @Override
    public String[] getAttributeValues(String name)
    {
        return attributes.get(name);
    }

    @Override
    public Map<String, String[]> getAttributes()
    {
        return java.util.Collections.unmodifiableMap(attributes);
    }

}
