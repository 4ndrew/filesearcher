/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

/**
 * Executor for task.
 */
public interface TaskExecutor<T> {
    void execute(T task) throws Exception;
}
