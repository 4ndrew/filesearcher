package org.aap.filesearcher;

/**
 * Task acceptor interface.
 */
public interface TaskAcceptor<T> {
    public void push(T task);
    public void eof();
}
