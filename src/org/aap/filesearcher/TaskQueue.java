/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

import java.util.LinkedList;

/**
 * Simple blocking task queue.
 *
 * @param <T> Task type
 */
public class TaskQueue<T> implements TaskAcceptor<T>, TaskSupplier<T> {
    private final LinkedList<T> taskQueue;
    private boolean endOfData;

    public TaskQueue() {
        taskQueue = new LinkedList<T>();
    }

    public T pull() throws InterruptedException {
        synchronized (taskQueue) {
            while (taskQueue.size() == 0 && !endOfData) {
                taskQueue.wait();
            }

            if (taskQueue.size() > 0) {
                return taskQueue.removeFirst();
            }
        }
        Thread.currentThread().interrupt();
        return null;
    }

    public void push(T task) {
        synchronized (taskQueue) {
            taskQueue.addLast(task);
            taskQueue.notify();
        }
    }

    @Override
    public void eof() {
        synchronized (taskQueue) {
            endOfData = true;
            taskQueue.notifyAll();
        }
    }
}
