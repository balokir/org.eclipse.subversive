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

package org.eclipse.team.svn.ui.history;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.data.AffectedPathsNode;
import org.eclipse.team.svn.ui.history.data.SVNChangedPathData;

/**
 * Content provider for the affected paths tree
 *
 * @author Sergiy Logvin
 */
public class AffectedPathsContentProvider implements ITreeContentProvider {
	protected AffectedPathsNode root;
	
	public void initialize(SVNChangedPathData [] affectedPaths, Collection<String> relatedPathPrefixes, Collection<String> relatedParents, long currentRevision) {
		this.root = new AffectedPathsNode(SVNTeamUIPlugin.instance().getResource("AffectedPathsContentProvider.RootName"), null, '\0');
		if (affectedPaths == null) {
			return;
		}
		
		for (int i = 0; i < affectedPaths.length; i++) {
			SVNChangedPathData row = affectedPaths[i];
			this.processPath(row, relatedPathPrefixes, relatedParents);
		}
		
		for (AffectedPathsNode node : this.root.getChildren()) {
			this.doCompress(node);
		}
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
	
	protected void processPath(SVNChangedPathData affectedPath, Collection<String> relatedPathPrefixes, Collection<String> relatedParents) {
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
			node = this.findByName(parent, name);
			if (node == null) {
				node = new AffectedPathsNode(name, parent, name.equals(affectedPath.resourceName) ? affectedPath.action : '\0');
				parent.addChild(node);
			} 
			else if (!st.hasMoreTokens()) {
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
	
	protected AffectedPathsNode findByName(AffectedPathsNode parent, String name) {
		for (AffectedPathsNode node : parent.getChildren()) {
			if (node.getName().equals(name)) {
				return node;
			}
		}
		return null;
	}
	
	protected boolean isRelatedParent(String fullPath, Collection<String> relatedParents) {
		if (relatedParents == null || relatedParents.contains(fullPath)) {
			return true;
		}
		return false;
	}

	protected boolean isRelatedPath(String fullPath, Collection<String> relatedPathPrefixes) {
		if (relatedPathPrefixes == null) {
			return true;
		}
		for (Iterator<String> it = relatedPathPrefixes.iterator(); it.hasNext(); ) {
			String prefix = it.next();
			if (fullPath.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
	
	protected void doCompress(AffectedPathsNode node) {
		List<AffectedPathsNode> children = node.getChildren();
		if (children.size() > 1) {
			for (AffectedPathsNode tNode : children) {
				this.doCompress(tNode);
			}
		}
		else if (children.size() == 1) {
			AffectedPathsNode child = children.get(0);
			
			if (node.getData().length > 0) {
				this.doCompress(child);
			}
			else {
				node.addCompressedNameSegment(child.getName());
				
				List<AffectedPathsNode> lowerChildren = child.getChildren();
				for (AffectedPathsNode tNode : lowerChildren) {
					tNode.setParent(node);
				}
				node.setChildren(lowerChildren);
				
				for (SVNChangedPathData data : child.getData()) {
					node.addData(data);
				}
				
				this.doCompress(node);
			}
		}
	}
	
}

