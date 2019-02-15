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

import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.Trigger;

public class ProcessJobContext implements JobContext {

    private static final long serialVersionUID = 476843895176221627L;

    private Long processInstanceId;
    private transient TimerManageRuntime kruntime;
    private TimerInstance timer;
    private Trigger trigger;

    private JobHandle jobHandle;
    private Long sessionId;

    private boolean newTimer;
    private TimerManageRuntime runtime;

    public ProcessJobContext(final TimerInstance timer, final Trigger trigger, final Long processInstanceId,
                             final TimerManageRuntime kruntime) {
        this.timer = timer;
        this.trigger = trigger;
        this.processInstanceId = processInstanceId;
        this.kruntime = kruntime;
        this.sessionId = timer.getSessionId();
        this.newTimer = true;
    }

    public ProcessJobContext(final TimerInstance timer, final Trigger trigger, final Long processInstanceId,
                             final TimerManageRuntime kruntime, boolean newTimer) {
        this.timer = timer;
        this.trigger = trigger;
        this.processInstanceId = processInstanceId;
        this.kruntime = kruntime;
        this.sessionId = timer.getSessionId();
        this.newTimer = newTimer;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public JobHandle getJobHandle() {
        return this.jobHandle;
    }

    public void setJobHandle(JobHandle jobHandle) {
        this.jobHandle = jobHandle;
    }

    public TimerInstance getTimer() {
        return timer;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public boolean isNewTimer() {
        return newTimer;
    }

    public boolean processRuntimeIsActive() {
        return false;
    }

    public void setKnowledgeRuntime(TimerManageRuntime kieSession) {
        this.runtime = kieSession;
    }

    public TimerManageRuntime getKnowledgeRuntime() {
        return runtime;
    }
}
