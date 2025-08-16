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
 * Copyright (c) Arbitrary Number Project Team. All rights reserved.
 */
package com.github.chainfs.v2.map;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chainfs.ASTNode;
import com.github.chainfs.GenerateChainFSStructure;
import com.github.chainfs.v2.CreateNode3;
import com.github.chainfs.v2.io.Folder;
import com.github.chainfs.v4.CreateSatoshiNode;

public class NLPProcessor {

    private static final Logger logger =
    		LoggerFactory.getLogger(NLPProcessor.class);


    private static final long PROCESSING_DELAY_MS = 2000; // 2 seconds

    public static void main(String[] args) throws IOException, InterruptedException {
    	process();
    }

    public static void process() throws IOException, InterruptedException {

        File commandPath =
        		new File(new File(
    					GenerateChainFSStructure.getDataDirectoryPath(), "/g"),
        				"commands to process");

	        while (true) {
	            processCommand();
	   	     TimeUnit.MILLISECONDS.sleep(PROCESSING_DELAY_MS);
	      }

       }


    private static void processCommand() throws IOException {
        // Example: read and print command
    	NLPCommandLogManager manager = NLPCommandLogManager.getInstance();
        Optional<String> commandOptional = manager.readNextCommand();
        if (commandOptional.isEmpty()) {
        	return;
        }
        String command = commandOptional.get();
        System.out.println("Processing NLP command: " + command);

        String mapPrefix = "create a mapping from an x value of ";
        String mapPrefix2 = "create a mapping from an address value of ";
        String mapPrefix3 = "create a mapping from an ethereum address value of ";
        String mapPrefix4 = "create s-node mapping from an x value of ";
        String mapPrefix5 = "create s-node mapping from an address value of ";
        String mapPrefix6 = "create s-node mapping from an ethereum address value of ";
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
        	CreateSatoshiNode.process(new BigInteger(xValue, 16), null);
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
        	ASTNode process = CreateNode3.process(new BigInteger(1, bitcoinAddress.getBytes()), null);
        	String s = "this g-node contains a mapping from a Bitcoin address value of " +
        			bitcoinAddress +
        			" to a g-node value of " + gValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	CreateSatoshiNode.process(new BigInteger(bitcoinAddress, 16), null);
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
        	ASTNode process = CreateNode3.process(new BigInteger(1, eth.getBytes()), null);
        	String s = "this g-node contains a mapping from an ethereum address value of " +
        			eth +
        			" to a g-node value of " + gValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	CreateSatoshiNode.process(new BigInteger(eth, 16), null);
        	PlatformLogger.logPlatformPath(f);
        }
        if (command.startsWith(mapPrefix4)) {
        	int pos = command.indexOf(" ", mapPrefix4.length() + 1);
        	String xValue = command.substring(mapPrefix4.length(), pos);
        	logger.info("xValue |" + xValue + "|");
        	String searchString = " back to the s-node value of ";
			pos = command.indexOf(searchString);
			String sValue = command.substring(pos + searchString.length(), command.length());
        	logger.info("sValue |" + sValue + "|");
        	ASTNode process = CreateNode3.process(new BigInteger(xValue, 16), null);
        	String s = "this g-node contains a mapping from an x value of " +
        			xValue +
        			" to a s-node value of " + sValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	String conflictDetectionCheck = "this g-node contains a mapping from an x value of ";
        	String[] conflictDetectionFileList = new File(path).list();
        	for (String file : conflictDetectionFileList) {
        		searchString = " to a g-node value of ";
        		if (file.startsWith(conflictDetectionCheck) &&
        				file.contains(searchString)) {
                	logger.warn("Detected a conflict between G-nodes and S-nodes");
                	logger.warn("Conflict detected at an x value of: " + xValue);
                	logger.warn("Conflict detected at s-node: " + sValue);
        			searchString = "  to a g-node value of  ";
        			pos = command.indexOf(searchString);
        			String gValue = command.substring(pos + searchString.length(), command.length());
                	logger.warn("Conflict detected at g-node: " + gValue);
                	logger.warn("Sleeping for a minute, then exiting...");
                	try {
						Thread.sleep(60000L);
					} catch (InterruptedException e) {
						throw new IllegalStateException(e);
					}
                	System.exit(0);
        		}
        	}
        	CreateSatoshiNode.process(new BigInteger(xValue, 16), null);
        	PlatformLogger.logPlatformPath(f);
        }
        if (command.startsWith(mapPrefix5)) {
        	int pos = command.indexOf(" ", mapPrefix5.length() + 1);
        	String bitcoinAddress = command.substring(mapPrefix5.length(), pos);
        	logger.info("xValue |" + bitcoinAddress + "|");
        	String searchString = " back to the s-node value of ";
			pos = command.indexOf(searchString);
			String sValue = command.substring(pos + searchString.length(), command.length());
        	logger.info("gValue |" + sValue + "|");
        	ASTNode process = CreateNode3.process(new BigInteger(1, bitcoinAddress.getBytes()), null);
        	String s = "this g-node contains a mapping from a Bitcoin address value of " +
        			bitcoinAddress +
        			" to a s-node value of " + sValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	String conflictDetectionCheck =
        			"this g-node contains a mapping from a Bitcoin address value of ";
        	String[] conflictDetectionFileList = new File(path).list();
        	for (String file : conflictDetectionFileList) {
        		searchString = " to a g-node value of ";
        		if (file.startsWith(conflictDetectionCheck) &&
        				file.contains(searchString)) {
                	logger.warn("Detected a conflict between G-nodes and S-nodes");
                	logger.warn("Conflict detected at Bitcoin address: " + bitcoinAddress);
                	logger.warn("Conflict detected at s-node: " + sValue);
        			pos = command.indexOf(searchString);
        			String gValue = command.substring(pos + searchString.length(), command.length());
                	logger.warn("Conflict detected at g-node: " + gValue);
                	logger.warn("Sleeping for a minute, then exiting...");
                	try {
						Thread.sleep(60000L);
					} catch (InterruptedException e) {
						throw new IllegalStateException(e);
					}
                	System.exit(0);
        		}
        	}
        	CreateSatoshiNode.process(new BigInteger(bitcoinAddress, 16), null);
        	PlatformLogger.logPlatformPath(f);
        }
        if (command.startsWith(mapPrefix6)) {
        	int pos = command.indexOf(" ", mapPrefix6.length() + 1);
        	String eth = command.substring(mapPrefix6.length(), pos);
        	logger.info("Ethereum address |" + eth + "|");
        	String searchString = " back to the g-node value of ";
			pos = command.indexOf(searchString);
			String sValue = command.substring(pos + searchString.length(), command.length());
        	logger.info("gValue |" + sValue + "|");
        	ASTNode process = CreateNode3.process(new BigInteger(1, eth.getBytes()), null);
        	String s = "this g-node contains a mapping from an ethereum address value of " +
        			eth +
        			" to a s-node value of " + sValue;
        	String path = GenerateChainFSStructure.getDataDirectoryPath()
        		+ process.getPath();
        	File f = new File(path, s);
        	f.createNewFile();
        	String conflictDetectionCheck =
        			"this g-node contains a mapping from an ethereum address value of ";
        	String[] conflictDetectionFileList = new File(path).list();
        	for (String file : conflictDetectionFileList) {
        		searchString = " to a g-node value of ";
        		if (file.startsWith(conflictDetectionCheck) &&
        				file.contains(searchString)) {
                	logger.warn("Detected a conflict between G-nodes and S-nodes");
                	logger.warn("Conflict detected at Bitcoin address: " + eth);
                	logger.warn("Conflict detected at s-node: " + sValue);
        			pos = command.indexOf(searchString);
        			String gValue = command.substring(pos + searchString.length(), command.length());
                	logger.warn("Conflict detected at g-node: " + gValue);
                	logger.warn("Sleeping for a minute, then exiting...");
                	try {
						Thread.sleep(60000L);
					} catch (InterruptedException e) {
						throw new IllegalStateException(e);
					}
                	System.exit(0);
        		}
        	}
        	CreateSatoshiNode.process(new BigInteger(eth, 16), null);
        	PlatformLogger.logPlatformPath(f);
        }
    }
}
