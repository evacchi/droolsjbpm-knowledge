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

import java.io.Serializable;
import java.util.Date;

import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.kie.api.time.SessionClock;

public class ProcessJob implements Job,
                                   Serializable {

    private static final long serialVersionUID = 6004839244692770390L;

    public void execute(JobContext c) {

        ProcessJobContext ctx = (ProcessJobContext) c;

        Long processInstanceId = ctx.getProcessInstanceId();
        TimerManageRuntime kruntime = ctx.getKnowledgeRuntime();
        try {
            kruntime.startOperation();
            if (processInstanceId == null) {
                throw new IllegalArgumentException("Could not find process instance for timer ");
            }

            ctx.getTimer().setLastTriggered(
                    new Date(ctx.getKnowledgeRuntime().<SessionClock>getSessionClock().getCurrentTime()));

            // if there is no more trigger reset period on timer so its node instance can be removed
            if (ctx.getTrigger().hasNextFireTime() == null) {
                ctx.getTimer().setPeriod(0);
            }

            kruntime.getSignalManager().signalEvent(processInstanceId,
                                                    "timerTriggered", ctx.getTimer());

            TimerManager tm = ctx.getKnowledgeRuntime().getTimerManager();

            if (ctx.getTimer().getPeriod() == 0) {
                tm.getTimerMap().remove(ctx.getTimer().getId());
                tm.getTimerService().removeJob(ctx.getJobHandle());
            }
        } catch (Throwable e) {
            TimerManager.logger.error("Error when executing timer job", e);
            throw new RuntimeException(e);
        } finally {
            kruntime.endOperation();
        }
    }
}
