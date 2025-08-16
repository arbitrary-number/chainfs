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
package com.github.chainfs.v2;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;

public class MultiSlotFileCounter {
    private final Path directory;
    private final int slots = 10;
    private int nextSlot = 0;

    public MultiSlotFileCounter(String dirPath) throws IOException {
        this.directory = Paths.get(dirPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    // Increment the counter in the next slot (round-robin)
    public synchronized long increment() throws IOException {
        int slotToUse = nextSlot;
        nextSlot = (nextSlot + 1) % slots;

        Path slotFile = directory.resolve("counter" + slotToUse + ".txt");
        return incrementSlot(slotFile);
    }

    // Increment the count in a single slot file atomically
    private long incrementSlot(Path slotFile) throws IOException {
        // Create file if missing
        if (!Files.exists(slotFile)) {
            Files.write(slotFile, "0\n".getBytes(), StandardOpenOption.CREATE_NEW);
        }

        try (RandomAccessFile raf = new RandomAccessFile(slotFile.toFile(), "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) {

            raf.seek(0);
            String line = raf.readLine();
            long currentValue = 0;
            if (line != null && !line.trim().isEmpty()) {
                currentValue = Long.parseLong(line.trim());
            }

            long newValue = currentValue + 1;

            raf.setLength(0);
            raf.seek(0);
            raf.writeBytes(Long.toString(newValue) + System.lineSeparator());

            return newValue;
        }
    }

    // Get total count by summing all slots
    public long getTotalCount() throws IOException {
        long total = 0;
        for (int i = 0; i < slots; i++) {
            Path slotFile = directory.resolve("counter" + i + ".txt");
            if (Files.exists(slotFile)) {
                try (BufferedReader reader = Files.newBufferedReader(slotFile)) {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        total += Long.parseLong(line.trim());
                    }
                }
            }
        }
        return total;
    }

    // Example usage
    public static void main(String[] args) throws IOException {
        MultiSlotFileCounter counter = new MultiSlotFileCounter("counterSlots");

        // Increment counter 15 times (rotates through slots 0-9, then 0-4)
        for (int i = 0; i < 15; i++) {
            long val = counter.increment();
            System.out.println("Incremented slot counter to: " + val);
        }

        long total = counter.getTotalCount();
        System.out.println("Total count across all slots: " + total);
    }
}
