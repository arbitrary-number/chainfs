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

import java.math.BigInteger;
import java.util.Scanner;

public class NLPChat {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hi! How are you today?");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }

			String prefix = "create a node based on the name ";
			String prefix2 = "create a high node based on the name ";
            if (input.toLowerCase().startsWith("convert ")) {
                String name = input.substring(8).trim();
                String value = convertNameToNumber(name, false);
                System.out.println("The numeric value of \"" + name + "\" is: " + value);
            } else if (input.toLowerCase().startsWith(
						prefix)) {
				String name = input.substring(prefix.length()).trim();
				String value = convertNameToNumber(name, false);
				System.out.println("The numeric value of \"" + name + "\" is: " + value);
				CreateNode3.process(new BigInteger(value), null);
            } else if (input.toLowerCase().startsWith(
						prefix2)) {
				String name = input.substring(prefix.length()).trim();
				String value = convertNameToNumber(name, false);
				System.out.println("The numeric value of \"" + name + "\" is: " + value);
				BigInteger highValue = new BigInteger("2").pow(256).subtract(
						new BigInteger("1"));
				CreateNode3.process(highValue, null);
			} else {
				System.out.println("I didn't understand that. Try: convert [name]\n"
				    		+ "or try: create a node based on the name [name]\n"
				    		+ "or try: create a high node based on the name [name] type 'exit'");
			}
        }

        scanner.close();
    }

    // Converts each letter to a number: a=1, ..., z=26, space=0
    public static String convertNameToNumber(String input, boolean space) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                sb.append(c - 'a' + 1);
                if (space) {
                	sb.append("0");
                }
            } else if (c == ' ') {
                sb.append("0");
            }
        }
        return sb.toString().trim();
    }
}
