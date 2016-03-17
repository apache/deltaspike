package org.apache.deltaspike.test.core.impl.future;

import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class FutureableTest {
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "FutureableTest.jar")
                .addPackage(Service.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "FutureableTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Service service;

    @Test
    public void future()
    {
        final Future<String> future = service.thatSLong(1000);
        int count = 0;
        for (int i = 0; i < 1000; i++)
        {
            if (future.isDone())
            {
                break;
            }
            count++;
        }
        try
        {
            assertEquals("done", future.get());
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            fail();
        }
        catch (final ExecutionException e)
        {
            fail(e.getMessage());
        }
        assertEquals(1000, count);
    }
}
