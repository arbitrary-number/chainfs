/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) Arbitrary Project Team. All rights reserved.
 */
package com.github.chainfs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileImporter {

    private static final Logger logger = LoggerFactory.getLogger(CreateNode2.class);

    public static void main(String[] args){
        File directoryToImport = new File("/EXAMPLE_FOLDER");
        process(directoryToImport, false);
    }

    public static void process(File directoryToImport, boolean recursive){
        File[] fileList = directoryToImport.listFiles();
        for (File file : fileList) {
            if (file.getName().matches("^(.*?)")) {
                logger.info("Importing: file.getAbsolutePath()");
                BigInteger gNum = convertToBigInteger(file.getName());
                String directory = CreateNode2.process(gNum, file);
                try {
                    String prefix = GenerateChainFSStructure.getDataDirectoryPath();
                    File directoryFile = new File(prefix + directory + "/");
                    logger.info("Copying: " + file + " to " + directoryFile);
                    File newFile = new File(directoryFile, file.getName());
                    Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
            if (recursive && file.isDirectory()) {
                process(file, true);
            }
        }
    }

    public static BigInteger convertToBigInteger(String filename){
        try {
            byte[] encoding = filename.getBytes("UTF-8");
            BigInteger value = new BigInteger(encoding);
            return value;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

}
