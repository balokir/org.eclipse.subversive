/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
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
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.internal.text.revisions.RevisionSelectionProvider;
import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNAnnotationData;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevisionRange;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.GetResourceAnnotationOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.team.ui.history.RevisionAnnotationController;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * Annotation based on Eclipse Platform support.
 * 
 * @author Alexander Gurov
 */
public class BuiltInAnnotate {
	protected AbstractDecoratedTextEditor textEditor;
	protected SVNHistoryPage historyPage;
	
	public void open(IWorkbenchPage page, IRepositoryResource remote, IFile resource, SVNRevisionRange revisions) {
		GetResourceAnnotationOperation annotateOp = new GetResourceAnnotationOperation(remote, revisions);
		annotateOp.setIncludeMerged(SVNTeamPreferences.getMergeBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.MERGE_INCLUDE_MERGED_NAME));
		annotateOp.setRetryIfMergeInfoNotSupported(true);
		IActionOperation showOp = this.prepareBuiltInAnnotate(annotateOp, page, remote, resource);
		CompositeOperation op = new CompositeOperation(showOp.getId(), showOp.getMessagesClass());
		op.add(annotateOp);
		op.add(showOp, new IActionOperation[] {annotateOp});
		UIMonitorUtility.doTaskScheduledDefault(page.getActivePart(), op);
	}

	protected IActionOperation prepareBuiltInAnnotate(final GetResourceAnnotationOperation annotateOp, final IWorkbenchPage page, final IRepositoryResource remote, final IFile resource) {
		CompositeOperation op = new CompositeOperation("Operation_BuiltInShowAnnotation", SVNUIMessages.class); //$NON-NLS-1$
		final RevisionInformation info = new RevisionInformation();
		IActionOperation prepareRevisions = new AbstractActionOperation("Operation_PrepareRevisions", SVNUIMessages.class) { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				Map<String, BuiltInAnnotateRevision> revisions = new HashMap<String, BuiltInAnnotateRevision>();
				SVNAnnotationData []data = annotateOp.getAnnotatedLines();
				if (data == null || data.length == 0) {
					return;
				}
				String noAuthor = SVNMessages.SVNInfo_NoAuthor;
				for (int i = 0; i < data.length; i++) {
					String revisionId = String.valueOf(data[i].revision);
					BuiltInAnnotateRevision revision = revisions.get(revisionId);
					if (revision == null) {
						revisions.put(revisionId, revision = new BuiltInAnnotateRevision(revisionId, data[i].author, CommitterColors.getDefault().getCommitterRGB(data[i].author == null ? noAuthor : data[i].author)));
						info.addRevision(revision);
					}
					revision.addLine(i + 1);
					if (data[i].mergedRevision != SVNRevision.INVALID_REVISION_NUMBER && data[i].mergedRevision != data[i].revision) {
						revision.addMergeInfo(i + 1, data[i].mergedRevision, data[i].mergedDate, data[i].mergedAuthor == null ? noAuthor : data[i].mergedAuthor, data[i].mergedPath);
					}
				}
				long from = SVNRevision.INVALID_REVISION_NUMBER, to = SVNRevision.INVALID_REVISION_NUMBER;
				for (BuiltInAnnotateRevision revision : revisions.values()) {
					revision.addLine(BuiltInAnnotateRevision.END_LINE);
					long revisionNum = revision.getRevision();
					if (from > revisionNum || from == SVNRevision.INVALID_REVISION_NUMBER) {
						from = revisionNum;
					}
					if (to < revisionNum) {
						to = revisionNum;
					}
				}
				IRepositoryResource resource = annotateOp.getRepositoryResource();
				ISVNConnector proxy = resource.getRepositoryLocation().acquireSVNProxy();
				try {
					SVNLogEntry []msgs = SVNUtility.logEntries(proxy, SVNUtility.getEntryReference(resource), SVNRevision.fromNumber(to), SVNRevision.fromNumber(from), ISVNConnector.Options.NONE, ISVNConnector.DEFAULT_LOG_ENTRY_PROPS, 0, new SVNProgressMonitor(this, monitor, null));
					for (int i = 0; i < msgs.length; i++) {
						BuiltInAnnotateRevision revision = revisions.get(String.valueOf(msgs[i].revision));
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
		IActionOperation attachMessages = new AbstractActionOperation("Operation_BuiltInShowView", SVNUIMessages.class) { //$NON-NLS-1$
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				page.getActivePart().getSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						try {
							BuiltInAnnotate.this.initializeEditor(page, remote, resource, info);
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
	
	protected void initializeEditor(final IWorkbenchPage page, final IRepositoryResource remote, final IFile resource, RevisionInformation info) throws PartInitException {
		IEditorPart editor = resource != null ? this.openEditor(page, resource) : this.openEditor(page, remote);
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
	}
	
	protected IEditorPart openEditor(IWorkbenchPage page, IRepositoryResource remote) throws PartInitException {
		int openType = OpenRemoteFileOperation.OPEN_DEFAULT;
		String openWith = null;
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(remote.getName());
		if (descriptor == null || !descriptor.isInternal()) {
			openType = OpenRemoteFileOperation.OPEN_DEFAULT;
			openWith = EditorsUI.DEFAULT_TEXT_EDITOR_ID; 
		} else {
			openType = OpenRemoteFileOperation.OPEN_SPECIFIED;
			openWith = descriptor.getId();
		}		
		OpenRemoteFileOperation op = new OpenRemoteFileOperation(new IRepositoryFile[] {(IRepositoryFile)remote}, openType, openWith);
		op.setRequiredDefaultEditorKind(AbstractDecoratedTextEditor.class);
		ProgressMonitorUtility.doTaskExternal(op, new NullProgressMonitor());
		if (op.getExecutionState() == IActionOperation.OK) {
			IEditorPart part = op.getEditors()[0];
			return this.findTextEditorPart(page, part, op.getRepositoryEditorInputs()[0]);
		}
		return null;
	}
	
	protected IEditorPart openEditor(IWorkbenchPage page, IFile resource) throws PartInitException {
		/*
		 * This method opens a text editor that supports the use of a revision ruler on the given file.
		 * But despite we don't support revision ruler now, this method is suitable for us
		 */
		return RevisionAnnotationController.openEditor(page, resource);
	}
	
	protected AbstractDecoratedTextEditor findTextEditorPart(IWorkbenchPage page, IEditorPart editor, IEditorInput input) {
		if (editor instanceof AbstractDecoratedTextEditor)
			return (AbstractDecoratedTextEditor) editor;
		if (editor instanceof MultiPageEditorPart) {
			MultiPageEditorPart mpep = (MultiPageEditorPart) editor;
			IEditorPart[] parts = mpep.findEditors(input);
			for (int i = 0; i < parts.length; i++) {
				IEditorPart editorPart = parts[i];
				if (editorPart instanceof AbstractDecoratedTextEditor) {
			        page.activate(mpep);
			        mpep.setActiveEditor(editorPart);
					return (AbstractDecoratedTextEditor) editorPart;
				}
			}
		}
		return null;
	}
	
}
