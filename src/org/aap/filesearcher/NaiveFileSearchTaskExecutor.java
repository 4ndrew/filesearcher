/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Simple Naive substring pattern search algorithm implementation.
 * Complexity: O((n-m+1) m),
 *  where m - length of substring,
 *  n - length of the searchable text.
 */
public class NaiveFileSearchTaskExecutor implements TaskExecutor<FileSearchTask> {
    private final byte[] patternBytes;
    private final TaskAcceptor<FileSearchTask> resultCollector;

    public NaiveFileSearchTaskExecutor(String dataSearch, Charset charset, TaskAcceptor<FileSearchTask> resultCollector) {
        this.resultCollector = resultCollector;
        this.patternBytes = dataSearch.getBytes(charset);
    }

    @Override
    public void execute(FileSearchTask task) throws Exception {
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
