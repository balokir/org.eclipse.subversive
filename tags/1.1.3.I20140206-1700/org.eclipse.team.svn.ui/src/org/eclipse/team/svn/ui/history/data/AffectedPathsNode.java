/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history.data;

import java.util.ArrayList;
import java.util.List;


/**
 * Node for the tree of the affected paths 
 *
 * @author Sergiy Logvin
 */
public class AffectedPathsNode {
	protected String name;
	protected String compressedName;
	protected List<AffectedPathsNode> children; 
	protected AffectedPathsNode parent;
	protected ArrayList<SVNChangedPathData> data;
	protected char status;
	
	public AffectedPathsNode(String name, AffectedPathsNode parent, char status) {
		this.name = this.compressedName = name;
		this.parent = parent;
		this.data = new ArrayList<SVNChangedPathData>();
		this.children = new ArrayList<AffectedPathsNode>();
		this.status = status;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getCompressedName() {
		return this.compressedName;
	}
	
	public void addCompressedNameSegment(String compressedName) {
		this.compressedName += "/" + compressedName; //$NON-NLS-1$
	}
	
	public String toString() {
		return this.compressedName;
	}
	
	public boolean hasChildren() {
		return this.children.size() > 0;
	}
	
	public AffectedPathsNode getParent() {
		return this.parent;
	}
	
	public List<AffectedPathsNode> getChildren() {		
		return this.children;
	}
	
	public boolean addChild(AffectedPathsNode child) {		
		if (this.children.contains(child)) {
			return false;
		}
		return this.children.add(child);
	}
	
	public boolean removeChild(AffectedPathsNode child) {
		if (this.children.contains(child)) {
			return this.children.remove(child);
		}
		return false;
	}
	
	public boolean equals(Object arg0) {
		if (arg0 instanceof AffectedPathsNode) {
			AffectedPathsNode node2 = (AffectedPathsNode)arg0;
			if (this.parent == null) {
				return node2.parent == null;
			}		
			if (this.parent.equals(node2.parent) && this.name.equals(node2.name)) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		int h = 17;
        h += (31 * (this.parent != null ? this.parent.hashCode() : 0));
        h += (31 * this.name.hashCode());
        return h;		
	}

	public SVNChangedPathData [] getData() {
		return this.data.toArray(new SVNChangedPathData[this.data.size()]);
	}
	
	protected List<SVNChangedPathData> getPathDataImpl(List<SVNChangedPathData> result) {
		result.addAll(this.data);
		for (AffectedPathsNode node : this.children) {
			node.getPathDataImpl(result);
		}
		return result;
	}

	public void addData(SVNChangedPathData data) {
		if (!this.data.contains(data)) {
			this.data.add(data);
		}
	}

	public SVNChangedPathData [] getPathData() {
    	List<SVNChangedPathData> tmp = this.getPathDataImpl(new ArrayList<SVNChangedPathData>());
    	return tmp.toArray(new SVNChangedPathData[tmp.size()]);
	}
	
	public void setParent(AffectedPathsNode parent) {
		this.parent = parent;
	}

	public void setChildren(List<AffectedPathsNode> children) {
		if (children != null) {
			this.children = children;
		}
		else {
			this.children.clear();
		}
	}
	
	public String getFullPath() {
		return this.parent != null ? this.parent.getFullPath() + "/" + this.compressedName : ""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public char getStatus() {
		return this.status;
	}
	
	public void setStatus(char status) {
		this.status = status;
	}
	
}
