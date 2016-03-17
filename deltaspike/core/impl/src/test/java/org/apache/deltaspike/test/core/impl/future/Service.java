package org.apache.deltaspike.test.core.impl.future;

import org.apache.deltaspike.core.api.future.Futureable;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class Service
{
    @Futureable // or CompletableFuture<String>
    public Future<String> thatSLong(final long sleep)
    {
        try
        {
            Thread.sleep(sleep);
            // return CompletableFuture.completedFuture("done");
            return new Future<String>()  // EE will have AsyncFuture but more designed for j8 ^^
            {
                @Override
                public boolean cancel(final boolean mayInterruptIfRunning)
                {
                    return false;
                }

                @Override
                public boolean isCancelled()
                {
                    return false;
                }

                @Override
                public boolean isDone()
                {
                    return true;
                }

                @Override
                public String get() throws InterruptedException, ExecutionException
                {
                    return "done";
                }

                @Override
                public String get(final long timeout, final TimeUnit unit)
                        throws InterruptedException, ExecutionException, TimeoutException
                {
                    return "done";
                }
            };
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            throw new IllegalStateException(e);
        }
    }
}
