/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.annotate;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetResourceAnnotationOperation;
import org.eclipse.team.svn.core.resource.IRemoteStorage;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.AbstractSVNView;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.history.IRepositoryEditorInput;
import org.eclipse.team.svn.ui.history.SVNHistoryPage;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Annotate view implementation
 * 
 * @author Alexander Gurov
 */
public class AnnotateView extends AbstractSVNView {
	public static final String VIEW_ID = AnnotateView.class.getName();
	
	protected AnnotateEntry []annotatedLines;
	
	protected TableViewer viewer;
	protected IEditorPart editor;
	protected SVNHistoryPage historyView;
	protected boolean canTrackSelection;
	
	private IPartListener editorStateListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partClosed(IWorkbenchPart part) {
			if (part == AnnotateView.this.editor) {
				AnnotateView.this.annotatedLines = new AnnotateEntry[0];
				AnnotateView.this.setTableInput();
				AnnotateView.this.editor.getSite().getPage().removePartListener(this);
				AnnotateView.this.editor = null;
				AnnotateView.this.historyView = null;
			}
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
		public void partOpened(IWorkbenchPart part) {
		}
	};

	public AnnotateView() {
		super(SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.Name"));
	}
	
    public void dispose() {
        if (this.editor != null) {
            this.editor.getSite().getPage().removePartListener(this.editorStateListener);

            IWorkbench workbench = SVNTeamUIPlugin.instance().getWorkbench();
    		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
    		page.closeEditor(this.editor, false);
        }

		super.dispose();
    }
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		this.viewer = this.createLogTable(parent);
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					AnnotateView.this.linesSelectionChanged(((IStructuredSelection)event.getSelection()).getFirstElement());
				}				
			}
		});
		this.viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		this.setTableInput();
		
