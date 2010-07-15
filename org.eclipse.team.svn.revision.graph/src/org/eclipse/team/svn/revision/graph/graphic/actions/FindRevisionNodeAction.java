/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.team.svn.revision.graph.graphic.ChangesNotifier;
import org.eclipse.team.svn.revision.graph.graphic.FindRevisionNodeDialog;
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

/**
 * @author Igor Burilo
 */
public class FindRevisionNodeAction extends Action {

	protected IEditorPart editor;
	
	protected static FindRevisionNodeDialogStub findDialogStub;
	
	/**
	 * Track active parts and retarget the find dialog accordingly	
	 */
	protected static class FindRevisionNodeDialogStub implements IPartListener2, DisposeListener, PropertyChangeListener {
				
		protected RevisionGraphEditor graphEditor;		
		protected FindRevisionNodeDialog findDialog;
		protected IWorkbenchWindow workbenchWindow;
		
		public FindRevisionNodeDialogStub(IWorkbenchPartSite site) {
			this.findDialog = new FindRevisionNodeDialog(site.getShell());
			this.findDialog.create();			
			this.findDialog.getShell().addDisposeListener(this);
			
			this.workbenchWindow = site.getWorkbenchWindow();
			IPartService service = this.workbenchWindow.getPartService();			
			service.addPartListener(this);
			
			this.partActivated(service.getActivePart());
		}
		
		public FindRevisionNodeDialog getDialog() {
			return this.findDialog;
		}
		
		protected void partActivated(IWorkbenchPart part) {		
			RevisionGraphEditor newGraphEditor = FindRevisionNodeAction.canSearch(part) ? (RevisionGraphEditor) part : null;
			if (this.graphEditor != newGraphEditor) {
				RevisionGraphEditor previousEditor = this.graphEditor;							
				this.graphEditor = newGraphEditor;
												
				//update listeners
				if (previousEditor != null) {
					((RevisionRootNode) previousEditor.getModel()).removePropertyChangeListener(this);
				}
				if (this.graphEditor != null) {
					((RevisionRootNode) this.graphEditor.getModel()).addPropertyChangeListener(this);
				}
				
				if (this.findDialog != null) {					
					this.findDialog.updateTarget(newGraphEditor);					
				}								
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partActivated(IWorkbenchPartReference partRef) {
			this.partActivated(partRef.getPart(true));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partClosed(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(true);
			RevisionGraphEditor graphEditor = FindRevisionNodeAction.canSearch(part) ? (RevisionGraphEditor) part : null;
			if (this.graphEditor == graphEditor) {
				this.partActivated((IWorkbenchPart) null);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent e) {			
			FindRevisionNodeAction.findDialogStub = null;

			if (this.workbenchWindow != null) {
				this.workbenchWindow.getPartService().removePartListener(this);
				this.workbenchWindow = null;
			}						
						
			if (this.graphEditor != null) {
				((RevisionRootNode) this.graphEditor.getModel()).removePropertyChangeListener(this);
			}
			
			this.graphEditor = null;
			this.findDialog = null;
		}

		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if (ChangesNotifier.FILTER_NODES_PROPERTY.equals(evt.getPropertyName()) ||
				ChangesNotifier.EXPAND_COLLAPSE_NODES_PROPERTY.equals(evt.getPropertyName())) {
				if (this.findDialog != null) {
					this.findDialog.changeGraphModel();	
				}								
			} else if (ChangesNotifier.REFRESH_NODES_PROPERTY.equals(evt.getPropertyName())) {				
				RevisionRootNode oldRoot = (RevisionRootNode) evt.getOldValue();
				oldRoot.removePropertyChangeListener(this);
				
				RevisionRootNode newRoot = (RevisionRootNode) evt.getNewValue();
				newRoot.addPropertyChangeListener(this);
				
				if (this.findDialog != null) {					
					this.findDialog.changeGraphModel();	
				}	
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partBroughtToTop(IWorkbenchPartReference partRef) {		
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partDeactivated(IWorkbenchPartReference partRef) {		
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partHidden(IWorkbenchPartReference partRef) {		
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partInputChanged(IWorkbenchPartReference partRef) {		
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partOpened(IWorkbenchPartReference partRef) {		
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partVisible(IWorkbenchPartReference partRef) {		
		}
	}
	
	public FindRevisionNodeAction(IWorkbenchPart part) {				
		this.init();
	}
	
	protected void init() {
		setId(ActionFactory.FIND.getId());		
		//TODO redefine action name from 'Find/Replace' to 'Find'
	}
	
	public void setActiveEditor(IEditorPart editor) {
		this.editor = editor;
	}
	
	@Override
	public void run() {	
		if (FindRevisionNodeAction.canSearch(this.editor)) {
			if (findDialogStub == null) { 
				findDialogStub = new FindRevisionNodeDialogStub(this.editor.getSite());				
			}
			
			FindRevisionNodeDialog dlg = findDialogStub.getDialog();
			dlg.updateTarget((RevisionGraphEditor) this.editor);
			dlg.open();							
		}
	}
	
	public static boolean canSearch(IWorkbenchPart part) {
		if (part instanceof RevisionGraphEditor) {
			RevisionGraphEditor editor = (RevisionGraphEditor) part; 
			return editor.getModel() instanceof RevisionRootNode;
		}
		return false;
	}
}
