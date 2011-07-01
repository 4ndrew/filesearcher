/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher.executor.impl;

import org.aap.filesearcher.FileSearchBean;
import org.aap.filesearcher.executor.TaskAcceptor;
import org.aap.filesearcher.executor.TaskExecutor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Simple Naive substring pattern search algorithm implementation.
 * Complexity: O(nm),
 *  where m - length of substring,
 *  n - length of the searchable text.
 */
public class NaiveFileSearchTaskExecutor implements TaskExecutor<FileSearchBean> {
    private final byte[] patternBytes;
    private final TaskAcceptor<FileSearchBean> resultCollector;

    public NaiveFileSearchTaskExecutor(byte[] patternBytes, TaskAcceptor<FileSearchBean> resultCollector) {
        this.resultCollector = resultCollector;
        this.patternBytes = patternBytes;
    }

    @Override
    public void execute(FileSearchBean task) throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(task.getInputFile());
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        try {
            // Naive substring matching algorithm
            int buff;
            while ((buff = bufferedInputStream.read()) != -1) {
                if (buff == (patternBytes[0] & 0xff)) {
                    bufferedInputStream.mark(patternBytes.length);
                    int k = 1;
                    while (k < patternBytes.length
                            && (buff = bufferedInputStream.read()) != -1
                            && (patternBytes[k] & 0xff) == buff) {
                        k++;
                    }

                    if (k == patternBytes.length) {
                        // We found whole pattern
                        resultCollector.push(task);
                        break;
                    }
                    bufferedInputStream.reset();
                }
            }
        } finally {
            try {
                bufferedInputStream.close();
            } catch (IOException ioe) { /* ignore silently */ }
            try {
                fileInputStream.close();
            } catch(IOException ioe) { /* ignore silently */ }
        }
    }
}
