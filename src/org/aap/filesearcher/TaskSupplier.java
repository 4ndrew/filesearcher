package org.aap.filesearcher;

/**
 * Task supplier.
 */
public interface TaskSupplier<T> {
    T pull() throws InterruptedException;
}
