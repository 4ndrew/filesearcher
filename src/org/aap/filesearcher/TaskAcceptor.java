/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

/**
 * Task acceptor interface.
 */
public interface TaskAcceptor<T> {
    public void push(T task);
    public void eof();
}
