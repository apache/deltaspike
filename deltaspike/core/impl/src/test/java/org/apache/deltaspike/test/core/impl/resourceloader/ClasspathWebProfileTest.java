package org.apache.deltaspike.test.core.impl.resourceloader;

import org.apache.deltaspike.core.api.resourceloader.ClasspathStorage;
import org.apache.deltaspike.core.api.resourceloader.ExternalResource;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * web profile will run in a separate JVM, as a result need to manually add the properties to the archive.
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class ClasspathWebProfileTest
{
    @Deployment
    public static Archive<?> createResourceLoaderArchive()
    {
        Archive<?> arch = ShrinkWrap.create(WebArchive.class, ClasspathWebProfileTest.class.getSimpleName() + ".war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset("some.propertykey = somevalue"), "myconfig.properties")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive());
        return arch;
    }

    @Inject
    @ExternalResource(storage = ClasspathStorage.class,location="myconfig.properties")
    private InputStream inputStream;

    @Inject
    @ExternalResource(storage = ClasspathStorage.class,location="myconfig.properties")
    private Properties properties;

    @Test
    public void testInputStream() throws IOException
    {
        Assert.assertNotNull(inputStream);
        Properties p = new Properties();
        p.load(inputStream);
        Assert.assertEquals("somevalue", p.getProperty("some.propertykey", "wrong answer"));
    }

    @Test
    public void testProperties()
    {
        Assert.assertEquals("somevalue",
                properties.getProperty("some.propertykey", "wrong answer"));
    }

    @Test
    public void testAmbiguousFileLookup(@ExternalResource(storage=ClasspathStorage.class,
            location="META-INF/beans.xml") InputStream inputStream)
    {
        // for some reason, this works
        Assert.assertNull(inputStream);
    }

    @Test
    public void testSuccessfulAmbiguousLookup(@ExternalResource(storage = ClasspathStorage.class,
            location="META-INF/beans.xml") List<InputStream> inputStreams)
    {
        Assert.assertTrue(inputStreams.size() > 1); //the count is different on as7 compared to the standalone setup
    }
}
