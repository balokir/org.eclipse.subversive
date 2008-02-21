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
 * @author Sergiy Logvin
 */
public class AffectedPathsContentProvider implements ITreeContentProvider {
	protected AffectedPathsNode root;
	
	public void initialize(SVNChangedPathData [] affectedPaths, Collection relatedPathPrefixes, Collection relatedParents, long currentRevision) {
		this.root = new AffectedPathsNode(SVNTeamUIPlugin.instance().getResource("AffectedPathsContentProvider.RootName"), null, '\0');
		if (affectedPaths == null) {
			return;
		}
		for (int i = 0; i < affectedPaths.length; i++) {
			SVNChangedPathData row = affectedPaths[i];
			this.processPath(row, relatedPathPrefixes, relatedParents);
		}
		this.doCompress(this.root);
		this.refreshStatuses(this.root);
	}

	public boolean hasChildren(Object element) {
		AffectedPathsNode node = (AffectedPathsNode)element;
		return node.hasChildren();
	}
	
	public Object[] getChildren(Object parentElement) {
		AffectedPathsNode parentNode = (AffectedPathsNode)parentElement;
		return parentNode.getChildren().toArray(new AffectedPathsNode[parentNode.getChildren().size()]);		
	}
	
	public void dispose() {
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object getParent(Object element) {
		return ((AffectedPathsNode)element).getParent();
	}
	
	public Object[] getElements(Object inputElement) {
		return new Object[] {this.root};
	}
	
	public AffectedPathsNode getRoot() {
		return this.root;
	}
	
	protected void processPath(SVNChangedPathData affectedPath, Collection relatedPathPrefixes, Collection relatedParents) {
		String fullResourcePath = affectedPath.getFullResourcePath();
		if (!this.isRelatedPath(fullResourcePath, relatedPathPrefixes) && !this.isRelatedParent(fullResourcePath, relatedParents)) {
			return;
		}
		StringTokenizer st = new StringTokenizer(fullResourcePath, "/");
		AffectedPathsNode node = null;
		AffectedPathsNode parent = this.root;
		// also handle changes for repository root
		AffectedPathsNode nextToLast = this.root;
		while (st.hasMoreTokens()) {
			String name = st.nextToken();
			node = parent.findByName(name);
			if (node == null) {
				node = new AffectedPathsNode(name, parent, name.equals(affectedPath.resourceName) ? affectedPath.action : '\0');
				parent.addChild(node);
			} 
			else if (name.equals(affectedPath.resourceName)) {
				node.setStatus(affectedPath.action);
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
	
	protected void doCompress(AffectedPathsNode node) {
		List<AffectedPathsNode> children = node.getChildren();
		if (node.getParent() == null) {
			for (Iterator it = children.iterator(); it.hasNext(); ) {
				this.doCompress((AffectedPathsNode)it.next());
			}
			return;
		}
		if (children.size() == 0) {
			return;
		}
		else if (children.size() == 1) {
			AffectedPathsNode nodeChild = children.get(0);
			if (node.getData() != null && node.getData().length > 0) {
				this.doCompress(nodeChild);
				return;
			}
			node.setName(node.getName() + "/" + nodeChild.getName());
			List<AffectedPathsNode> lowerChildren = nodeChild.getChildren();
			for (Iterator it = lowerChildren.iterator(); it.hasNext(); ) {
				((AffectedPathsNode)it.next()).setParent(node);
			}
			node.setChildren(lowerChildren);
			SVNChangedPathData [] data = nodeChild.getData();
			for (int i = 0; i < data.length; i++) {
				node.addData(data[i]);
			}
			this.doCompress(node);
			for (Iterator it = lowerChildren.iterator(); it.hasNext(); ) {
				this.doCompress((AffectedPathsNode)it.next());
			}
		}
		else {
			for (Iterator it = children.iterator(); it.hasNext(); ) {
				this.doCompress((AffectedPathsNode)it.next());
			}
		}
		
	}
	
	protected void refreshStatuses(AffectedPathsNode node) {
		List children = node.getChildren();
		if (children.size() == 0) {
			return;
		}
		SVNChangedPathData [] affectedPathData = node.getData();
		if (affectedPathData != null && affectedPathData.length > 0) {
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				AffectedPathsNode currentNode = (AffectedPathsNode)iter.next();
				for (int i = 0; i < affectedPathData.length; i++) {
					SVNChangedPathData affectedPath = affectedPathData[i];
					if (currentNode.getName().equals(affectedPath.resourceName)) {
						currentNode.setStatus(affectedPath.action);
						break;
					}
				}
				this.refreshStatuses(currentNode);
			}
		}
		else {
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				this.refreshStatuses((AffectedPathsNode)iter.next());
			}
		}
	}
	
}

