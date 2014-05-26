package org.apache.deltaspike.data.impl.handler;

import static org.apache.deltaspike.data.test.util.TestDeployments.finalizeDeployment;
import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryInterface;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class EntityManagerDelegateHandlerTest extends TransactionalTestCase
{
    @Deployment
    public static Archive<?> deployment()
    {
        return finalizeDeployment(EntityRepositoryHandlerTest.class,
                initDeployment()
                    .addClasses(ExtendedRepositoryInterface.class)
                    .addPackage(Simple.class.getPackage()));
    }

    @Inject
    private ExtendedRepositoryInterface repository;

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void should_delete_detached_entity() {
        // given
        Simple simple = testData.createSimple("should_merge_entity");
        Long id = simple.getId();

        // when
        repository.detach(simple);
        repository.remove(repository.merge(simple));

        // then
        assertNotNull(id);
        Simple search = repository.findBy(id);
        assertNull(search);
    }

    @Override
    protected EntityManager getEntityManager()
    {
        return entityManager;
    }

}
