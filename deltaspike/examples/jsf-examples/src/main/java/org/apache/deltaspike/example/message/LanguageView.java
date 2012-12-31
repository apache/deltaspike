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
package org.apache.deltaspike.example.message;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;

/**
 *
 */
@Named
@SessionScoped
public class LanguageView implements Serializable
{
    private static final Locale ENGLISH = Locale.ENGLISH;
    private static final Locale FRENCH = Locale.FRENCH;
    private static final Locale DUTCH = new Locale("nl");

    private Locale selectedLanguage = new Locale("en");

    public boolean isEnglish()
    {
        return ENGLISH.equals(selectedLanguage);
    }

    public boolean isFrench()
    {
        return FRENCH.equals(selectedLanguage);
    }

    public boolean isDutch()
    {
        return DUTCH.equals(selectedLanguage);
    }

    public void setEnglish()
    {
        selectedLanguage = ENGLISH;
    }

    public void setFrench()
    {
        selectedLanguage = FRENCH;
    }

    public void setDutch()
    {
        selectedLanguage = DUTCH;
    }

    public Locale getSelectedLanguage()
    {
        return selectedLanguage;
    }

    public void setSelectedLanguage(Locale someSelectedLanguage)
    {
        selectedLanguage = someSelectedLanguage;
    }
}
