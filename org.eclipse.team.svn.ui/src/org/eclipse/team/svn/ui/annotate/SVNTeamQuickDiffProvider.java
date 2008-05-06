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

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.GetLocalFileContentOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;

/**
 * Quick diff provider. Uses BASE working copy revision to compare. 
 * 
 * @author Alexander Gurov
 */
public class SVNTeamQuickDiffProvider implements IQuickDiffReferenceProvider, IResourceStatesListener, IElementStateListener {
	protected String id;
	protected ITextEditor editor;
	protected IDocumentProvider documentProvider;
	protected boolean referenceInitialized;
	protected IDocument reference;
	protected ILocalResource savedState;
	protected Job updateJob;

	public void setActiveEditor(ITextEditor editor) {
		IEditorInput editorInput = editor.getEditorInput();
		if (ResourceUtil.getFile(editorInput) != null) {
			this.editor = editor;
			this.documentProvider = editor.getDocumentProvider();
			SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
			if (this.documentProvider != null) {
				this.documentProvider.addElementStateListener(this);
			}
			this.referenceInitialized = true;
		}
	}

	public void dispose() {
		if (this.updateJob != null && this.updateJob.getState() != Job.NONE) {
			this.updateJob.cancel();
		}
		this.referenceInitialized = false;
		
		if (this.documentProvider != null) {
			this.documentProvider.removeElementStateListener(this);
		}
		SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, this);
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IDocument getReference(IProgressMonitor monitor) throws CoreException {
		if (this.referenceInitialized) {
			if (this.reference == null) {
				this.readDocument(monitor);
			}
			return this.reference;
		}
		return null;
	}

	public boolean isEnabled() {
		return this.referenceInitialized && this.isShared();
	}

	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		IFile file = this.getFile();
		if (file != null && this.isEnabled() && event.contains(this.getFile())) {
			this.backgroundFetch();
		}
	}

	public void elementContentReplaced(Object element) {
		IFile file = this.getFile();
		if (file != null && this.isEnabled() && this.editor.getEditorInput() == element) {
			this.backgroundFetch();
		}
	}

	public void elementContentAboutToBeReplaced(Object element) {
	}

	public void elementDeleted(Object element) {
	}

	public void elementDirtyStateChanged(Object element, boolean isDirty) {
	}

	public void elementMoved(Object originalElement, Object movedElement) {
	}
	
	protected boolean isShared() {
		ILocalResource local = this.getLocalResource();
		return local != null && IStateFilter.SF_VERSIONED.accept(local);
	}
	
	protected ILocalResource getLocalResource() {
		return SVNRemoteStorage.instance().asLocalResource(this.getFile());
	}
	
	protected IFile getFile() {
		return this.editor == null ? null : ResourceUtil.getFile(this.editor.getEditorInput());
	}
	
	protected void backgroundFetch() {
		if (this.updateJob != null && this.updateJob.getState() != Job.NONE) {
			this.updateJob.cancel();
		}
		this.updateJob = ProgressMonitorUtility.doTaskScheduledDefault(new AbstractActionOperation("Operation.QuickDiff") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				SVNTeamQuickDiffProvider.this.readDocument(monitor);
			}
		});
	}
	
	protected void readDocument(IProgressMonitor monitor) {
		if (this.reference == null) {
			this.reference = new Document();
		}
		if (this.documentProvider instanceof IStorageDocumentProvider) {
			IStorageDocumentProvider provider = (IStorageDocumentProvider)this.documentProvider;
			String encodingTmp = provider.getEncoding(this.editor.getEditorInput());
			String encoding = encodingTmp == null ? provider.getDefaultEncoding() : encodingTmp;
			
			ILocalResource tmp = this.getLocalResource();
			if (this.savedState == null || tmp != null && this.savedState.getRevision() != tmp.getRevision()) {
				this.savedState = tmp;
				final GetLocalFileContentOperation contentOp = new GetLocalFileContentOperation(tmp.getResource(), Kind.BASE);
				CompositeOperation op = new CompositeOperation("Operation.PrepareQuickDiff");
				op.add(contentOp);
				op.add(new InitializeDocumentOperation(encoding) {
					public InputStream getInputStream() {
						return contentOp.getContent();
					}
				}, new IActionOperation[] {contentOp});
				ProgressMonitorUtility.doTaskExternalDefault(op, monitor);
			}
		}
		else if (!monitor.isCanceled()) {
			this.reference.set("");
		}
	}
	
	protected abstract class InitializeDocumentOperation extends AbstractActionOperation {
		public String encoding;
		
		public InitializeDocumentOperation(String encoding) {
			super("Operation.InitializeDocument");
			this.encoding = encoding;
		}
		
		public abstract InputStream getInputStream() throws Exception;
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			InputStream content = this.getInputStream();
			Reader in = null;
			CharArrayWriter store = null;
			try {
				in = new BufferedReader(new InputStreamReader(content, this.encoding));
				store = new CharArrayWriter();
				char []buf = new char[2048];
				int len;
				while ((len = in.read(buf)) > 0 && !monitor.isCanceled()) {
					store.write(buf, 0, len);
				}
				if (!monitor.isCanceled()) {
					SVNTeamQuickDiffProvider.this.reference.set(store.toString());
				}
			}
			finally {
				if (store != null) {
					try {store.close();} catch (Exception ex) {}
				}
				if (in != null) {
					try {in.close();} catch (Exception ex) {}
				}
				try {content.close();} catch (Exception ex) {}
			}
		}
	}
	
}
