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
package org.apache.deltaspike.beanvalidation.test;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.deltaspike.beanvalidation.impl.CDIAwareConstraintValidatorFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InjectedValidationTest
{
    @Deployment
    public static WebArchive createArchive()
    {
        return ShrinkWrap.create(WebArchive.class,"beanval.war")
                .addClasses(BasicPojo.class,InjectableConstraintValidator.class,
                        CDIAwareConstraintValidatorFactory.class,ArraySize.class,
                        ArrayChecker.class)
                .addAsWebInfResource("validation.xml", "classes/META-INF/validation.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCore());
    }
    
    private Validator validator;
    
    @Before
    public void initValidator()
    {
        this.validator = Validation.byDefaultProvider().configure()
                .constraintValidatorFactory(new CDIAwareConstraintValidatorFactory())
                .buildValidatorFactory().getValidator();
    }
    
    @Test
    public void testValidate()
    {
        String[] entries = new String[]{"abc","def","ghi"};
        BasicPojo p = new BasicPojo();
        p.setValue(entries);
        final Set<ConstraintViolation<BasicPojo>> violations = validator.validate(p);
        StringBuilder sb = new StringBuilder();
        for(ConstraintViolation<BasicPojo> violation : violations) {
            sb.append(violation.getMessage() + " "+ violation.getPropertyPath());
        }
        Assert.assertTrue(sb.toString(),violations.isEmpty());
        
    }
}
