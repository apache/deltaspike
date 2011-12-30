package org.apache.deltaspike.core.api.literal;

import javax.enterprise.inject.Any;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal for the {@link javax.enterprise.inject.Any} annotation.
 */
public class AnyLiteral extends AnnotationLiteral<Any> implements Any
{
    private static final long serialVersionUID = -8623640277155878657L;
}