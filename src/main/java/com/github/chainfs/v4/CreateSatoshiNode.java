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
import java.math.BigInteger;

import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chainfs.ASTNode;
import com.github.chainfs.GenerateChainFSStructure;
import com.github.chainfs.NumberFormatUtils;
import com.github.chainfs.PKTreeManager;
import com.github.chainfs.v2.Bech32m;
import com.github.chainfs.v2.BitcoinPublicKeyCompressor;
import com.github.chainfs.v2.EncodingUtils;
import com.github.chainfs.v2.EthereumAddress;
import com.github.chainfs.v2.LegacyBitcoinAddressGenerator;
import com.github.chainfs.v2.NestedSegwitAddress;
import com.github.chainfs.v2.SafeFilename;
import com.github.chainfs.v2.UtilityToFindABitcoinAddressFromAPublicKey;
import com.github.chainfs.v2.map.NLPCommandLogManager;

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
public class CreateSatoshiNode {

	// Must be set to true to prevent distributed file conflicts for
	// file system integrity
	private static final boolean BIDIRECTIONAL = true;

    private static final int BIT_LENGTH = 256;

    private static final Logger logger = LoggerFactory.getLogger(CreateSatoshiNode.class);

    private static final BigInteger TWO = new BigInteger("2");

    private static final BigInteger SEVEN = new BigInteger("7");

    // @formatter:off
    static final BigInteger p =
        new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
    	  	  		 "FFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);

	private static final boolean PROCESS_PK_TREES = false;

    // @formatter:on

    public static void main(String[] args){
        // BigInteger initialNodeCount = new BigInteger("9", 16);
        // @formatter:off
        BigInteger initialNodeCount = new BigInteger("20474324734897982753892347983298");
        //"aa9a3e43e57a4cd717991c959f9fc40c43aa2667e16ea169d9f8310670af15ce", 16);
        //"1110001011001110" +
        		                                     //"0011001110001100", 2);
        // @formatter:on
        process(initialNodeCount, null);
    }

    public static ASTNode process(BigInteger gMultiplier, File file){

        logger.info("gMultiplier: " + gMultiplier);
        logger.info("Bit length: " + gMultiplier.bitLength());

        SecP256K1Curve curve = new SecP256K1Curve();

        ECPoint G = SatoshiPoint.getSatoshiPoint(curve);

        logger.info("Initializing initial node from infinity...");
        ECPoint current = curve.getInfinity();

        BigInteger gCount = new BigInteger("0");
        boolean firstOneFound = false;
        StringBuilder path = new StringBuilder();
        ASTNode gTree = new ASTNode("g", "infinity");
        ASTNode pkTree = new ASTNode("pk", "infinity");
        ASTNode currentGNode = gTree;
        ASTNode currentGNode2 = gTree;
        logger.info("Looping from " + BIT_LENGTH + " to 0");
        for (int i = BIT_LENGTH; i >= 1; i--) {
            int bitIndex = i - 1;
            boolean isOne = gMultiplier.testBit(bitIndex);
            int bit = isOne ? 1 : 0;
            if (isOne) {
                if (firstOneFound) {
                    gCount = gCount.add(gCount);
                    logger.info("Doubling to create fs double node... (" + bit + ")");
                    current = current.twice();

                    String currentPath = currentGNode.getPath();
                    String newPath = currentPath + "/" + gCount.toString(36) + "g";
                    ASTNode gChild = new ASTNode("gDouble", gCount + "g", null, null, null, newPath);
                    currentGNode.addChild(gChild);
                    currentGNode2 = currentGNode;
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild);
                    gCount = gCount.add(new BigInteger("1"));
                    logger.info("Adding g to create fs node... (" + bit + ")");
                    current = current.add(G);
                    firstOneFound = true;
                    path.append("/" + gCount + "g/");
                    String newPath2 = currentGNode2.getPath() + "/" + gCount.toString(36) + "g";
                    ASTNode gChild2 = new ASTNode("gAdd", gCount + "g", null, null, null, newPath2);
                    currentGNode = gChild2;
                    currentGNode.addChild(gChild2);
                    createNode(256 - i, current, gCount, path.toString(), gChild2);
                } else {
                    gCount = gCount.add(new BigInteger("1"));
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
                    gCount = gCount.add(gCount);
                    current = current.twice();
                    path.append("/" + gCount.toString(36) + "g/");
                    ASTNode gChild = new ASTNode("gDouble", gCount + "g", null, null, null,
                            currentGNode.getPath() + "/" + gCount.toString(36) + "g");
                    currentGNode.addChild(gChild);
                    currentGNode = gChild;
                    createNode(256 - i, current, gCount, path.toString(), gChild);
                } else {
                    logger.info("Still at infinity: i = " + i);
                }
            }
        }
        return currentGNode;
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

