package org.kie.api.runtime.process;

public interface BaseWorkItem {

    /**
     * The unique id of this work item
     * @return the id of this work item
     */
    long getId();

    /**
     * The name of the work item.  This represents the type
     * of work that should be executed.
     * @return the name of the work item
     */
    String getName();

    /**
     * The state of the work item.
     * @return the state of the work item
     */
    int getState();

    /**
     * The id of the process instance that requested the execution of this
     * work item
     * @return the id of the related process instance
     */
    long getProcessInstanceId();
}
