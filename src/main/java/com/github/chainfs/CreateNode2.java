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
 * Creates the initial node: g, for chainfs, using the secure secp256k1 algorithm
 *
 * The secp256k1 algorithm combined with the double and add algorithm, forms
 * the basis of the secure chainfs file system.  The filesystem uses an ASTTree
 * structure for efficiency.
 *
 * Features:
 *
 * - Stores one file per g folder
 * - Ability to store more files than the number of subatomic particles in the
 *   universe
 * - Each file gets its own special g node  (storing in g mode)
 * - Universal distributed RAID redundancy provides a 100% guaranteed no data loss
 * - Ability to locate photos and videos at dedicated g nodes (storing in g2mode).
 *   Note that with g2mode, the parameters must include the g node number in addition
 *   to the media file name
 * - You can also store your favorite novels and ebooks securely in g2mode to prevent
 *   them from being lost
 * - The g nodes will be arranged in a double and add AST tree node for efficiency
 *
 * Planned features: maintain a second AST tree of incomplete nodes (nodes missing
 * the g metadata file, with the tree leaves and nodes based on the double and add
 * algorithm using the public key number).  A sync class will be created that syncs
 * any nodes that match between the g tree and this new pk tree to complete the
 * required chainfs metadata (x,y,y^2modp etc.).  This second AST tree can also be
 * used for offline backup in case the internet goes down, and the data can be
 * sent to your distributed backup systems to guard against data loss (similar to
 * RAID systems, but with more features).  This will form the pk double and add
 * AST tree.
 *
 * To store files in the system you must convert them to base p in numerical form
 * and then use the StoreBaseP class so that each file will be stored at the correct
 * node.  Even though it uses a folder per file, there are 2^256 folders available
 * so even if you took all the files in the universe, it still wouldn't use up all
 * the folders available in chainfs.
 *
 * Filesystem limits:
 *
 * Maximum folder count limit in chainfs: 2^256
 *
 * Note that due to the double and add algorithm, some g numbers are not reachable
 * e.g. 6, 7 and 8 and won't be used
 *
 * Standards based:
 *
 * Works with existing Linux tools:
 *
 * rsync - restore your chainfs from backups on your LAN, WAN, or from the internet
 *       - may also be used to backup your chainfs to your LAN, WAN, or to the internet
 *       - any new chainfs nodes will automatically be created in the correct places
 *         in the fs ASTTrees
 * scp   - can be used as an alternative to rsync
 * find - find an existing file or metadata file
 * ls - list your files
 *
 */
// @formatter:on
public class CreateNode2 {

	// Must be set to true to prevent distributed file conflicts for
	// file system integrity
	private static final boolean BIDIRECTIONAL = true;

    private static final int BIT_LENGTH = 256;

    private static final Logger logger = LoggerFactory.getLogger(CreateNode2.class);

    private static final BigInteger TWO = new BigInteger("2");

    private static final BigInteger SEVEN = new BigInteger("7");

    // @formatter:off
    static final BigInteger p =
        new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
    	  	  		 "FFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

	private static final String SEPERATOR = "_";

    // @formatter:on

    public static void main(String[] args){
        // BigInteger initialNodeCount = new BigInteger("9", 16);
        // @formatter:off
        BigInteger initialNodeCount = new BigInteger("1110001011001110" +
        		                                     "0011001110001100", 2);
        // @formatter:on
        process(initialNodeCount, null);
    }

