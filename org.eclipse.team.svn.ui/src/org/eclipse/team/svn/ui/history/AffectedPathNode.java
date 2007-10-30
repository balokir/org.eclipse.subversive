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

package org.eclipse.team.svn.ui.history;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Node for the tree of the affected paths 
 *
 * @author Sergiy Logvin
 */
public class AffectedPathNode {
	
	protected String name;
	protected List children = new ArrayList(); 
	protected AffectedPathNode parent;
	protected List data = null;
	protected String status;
	
	public AffectedPathNode(String name, AffectedPathNode parent, String status) {
		this.name = name;
		this.parent = parent;
		this.data = new ArrayList();
		this.status = status;
	}
	
	public String getName() {
		return this.name;
	}
	
	public AffectedPathNode findByName(String name) {
		for (Iterator it = this.children.iterator(); it.hasNext(); ) {
			AffectedPathNode node = (AffectedPathNode)it.next();
			if ((node).getName().equals(name)) {
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
	
	public AffectedPathNode getParent() {
		return this.parent;
	}
	
	public List getChildren() {		
		return this.children;
	}
	
	public boolean addChild(AffectedPathNode child) {		
		if (this.children.contains(child)) {
			return false;
		}
		return this.children.add(child);
	}
	
	public void addChildren(List children) {
		for (Iterator it = children.iterator(); it.hasNext(); ) {
			this.addChild((AffectedPathNode)it.next());
		}		
	}
	
	public boolean removeChild(AffectedPathNode child) {
		if (this.children.contains(child)) {
			return this.children.remove(child);
		}
		return false;
	}
	
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof AffectedPathNode)) {
			return false;
		}
		AffectedPathNode node2 = (AffectedPathNode)arg0;
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

	public String [][]getData() {
		return (String [][])this.data.toArray(new String[this.data.size()][]);
	}
	
	protected List getPathDataImpl(List result) {
		if (this.data != null) {
			result.addAll(this.data);
		}
		for (Iterator it = this.children.iterator(); it.hasNext(); ) {
			((AffectedPathNode)it.next()).getPathDataImpl(result);
		}

		return result;
	}

	public void addData(String []data) {
		if (!this.data.contains(data)) {
			this.data.add(data);
		}
	}

	public String [][]getPathData() {
    	List tmp = this.getPathDataImpl(new ArrayList());
    	return (String [][])tmp.toArray(new String[tmp.size()][]);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setParent(AffectedPathNode parent) {
		this.parent = parent;
	}

	public void setChildren(List children) {
		this.children = children;
	}
	
	public String getFullPath() {
		String path = "";
		if (this.parent != null) {
			path = this.parent.getFullPath() + "/" + this.name;
		}
		return path;
	}

	public String getStatus() {
		return this.status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
}
