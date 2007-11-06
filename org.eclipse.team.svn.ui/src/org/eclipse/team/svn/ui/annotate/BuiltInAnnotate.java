/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.annotate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.internal.text.revisions.RevisionSelectionProvider;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.team.svn.core.client.ISVNClientWrapper;
import org.eclipse.team.svn.core.client.LogEntry;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.operation.remote.GetResourceAnnotationOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Annotation based on Eclipse Platform support.
 * 
 * @author Alexander Gurov
 */
public class BuiltInAnnotate {
	protected AbstractDecoratedTextEditor textEditor;
	protected SVNHistoryPage historyPage;
	
	public void open(IWorkbenchPage page, IRepositoryResource remote, IFile resource) {
		GetResourceAnnotationOperation annotateOp = new GetResourceAnnotationOperation(remote);
		IActionOperation showOp = this.prepareBuiltInAnnotate(annotateOp, page, resource);
		CompositeOperation op = new CompositeOperation(showOp.getId());
		op.add(annotateOp);
		op.add(showOp, new IActionOperation[] {annotateOp});
		UIMonitorUtility.doTaskScheduledDefault(page.getActivePart(), op);
	}

	protected IActionOperation prepareBuiltInAnnotate(final GetResourceAnnotationOperation annotateOp, final IWorkbenchPage page, final IFile resource) {
		CompositeOperation op = new CompositeOperation("Operation.BuiltInShowAnnotation");
		final RevisionInformation info = new RevisionInformation();
		IActionOperation prepareRevisions = new AbstractNonLockingOperation("Operation.PrepareRevisions") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				Map revisions = new HashMap();
				String [][]lines = annotateOp.getAnnotatedLines();
				if (lines == null || lines.length == 0) {
					return;
				}
				RGB palette = SVNTeamPreferences.getAnnotateRGB(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.ANNOTATE_RGB_BASE_NAME);
				boolean useOneRGB = SVNTeamPreferences.getAnnotateBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_NAME);
				for (int i = 0; i < lines.length; i++) {
					BuiltInAnnotateRevision revision = (BuiltInAnnotateRevision)revisions.get(lines[i][0]);
					if (revision == null) {
						revisions.put(lines[i][0], revision = new BuiltInAnnotateRevision(lines[i][0], lines[i][1], useOneRGB ? palette : CommitterColors.getDefault().getCommitterRGB(lines[i][1])));
						info.addRevision(revision);
					}
					revision.addLine(Integer.parseInt(lines[i][2]));
				}
				long from = Revision.INVALID_REVISION_NUMBER, to = Revision.INVALID_REVISION_NUMBER;
				for (Iterator it = revisions.values().iterator(); it.hasNext(); ) {
					BuiltInAnnotateRevision revision = (BuiltInAnnotateRevision)it.next();
					revision.addLine(BuiltInAnnotateRevision.END_LINE);
					long revisionNum = Long.parseLong(revision.getId());
					if (from > revisionNum || from == Revision.INVALID_REVISION_NUMBER) {
						from = revisionNum;
					}
					if (to < revisionNum) {
						to = revisionNum;
					}
				}
				IRepositoryResource resource = annotateOp.getRepositoryResource();
				ISVNClientWrapper proxy = resource.getRepositoryLocation().acquireSVNProxy();
				try {
					LogEntry []msgs = GetLogMessagesOperation.getMessagesImpl(proxy, resource, Revision.fromNumber(to), Revision.fromNumber(from), ISVNClientWrapper.DEFAULT_LOG_ENTRY_PROPS, 0, false, this, monitor);
					for (int i = 0; i < msgs.length; i++) {
						BuiltInAnnotateRevision revision = (BuiltInAnnotateRevision)revisions.get(String.valueOf(msgs[i].revision));
						if (revision != null) {
							revision.setLogMessage(msgs[i]);
						}
					}
				}
				finally {
					resource.getRepositoryLocation().releaseSVNProxy(proxy);
				}
			}
		};
		op.add(prepareRevisions, new IActionOperation[] {annotateOp});
		IActionOperation attachMessages = new AbstractNonLockingOperation("Operation.BuiltInShowView") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				page.getActivePart().getSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						try {
							BuiltInAnnotate.this.initializeEditor(page, resource, info);
						}
						catch (PartInitException ex) {
							throw new RuntimeException(ex);
						}
					}
				});
			}
		};
		op.add(attachMessages, new IActionOperation[] {annotateOp, prepareRevisions});
		return op;
	}
	
	protected void initializeEditor(final IWorkbenchPage page, final IFile resource, RevisionInformation info) throws PartInitException {
		IEditorPart editor = this.openEditor(page, resource);
	    if (editor instanceof AbstractDecoratedTextEditor) {
	    	this.textEditor = (AbstractDecoratedTextEditor)editor;
	    	final ISelectionProvider provider = (ISelectionProvider)this.textEditor.getAdapter(RevisionSelectionProvider.class);
	    	if (provider != null) {
		    	final ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection() instanceof IStructuredSelection) {
							BuiltInAnnotateRevision selected = (BuiltInAnnotateRevision)((IStructuredSelection)event.getSelection()).getFirstElement();
							if (selected != null) {
								if (BuiltInAnnotate.this.historyPage != null) {
									BuiltInAnnotate.this.historyPage.selectRevision(Long.parseLong(selected.getId()));
								}
							}
						}
					}
				};
		    	provider.addSelectionChangedListener(selectionListener);
		    	page.addPartListener(new IPartListener() {
					public void partClosed(IWorkbenchPart part) {
						if (part instanceof IHistoryView || part == BuiltInAnnotate.this.textEditor) {
							page.removePartListener(this);
							provider.removeSelectionChangedListener(selectionListener);
						}
					}
					public void partActivated(IWorkbenchPart part) {
					}
					public void partBroughtToTop(IWorkbenchPart part) {
					}
					public void partDeactivated(IWorkbenchPart part) {
					}
					public void partOpened(IWorkbenchPart part) {
					} 
		    	});
	    	}
	    	this.textEditor.showRevisionInformation(info, SVNTeamQuickDiffProvider.class.getName());
	    }
	    
	    IHistoryView view = (IHistoryView)page.showView((SVNHistoryPage.VIEW_ID));
		if (view != null) {
			this.historyPage = (SVNHistoryPage)view.showHistoryFor(resource);
			//FIXME enqueue selection event
			//view.selectRevision(Long.parseLong(((BuiltInAnnotateRevision)info.getRevisions().get(0)).getId()));
		}
	}
	
	protected IEditorPart openEditor(IWorkbenchPage page, IFile resource) throws PartInitException {
		IEditorPart part = ResourceUtil.findEditor(page, resource);
		if (part != null && part instanceof AbstractDecoratedTextEditor) {
			page.activate(part);
			return part;
		}
		
		return IDE.openEditor(page, resource, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
	}
	
}
