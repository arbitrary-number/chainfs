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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;

/**
 * The primary data structure used in chainfs
 */
public class ASTNode {

    private String type; // Node type, e.g., "IfStatement", "VariableDeclaration"

    private String value; // Optional value, e.g., variable name, literal, operator

    private List<ASTNode> children; // Child nodes

    private ASTNode parent;

    private ASTNode root;

    private BigInteger gCount;

    private ECPoint ecPoint;

    public ECPoint getEcPoint() {
		return ecPoint;
	}

	public void setEcPoint(ECPoint ecPoint) {
		this.ecPoint = ecPoint;
	}

	public BigInteger getgCount() {
		return gCount;
	}

	public void setgCount(BigInteger gCount) {
		this.gCount = gCount;
	}

	private List<ASTNode> directLine = new ArrayList<ASTNode>(); // line to/from root

    private String path = new String(); // path to/drom root

    public ASTNode(String type) {
        this(type, null);
    }

    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public ASTNode(String type, String value, ASTNode parent, ASTNode root, List<ASTNode> directLine, String path) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
        this.parent = parent;
        this.root = root;
        this.directLine = directLine;
        this.path = path;
    }

    public String getPath(){
        return path;
    }

    public void addChild(ASTNode child){
        children.add(child);
    }

    public List<ASTNode> getChildren(){
        return children;
    }

    public String getType(){
        return type;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }

    public void printTree(String indent){
        System.out.println(indent + type + (value != null ? ": " + value : ""));
        for (ASTNode child : children) {
            child.printTree(indent + "  ");
        }
    }

    public void printTree(){
        printTree("");
    }
}