    public static String process(BigInteger initialNodeCount, File file){

        logger.info("initialNodeCount: " + initialNodeCount);
        logger.info("Bit length: " + initialNodeCount.bitLength());

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

        logger.info("Initializing initial node from infinity...");
        ECPoint current = curve.getInfinity();

        int gCount = 0;
        boolean firstOneFound = false;
        StringBuilder path = new StringBuilder();
        ASTNode gTree = new ASTNode("g", "infinity");
        ASTNode pkTree = new ASTNode("pk", "infinity");
        ASTNode currentGNode = gTree;
        ASTNode currentGNode2 = gTree;
        logger.info("Looping from " + BIT_LENGTH + " to 0");
        for (int i = BIT_LENGTH; i >= 1; i--) {
            int bitIndex = i - 1;
            boolean isOne = initialNodeCount.testBit(bitIndex);
            int bit = isOne ? 1 : 0;
            logger.info("i = " + i);
            logger.info("bitIndex = " + bitIndex);
            logger.info("bit = " + bit);
            if (isOne) {
                if (firstOneFound) {
                    gCount = gCount + gCount;
                    logger.info("Doubling to create fs double node... (" + bit + ")");
                    current = current.twice();

                    String currentPath = currentGNode.getPath();
                    String newPath = currentPath + "/" + gCount + "g";
                    ASTNode gChild = new ASTNode("gDouble", gCount + "g", null, null, null, newPath);
                    currentGNode.addChild(gChild);
                    currentGNode2 = currentGNode;
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild);
                    gCount++;
                    logger.info("Adding g to create fs node... (" + bit + ")");
                    current = current.add(G);
                    firstOneFound = true;
                    path.append("/" + gCount + "g/");
                    String newPath2 = currentGNode2.getPath() + "/" + gCount + "g";
                    ASTNode gChild2 = new ASTNode("gAdd", gCount + "g", null, null, null, newPath2);
                    currentGNode = gChild2;
                    currentGNode.addChild(gChild2);
                    createNode(256 - i, current, gCount, path.toString(), gChild2);
                } else {
                    gCount++;
                    logger.info("Adding g to create first fs node... (" + bit + ")");
                    logger.info("FS Node before g node = " + getFSNodeName(current));
                    current = current.add(G);
                    logger.info(" New FS Node name = " + getFSNodeName(current));
                    firstOneFound = true;
                    path.append("/g/");
                    ASTNode gChild = new ASTNode("gDouble", gCount + "g", null, null, null, "/g");
                    gTree.addChild(gChild);
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild);
                }
            } else {
                if (firstOneFound) {
                    gCount = gCount + gCount;
                    String nodeName = getFSNodeName(current);
                    logger.info("Node name =  " + nodeName);
                    BigInteger currentTwo = new BigInteger(nodeName).multiply(TWO).mod(p);
                    ECFieldElement rhs = curve.fromBigInteger(currentTwo);
                    logger.info("RHS =  " + rhs);

                    current = current.twice();
                    logger.info("New name after secure doubling =  " + getFSNodeName(current));
                    path.append("/" + gCount + "g/");
                    ASTNode gChild = new ASTNode("gDouble", gCount + "g", null, null, null,
                            currentGNode.getPath() + "/" + gCount + "g");
                    currentGNode.addChild(gChild);
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild);
                } else {
                    logger.info("Still at infinity: i = " + i);
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

    static void createNode(int step, ECPoint point, int gCount, String path, ASTNode astNode){
        point = point.normalize();
        String label = (step == -1) ? "Public Key" : "Step " + step;
        logger.info(label + ":");
        try {
            BigInteger xBigInteger = point.getAffineXCoord().toBigInteger();
            String xBeforeMod = xBigInteger.toString(16);
            BigInteger xModP = xBigInteger.mod(p);
			String x = xModP.toString(16);
            logger.info("x = " + x);
            BigInteger yBigInteger = point.getAffineYCoord().toBigInteger();
            String yBeforeMod = yBigInteger.toString(16);
            BigInteger yModP = yBigInteger.mod(p);
			String y = yModP.toString(16);
            logger.info("y = " + y);
            String dataDirectoryPath = GenerateChainFSStructure.getDataDirectoryPath();
            String newNode = null;
            if (astNode != null) {
                newNode = dataDirectoryPath + astNode.getPath() + "/";
            }
            newNode = newNode.replaceAll("\\\\", "/");
            logger.info("Creating " + gCount + "g node at " + newNode);
            File newNodeFile = new File(newNode);
            boolean mkdirResult = newNodeFile.mkdirs();
            logger.info("Mkdir result: " + mkdirResult);
            // Create the required fs metadata:
            String publicKeyCoordinates = x + y;
            String hexXY = NumberFormatUtils.concatXY(xModP, yModP, 32); // For secp256k1: 32 bytes
            //store without "04" for performance reasons:
            if (BIDIRECTIONAL) {
            	PKTreeManager.process(new BigInteger(hexXY, 16), null,
            		astNode.getPath(), false);
            }
			File publicKeyFile = new File(newNodeFile, "pkdec" +
            		SEPERATOR + publicKeyCoordinates);
            publicKeyFile.createNewFile();
            File publicKeyFile2 = new File(newNodeFile, "public_key" +
            		SEPERATOR + "04" + hexXY);
            publicKeyFile2.createNewFile();
            File publicKeyFile3 = new File(newNodeFile, "pu" + SEPERATOR +
            		hexXY);
            publicKeyFile3.createNewFile();
            File publicKeyFile4 = new File(newNodeFile, hexXY);
            publicKeyFile4.createNewFile();
            File xFile = new File(newNodeFile, "x-" + x);
            xFile.createNewFile();
            File xBeforeModPFile = new File(newNodeFile, "xbeforemod" +
            		SEPERATOR + xBeforeMod);
            xBeforeModPFile.createNewFile();
            // secure secp256k1 formula: y^2 mod p = (x ^ 3 + 7) mod p
            BigInteger LHS = yBigInteger.pow(2).mod(p);
            BigInteger RHS = xBigInteger.pow(3).add(SEVEN).mod(p);
            File yFile = new File(newNodeFile, "y-" + y);
            yFile.createNewFile();
            File yBeforeModPFile = new File(newNodeFile, "ybeforemod"
            		+ SEPERATOR + yBeforeMod);
            yBeforeModPFile.createNewFile();
            File y2modpFile = new File(newNodeFile, "y2modp" +
            		SEPERATOR + LHS);
            y2modpFile.createNewFile();
            File x3p7modpFile = new File(newNodeFile, "x3p7modp" +
            		SEPERATOR + RHS);
            x3p7modpFile.createNewFile();
            File gFile = new File(newNodeFile, gCount + "g");
            gFile.createNewFile();
            File gFile2 = new File(newNodeFile, "g" +
            		SEPERATOR + + gCount);
            gFile2.createNewFile();
            File gFile3 = new File(newNodeFile, "private_key" +
            		SEPERATOR + gCount);
            gFile3.createNewFile();
            File gFile4 = new File(newNodeFile, "pr" + SEPERATOR +
            		gCount);
            gFile4.createNewFile();
            if (LHS.equals(RHS)) {
                logger.info("FS node successfully created on secure secp256k1 curve");
            } else {
                logger.info("FS node not on secure secp256k1 curve");
            }
        } catch (Exception e) {
            logger.info("FS nodes shouldn't be created at infinity");
        }
    }
}
