/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
 *
 */

package org.kie.api.runtime.process;

import java.util.Map;

/**
 * Represents one unit of work that needs to be executed.  It contains
 * all the information that it necessary to execute this unit of work
 * as parameters, and (possibly) results related to its execution.
 * <p>
 * <code>WorkItems</code> are conceptually identical to <code>TypedWorkItems</code>
 * but, instead of plain Java objects, they use Maps to represent parameters
 * and results.
 *
 * @see org.kie.api.runtime.process.TypedWorkItem
 * @see org.kie.api.runtime.process.TypedWorkItemHandler
 * @see org.kie.api.runtime.process.WorkItemHandler
 * @see org.kie.api.runtime.process.WorkItemManager
 */
public interface WorkItem extends TypedWorkItem<Map<String, Object>, Map<String, Object>> {

    int PENDING   = 0;
    int ACTIVE    = 1;
    int COMPLETED = 2;
    int ABORTED   = 3;

    /**
     * Returns the value of the parameter with the given name.  Parameters
     * can be used to pass information necessary for the execution of this
     * work item.  Returns <code>null</code> if the parameter cannot be found.
     * @param name the name of the parameter
     * @return the value of the parameter
     */
    Object getParameter(String name);

    /**
     * Returns the map of parameters of this work item.  Parameters
     * can be used to pass information necessary for the execution
     * of this work item.
     * @return the map of parameters of this work item
     */
    Map<String, Object> getParameters();

    /**
     * Returns the value of the result parameter with the given name.  Result parameters
     * can be used to pass information related the result of the execution of this
     * work item.  Returns <code>null</code> if the result cannot be found.
     * @param name the name of the result parameter
     * @return the value of the result parameter
     */
    Object getResult(String name);

    /**
     * Returns the map of result parameters of this work item.  Result parameters
     * can be used to pass information related the result of the execution of this
     * work item.
     * @return the map of results of this work item
     */
    Map<String, Object> getResults();

}