//		Setting context help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.annotationViewContext");
	}

	public void showEditor(IResource resource) {
	    this.showEditor(resource, null, null);
	}
	
	public void showEditor(IResource resource, SVNRevision topRevision, SVNRevision pegRevision) {
		this.wcResource = resource;
		IRepositoryResource remoteResource = null;
		if (resource != null) {
		    IRemoteStorage storage = SVNRemoteStorage.instance();
			remoteResource = storage.asRepositoryResource(resource);
			if (remoteResource != null) {
			    remoteResource.setSelectedRevision(topRevision);
			    remoteResource.setPegRevision(pegRevision);
			}
		}
		
		this.showEditorImpl(remoteResource);
	}
	
	public void showEditor(IRepositoryResource resource) {
		this.wcResource = null;
		this.showEditorImpl(resource);
	}
	
	protected void showEditorImpl(IRepositoryResource resource) {
	    if (this.editor != null) {
			this.editor.getSite().getPage().closeEditor(this.editor, false);
		}
	    
		this.repositoryResource = resource;
		
		this.showResourceLabel();
	    
		if (resource == null) {
			this.showEditor((byte [])null);
			return;
		}
		
		final GetResourceAnnotationOperation annotateOp = new GetResourceAnnotationOperation(resource);
		IActionOperation showOp = new AbstractActionOperation("Operation.AShowAnnotation") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				AnnotateView.this.getSite().getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						String [][]lines = annotateOp.getAnnotatedLines();
						ArrayList entries = new ArrayList();
						AnnotateEntry entry = null;
						for (int i = 0; i < lines.length; i++) {
							if (entry == null) {
								entry = new AnnotateEntry();
								entry.setRevision(lines[i][0]);
								entry.setAuthor(lines[i][1]);
								entry.setFirstLine(lines[i][2]);
							}
							else if (!entry.getRevision().equals(lines[i][0])) {
								entries.add(entry);
								entry = new AnnotateEntry();
								entry.setRevision(lines[i][0]);
								entry.setAuthor(lines[i][1]);
								entry.setFirstLine(lines[i][2]);
							}
							else {
								entry.setLastLine(lines[i][2]);
							}
						}
						if (entry == null) {
							AnnotateView.this.showEditor((byte [])null);
							MessageDialog dialog = new MessageDialog(
									AnnotateView.this.getSite().getShell(),
									SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.NoContent.Title"), 
									null, 
									MessageFormat.format(SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.NoContent.Message"), new String[] {AnnotateView.this.repositoryResource.getName()}),
									MessageDialog.INFORMATION, 
									new String[] {IDialogConstants.OK_LABEL}, 
									0);
							dialog.open();
							return;
						}
						entries.add(entry);
						AnnotateView.this.annotatedLines = (AnnotateEntry [])entries.toArray(new AnnotateEntry[entries.size()]);
						AnnotateView.this.showEditor(annotateOp.getContent());
					}
				});
			}
		};
		CompositeOperation op = new CompositeOperation(showOp.getId());
		op.add(annotateOp);
		op.add(showOp, new IActionOperation[] {annotateOp});
		
		UIMonitorUtility.doTaskScheduledDefault(this, op);
	}
	
	public void setFocus() {
		
	}

	protected void showEditor(byte []data) {
		this.canTrackSelection = true;
		
		this.setTableInput();
		
		if (data == null) {
			return;
		}
		
		IWorkbench workbench = SVNTeamUIPlugin.instance().getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		
		UIMonitorUtility.doTaskBusyDefault(new AbstractActionOperation("Operation.AShowHistoryPart") {
            protected void runImpl(IProgressMonitor monitor) throws Exception {
    			if (AnnotateView.this.historyView == null) {
    				IHistoryView view = (IHistoryView)page.showView(SVNHistoryPage.VIEW_ID);
    				if (view != null) {
    					AnnotateView.this.historyView = (SVNHistoryPage)view.showHistoryFor(AnnotateView.this.wcResource != null ? (Object)AnnotateView.this.wcResource : AnnotateView.this.repositoryResource);
    				}
    			}
            }
        });
		
		AnnotateEditorInput input = new AnnotateEditorInput((IRepositoryFile)this.repositoryResource, data);
		OpenRemoteFileOperation op = new OpenRemoteFileOperation(new IRepositoryEditorInput[] {input}, OpenRemoteFileOperation.OPEN_SPECIFIED, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
		UIMonitorUtility.doTaskBusyDefault(op);
		IEditorPart []editors = op.getEditors();
		if (op.getEditors() != null && op.getEditors().length > 0) {
		    this.editor = editors[0];
		    if (this.editor instanceof ITextEditor) {
				((IPostSelectionProvider)((ITextEditor)this.editor).getSelectionProvider()).addPostSelectionChangedListener(
						new ISelectionChangedListener() {
							public void selectionChanged(SelectionChangedEvent event) {
								AnnotateView.this.textSelectionChanged((ITextSelection)event.getSelection());
							}
						}
					);
		    }
			this.editor.getSite().getPage().addPartListener(this.editorStateListener);
		}
	}
	
	protected void textSelectionChanged(ITextSelection selection) {
		if (this.annotatedLines.length > 0) {
			int line = selection.getStartLine() + 1;
			int idx = -1;
			for (int i = 0; i < this.annotatedLines.length; i++) {
				if (Integer.parseInt(this.annotatedLines[i].getFirstLine()) <= line &&
					Integer.parseInt(this.annotatedLines[i].getLastLine()) >= line) {
					idx = i;
					break;
				}
			}
			if (idx != -1) {
				IStructuredSelection current = (IStructuredSelection)this.viewer.getSelection();
				if (current == null || current.isEmpty() || current.getFirstElement() != this.annotatedLines[idx]) {
					this.canTrackSelection = false;
					String []data = this.annotatedLines[idx].getData();
					this.viewer.setSelection(new StructuredSelection(this.annotatedLines[idx]));
					if (this.historyView != null) {
						this.historyView.selectRevision(Long.parseLong(data[0]));
					}
					this.canTrackSelection = true;
				}
			}
		}
	}
	
	protected void linesSelectionChanged(Object selection) {
		if (!this.canTrackSelection || !(this.editor instanceof ITextEditor) || selection == null) {
			return;
		}
		AnnotateEntry entry = (AnnotateEntry)selection;
		
		final ITextEditor editor = (ITextEditor)this.editor;
		IDocumentProvider provider = editor.getDocumentProvider();
		final IDocument document = provider.getDocument(editor.getEditorInput());
		final int selectedIdxStart = Integer.parseInt(entry.getFirstLine()) - 1;
		final int selectedIdxStop = Integer.parseInt(entry.getLastLine()) - 1;
		
		UIMonitorUtility.doTaskBusyDefault(new AbstractActionOperation("Operation.AChangeSelection") {
            protected void runImpl(IProgressMonitor monitor) throws Exception {
        		int start = document.getLineOffset(selectedIdxStart);
        		IRegion region = document.getLineInformation(selectedIdxStop);
        		int stop = document.getLineOffset(selectedIdxStop) + region.getLength();
        		int length = document.getLength();
        		if (stop < length) {
        			stop = document.getLineOffset(selectedIdxStop + 1);
        		}
        		
        		editor.selectAndReveal(start, stop - start);
            }
        });

		if (this.historyView != null) {
			this.historyView.selectRevision(Long.parseLong(((AnnotateEntry)selection).getData()[0]));
		}
	}
	
	protected TableViewer createLogTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
	
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		TableViewer viewer = new TableViewer(table);
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.Revision"));
		layout.addColumnData(new ColumnWeightData(20, true));
	
		// author
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.Author"));
		layout.addColumnData(new ColumnWeightData(35, true));
	
		// line number
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setAlignment(SWT.RIGHT);
		col.setText(SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.Lines"));
		layout.addColumnData(new ColumnWeightData(45, true));

		viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return AnnotateView.this.repositoryResource == null ? new AnnotateEntry[0] : AnnotateView.this.annotatedLines;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		viewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				return ((AnnotateEntry)element).getData()[columnIndex];
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return true;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		viewer.setInput(this.annotatedLines);

		return viewer;
	}

	protected void setTableInput() {
		if (!this.viewer.getTable().isDisposed()) {
			this.viewer.setInput(this.annotatedLines);
		}
	}
	
	protected void disconnectView() {
		this.getSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				AnnotateView.this.showEditor((IResource)null);
			}
		});
	}
	
	protected void refreshView() {
	}
	
	protected class AnnotateEntry {
		protected String []data;
		
		public AnnotateEntry() {
			this.data = new String[5];
		}
		
		public String getRevision() {
			return this.data[0];
		}
		
		public void setRevision(String revision) {
			this.data[0] = revision;
		}
		
		public String getAuthor() {
			return this.data[1];
		}
		
		public void setAuthor(String author) {
			this.data[1] = author;
		}
		
		public String getFirstLine() {
			return this.data[3] == null ? this.data[4] : this.data[3];
		}
		
		public void setFirstLine(String line) {
			this.data[3] = line;
			this.makeLinesText();
		}
		
		public String getLastLine() {
			return this.data[4] == null ? this.data[3] : this.data[4];
		}
		
		public void setLastLine(String line) {
			this.data[4] = line;
			this.makeLinesText();
		}
		
		public String []getData() {
			return this.data;
		}
		
		protected void makeLinesText() {
			if (this.data[3] != null) {
				if (this.data[4] != null) {
					int lines = Integer.parseInt(this.data[4]) - Integer.parseInt(this.data[3]) + 1;
					this.data[2] = MessageFormat.format(SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.MultiLine"), new String[] {String.valueOf(lines), this.data[3], this.data[4]});
				}
				else {
					this.data[2] = MessageFormat.format(SVNTeamUIPlugin.instance().getResource("SVNAnnotateView.OneLine"), new String[] {this.data[3]});
				}
			}
			else {
				this.data[2] = this.data[4];
			}
		}
		
	}

	protected boolean needsLinkWithEditorAndSelection() {
		return false;
	}

}
