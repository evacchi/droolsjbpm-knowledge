/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.instance.timer;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.core.time.Job;
import org.drools.core.time.JobHandle;
import org.drools.core.time.TimerService;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.CronTrigger;
import org.drools.core.time.impl.IntervalTrigger;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TimerManager {

    static final Logger logger = LoggerFactory.getLogger(TimerManager.class);

    private long timerId = 0;

    private TimerManageRuntime kruntime;
    private TimerService timerService;
    private Map<Long, TimerInstance> timers = new ConcurrentHashMap<Long, TimerInstance>();
    public static final Job processJob = new ProcessJob();
    public static final Job startProcessJob = new StartProcessJob();

    public TimerManager(TimerManageRuntime kruntime, TimerService timerService) {
        this.kruntime = kruntime;
        this.timerService = timerService;
    }

    public void registerTimer(final TimerInstance timer, ProcessInstance processInstance) {
        try {
            kruntime.startOperation();

            timer.setId(++timerId);
            timer.setProcessInstanceId(processInstance.getId());
            timer.setSessionId(((KieSession) kruntime).getIdentifier());
            timer.setActivated(new Date());

            Trigger trigger = null;

            if (timer.getCronExpression() != null) {
                Date startTime = new Date(timerService.getCurrentTime() + 1000);
                trigger = new CronTrigger(timerService.getCurrentTime(), startTime, null, -1, timer.getCronExpression(), null, null);
                // cron timers are by nature repeatable
                timer.setPeriod(1);
            } else {
                trigger = new IntervalTrigger(timerService.getCurrentTime(), null, null, timer.getRepeatLimit(),
                                              timer.getDelay(), timer.getPeriod(), null, null);
            }
            ProcessJobContext ctx = new ProcessJobContext(timer, trigger, processInstance.getId(), this.kruntime);

            JobHandle jobHandle = this.timerService.scheduleJob(processJob, ctx, trigger);

            timer.setJobHandle(jobHandle);
            timers.put(timer.getId(), timer);
        } finally {
            kruntime.endOperation();
        }
    }

    public void registerTimer(final TimerInstance timer, String processId, Map<String, Object> params) {
        try {
            kruntime.startOperation();

            timer.setId(++timerId);
            timer.setProcessInstanceId(-1l);
            timer.setSessionId(kruntime.getIdentifier());
            timer.setActivated(new Date());

            Trigger trigger = null;

            if (timer.getCronExpression() != null) {
                Date startTime = new Date(timerService.getCurrentTime() + 1000);
                trigger = new CronTrigger(timerService.getCurrentTime(), startTime, null, -1, timer.getCronExpression(), null, null);
                // cron timers are by nature repeatable
                timer.setPeriod(1);
            } else {
                trigger = new IntervalTrigger(timerService.getCurrentTime(), null, null, timer.getRepeatLimit(), timer.getDelay(),
                                              timer.getPeriod(), null, null);
            }
            StartProcessJobContext ctx = new StartProcessJobContext(timer, trigger, processId, params, this.kruntime);

            JobHandle jobHandle = this.timerService.scheduleJob(startProcessJob, ctx, trigger);

            timer.setJobHandle(jobHandle);
            timers.put(timer.getId(), timer);
        } finally {
            kruntime.endOperation();
        }
    }

    public void internalAddTimer(final TimerInstance timer) {
        long delay;
        Date lastTriggered = timer.getLastTriggered();
        if (lastTriggered == null) {
            Date activated = timer.getActivated();
            Date now = new Date();
            long timespan = now.getTime() - activated.getTime();
            delay = timer.getDelay() - timespan;
            if (delay < 0) {
                delay = 0;
            }
        } else {
            Date now = new Date();
            long timespan = now.getTime() - lastTriggered.getTime();
            delay = timespan - timer.getPeriod();
            if (delay < 0) {
                delay = 0;
            }
        }
        Trigger trigger = new IntervalTrigger(timerService.getCurrentTime(), null, null, -1, delay, timer.getPeriod(), null, null);
        ProcessJobContext ctx = new ProcessJobContext(timer, trigger, timer.getProcessInstanceId(), this.kruntime);

        JobHandle jobHandle = this.timerService.scheduleJob(processJob, ctx, trigger);
        timer.setJobHandle(jobHandle);
        timers.put(timer.getId(), timer);
    }

    public void cancelTimer(long timerId) {
        try {
            kruntime.startOperation();

            TimerInstance timer = timers.remove(timerId);
            if (timer != null) {
                timerService.removeJob(timer.getJobHandle());
            }
        } finally {
            kruntime.endOperation();
        }
    }

    public void dispose() {
        // for ( TimerInstance timer : timers.values() ) {
        // timerService.removeJob( timer.getJobHandle() );
        // }
        if (timerService.getClass().getCanonicalName().equals("org.jbpm.process.core.timer.impl.RegisteredTimerServiceDelegate")) {
            timers.clear();
            return;
        }
        for (Iterator<TimerInstance> it = timers.values().iterator(); it.hasNext(); ) {
            TimerInstance timer = it.next();
            timerService.removeJob(timer.getJobHandle());
            it.remove();
        }
        timerService.shutdown();
    }

    public TimerService getTimerService() {
        return this.timerService;
    }

    public Collection<TimerInstance> getTimers() {
        return timers.values();
    }

    public Map<Long, TimerInstance> getTimerMap() {
        return this.timers;
    }

    public long internalGetTimerId() {
        return timerId;
    }

    public void internalSetTimerId(long timerId) {
        this.timerId = timerId;
    }

    public void setTimerService(TimerService timerService) {
        this.timerService = timerService;
    }

    /**
     * Overdue aware trigger that introduces fixed delay to allow completion of session initialization
     */
    public static class OverdueTrigger implements Trigger {

        private static final long serialVersionUID = -2368476147776308013L;

        public static final long OVERDUE_DELAY = Long.parseLong(System.getProperty("jbpm.overdue.timer.delay", "2000"));

        private Trigger orig;
        private TimerManageRuntime kruntime;

        public OverdueTrigger(Trigger orig, TimerManageRuntime kruntime) {
            this.orig = orig;
            this.kruntime = kruntime;
        }

        public Date hasNextFireTime() {
            Date date = orig.hasNextFireTime();
            if (date == null) {
                return null;
            }
            long then = date.getTime();
            long now = kruntime.getSessionClock().getCurrentTime();
            // overdue timer
            if (then < now) {
                return new Date((now + OVERDUE_DELAY));
            } else {
                return orig.hasNextFireTime();
            }
        }

        public Date nextFireTime() {
            return orig.nextFireTime();
        }
    }
}
