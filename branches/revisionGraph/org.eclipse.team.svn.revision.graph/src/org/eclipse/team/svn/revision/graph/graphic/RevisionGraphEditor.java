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
package org.eclipse.team.svn.revision.graph.graphic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.team.svn.revision.graph.cache.TimeMeasure;
import org.eclipse.team.svn.revision.graph.graphic.actions.AddRevisionLinksAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.ComparePropertiesAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.CompareWithEachOtherAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.CompareWithHeadAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.CompareWithPreviousAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.CreateBranchTagAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.CreatePatchAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.ExportAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.ExtractAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.OpenAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.RevisionGraphContextMenuManager;
import org.eclipse.team.svn.revision.graph.graphic.actions.ShowAnnotationAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.ShowHistoryAction;
import org.eclipse.team.svn.revision.graph.graphic.actions.ShowPropertiesAction;
import org.eclipse.team.svn.revision.graph.graphic.editpart.GraphEditPartFactory;
import org.eclipse.team.svn.ui.action.remote.BranchTagAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * TODO fake editor to quick startup. For real work, rework it.
 * 
 * @author Igor Burilo
 */
public class RevisionGraphEditor extends GraphicalEditor {

	protected RevisionGraphOutlinePage outlinePage; 
	
	public RevisionGraphEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);			
	}		
	
	/**
	 * Set up the editor's inital content (after creation).
	 */
	@Override
	protected void initializeGraphicalViewer() {
		/*
		 * This can be a time consuming operation, e.g.
		 * for 43000 revisions it takes about 62 sec.
		 * But it seems we can't do anything with it, as
		 * this operation has to be executed in UI thread. 
		 * 
		 *  See GEF bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=255534
		 */
		GraphicalViewer viewer = getGraphicalViewer();				
		TimeMeasure setContentsMeasure = new TimeMeasure("Set contents");
		//set the contents of this editor
		viewer.setContents(getModel()); 
		setContentsMeasure.end();		
						
		// listen for dropped parts
		//viewer.addDropTargetListener(createTransferDropTargetListener());		
	}
	
	public RevisionRootNode getModel() {
		RevisionGraphEditorInput editorInput = (RevisionGraphEditorInput) this.getEditorInput();
		return editorInput.getModel();
	}
	
	/**
	 * Configure the graphical viewer before it receives contents.
	 * <p>This is the place to choose an appropriate RootEditPart and EditPartFactory
	 * for your editor. The RootEditPart determines the behavior of the editor's "work-area".
	 * For example, GEF includes zoomable and scrollable root edit parts. The EditPartFactory
	 * maps model elements to edit parts (controllers).</p>
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	protected void configureGraphicalViewer() {	
		super.configureGraphicalViewer();
		
		GraphicalViewer viewer = getGraphicalViewer();
		ScalableRootEditPart root = new ScalableRootEditPart();
		
		//zoom
		//TODO probably we need to remember zoom between sessions 
		List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);
		//it seems we don't need it
//		IAction zoomIn = new ZoomInAction(root.getZoomManager());
//		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
//		getActionRegistry().registerAction(zoomIn);
//		getActionRegistry().registerAction(zoomOut);
//		getSite().getKeyBindingService().registerAction(zoomIn);
//		getSite().getKeyBindingService().registerAction(zoomOut);
		
		root.getZoomManager().setZoom(1);		
		// Scroll-wheel Zoom
		getGraphicalViewer().setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1), 
				MouseWheelZoomHandler.SINGLETON);
		
		
		viewer.setRootEditPart(root);
		viewer.setEditPartFactory(new GraphEditPartFactory());
		
		//TODO remember between sessions
		boolean isSimpleMode = true;
		this.getModel().init(isSimpleMode);
		
		//context menu
		RevisionGraphContextMenuManager menuManager = new RevisionGraphContextMenuManager(viewer, this, getActionRegistry());
		viewer.setContextMenu(menuManager);
		getSite().registerContextMenu(menuManager, viewer);
	}

	protected RevisionGraphOutlinePage getOutlinePage() {
		if(this.outlinePage == null && this.getGraphicalViewer() != null) {
			this.outlinePage = new RevisionGraphOutlinePage(this.getGraphicalViewer());			
		}
		return this.outlinePage;
	}
	
	public Object getAdapter(Class adapter) {
		if(adapter == GraphicalViewer.class || adapter == EditPartViewer.class) {
			return getGraphicalViewer();
		} else if(adapter == ZoomManager.class) {
				return ((ScalableRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
		} 
		else if (adapter == IContentOutlinePage.class) {
			return new RevisionGraphOutlinePage(this.getGraphicalViewer());
		}
		return super.getAdapter(adapter);
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		
	}
	
	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	protected void createActions() {
		//register other actions
		
		ActionRegistry registry = getActionRegistry();
		IAction action;
				
		List<String> selectedActions = getSelectionActions();
		
		action = new ShowHistoryAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new CompareWithEachOtherAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new CompareWithHeadAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new CompareWithPreviousAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new ShowPropertiesAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new ComparePropertiesAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new ExportAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new CreatePatchAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new ExtractAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new CreateBranchTagAction(this, BranchTagAction.BRANCH_ACTION);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new CreateBranchTagAction(this, BranchTagAction.TAG_ACTION);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new AddRevisionLinksAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new OpenAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
		
		action = new ShowAnnotationAction(this);
		registry.registerAction(action);
		selectedActions.add(action.getId());
	}

	public GraphicalViewer getViewer() {
		return getGraphicalViewer();
	}
		
	@Override
	public void dispose() {
		//clear resources
		RevisionGraphEditorInput editorInput = (RevisionGraphEditorInput) this.getEditorInput();
		if (editorInput != null) {
			editorInput.model = null;	
		}
		
		super.dispose();
	}

}

