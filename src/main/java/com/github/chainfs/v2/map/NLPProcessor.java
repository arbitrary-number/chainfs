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
package com.github.chainfs.v2.map;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chainfs.ASTNode;
import com.github.chainfs.GenerateChainFSStructure;
import com.github.chainfs.v2.CreateNode3;
import com.github.chainfs.v2.io.Folder;

public class NLPProcessor {

    private static final Logger logger =
    		LoggerFactory.getLogger(NLPProcessor.class);


    private static final long PROCESSING_DELAY_MS = 20000; // 2 seconds

    public static void main(String[] args) throws IOException, InterruptedException {
    	process();
    }

    public static void process() throws IOException, InterruptedException {

        File commandPath =
        		new File(new File(
    					GenerateChainFSStructure.getDataDirectoryPath(), "/g"),
        				"commands to process");

        while (true) {
            boolean processed = false;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(
            		commandPath.toPath(), "*")) {
                for (Path commandFile : stream) {
                    Path lockFile = commandFile.resolveSibling(commandFile.getFileName().toString() + ".lock");

                    if (Files.exists(lockFile)) {
                        continue; // Skip file that's already locked
                    }

                    // Try to create lock file
                    try {
                        Files.createFile(lockFile);
                        System.out.println("Locked: " + commandFile.getFileName());

                        // Simulate processing
                        processCommand(commandFile);

                        // Remove command and lock
                        Files.deleteIfExists(commandFile);
                        Files.deleteIfExists(lockFile);

                        System.out.println("Processed and deleted: " + commandFile.getFileName());
                        processed = true;
                        break; // Process one file at a time
                    } catch (IOException e) {
                        System.err.println("Failed to create lock for: " + commandFile + " — skipping");
                    }
                }
            }

            // Wait before checking for the next command
            if (processed) {
                TimeUnit.MILLISECONDS.sleep(PROCESSING_DELAY_MS);
            } else {
                // No command found, wait a bit longer
                TimeUnit.MILLISECONDS.sleep(PROCESSING_DELAY_MS / 2);
            }
        }
    }

    private static void processCommand(Path commandFile) throws IOException {
        // Example: read and print command
        String command = commandFile.toFile().getName();
        System.out.println("Processing NLP command: " + command);

        String mapPrefix = "create a mapping from an x value of ";
        String mapPrefix2 = "create a mapping from an address value of ";
        String mapPrefix3 = "create a mapping from an ethereum address value of ";
        if (command.startsWith(mapPrefix)) {
        	int pos = command.indexOf(" ", mapPrefix.length() + 1);
        	String xValue = command.substring(mapPrefix.length(), pos);
        	logger.info("xValue |" + xValue + "|");
        	String searchString = " back to the g-node value of ";
			pos = command.indexOf(searchString);
			String gValue = command.substring(pos + searchString.length(), command.length());
        	logger.info("gValue |" + gValue + "|");
        	ASTNode process = CreateNode3.process(new BigInteger(xValue, 16), null);
        	String s = "this g-node contains a mapping from an x value of " +
        			xValue +
        			" to a g-node value of " + gValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	PlatformLogger.logPlatformPath(f);
        }
        if (command.startsWith(mapPrefix2)) {
        	int pos = command.indexOf(" ", mapPrefix2.length() + 1);
        	String bitcoinAddress = command.substring(mapPrefix2.length(), pos);
        	logger.info("xValue |" + bitcoinAddress + "|");
        	String searchString = " back to the g-node value of ";
			pos = command.indexOf(searchString);
			String gValue = command.substring(pos + searchString.length(), command.length());
        	logger.info("gValue |" + gValue + "|");
        	ASTNode process = CreateNode3.process(new BigInteger(bitcoinAddress, 16), null);
        	String s = "this g-node contains a mapping from a Bitcoin address value of " +
        			bitcoinAddress +
        			" to a g-node value of " + gValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	PlatformLogger.logPlatformPath(f);
        }
        if (command.startsWith(mapPrefix3)) {
        	int pos = command.indexOf(" ", mapPrefix3.length() + 1);
        	String eth = command.substring(mapPrefix3.length(), pos);
        	logger.info("Ethereum address |" + eth + "|");
        	String searchString = " back to the g-node value of ";
			pos = command.indexOf(searchString);
			String gValue = command.substring(pos + searchString.length(), command.length());
        	logger.info("gValue |" + gValue + "|");
        	ASTNode process = CreateNode3.process(new BigInteger(eth, 16), null);
        	String s = "this g-node contains a mapping from an ethereum address value of " +
        			eth +
        			" to a g-node value of " + gValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	PlatformLogger.logPlatformPath(f);
        }
    }
}
