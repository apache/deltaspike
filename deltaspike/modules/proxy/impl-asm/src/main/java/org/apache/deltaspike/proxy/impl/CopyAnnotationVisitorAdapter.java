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
package org.apache.deltaspike.proxy.impl;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class CopyAnnotationVisitorAdapter extends AnnotationVisitor
{
    private final AnnotationVisitor from;
    private final AnnotationVisitor to;

    public CopyAnnotationVisitorAdapter(AnnotationVisitor from, AnnotationVisitor copyTo)
    {
        super(Opcodes.ASM7);

        this.from = from;
        this.to = copyTo;
    }

    @Override
    public void visit(String name, Object value)
    {
        if (from != null)
        {
            from.visit(name, value);
        }
        to.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String desc, String value)
    {
        if (from != null)
        {
            from.visitEnum(name, desc, value);
        }
        to.visitEnum(name, desc, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc)
    {
        if (from == null)
        {
            return new CopyAnnotationVisitorAdapter(
                    null,
                    to.visitAnnotation(name, desc));
        }

        return new CopyAnnotationVisitorAdapter(
                from.visitAnnotation(name, desc),
                to.visitAnnotation(name, desc));
    }

    @Override
    public AnnotationVisitor visitArray(String name)
    {
        if (from == null)
        {
            return new CopyAnnotationVisitorAdapter(
                    null,
                    to.visitArray(name));
        }

        return new CopyAnnotationVisitorAdapter(
                from.visitArray(name),
                to.visitArray(name));
    }

    @Override
    public void visitEnd()
    {
        if (from != null)
        {
            from.visitEnd();
        }
        to.visitEnd();
    }
}
