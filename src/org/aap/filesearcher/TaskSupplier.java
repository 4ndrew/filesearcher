/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

/**
 * Task supplier.
 */
public interface TaskSupplier<T> {
    T pull() throws InterruptedException;
}
