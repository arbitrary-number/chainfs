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

import java.util.HashMap;

/**
 * The class provides a set of common encoding techniques used by cryptographers.  Many
 * of the examples and methods are provided for educational purposes and include many
 * of the common terms used in cryptographic discussions.
 */
public class EncodingUtils {

	public static void main(String[] parameters) {
		String theResultInGematria = encodeAsGematria("Hello");
		System.out.println("In \"Gematria\", \"Hello\" is encoded as: " +
				theResultInGematria + ".");

		String theResult = encodeAsGematriaWithZeroes("Hello");
		System.out.println("In \"Gematria with noughts\", \"Hello\" is encoded as: " +
				theResult + ".");

		theResult =
				encodeAsReverseGematria("Hello");
		System.out.println("In \"Reverse Gematria\", \"Hello\" is encoded as: " +
				theResult + ".");

		theResult =
				encodeAsReverseGematriaWithZeroes("Hello");
		System.out.println("In \"Reverse Gematria with noughts\", \"Hello\" is encoded as: " +
				theResult + ".");
	}

    public static String decodeGematria(String input) {
        StringBuilder result = new StringBuilder();
        char[] parts = input.toCharArray();

        for (char part : parts) {
            try {
                int num = (int) part - 48;
                if (num >= 1 && num <= 26) {
                	char letter = (char) (num + 96);
                    result.append(letter);
                } else {
                    // Optionally skip or log invalid values
                }
            } catch (NumberFormatException e) {
                // Ignore non-numeric parts
            }
        }
        return result.toString();
    }

    public static String decodeReverseGematria(String input) {
        StringBuilder result = new StringBuilder();
        char[] parts = input.toCharArray();

        for (char part : parts) {
            try {
                int num = (int) part - 48;
                if (num >= 1 && num <= 26) {
                	char letter = (char) ((27 - num) + 96);
                    result.append(letter);
                } else {
                    // Optionally skip or log invalid values
                }
            } catch (NumberFormatException e) {
                // Ignore non-numeric parts
            }
        }
        return result.toString();
    }

    public static String decodeHebrewGematria(String input) {
        StringBuilder result = new StringBuilder();
        char[] parts = input.toCharArray();

        for (char part : parts) {
            try {
                int num = (int) part - 48;
                if (num >= 1 && num <= 26) {
                	char letter = (char) (num + 96);
                    result.append(letter);
                } else {
                    // Optionally skip or log invalid values
                }
            } catch (NumberFormatException e) {
                // Ignore non-numeric parts
            }
        }
        return result.toString();
    }

	public static String encodeAsGematria(String inputData) {
		StringBuilder sb = new StringBuilder();
		inputData = inputData.toLowerCase();
        for (char c : inputData.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                int num = c - 'a' + 1;
                if (sb.length() > 0) sb.append(' ');
                sb.append(num);
            }
            // ignore other characters (spaces, punctuation)
        }
        return sb.toString();
	}

	public static String encodeAsReverseGematria(String inputData) {
		StringBuilder sb = new StringBuilder();
		inputData = inputData.toLowerCase();
        for (char c : inputData.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                int num = 27 - (c - 'a' + 1);
                if (sb.length() > 0) sb.append(' ');
                sb.append(num);
            }
            // ignore other characters (spaces, punctuation)
        }
        return sb.toString();
	}

	public static String encodeAsGematriaWithZeroes(String inputData) {
		StringBuilder sb = new StringBuilder();
		inputData = inputData.toLowerCase();
        for (char c : inputData.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                int num = c - 'a' + 1;
                if (sb.length() > 0) sb.append('0');
                sb.append(num);
            }
            // ignore other characters (spaces, punctuation)
        }
        return sb.toString();
	}

	public static String encodeAsReverseGematriaWithZeroes(String inputData) {
		StringBuilder sb = new StringBuilder();
		inputData = inputData.toLowerCase();
        for (char c : inputData.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                int num = 27 - (c - 'a' + 1);
                if (sb.length() > 0) sb.append('0');
                sb.append(num);
            }
            // ignore other characters (spaces, punctuation)
        }
        return sb.toString();
	}

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        // Pad with leading zero if hex length is odd
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }

        int len = hex.length();
        byte[] result = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                  + Character.digit(hex.charAt(i+1), 16));
        }
        return result;
    }

	public static String convertToReverseGematriaWithSpaces() {
		return null;

	}

	// Create my alpha encoding format

	// Create my rectangular format


	private static final HashMap<Integer, Character> hebrewMap
		= new HashMap<Integer, Character>();

	static {
		hebrewMap.put(1, 'a');
		hebrewMap.put(2, 'b');
		hebrewMap.put(3, 'c');
		hebrewMap.put(4, 'd');
		hebrewMap.put(5, 'e');
		hebrewMap.put(6, 'f');
		hebrewMap.put(7, 'g');
		hebrewMap.put(8, 'h');
		hebrewMap.put(9, 'i');
		hebrewMap.put(600, 'j');
		hebrewMap.put(10, 'k');
		hebrewMap.put(20, 'l');
		hebrewMap.put(30 ,'m');
		hebrewMap.put(40, 'n');
		hebrewMap.put(50, 'o');
		hebrewMap.put(60, 'p');
		hebrewMap.put(70, 'q');
		hebrewMap.put(80, 'r');
		hebrewMap.put(90, 's');
		hebrewMap.put(100, 't');
		hebrewMap.put(200, 'u');
		hebrewMap.put(700, 'v');
		hebrewMap.put(900, 'w');
		hebrewMap.put(300, 'x');
		hebrewMap.put(400, 'y');
		hebrewMap.put(500, 'z');
	}
}
