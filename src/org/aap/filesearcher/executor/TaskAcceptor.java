/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor;

/**
 * Task acceptor interface.
 */
public interface TaskAcceptor<T> {
    /**
     * Push new task.
     *
     * @param task Task (can't be nul).
     * @throws IllegalArgumentException if argument if null
     * @throws InterruptedException if thread interrupted during execution
     */
    public void push(T task) throws IllegalArgumentException, InterruptedException;

    /**
     * Signal Acceptor End Of Data.
     */
    public void signalEndOfData();
}
