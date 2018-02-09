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
package org.apache.deltaspike.core.impl.monitoring;

import org.apache.deltaspike.core.api.monitoring.MonitorResultEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * This class will observe all {@link MonitorResultEvent}s
 * and log them accordingly
 */
@ApplicationScoped
public class InvocationResultLogger
{
    private static final Logger logger = Logger.getLogger(InvocationResultLogger.class.getName());

    private static final int DEFAULT_MAX_LOG_LINES = 8;
    private static final String PROPERTY_MAX_LOG_LINES = "MAX_LOG_LINES";

    private int maxLogLines = DEFAULT_MAX_LOG_LINES + 1;

    @PostConstruct
    private void init()
    {
        String maxLogLinesProp = System.getProperty(PROPERTY_MAX_LOG_LINES);
        if (maxLogLinesProp != null)
        {
            maxLogLines = Integer.parseInt(maxLogLinesProp) + 1;
        }

        logger.info("Using MAX_LOG_LINE=" + maxLogLines);
    }

    public void logMonitorResultEvents(@Observes MonitorResultEvent mre)
    {
        // we copy them because we don't like to make the event data dirty.
        // there might be other observers interested in the result...
        List<ResultEntry> methodInvocations
            = createMethodResultEntries(mre.getMethodInvocations(), mre.getMethodDurations());
        List<ResultEntry> classInvocations
            = createClassResultEntries(mre.getClassInvocations());

        StringBuilder sb = new StringBuilder();
        sb.append("Top Class Invocations:\n");
        for (int i = 1; i < maxLogLines && i <= classInvocations.size(); i++)
        {
            ResultEntry re = classInvocations.get(classInvocations.size() - i);
            sb.append("  count: ").append(re.getCount()).append("\t").append(re.getToken()).append("\n");
        }
        logger.info(sb.toString());

        sb = new StringBuilder();
        sb.append("Top Method Invocations:\n");
        for (int i = 1; i < maxLogLines && i <= methodInvocations.size(); i++)
        {
            ResultEntry re = methodInvocations.get(methodInvocations.size() - i);
            sb.append("  dur[ms]: ").append(re.getDuration() / 1e6f).append("\tcount: ").
                    append(re.getCount()).append("\t").append(re.getToken()).append("\n");
        }
        logger.info(sb.toString());
    }


    private List<ResultEntry> createMethodResultEntries(Map<String, AtomicInteger> invocations,
                                                        Map<String, AtomicLong> durations)
    {
        List<ResultEntry> resultEntries = new ArrayList<ResultEntry>(invocations.size());



        for (Map.Entry<String, AtomicInteger> entry : invocations.entrySet())
        {
            long dur = durations.get(entry.getKey()).longValue();
            resultEntries.add(new ResultEntry(entry.getValue().intValue(), entry.getKey(), dur));
        }

        Collections.sort(resultEntries);

        return resultEntries;
    }

    private List<ResultEntry> createClassResultEntries(Map<String, AtomicInteger> invocations)
    {
        List<ResultEntry> resultEntries = new ArrayList<ResultEntry>(invocations.size());

        for (Map.Entry<String, AtomicInteger> entry : invocations.entrySet())
        {
            resultEntries.add(new ResultEntry(entry.getValue().intValue(), entry.getKey(), 0L));
        }

        Collections.sort(resultEntries);

        return resultEntries;
    }

    private static class ResultEntry implements Comparable<ResultEntry>
    {
        private Integer count;
        private String token;
        private long    duration;

        private ResultEntry(Integer count, String token, long duration)
        {
            this.count = count;
            this.token = token;
            this.duration = duration;
        }

        public Integer getCount()
        {
            return count;
        }

        public String getToken()
        {
            return token;
        }

        public long getDuration()
        {
            return duration;
        }

        public int compareTo(ResultEntry o)
        {
            if (duration == 0 && o.duration == 0)
            {
                return count.compareTo(o.count);
            }

            return duration < o.duration ? -1 : (duration == o.duration ? 0 : 1);
        }
    }
}
