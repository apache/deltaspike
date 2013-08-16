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
package org.apache.deltaspike.test.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A few helper methods for testing serialisation.
 * They help serializing to a byte[] and back to the object
 */
public class Serializer<T>
{

    /**
     * Serializes the given instance to a byte[] and immediately
     * de-serialize it back.
     * @param original instance
     * @return the deserialized new instance
     */
    public T roundTrip(T original)
    {
        return deserialize(serialize(original));
    }

    /**
     * Serializes the given instance to a byte[].
     */
    public byte[] serialize(T o)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * De-serializes the given byte[] to an instance of T.
     */
    public T deserialize(byte[] serial)
    {
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(serial);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
