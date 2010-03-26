/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.revision.graph.PathRevision.RevisionNodeAction;
import org.eclipse.team.svn.revision.graph.graphic.RevisionNode;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Base class for revision graph actions
 * 
 * @author Igor Burilo
 */
public abstract class BaseRevisionGraphAction extends SelectionAction {

	public BaseRevisionGraphAction(IWorkbenchPart part) {
		super(part);
	}
		
	/*
	 * Filter for revision edit parts
	 */
	protected static abstract class AbstractRevisionEditPartFilter {		
		public abstract boolean accept(RevisionEditPart editPart);	
	}
	
	protected final static AbstractRevisionEditPartFilter NOT_DELETED_ACTION_FILTER = new AbstractRevisionEditPartFilter() {
		@Override
		public boolean accept(RevisionEditPart editPart) {			
			RevisionNode node = editPart.getCastedModel();
			return node.getAction() != RevisionNodeAction.DELETE;
		}		
	};
	
	protected final static AbstractRevisionEditPartFilter EXIST_IN_PREVIOUS_FILTER = new AbstractRevisionEditPartFilter() {
		@Override
		public boolean accept(RevisionEditPart editPart) {			
			RevisionNode node = editPart.getCastedModel();
			RevisionNodeAction action = node.getAction();
			if (action == RevisionNodeAction.MODIFY || action == RevisionNodeAction.NONE) {
				return true;
			}		
			return false;
		}		
	};
	
	protected final static AbstractRevisionEditPartFilter NULL_FILTER = new AbstractRevisionEditPartFilter() {
		@Override
		public boolean accept(RevisionEditPart editPart) {	
			return true;
		}
	};
	
	protected void runOperation(final IActionOperation op) {
		if (op == null) {
			return;
		}		
		UIMonitorUtility.doTaskScheduledDefault(this.getWorkbenchPart(), op);
	}
	
	protected boolean isEnable(AbstractRevisionEditPartFilter filter, int editPartsCount) {
		return this.getSelectedEditParts().length == editPartsCount && this.getSelectedEditParts(filter).length == editPartsCount;
	}
	
	protected RevisionEditPart[] getSelectedEditParts(AbstractRevisionEditPartFilter filter) {
		List<RevisionEditPart> res = new ArrayList<RevisionEditPart>();
		List<?> selected = this.getSelectedObjects();
		if (!selected.isEmpty()) {
			Iterator<?> iter = selected.iterator(); 
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof RevisionEditPart) {
					RevisionEditPart editPart = (RevisionEditPart) obj;
					if (filter.accept(editPart)) {
						res.add(editPart);
					}					
				}
			}						
		} 	
		return res.toArray(new RevisionEditPart[0]);
	}
	
	protected RevisionEditPart[] getSelectedEditParts() {
		return this.getSelectedEditParts(NULL_FILTER);
	}
	
	protected RevisionEditPart getSelectedEditPart() {
		List<?> selected = this.getSelectedObjects();
		if (!selected.isEmpty()) {
			Object obj = selected.get(0);
			if (obj instanceof RevisionEditPart) {
				return (RevisionEditPart) obj;
			}
		}
		return null;
	}
	
	protected IRepositoryLocation getRepositoryLocation(RevisionEditPart editPart) {
		return editPart.getRevisionRootNode().getRepositoryResource().getRepositoryLocation();
	}
	
	public static IRepositoryResource[] convertToResources(RevisionEditPart[] editParts) {		
		IRepositoryResource[] result = new IRepositoryResource[editParts.length];
		for (int i = 0; i < editParts.length; i ++) {
			result[i] = BaseRevisionGraphAction.convertToResource(editParts[i]);
		}
		return result;
	}
	
	public static IRepositoryResource convertToResource(RevisionEditPart editPart) {
		RevisionNode revisionNode = editPart.getCastedModel();
		RevisionRootNode rootNode = editPart.getRevisionRootNode();	
		
		boolean isFolder = !(rootNode.getRepositoryResource() instanceof IRepositoryFile);					
		String url = rootNode.getRevisionFullPath(revisionNode);		
		IRepositoryResource resource = SVNUtility.asRepositoryResource(url, isFolder);
		SVNRevision svnRev = SVNRevision.fromNumber(revisionNode.getRevision());
		resource.setSelectedRevision(svnRev);
		resource.setPegRevision(svnRev);
		
		return resource;
	}
}
