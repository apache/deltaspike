package org.apache.deltaspike.test.jsf.impl.config.view.security;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.deltaspike.security.api.authorization.AbstractDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;

@ApplicationScoped
public class AlwaysDeniedVoter extends AbstractDecisionVoter implements AccessDecisionVoter, TestVoter
{
    private static final long serialVersionUID = 1L;
    
    private int invocations = 0;
    
    @Override
    public int getInvocations()
    {
        return invocations;
    }

    @Override
    public void reset()
    {
        invocations = 0;
    }

    @Override
    public Set<SecurityViolation> checkPermission(AccessDecisionVoterContext accessDecisionVoterContext)
    {
        invocations++;
        HashSet<SecurityViolation> violations = new HashSet<SecurityViolation>();
        violations.add(newSecurityViolation("denied"));
        return violations;
    }

}
