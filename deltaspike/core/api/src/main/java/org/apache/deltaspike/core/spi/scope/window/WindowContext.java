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
package org.apache.deltaspike.core.spi.scope.window;

import org.apache.deltaspike.core.spi.AttributeAware;

/**
 * <p>We support the general notion of multiple 'windows'.
 * That might be different parallel edit pages in a
 * desktop application (think about different open documents
 * in an editor) or multiple browser tabs in a
 * web application.</p>
 * <p>For web applications each browser tab or window will be
 * represented by an own {@link WindowContext}. All those
 * {@link WindowContext}s will be held in the users servlet
 * session as &#064;SessionScoped bean.
 * </p>
 */
public interface WindowContext extends AttributeAware
{
    /**
     * @return the unique identifier for this window context
     */
    String getWindowId();

}
