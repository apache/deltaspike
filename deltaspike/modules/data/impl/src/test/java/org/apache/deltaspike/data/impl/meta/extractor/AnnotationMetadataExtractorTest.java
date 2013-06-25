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
package org.apache.deltaspike.data.impl.meta.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.meta.RepositoryEntity;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.RepositoryInterface;
import org.junit.Test;

public class AnnotationMetadataExtractorTest
{

    @Test
    public void should_extract_entity_class_from_repo_annotation()
    {
        // given
        AnnotationMetadataExtractor extractor = new AnnotationMetadataExtractor();

        // when
        RepositoryEntity result = extractor.extract(RepositoryInterface.class);

        // then
        assertNotNull(result);
        assertEquals(Simple.class, result.getEntityClass());
        assertEquals(Long.class, result.getPrimaryClass());
    }

    @Test
    public void should_throw_excption_when_annotation_with_entity_class_not_present()
    {
        // given
        AnnotationMetadataExtractor extractor = new AnnotationMetadataExtractor();

        // when
        RepositoryEntity result = extractor.extract(NoEntityPresentRepository.class);

        // then
        assertNull(result);
    }

    @Test
    public void should_throw_exception_when_annotation_with_non_entity_class()
    {
        // given
        AnnotationMetadataExtractor extractor = new AnnotationMetadataExtractor();

        // when
        RepositoryEntity result = extractor.extract(NonEntityRepository.class);

        // then
        assertNull(result);
    }

    @Repository
    private static class NoEntityPresentRepository
    {
    }

    @Repository(forEntity = Object.class)
    private static class NonEntityRepository
    {
    }
}
