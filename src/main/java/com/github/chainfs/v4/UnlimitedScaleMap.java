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
package com.github.chainfs.v4;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chainfs.ASTNode;
import com.github.chainfs.GenerateChainFSStructure;
import com.github.chainfs.v2.CreateNode3;

/*
 * Store up to 2^256 - 1 data points with O(1) lookup time using
 * the SecP256K1 Curve with both Infinity -> G and/or
 * Infinity -> Satoshi Origin, redundancy and disk persistence.
 */
public class UnlimitedScaleMap {

	private boolean USE_G_TREE = true;

	private boolean USE_SATOSHI_TREE = true;

    private static final Logger logger = LoggerFactory.getLogger(UnlimitedScaleMap.class);

	public UnlimitedScaleMap() {}

	public String put(String key, String value, boolean overwrite) {
		String previousValue = null;
		BigInteger keyNumber = new BigInteger(1, key.getBytes());
		if (USE_G_TREE) {
			ASTNode treeNode = CreateNode3.process(keyNumber, null);
			String fsNode = GenerateChainFSStructure.getDataDirectoryPath() + treeNode.getPath();
			File keyFile = new File(fsNode, "Unlimited scale map key with key " + key);
			try {
				boolean result = keyFile.createNewFile();
				if (result) {
					previousValue = new String(Files.readAllBytes(keyFile.toPath()));
				}
				if (overwrite || previousValue == null) {
					Files.write(keyFile.toPath(), value.getBytes(),
							StandardOpenOption.TRUNCATE_EXISTING);
				}
				if (USE_SATOSHI_TREE) {
					treeNode = CreateSatoshiNode.process(keyNumber, null);
					fsNode = GenerateChainFSStructure.getDataDirectoryPathSatoshi() +
							treeNode.getPath();
					keyFile = new File(fsNode, "Unlimited scale map key with key " + key);
					try {
						result = keyFile.createNewFile();
						if (result) {
							String backupValue = new String(Files.readAllBytes(keyFile.toPath()));
							if (!backupValue.equals(previousValue)) {
								logger.warn("Backup value did not equal previous value");
							}
						}
						if (overwrite || previousValue == null) {
							Files.write(keyFile.toPath(), value.getBytes(),
									StandardOpenOption.TRUNCATE_EXISTING);
						}
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
				return previousValue;
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	public String get(String key) {
		String previousValue = null;
		BigInteger keyNumber = new BigInteger(1, key.getBytes());
		if (USE_G_TREE) {
			ASTNode treeNode = CreateNode3.process(keyNumber, null);
			String fsNode = GenerateChainFSStructure.getDataDirectoryPath() + treeNode.getPath();
			File keyFile = new File(fsNode, "Unlimited scale map key with key " + key);
			try {
				previousValue = new String(Files.readAllBytes(keyFile.toPath()));
				if (USE_SATOSHI_TREE) {
					treeNode = CreateSatoshiNode.process(keyNumber, null);
					fsNode = GenerateChainFSStructure.getDataDirectoryPathSatoshi() +
							treeNode.getPath();
					keyFile = new File(fsNode, "Unlimited scale map key with key " + key);
					try {
						String backupValue = new String(Files.readAllBytes(keyFile.toPath()));
						if (!backupValue.equals(previousValue)) {
							logger.warn("Backup value did not equal previous value");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return previousValue;
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}
}
