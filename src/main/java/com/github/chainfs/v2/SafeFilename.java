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
package com.github.chainfs.v2;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SafeFilename {

    // Pattern to match characters not allowed on Windows and problematic on Unix
    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");

    // Optional: match emojis (Unicode range for emoticons, pictographs, etc.)
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
        "[\\p{So}\\p{Cn}&&[^\\p{L}\\p{N}\\p{P}\\p{Z}]]"
    );

    // Normalize to NFC (safe default across platforms)
    public static String normalize(String filename) {
        return Normalizer.normalize(filename, Normalizer.Form.NFC);
    }

    // Remove invalid characters
    public static String removeIllegalChars(String filename) {
        return ILLEGAL_CHARS.matcher(filename).replaceAll("_");
    }

    // Optional: strip emojis
    public static String removeEmojis(String filename) {
        return EMOJI_PATTERN.matcher(filename).replaceAll("");
    }

    // Truncate to a safe max length (e.g., 255 bytes)
    public static String truncate(String filename, int maxLength) {
        if (filename.length() <= maxLength) return filename;
        return filename.substring(0, maxLength);
    }

    // All-in-one method
    public static String makeSafe(String input, boolean stripEmojis) {
        String name = normalize(input);
        if (stripEmojis) {
            name = removeEmojis(name);
        }
        name = removeIllegalChars(name);
        return truncate(name, 255); // max safe length
    }

    public static void main(String[] parameters) {
    	String unsafeName = "🚀 Launch Plan: Phase 1 (final).docx";
    	String safeName = SafeFilename.makeSafe(unsafeName, true);

    	System.out.println("Safe filename: " + safeName);
    	// Output: " Launch Plan_ Phase 1 (final).docx"
    }
}
