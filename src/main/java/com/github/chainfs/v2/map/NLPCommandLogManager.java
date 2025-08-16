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
import java.nio.file.*;
import java.util.*;

import com.github.chainfs.GenerateChainFSStructure;

public class NLPCommandLogManager {

    private static final int MAX_LINES_PER_FILE = 10000;

	private static final NLPCommandLogManager INSTANCE = new NLPCommandLogManager();

    private Path stateWrite;

    private Path stateRead;

    private Path commandPath;

    public NLPCommandLogManager() {
        commandPath =
        		new File(new File(
    					GenerateChainFSStructure.getDataDirectoryPath(), "/g"),
        				"commands to process").toPath();

        stateWrite = commandPath.resolve("state.write");
        stateRead = commandPath.resolve("state.read");

		try {
	       if (!Files.exists(stateWrite)) {
				Files.writeString(stateWrite, "1\n0");
	       }

	        if (!Files.exists(stateRead)) {
	        	Files.writeString(stateRead, "1\n0");
	        }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    // === Append a command ===
    public synchronized void appendCommand(String command) throws IOException {
        int fileNum = getWriteFileNumber();
        int lineCount = getWriteLineCount();

        Path file = getLogFile(fileNum);
        Files.writeString(file, command + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        lineCount++;
        if (lineCount >= MAX_LINES_PER_FILE) {
            fileNum++;
            lineCount = 0;
        }

        saveWriteState(fileNum, lineCount);
    }

    // === Read next command ===
    public synchronized Optional<String> readNextCommand() throws IOException {
        int fileNum = getReadFileNumber();
        int lineNum = getReadLineNumber();

        Path file = getLogFile(fileNum);
        if (!Files.exists(file)) return Optional.empty(); // nothing to read yet

        List<String> lines = Files.readAllLines(file);
        if (lineNum >= lines.size()) {
            // done with this file
            Files.delete(file);
            saveReadState(fileNum + 1, 0);
            return readNextCommand(); // try next file
        }

        String command = lines.get(lineNum);
        saveReadState(fileNum, lineNum + 1);
        return Optional.of(command);
    }

    // === Helper methods ===
    private int getWriteFileNumber() throws IOException {
        return Integer.parseInt(Files.readAllLines(stateWrite).get(0).trim());
    }

    private int getWriteLineCount() throws IOException {
        return Integer.parseInt(Files.readAllLines(stateWrite).get(1).trim());
    }

    private void saveWriteState(int fileNum, int lineCount) throws IOException {
        Files.write(stateWrite, Arrays.asList(String.valueOf(fileNum), String.valueOf(lineCount)));
    }

    private int getReadFileNumber() throws IOException {
        return Integer.parseInt(Files.readAllLines(stateRead).get(0).trim());
    }

    private int getReadLineNumber() throws IOException {
        return Integer.parseInt(Files.readAllLines(stateRead).get(1).trim());
    }

    private void saveReadState(int fileNum, int lineNum) throws IOException {
        Files.write(stateRead, Arrays.asList(String.valueOf(fileNum), String.valueOf(lineNum)));
    }

    private Path getLogFile(int fileNum) {
        return commandPath.resolve(String.format("commands to process.%03d.log", fileNum));
    }

	public static NLPCommandLogManager getInstance() {
		return INSTANCE;
	}
}
