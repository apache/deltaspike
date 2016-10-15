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
package org.apache.deltaspike.example;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Named
@ViewScoped
@Transactional
public class ArticleController implements Serializable
{

    private Article article = new Article();

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;
    
    @Inject
    private FacesContext facesContext;
    
    @HttpParam("aid") 
    @Inject
    private String aid; // article id

    public Article getArticle()
    {
        return article;
    }

    public void setArticle(Article article)
    {
        this.article = article;
    }

    public Article findArticleById(Long id)
    {
        article = em.find(Article.class, id);
        return article;
    }

    
    public String persist()
    {
        article.setDate(new Date());
        em.merge(this.article);
        facesContext.addMessage(null, new FacesMessage("article:" + article.getTitle() + " persisted"));
        return "persisted";
    }

    public String delete(Article article)
    {
        em.remove(article);
        facesContext.addMessage(null, new FacesMessage("article:" + article.getTitle() + " deleted"));
        return "deleted";
    }

    public List<Article> getAllArticles()
    {
        return em.createQuery("from Article a order by a.id desc").getResultList();
    }
    
    public void loadArticle() 
    {
        if (aid != null)
        {
            this.article = findArticleById(Long.valueOf(aid));
        }
    }
    
}
