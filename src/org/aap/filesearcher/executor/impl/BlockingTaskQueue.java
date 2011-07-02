/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor.impl;

import org.aap.filesearcher.executor.TaskAcceptor;
import org.aap.filesearcher.executor.TaskSupplier;

import java.util.LinkedList;

/**
 * Simple blocking task queue.
 *
 * <p>Call to {@link #pull()} will be blocked if queue is empty.
 * <p>Call to {@link #push(Object)} will be blocked if queue size greater than max size.
 *
 * @param <T> Task type
 * @see #BlockingTaskQueue(int)
 */
public class BlockingTaskQueue<T> implements TaskAcceptor<T>, TaskSupplier<T> {
    private final LinkedList<T> taskQueue;
    private final int maxSize;
    private boolean endOfData;

    /** Size update lock, should not be locked  */
    private final Object sizeUpdateLock = new Object();
    private int currentSize = 0;

    /**
     * Create blocking queue with {@link Integer#MAX_VALUE} as max queue size.
     * @see #BlockingTaskQueue(int)
     */
    public BlockingTaskQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Create blocking queue with specified max size (push will be blocked).
     *
     * @param maxSize Max size of the queue
     * @throws IllegalArgumentException if maxSize equals or less zero
     */
    public BlockingTaskQueue(int maxSize) throws IllegalArgumentException {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize shall be greater than zero");
        }
        this.maxSize = maxSize;
        taskQueue = new LinkedList<T>();
    }

    /**
     * Call to this method can be blocked if queue size is zero.
     *
     * @return New object (possibly null)
     * @throws InterruptedException if interrupted
     * @see #BlockingTaskQueue(int)
     */
    @Override
    public T pull() throws InterruptedException {
        synchronized (taskQueue) {
            while (taskQueue.size() == 0 && !endOfData) {
                taskQueue.wait();
            }

            if (taskQueue.size() > 0) {
                T task = taskQueue.removeFirst();
                notifyUpdateSize(taskQueue.size());
                return task;
            }
        }
        Thread.currentThread().interrupt();
        return null;
    }

    /**
     * Call to this method can be blocked if max queue size is reached.
     *
     * @throws IllegalArgumentException if task is null
     * @throws InterruptedException if interrupted
     */
    @Override
    public void push(T task) throws IllegalArgumentException, InterruptedException {
        if (task == null) {
            throw new IllegalArgumentException("Task shall not be null");
        }

        boolean isAdded = false;
        while (!isAdded) {
            int currentSize;
            synchronized (taskQueue) {
                currentSize = taskQueue.size();
                if (currentSize < maxSize) {
                    isAdded = true;
                    taskQueue.addLast(task);
                    taskQueue.notify();
                }
            }

            if (!isAdded) {
                waitForSizeLowerThan(currentSize - maxSize / 2 + 1);
            }
        }
    }

    @Override
    public void signalEndOfData() {
        synchronized (taskQueue) {
            endOfData = true;
            taskQueue.notifyAll();
        }
    }

    /**
     * Safely update length of list (due we can't use concurrent).
     * @param newSize new size of list.
     */
    private void notifyUpdateSize(int newSize) {
        synchronized (sizeUpdateLock) {
            currentSize = newSize;
            sizeUpdateLock.notifyAll();
        }
    }

    /**
     * Wait until list size is changed to specified value or lower.
     *
     * @param expectedSize Expected size.
     * @throws InterruptedException if thread interrupted.
     */
    private void waitForSizeLowerThan(int expectedSize) throws InterruptedException {
        synchronized (sizeUpdateLock) {
            while (expectedSize < currentSize) {
                sizeUpdateLock.wait();
            }
        }
    }
}
