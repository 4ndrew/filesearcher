/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor;

/**
 * Task supplier interface.
 */
public interface TaskSupplier<T> {
    /**
     * Pull new task from supplier.
     *
     * @return new task for processing (null task is allowed)
     * @throws InterruptedException if end of task is occurs or processing canceled.
     */
    T pull() throws InterruptedException;
}
