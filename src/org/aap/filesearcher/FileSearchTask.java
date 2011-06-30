/*
 * Copyright 2011 Andrew Porokhin. All rights reserved.
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */

package org.aap.filesearcher;

import java.io.File;

/**
 * File-search task bean.
 */
public class FileSearchTask implements Task {
    private final File inputFile;

    public FileSearchTask(File inputFile) {
        this.inputFile = inputFile;
    }

        public File getInputFile() {
        return inputFile;
    }

    @Override
    public String toString() {
        return "FileSearchTask: " + inputFile.toString();
    }
}
