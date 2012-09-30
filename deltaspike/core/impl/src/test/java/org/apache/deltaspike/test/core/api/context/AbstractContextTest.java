package org.apache.deltaspike.test.core.api.context;

import javax.enterprise.inject.spi.Extension;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Ignore;

/**
 * We test the AbstractContext by implementing a simple dummy context.
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class AbstractContextTest
{
    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "abstractContextTest.jar")
                .addPackage(AbstractContextTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "abstractContextTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, DummyScopeExtension.class);
    }


    @Test
    @Ignore
    //X Test is disabled out due to a bug in the owb-arquillian and weld-arquillian containers
    //X which needs to get fixed first. All tested containers so far do NOT respect the Extensions
    //X from the ShrinkWrap archive but only the ones from the classpath.
    public void testDummyContext()
    {
        DummyBean dummyBean = BeanProvider.getContextualReference(DummyBean.class);

        Assert.assertEquals(4711, dummyBean.getI());

        dummyBean.setI(4712);

        DummyBean dummyBean2 = BeanProvider.getContextualReference(DummyBean.class);
        Assert.assertEquals(4712, dummyBean.getI());
    }
}
