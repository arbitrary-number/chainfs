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
package com.github.chainfs;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates the initial structure for chainfs, following XDG standards
 */
public class GenerateChainFSStructure {

    // Use XDG Standards for ChainFS
    private static final String LOCAL_SHARE_DATA_SECP256K1 = "/.local/share/data/secp256k1";

    private static final Logger logger = LoggerFactory.getLogger(GenerateChainFSStructure.class);

    public static void main(String[] args){
        try {
            String dataDirPath = getDataDirectoryPath();
            File dataDir = new File(dataDirPath);

            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                if (created) {
                    logger.info("Directory created: {}", dataDirPath);
                } else {
                    logger.error("Failed to create directory: {}", dataDirPath);
                }
            } else {
                logger.info("Directory already exists: {}", dataDirPath);
            }
        } catch (Exception e) {
            logger.error("An error occurred while creating the directory", e);
        }
    }

    public static String getDataDirectoryPathSatoshi() {
    	return getDataDirectoryPath() + "/satoshi";
    }

    public static String getDataDirectoryPath() {
        Map<String, String> env = System.getenv();
        String os = System.getProperty("os.name").toLowerCase();

        String baseDir;

        try {
	        if (os.contains("win")) {
	            String userProfile = env.get("USERPROFILE");
	            if (userProfile == null) {
	                throw new IOException("Environment variable USERPROFILE is not set");
	            }
	            baseDir = userProfile + LOCAL_SHARE_DATA_SECP256K1;
	        } else {
	            String home = env.get("HOME");
	            if (home == null) {
	                throw new IOException("Environment variable HOME is not set");
	            }
	            baseDir = home + LOCAL_SHARE_DATA_SECP256K1;
	        }
        } catch (Exception e) {
        	throw new IllegalStateException(e);
        }
        return baseDir;
    }
}