    static void createNode(int step, ECPoint point, BigInteger gCount,
    		String path, ASTNode astNode){
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
            String dataDirectoryPath = GenerateChainFSStructure.getDataDirectoryPathSatoshi();
            String newNode = null;
            if (astNode != null) {
                String astNodePath = astNode.getPath();
				newNode = dataDirectoryPath + astNodePath + "/";
            }
            newNode = newNode.replaceAll("\\\\", "/");
            logger.info("Creating " + gCount + "g node at " + newNode);
            File newNodeFile = new File(newNode);
            boolean mkdirResult = newNodeFile.mkdirs();
            logger.info("Mkdir result: " + mkdirResult);
            // The following code creates the metadata files:
            String publicKeyInHexadecimalForm = NumberFormatUtils.concatXY(xModP, yModP, 32); // For secp256k1: 32 bytes
            if (BIDIRECTIONAL) {
            	// x and y must be stored separately
            	// for compliance with the curve
            	String astNodePath2 = astNode.getPath();
				String gLinkPointerX= "g_link_pointer_x_" +
						astNodePath2.replaceAll("\\\\", "/")
						.replaceAll("/", "_");
				if (PROCESS_PK_TREES) {
					PKTreeManager.process(xModP, null,
	            		gLinkPointerX, true, "pkx");
	            	String gLinkPointerY = astNodePath2
	            			.replaceAll("\\\\", "/")
	            			.replaceAll("/", "_");
					PKTreeManager.process(yModP, null,
	            		"g_link_pointer_y_" + gLinkPointerY, true, "pky");
				}
            }
			NLPCommandLogManager instance = NLPCommandLogManager.getInstance();
            String uncompressedBitcoinAddress = "04" + publicKeyInHexadecimalForm;
			File publicKeyFile2 = new File(newNodeFile,
            		SafeFilename.makeSafe("The Bitcoin Public Key for this node in uncompressed form is " +
            		uncompressedBitcoinAddress + ".", true));
            publicKeyFile2.createNewFile();
            String command = "create s-node mapping from an address value of " +
				uncompressedBitcoinAddress +
				" back to the s-node value of " + gCount;
            instance.appendCommand(command);
            String compressedBitcoinAddress =
            		BitcoinPublicKeyCompressor.compressPublicKey(uncompressedBitcoinAddress);
			File publicKeyFile3 = new File(newNodeFile,
            		SafeFilename.makeSafe("The Bitcoin Public Key for this node in compressed form is " +
            		compressedBitcoinAddress + ".", true));
            publicKeyFile3.createNewFile();
            instance.appendCommand("create s-node mapping from an address value of " +
            		compressedBitcoinAddress +
            		" back to the s-node value of " + gCount);
            File gValue = new File(newNodeFile,
            		SafeFilename.makeSafe("The G value and Private Key for this node is " +
            		gCount + ".", false));
            gValue.createNewFile();
            File gValueGematria = new File(newNodeFile,
            		SafeFilename.makeSafe("The G value and Private Key for this node in Gematria is " +
            		EncodingUtils.decodeGematria(String.valueOf(gCount)) + ".", false));
            gValueGematria.createNewFile();
            File file = new File(newNodeFile,
            		SafeFilename.makeSafe("The G value and Private Key for this node in Reverse Gematria is " +
            		EncodingUtils.decodeReverseGematria(String.valueOf(gCount)) + ".", false));
            file.createNewFile();
            File gValueHebrewGematria = new File(newNodeFile,
            		SafeFilename.makeSafe("The G value and Private Key for this node in Hebrew Gematria is " +
            		EncodingUtils.decodeHebrewGematria(String.valueOf(gCount)) + ".", false));
            gValueHebrewGematria.createNewFile();
            File publicKeyFile4 = new File(newNodeFile, "The concatenated value of x and y " +
            		"for this node is " + publicKeyInHexadecimalForm);
            publicKeyFile4.createNewFile();
            File xFile = new File(newNodeFile, "The value of x for this node is " + x);
            if (xFile.createNewFile()) {
            	instance.appendCommand("create s-node mapping from an x value of " +
            			x +
            			" back to the s-node value of " + gCount);
            }
            File xBeforeModPFile = new File(newNodeFile, "The value of x for this node before applying mod p is  " +
            		xBeforeMod);
            xBeforeModPFile.createNewFile();
            // secure secp256k1 formula: y^2 mod p = (x ^ 3 + 7) mod p
            BigInteger LHS = yBigInteger.pow(2).mod(p);
            BigInteger RHS = xBigInteger.pow(3).add(SEVEN).mod(p);
            File yFile = new File(newNodeFile, "The value of y for this node is " + y);
            yFile.createNewFile();
            File yBeforeModPFile = new File(newNodeFile, "The value of y for this node before applying mod p is "
            		+ yBeforeMod);
            yBeforeModPFile.createNewFile();
            File y2modpFile = new File(newNodeFile, "The value of the left hand size of the equation, y^2 mod p, is " +
            		LHS);
            y2modpFile.createNewFile();
            File x3p7modpFile = new File(newNodeFile,
            		"The value of the right hand size of the equation, x^3 + 7 mod p, is " +
            		RHS);
            x3p7modpFile.createNewFile();
            String segwit = UtilityToFindABitcoinAddressFromAPublicKey
				.getABitcoinAddressInTheSegWitFormatFromThePublicKeyInHexadecimalForm(
						compressedBitcoinAddress);
			file = new File(newNodeFile,
            		"The Bitcoin address of this node in Segwit format is " +
            		segwit);
            if (file.createNewFile()) {
            	instance.appendCommand("create s-node mapping from an address value of " +
            		segwit +
            		" back to the s-node value of " + gCount);
            }
            file = new File(newNodeFile,
            		"The Bitcoin address of this node in Legacy format is " +
            		UtilityToFindABitcoinAddressFromAPublicKey
            			.getABitcoinAddressInTheOldFormatFromThePublicKeyInHexadecimalForm(
            					compressedBitcoinAddress));
            file.createNewFile();
            String nestedSegwit = NestedSegwitAddress
            		.generateNestedSegwitAddress(
            		EncodingUtils.hexToBytes(compressedBitcoinAddress));
            file = new File(newNodeFile,
            		SafeFilename.makeSafe("The Bitcoin address of this node in the Nested Segwit Format is " +
            				nestedSegwit + ".", false));
            if (file.createNewFile()) {
                instance.appendCommand("create s-node mapping from an address value of " +
                		nestedSegwit +
                		" back to the s-node value of " + gCount);
            }
            String uncompressedLegacyAddress = LegacyBitcoinAddressGenerator
            		.generateUncompressedLegacyAddress(
            		uncompressedBitcoinAddress);
            file = new File(newNodeFile,
            		SafeFilename.makeSafe("The Bitcoin address of this node in the uncompressed Legacy Format is " +
            				uncompressedLegacyAddress + ".", false));
            if (file.createNewFile()) {
                instance.appendCommand("create s-node mapping from an address value of " +
                		uncompressedLegacyAddress +
                		" back to the s-node value of " + gCount);
            }
            String checksumAddress = EthereumAddress.toChecksumAddress(EthereumAddress.getEthereumAddress(
					EncodingUtils.hexToBytes(publicKeyInHexadecimalForm)));
			File eth = new File(newNodeFile,
            		"The Ethereum address of this node in Legacy format is " +
            		checksumAddress);
            if (eth.createNewFile()) {
            	instance.appendCommand("create s-node mapping from an ethereum address value of " +
            		checksumAddress +
            		" back to the s-node value of " + gCount);
            }
            String taproot = Bech32m.encode("bc", 0x01, EncodingUtils.hexToBytes(x));
            file = new File(newNodeFile,
            		SafeFilename.makeSafe("The Bitcoin address of this node in the Taproot format is " +
            				taproot + ".", false));
            if (file.createNewFile()) {
                instance.appendCommand("create s-node mapping from an address value of " +
                		taproot +
                		" back to the s-node value of " + gCount);
            }
            if (!LHS.equals(RHS)) {
                throw new IllegalStateException("FS node not on secure secp256k1 curve");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
