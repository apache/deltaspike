package org.apache.deltaspike.data.test.domain;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class SuperSimple
{
    private String superName;

    public String getSuperName()
    {
        return superName;
    }

    public void setSuperName(String superName)
    {
        this.superName = superName;
    }

}
