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
 * Knuth–Morris–Pratt algorithm substring pattern searching algorithm implementation.
 * Complexity: O(m)+O(n)
 *  where m - length of substring,
 *  n - length of the searchable text.
 */
public class KMPFileSearchTaskExecutorNIO implements TaskExecutor<FileSearchBean> {
    private final Logger logger = Logger.getLogger(KMPFileSearchTaskExecutorNIO.class);
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private final byte[] patternBytes;
    private final int[] kmpNext;
    private final ByteBuffer byteBuffer;
    private final TaskAcceptor<FileSearchBean> resultCollector;

    public KMPFileSearchTaskExecutorNIO(byte[] patternBytes, TaskAcceptor<FileSearchBean> resultCollector) {
        this(patternBytes, resultCollector, DEFAULT_BUFFER_SIZE);
    }

    public KMPFileSearchTaskExecutorNIO(byte[] patternBytes, TaskAcceptor<FileSearchBean> resultCollector, int bufferSize) {
        this.resultCollector = resultCollector;
        this.patternBytes = patternBytes;
        this.byteBuffer = ByteBuffer.allocate(bufferSize);

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
    public void execute(FileSearchBean task) throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(task.getInputFile());
        final FileChannel fc = fileInputStream.getChannel();

        logger.debug(task);

        try {
            int j = 0;
            int buffReaded;
            byteBuffer.clear();
            logger.debug(String.format("0 Buffer: %d - %d - %d\n",
                    byteBuffer.position(),
                    byteBuffer.limit(),
                    byteBuffer.capacity()));
            while ((buffReaded = fc.read(byteBuffer)) != -1 && j < patternBytes.length) {
                if (buffReaded == 0)
                    continue;

                byteBuffer.position(0);
                byteBuffer.limit(buffReaded);
                while (byteBuffer.hasRemaining()) {
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
                logger.debug(String.format("N Buffer: %d - %d - %d\n",
                    byteBuffer.position(),
                    byteBuffer.limit(),
                    byteBuffer.capacity()));
            }
        } finally {
            try {
                fileInputStream.close();
            } catch(IOException ioe) { /* ignore silently */ }
        }
    }
}
