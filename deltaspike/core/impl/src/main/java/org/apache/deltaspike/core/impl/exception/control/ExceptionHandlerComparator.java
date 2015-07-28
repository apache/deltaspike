/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.deltaspike.core.impl.exception.control;

import org.apache.deltaspike.core.api.exception.control.HandlerMethod;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.util.HierarchyDiscovery;

import javax.enterprise.inject.Typed;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

/**
 * Comparator to sort exception handlers according qualifier
 * ({@link org.apache.deltaspike.core.api.exception.control.BeforeHandles} first), ordinal
 * (highest to lowest) and finally hierarchy (least to most specific).
 */
@SuppressWarnings({ "MethodWithMoreThanThreeNegations" })
@Typed()
public final class ExceptionHandlerComparator implements Comparator<HandlerMethod<?>>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(HandlerMethod<?> lhs, HandlerMethod<?> rhs)
    {
        if (lhs.equals(rhs))
        {
            return 0;
        }

        // Really this is so all handlers are returned in the TreeSet (even if they're of the same type, but one is
        // before, the other is not

        // Make sure both handlers are handling the same type, and also have the same qualifiers, if both of those are
        // true, then precedence comes into play
        if (lhs.getExceptionType().equals(rhs.getExceptionType()) && lhs.getQualifiers().equals(rhs.getQualifiers()))
        {
            final int precedenceReturnValue = comparePrecedence(lhs.getOrdinal(), rhs.getOrdinal(),
                    lhs.isBeforeHandler());

            // We really shouldn't be running into this case where everything is the same up until now,
            // but just in case, return both so both handlers are run.
            if (precedenceReturnValue == 0)
            {
                return -1;
            }

            // Precedence is different
            return precedenceReturnValue;
        }
        else
        {
            // Different qualifiers
            if (lhs.getExceptionType().equals(rhs.getExceptionType())
                    && !lhs.getQualifiers().equals(rhs.getQualifiers()))
            {
                if (lhs.getQualifiers().contains(new AnyLiteral()))
                {
                    return -1; // Make sure @Any is first, as it's less specific
                }
                return 1;
            }
            return compareHierarchies(lhs.getExceptionType(), rhs.getExceptionType());
        }

        // Currently we're only looking at one type of traversal mode, if this changes, we'll need
        // to re-add lines to check for this.
    }

    private int compareHierarchies(Type lhsExceptionType, Type rhsExceptionType)
    {
        HierarchyDiscovery lhsHierarchy = new HierarchyDiscovery(lhsExceptionType);
        Set<Type> lhsTypeclosure = lhsHierarchy.getTypeClosure();

        if (lhsTypeclosure.contains(rhsExceptionType))
        {
            final int indexOfLhsType = new ArrayList<Type>(lhsTypeclosure).indexOf(lhsExceptionType);
            final int indexOfRhsType = new ArrayList<Type>(lhsTypeclosure).indexOf(rhsExceptionType);

            if (indexOfLhsType > indexOfRhsType)
            {
                return 1;
            }
        }
        return -1;
    }

    private int comparePrecedence(final int lhs, final int rhs, final boolean isLhsBefore)
    {
        if (!isLhsBefore)
        {
            return (lhs - rhs);
        }
        else
        {
            return (lhs - rhs) * -1;
        }
    }
}
