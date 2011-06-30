package org.aap.filesearcher;

/**
 * Runner for task.
 * @param <T> Type of Task.
 */
public class TaskRunner<T> implements Runnable {
    private final TaskSupplier<T> taskSupplier;
    private final TaskExecutor<T> taskExecutor;
    private volatile long tasksProcessed;
    private volatile Thread executorThread;

    public TaskRunner(TaskSupplier<T> taskSupplier, TaskExecutor<T> taskExecutor) {
        this.taskSupplier = taskSupplier;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {
        tasksProcessed = 0;
        executorThread = Thread.currentThread();
        while (!executorThread.isInterrupted()) {
            try {
                final T t = taskSupplier.pull();
                if (t != null) {
                    System.out.println("[" + executorThread.getName() + "] Executing: " + t);
                    taskExecutor.execute(t);
                    tasksProcessed++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.out.println("Task throws an exception, ignoring...");
                e.printStackTrace(System.err);
            }
        }
    }

    public long getTasksProcessed() {
        return tasksProcessed;
    }

    public Thread getExecutorThread() {
        return executorThread;
    }

}
