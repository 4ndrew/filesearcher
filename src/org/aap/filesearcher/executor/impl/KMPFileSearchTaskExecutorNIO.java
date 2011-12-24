/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor.impl;

import org.aap.filesearcher.FileSearchBean;
import org.aap.filesearcher.executor.TaskAcceptor;
import org.aap.filesearcher.executor.TaskExecutor;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Knuth–Morris–Pratt algorithm substring pattern searching algorithm implementation using
 * new I/O API (see {@link java.nio}).
 * <p/>
 * Complexity: O(m)+O(n)
 * where m - length of substring,
 * n - length of the searchable text.
 */
public class KMPFileSearchTaskExecutorNIO implements TaskExecutor<FileSearchBean> {
    private final Logger logger = Logger.getLogger(KMPFileSearchTaskExecutorNIO.class);
    private final byte[] patternBytes;
    private final int bufferSize;

    private final int[] kmpNext;
    private final TaskAcceptor<FileSearchBean> resultCollector;
    private int counter = 0;

    public KMPFileSearchTaskExecutorNIO(byte[] patternBytes, TaskAcceptor<FileSearchBean> resultCollector, int bufferSize) {
        this.resultCollector = resultCollector;
        this.patternBytes = patternBytes;
        this.bufferSize = bufferSize;

        this.kmpNext = new int[patternBytes.length];
        // Pre-compute
        int j = -1;
        for (int i = 0; i < patternBytes.length; i++) {
            if (i == 0) {
                kmpNext[i] = -1;
            } else if (patternBytes[i] != patternBytes[j]) {
                kmpNext[i] = j;
            } else {
                kmpNext[i] = kmpNext[j];
            }

            while (j >= 0 && patternBytes[i] != patternBytes[j]) {
                j = kmpNext[j];
            }

            j++;
        }
    }

    @Override
    public Object initializeBuffer() {
        return ByteBuffer.allocateDirect(bufferSize);
    }

    @Override
    public void execute(FileSearchBean task, Object executorBuffer) throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(task.getInputFile());
        final FileChannel fc = fileInputStream.getChannel();
        final ByteBuffer byteBuffer = (ByteBuffer) executorBuffer;

        try {
            int j = 0;
            int buffReaded;
            byteBuffer.clear();

            logger.debug(String.format("0 [%d]Buffer: %d - %d - %d\n",
                    counter++,
                    byteBuffer.position(),
                    byteBuffer.limit(),
                    byteBuffer.capacity()));
            while ((buffReaded = fc.read(byteBuffer)) != -1 && j < patternBytes.length) {
                if (buffReaded == 0)
                    continue;

                byteBuffer.position(0);
                byteBuffer.limit(buffReaded);

                logger.debug(String.format("N Buffer reading: %d - %d - %d - %d\n",
                        byteBuffer.position(),
                        byteBuffer.limit(),
                        byteBuffer.capacity(),
                        byteBuffer.remaining()));

                while (byteBuffer.hasRemaining() && j < patternBytes.length) {
                    final int currChar = byteBuffer.get() & 0xff;

                    while (j >= 0 && currChar != (patternBytes[j] & 0xff)) {
                        j = kmpNext[j];
                    }
                    j++;

                    if (j >= patternBytes.length) {
                        resultCollector.push(task);
                        return;
                        //  j = kmpNext[j];
                    }
                }
                byteBuffer.clear();
                logger.debug(String.format("N Buffer: %d - %d - %d - %d\n",
                        byteBuffer.position(),
                        byteBuffer.limit(),
                        byteBuffer.capacity(),
                        byteBuffer.remaining()));
            }
        } finally {
            try {
                fc.close();
            } catch (IOException ioe) { /* ignore silently */ }
            try {
                fileInputStream.close();
            } catch (IOException ioe) { /* ignore silently */ }
        }
    }
}
