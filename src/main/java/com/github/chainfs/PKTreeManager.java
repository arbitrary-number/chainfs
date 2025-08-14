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
package com.github.chainfs;

import java.io.File;
import java.math.BigInteger;

import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @formatter:off
/**
 * Creates the PK tree
 *
 */
// @formatter:on
public class PKTreeManager {

    private static final int BIT_LENGTH = 512;

    private static final Logger logger = LoggerFactory.getLogger(PKTreeManager.class);

    private static final BigInteger TWO = new BigInteger("2");

    private static final BigInteger SEVEN = new BigInteger("7");

    // @formatter:off
    static final BigInteger p =
        new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
    	  	  		 "FFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

    // @formatter:on

    public static void main(String[] args){
        // BigInteger initialNodeCount = new BigInteger("9", 16);
        // @formatter:off
        BigInteger initialNodeCount = new BigInteger("1110001011001110" +
        		                                     "0011001110001100", 2);
        // @formatter:on
        process(initialNodeCount, null, null, true);
    }

    public static String process(BigInteger initialNodeCount, File file
    		, String gLinkPointer, boolean verbose){

    	if (verbose) {
    		logger.info("initialNodeCount: " + initialNodeCount);
    		logger.info("Bit length: " + initialNodeCount.bitLength());
    	}
        SecP256K1Curve curve = new SecP256K1Curve();

        // @formatter:off
        BigInteger Gx =
            new BigInteger(
                "79BE667EF9DCBBAC" +
                "55A06295CE870B07" +
                "029BFCDB2DCE28D9" +
                "59F2815B16F81798", 16);

        BigInteger Gy =
            new BigInteger(
                "483ADA7726A3C465" +
                "5DA4FBFC0E1108A8" +
                "FD17B448A6855419" +
                "9C47D08FFB10D4B8", 16);
        // @formatter:on

        ECPoint G = curve.createPoint(Gx, Gy);

        if (verbose) {
        	logger.info("Initializing initial node from infinity...");
        }
        ECPoint current = curve.getInfinity();

        int gCount = 0;
        boolean firstOneFound = false;
        StringBuilder path = new StringBuilder();
        ASTNode pkTree = new ASTNode("pk", "infinity");
        ASTNode currentGNode = pkTree;
        ASTNode currentGNode2 = pkTree;
        logger.info("Looping from " + BIT_LENGTH + " to 0");
        for (int i = BIT_LENGTH; i >= 1; i--) {
            int bitIndex = i - 1;
            boolean isOne = initialNodeCount.testBit(bitIndex);
            int bit = isOne ? 1 : 0;
            if (verbose) {
            	logger.info("i = " + i);
            	logger.info("bitIndex = " + bitIndex);
            	logger.info("bit = " + bit);
            }
            if (isOne) {
                if (firstOneFound) {
                    gCount = gCount + gCount;
                    if (verbose) {
                    	logger.info("Doubling to create fs double node... (" + bit + ")");
                    }
                    current = current.twice();

                    String currentPath = currentGNode.getPath();
                    String newPath = currentPath + "/" + gCount + "pk";
                    ASTNode gChild = new ASTNode("gDouble", gCount + "pk", null, null, null, newPath);
                    currentGNode.addChild(gChild);
                    currentGNode2 = currentGNode;
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild, verbose);
                    gCount++;
                    if (verbose) {
                    	logger.info("Adding g to create fs node... (" + bit + ")");
                    }
                    current = current.add(G);
                    firstOneFound = true;
                    path.append("/" + gCount + "g/");
                    String newPath2 = currentGNode2.getPath() + "/" + gCount + "pk";
                    ASTNode gChild2 = new ASTNode("gAdd", gCount + "pk", null, null, null, newPath2);
                    currentGNode = gChild2;
                    currentGNode.addChild(gChild2);
                    createNode(256 - i, current, gCount, path.toString(), gChild2, verbose);
                } else {
                    gCount++;
                    if (verbose) {
                    	logger.info("Adding g to create first fs node... (" + bit + ")");
                    	logger.info("FS Node before g node = " + getFSNodeName(current));
                    }
                    current = current.add(G);
                    if (verbose) {
                    	logger.info(" New FS Node name = " + getFSNodeName(current));
                    }
                    firstOneFound = true;
                    path.append("/pk/");
                    ASTNode gChild = new ASTNode("gDouble", gCount + "pk", null, null, null,
                    		"/pk");
                    pkTree.addChild(gChild);
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild, verbose);
                }
            } else {
                if (firstOneFound) {
                    gCount = gCount + gCount;
                    String nodeName = getFSNodeName(current);
                    if (verbose) {
                    	logger.info("Node name =  " + nodeName);
                    }
                    BigInteger currentTwo = new BigInteger(nodeName).multiply(TWO).mod(p);
                    ECFieldElement rhs = curve.fromBigInteger(currentTwo);
                    if (verbose) {
                    	logger.info("RHS =  " + rhs);
                    }
                    current = current.twice();
                    if (verbose) {
                    	logger.info("New name after secure doubling =  " + getFSNodeName(current));
                    }
                    path.append("/" + gCount + "g/");
                    ASTNode gChild = new ASTNode("gDouble", gCount + "pk", null, null, null,
                            currentGNode.getPath() + "/" + gCount + "pk");
                    currentGNode.addChild(gChild);
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild, verbose);
                } else {
                	if (verbose) {
                		logger.info("Still at infinity: i = " + i);
                	}
                }
            }
        }
        return currentGNode.getPath();
    }

    private static String getFSNodeName(ECPoint current){
        ECFieldElement affineYCoord = current.normalize().getAffineYCoord();
        if (affineYCoord == null) {
            return "infinity";
        }
        BigInteger y = affineYCoord.toBigInteger();
        ECFieldElement affineXCoord = current.normalize().getAffineXCoord();
        BigInteger x = affineXCoord.toBigInteger();
        BigInteger LHS = y.pow(2).mod(p); // y^2 mod p = (x ^ 3 + 7) mod p
        BigInteger RHS = x.pow(3).add(SEVEN).mod(p);
        if (!LHS.equals(RHS)) {
            throw new IllegalStateException("Curve validation unsuccessful");
        }
        return LHS.toString();
    }

    static void createNode(int step, ECPoint point, int gCount,
    		String path, ASTNode astNode, boolean verbose){
        point = point.normalize();
        String label = (step == -1) ? "Public Key" : "Step " + step;
        if (verbose) {
        	logger.info(label + ":");
        }
        try {
            BigInteger xBigInteger = point.getAffineXCoord().toBigInteger();
            String x = xBigInteger.toString(16);
            if (verbose) {
            	logger.info("x = " + x);
            }
            BigInteger yBigInteger = point.getAffineYCoord().toBigInteger();
            String y = yBigInteger.toString(16);
            if (verbose) {
            	logger.info("y = " + y);
            }
            String dataDirectoryPath = GenerateChainFSStructure.getDataDirectoryPath();
            String newNode = null;
            if (astNode != null) {
                newNode = dataDirectoryPath + astNode.getPath() + "/";
            }
            newNode = newNode.replaceAll("\\\\", "/");
            if (verbose) {
            	logger.info("Creating " + gCount + "g node at " + newNode);
            }
            File newNodeFile = new File(newNode);
            boolean mkdirResult = newNodeFile.mkdirs();
            if (verbose) {
            	logger.info("Mkdir result: " + mkdirResult);
            }
            // Create the required fs metadata:
            File publicKeyFile = new File(newNodeFile, x + y);
            publicKeyFile.createNewFile();
            File publicKeyFile2 = new File(newNodeFile, "public_key-" + x + y);
            publicKeyFile2.createNewFile();
            File publicKeyFile3 = new File(newNodeFile, "pu-" + x + y);
            publicKeyFile3.createNewFile();
            File publicKeyFile4 = new File(newNodeFile, "04" + x + y);
            publicKeyFile4.createNewFile();
            File xFile = new File(newNodeFile, "x-" + x);
            xFile.createNewFile();
            // secure secp256k1 formula: y^2 mod p = (x ^ 3 + 7) mod p
            BigInteger LHS = yBigInteger.pow(2).mod(p);
            BigInteger RHS = xBigInteger.pow(3).add(SEVEN).mod(p);
            File yFile = new File(newNodeFile, "y-" + y);
            yFile.createNewFile();
            File y2modpFile = new File(newNodeFile, "y2modp-" + LHS);
            y2modpFile.createNewFile();
            File x3p7modpFile = new File(newNodeFile, "x3p7modp-" + RHS);
            x3p7modpFile.createNewFile();
            File gFile = new File(newNodeFile, gCount + "pk");
            gFile.createNewFile();
            File gFile2 = new File(newNodeFile, "pk-" + gCount);
            gFile2.createNewFile();
            File gFile3 = new File(newNodeFile, "private_key-" + gCount);
            gFile3.createNewFile();
            File gFile4 = new File(newNodeFile, "pr-" + gCount);
            gFile4.createNewFile();
            if (LHS.equals(RHS)) {
            	if (verbose) {
            		logger.info("FS node successfully created on secure secp256k1 curve");
            	}
            } else {
            	if (verbose) {
            		logger.info("FS node not on secure secp256k1 curve");
            	}
            }
        } catch (Exception e) {
            logger.info("FS nodes shouldn't be created at infinity");
        }
    }
}
