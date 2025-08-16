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
package com.github.chainfs.v2.io;

import java.io.File;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chainfs.ASTNode;
import com.github.chainfs.GenerateChainFSStructure;
import com.github.chainfs.v2.CreateNode3;

public class Folder {

    private static final Logger logger = LoggerFactory.getLogger(Folder.class);

	public static void createFolder(BigInteger gNode, String name) {
		ASTNode process = CreateNode3.process(gNode, null);
		String path = process.getPath();
		File folder;
		folder = new File(new File(
					GenerateChainFSStructure.getDataDirectoryPath(), path), name);
		boolean mkdir = folder.mkdir();
		if (!mkdir) {
			logger.info("Folder not created");
		}
	}

	public static void main(String[] parameters) {
		Folder.createFolder(new BigInteger("1"), "commands to process");
	}
}
