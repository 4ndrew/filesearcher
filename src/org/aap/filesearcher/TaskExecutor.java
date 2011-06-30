package org.aap.filesearcher;

/**
 * Executor for task.
 */
public interface TaskExecutor<T> {
    void execute(T task) throws Exception;
}
