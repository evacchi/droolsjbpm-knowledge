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

import java.util.Map;

import org.drools.core.time.Trigger;

public class StartProcessJobContext extends ProcessJobContext {

    private static final long serialVersionUID = -5219141659893424294L;
    private String processId;
    private Map<String, Object> paramaeters;

    public StartProcessJobContext(
            TimerInstance timer,
            Trigger trigger,
            String processId,
            Map<String, Object> params,
            TimerManageRuntime kruntime) {
        super(timer, trigger, null, kruntime);
        this.processId = processId;
        this.paramaeters = params;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Map<String, Object> getParamaeters() {
        return paramaeters;
    }

    public void setParamaeters(Map<String, Object> paramaeters) {
        this.paramaeters = paramaeters;
    }

    @Override
    public boolean isNewTimer() {
        return false;
    }
}
