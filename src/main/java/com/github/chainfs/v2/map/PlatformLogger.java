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

import java.io.File;

public class PlatformLogger {
    public static void logPlatformPath(File file) {
        String rawPath = file.getAbsolutePath();
        String osName = System.getProperty("os.name").toLowerCase();

        String platformPath;
        if (osName.contains("win")) {
            // Windows: keep backslashes
            platformPath = rawPath.replace("/", "\\");
        } else {
            // Unix-like: convert to forward slashes
            platformPath = rawPath.replace("\\", "/");
        }

        System.out.println("Logging path: " + platformPath);
    }

    public static void main(String[] args) {
        File file = new File("some/nested/path/example.txt");
        logPlatformPath(file);
    }
}
