/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elena Matokhina - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.history;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * Content provider for the affected paths tree
 *
 * @author Elena Matokhina
 */
public class AffectedPathsContentProvider implements ITreeContentProvider {
	protected AffectedPathNode root;
	
	public void initialize(String[][] affectedPaths, Collection relatedPathPrefixes, Collection relatedParents, long currentRevision) {
		this.root = new AffectedPathNode(SVNTeamUIPlugin.instance().getResource("AffectedPathsContentProvider.RootName"), null, null);
		if (affectedPaths == null) {
			return;
		}
		for (int i = 0; i < affectedPaths.length; i++) {
			String[] row = affectedPaths[i];
			this.processPath(row, relatedPathPrefixes, relatedParents);
		}
		this.doCompress(this.root);
		this.refreshStatuses(this.root);
	}

	public boolean hasChildren(Object element) {
		AffectedPathNode node = (AffectedPathNode)element;
		return node.hasChildren();
	}
	
	public Object[] getChildren(Object parentElement) {
		AffectedPathNode parentNode = (AffectedPathNode)parentElement;
		return parentNode.getChildren().toArray(new AffectedPathNode[parentNode.getChildren().size()]);		
	}
	
	public void dispose() {
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object getParent(Object element) {
		return ((AffectedPathNode)element).getParent();
	}
	
	public Object[] getElements(Object inputElement) {
		return new Object[] {this.root};
	}
	
	public AffectedPathNode getRoot() {
		return this.root;
	}
	
	protected void processPath(String []affectedPath, Collection relatedPathPrefixes, Collection relatedParents) {
		String fullPath = affectedPath[2] + (affectedPath[2].length() > 0 ? "/" : "") + affectedPath[1];
		if (!this.isRelatedPath(fullPath, relatedPathPrefixes) && !this.isRelatedParent(fullPath, relatedParents)) {
			return;
		}
		StringTokenizer st = new StringTokenizer(fullPath, "/");
		AffectedPathNode node = null;
		AffectedPathNode parent = this.root;
		// also handle changes for repository root
		AffectedPathNode nextToLast = this.root;
		while (st.hasMoreTokens()) {
			String name = st.nextToken();
			node = parent.findByName(name);
			if (node == null) {
				node = new AffectedPathNode(name, parent, name.equals(affectedPath[1]) ? affectedPath[0] : null);
				parent.addChild(node);
			} 
			else if (name.equals(affectedPath[1])) {
				node.setStatus(affectedPath[0]);
			}
			nextToLast = parent;
			parent = node;
		}
		nextToLast.addData(affectedPath);
		if (node != null && (node.getChildren() == null || node.getChildren().size() == 0) && (node.getPathData() == null || node.getPathData().length == 0)) {
			nextToLast.removeChild(node);
		}
	}
	
	protected boolean isRelatedParent(String fullPath, Collection relatedParents) {
		if (relatedParents == null || relatedParents.contains(fullPath)) {
			return true;
		}
		return false;
	}

	protected boolean isRelatedPath(String fullPath, Collection relatedPathPrefixes) {
		if (relatedPathPrefixes == null) {
			return true;
		}
		for (Iterator it = relatedPathPrefixes.iterator(); it.hasNext(); ) {
			String prefix = (String)it.next();
			if (fullPath.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
	
	protected void doCompress(AffectedPathNode node) {
		List children = node.getChildren();
		if (node.getParent() == null) {
			for (Iterator it = children.iterator(); it.hasNext(); ) {
				this.doCompress((AffectedPathNode)it.next());
			}
			return;
		}
		if (children.size() == 0) {
			return;
		}
		else if (children.size() == 1) {
			AffectedPathNode nodeChild = (AffectedPathNode)children.get(0);
			if (node.getData() != null && node.getData().length > 0) {
				this.doCompress(nodeChild);
				return;
			}
			node.setName(node.getName() + "/" + nodeChild.getName());
			List lowerChildren = nodeChild.getChildren();
			for (Iterator it = lowerChildren.iterator(); it.hasNext(); ) {
				((AffectedPathNode)it.next()).setParent(node);
			}
			node.setChildren(lowerChildren);
			String [][]data = nodeChild.getData();
			for (int i = 0; i < data.length; i++) {
				node.addData(data[i]);
			}
			this.doCompress(node);
			for (Iterator it = lowerChildren.iterator(); it.hasNext(); ) {
				this.doCompress((AffectedPathNode)it.next());
			}
		}
		else {
			for (Iterator it = children.iterator(); it.hasNext(); ) {
				this.doCompress((AffectedPathNode)it.next());
			}
		}
		
	}
	
	protected void refreshStatuses(AffectedPathNode node) {
		List children = node.getChildren();
		if (children.size() == 0) {
			return;
		}
		String [][]affectedPathData = node.getData();
		if (affectedPathData != null && affectedPathData.length > 0) {
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				AffectedPathNode currentNode = (AffectedPathNode)iter.next();
				for (int i = 0; i < affectedPathData.length; i++) {
					String []affectedPath = affectedPathData[i];
					if (currentNode.getName().equals(affectedPath[1])) {
						currentNode.setStatus(affectedPath[0]);
						break;
					}
				}
				this.refreshStatuses(currentNode);
			}
		}
		else {
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				this.refreshStatuses((AffectedPathNode)iter.next());
			}
		}
	}
	
}

