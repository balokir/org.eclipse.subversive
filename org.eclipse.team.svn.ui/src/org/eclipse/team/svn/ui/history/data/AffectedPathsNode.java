/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import java.util.Iterator;
import java.util.List;


/**
 * Node for the tree of the affected paths 
 *
 * @author Sergiy Logvin
 */
public class AffectedPathsNode {
	protected String name;
	protected List<AffectedPathsNode> children = new ArrayList<AffectedPathsNode>(); 
	protected AffectedPathsNode parent;
	protected ArrayList<SVNChangedPathData> data = null;
	protected char status;
	
	public AffectedPathsNode(String name, AffectedPathsNode parent, char status) {
		this.name = name;
		this.parent = parent;
		this.data = new ArrayList<SVNChangedPathData>();
		this.status = status;
	}
	
	public String getName() {
		return this.name;
	}
	
	public AffectedPathsNode findByName(String name) {
		for (Iterator<AffectedPathsNode> it = this.children.iterator(); it.hasNext(); ) {
			AffectedPathsNode node = it.next();
			if (node.getName().equals(name)) {
				return node;
			}
		}
		
		return null;
	}
	
	public String toString() {	
		return this.name;
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
	
	public void addChildren(List<AffectedPathsNode> children) {
		for (Iterator<AffectedPathsNode> it = children.iterator(); it.hasNext(); ) {
			this.addChild(it.next());
		}		
	}
	
	public boolean removeChild(AffectedPathsNode child) {
		if (this.children.contains(child)) {
			return this.children.remove(child);
		}
		return false;
	}
	
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof AffectedPathsNode)) {
			return false;
		}
		AffectedPathsNode node2 = (AffectedPathsNode)arg0;
		if (this.parent == null) {
			return node2.parent == null;
		}		
		if (this.parent.equals(node2.parent) &&
				this.getName().equals(node2.getName())) {
			return true;
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
		if (this.data != null) {
			result.addAll(this.data);
		}
		for (Iterator<AffectedPathsNode> it = this.children.iterator(); it.hasNext(); ) {
			it.next().getPathDataImpl(result);
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
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setParent(AffectedPathsNode parent) {
		this.parent = parent;
	}

	public void setChildren(List<AffectedPathsNode> children) {
		this.children = children;
	}
	
	public String getFullPath() {
		String path = "";
		if (this.parent != null) {
			path = this.parent.getFullPath() + "/" + this.name;
		}
		return path;
	}

	public char getStatus() {
		return this.status;
	}
	
	public void setStatus(char status) {
		this.status = status;
	}
	
}
