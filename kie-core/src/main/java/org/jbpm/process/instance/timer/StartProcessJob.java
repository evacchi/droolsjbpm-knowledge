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

import static org.jbpm.process.instance.timer.TimerManager.logger;

public class StartProcessJob implements Job,
                                        Serializable {

        private static final long serialVersionUID = 1039445333595469160L;

        public void execute(JobContext c) {

            StartProcessJobContext ctx = (StartProcessJobContext) c;

            TimerManageRuntime kruntime = ctx.getKnowledgeRuntime();
            TimerManager tm = kruntime.getTimerManager();
            
            if (!ctx.processRuntimeIsActive()) {
                logger.debug("Timer for starting process {} is ignored as the deployment is in deactivated state", ctx.getProcessId());
                tm.getTimerMap().remove(ctx.getTimer().getId());
                tm.getTimerService().removeJob(ctx.getJobHandle());
                
                return;
            }
            try {
                kruntime.startOperation();
                ctx.getTimer().setLastTriggered(
                        new Date(ctx.getKnowledgeRuntime().<SessionClock> getSessionClock().getCurrentTime()));

                // if there is no more trigger reset period on timer so its node instance can be removed
                if (ctx.getTrigger().hasNextFireTime() == null) {
                    ctx.getTimer().setPeriod(0);
                }
                kruntime.startProcess(ctx.getProcessId(), ctx.getParamaeters(), "timer");

                if (ctx.getTimer().getPeriod() == 0) {
                    tm.getTimerMap().remove(ctx.getTimer().getId());
                    tm.getTimerService().removeJob(ctx.getJobHandle());
                }

            } catch (Throwable e) {
                logger.error("Error when executing start process " + ctx.getProcessId() + " timer job", e);

            } finally {
                kruntime.endOperation();
            }
        }

    }