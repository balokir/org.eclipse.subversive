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

package org.eclipse.team.svn.ui.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNConnectorException;
import org.eclipse.team.svn.core.connector.SVNLogEntry;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.local.GetRemoteContentsOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.UpdateOperation;
import org.eclipse.team.svn.core.operation.remote.ExportOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.operation.remote.LocateResourceURLInHistoryOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryContainer;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.AbstractRepositoryTeamAction;
import org.eclipse.team.svn.ui.action.remote.BranchTagAction;
import org.eclipse.team.svn.ui.action.remote.CreatePatchAction;
import org.eclipse.team.svn.ui.action.remote.OpenFileAction;
import org.eclipse.team.svn.ui.action.remote.OpenFileWithAction;
import org.eclipse.team.svn.ui.action.remote.OpenFileWithExternalAction;
import org.eclipse.team.svn.ui.action.remote.OpenFileWithInplaceAction;
import org.eclipse.team.svn.ui.composite.LogMessagesComposite;
import org.eclipse.team.svn.ui.composite.LogMessagesComposite.HistoryCategory;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.dialog.ReplaceWarningDialog;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.operation.CorrectRevisionOperation;
import org.eclipse.team.svn.ui.operation.LocalShowAnnotationOperation;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.remote.ExportPanel;
import org.eclipse.team.svn.ui.panel.view.HistoryFilterPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.utility.LockProposeUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Internals of the HistoryView. Can be used in Eclipse 3.2 generic HistoryView or in our own HistoryView
 * 
 * @author Alexander Gurov
 */
public class HistoryViewImpl {
	public static final int PAGING_ENABLED = 0x01;
	public static final int COMPARE_MODE = 0x02;
	public static final int HIDE_UNRELATED = 0x04;
	public static final int STOP_ON_COPY = 0x08;
	public static final int GROUP_BY_DATE = 0x10;
	
	protected IResource wcResource;
	protected IRepositoryResource repositoryResource;
	
	protected IViewInfoProvider viewInfoProvider;
	
	protected LogMessagesComposite history;
	
	protected String filterByAuthor;
	protected String filterByComment;
	
	protected Action showCommentViewerAction;
	protected Action showAffectedPathsViewerAction;
	protected Action hideUnrelatedAction;
	protected Action hideUnrelatedDropDownAction;
	protected Action stopOnCopyAction;
	protected Action stopOnCopyDropDownAction;
	protected Action groupByDateDropDownAction;
	protected Action getNextPageAction;
	protected Action getAllPagesAction;
	protected Action clearFilterAction;
	protected Action clearFilterDropDownAction;
	protected Action filterAction;
	protected Action filterDropDownAction;
	protected Action hierarchicalAction;
	protected Action flatAction;
	protected Action compareModeAction;
	protected Action compareModeDropDownAction;
	protected Action groupByDateAction;
	
	protected long limit = 25;
	protected boolean pagingEnabled = false;
	protected boolean isCommentFilterEnabled = false;
	protected int options = 0;
	protected long currentRevision = 0;
	protected ILocalResource compareWith;
	
	protected IPropertyChangeListener configurationListener;

	protected SVNLogEntry []logMessages;
	
	public HistoryViewImpl(IResource wcResource, IRepositoryResource repositoryResource, IViewInfoProvider viewInfoProvider) {
		this.wcResource = wcResource;
		this.repositoryResource = repositoryResource;
		
		this.viewInfoProvider = viewInfoProvider;
		
		this.filterByAuthor = "";
		this.filterByComment = "";
		
		this.configurationListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().startsWith(SVNTeamPreferences.HISTORY_BASE)) {
					HistoryViewImpl.this.refreshOptionButtons();
				}
			}
		};
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this.configurationListener);
	}
	
	public IWorkbenchPartSite getSite() {
		return this.viewInfoProvider.getPartSite();
	}
	
	public IActionBars getActionBars() {
		return this.viewInfoProvider.getActionBars();
	}

	public int getOptions() {
		return this.options;
	}
	
	public void setCompareWith(ILocalResource compareWith) {
		this.compareWith = compareWith;
	}
	
	public void setOptions(int mask, int values) {
		this.options = (this.options & ~mask) | (mask & values);
        IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
        SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE, (HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
        SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME, (HistoryViewImpl.this.options & HistoryViewImpl.PAGING_ENABLED) != 0);
        this.refreshOptionButtons();
	}
	
	public IResource getResource() {
		return this.wcResource;
	}
	
	public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public void dispose() {
		SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this.configurationListener);
		this.history.dispose();
	}

	public Control getControl() {
		return this.history;
	}

	public void createPartControl(Composite parent) {
		this.history = new LogMessagesComposite(parent, 70, SWT.MULTI);
		
		Tree table = this.history.getTreeViewer().getTree();
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(table);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				final IStructuredSelection tSelection = (IStructuredSelection)HistoryViewImpl.this.history.getTreeViewer().getSelection();
				Action tAction = null;
				boolean onlyLogEntries = true;
				Object []selected = tSelection.toArray();
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] instanceof HistoryCategory) {
						onlyLogEntries = false;
						break;
					}
				}
				if (onlyLogEntries) {
					if (HistoryViewImpl.this.repositoryResource instanceof IRepositoryFile) {
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Open")) {
							public void run() {
								HistoryViewImpl.this.handleDoubleClick(tSelection.getFirstElement(), false);
							}
						});
						tAction.setEnabled(tSelection.size() == 1);
						
						//FIXME: "Open with" submenu shouldn't be hardcoded after reworking of
						//       the HistoryView. Should be made like the RepositoriesView menu.
						MenuManager sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("HistoryView.OpenWith"), "historyOpenWithMenu");
						sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
						
						sub.add(new Separator("nonDefaultTextEditors"));
						String name = HistoryViewImpl.this.repositoryResource.getName();
						IEditorDescriptor[] editors = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getEditors(name);
						for (int i = 0; i < editors.length; i++) {
		    				if (!editors[i].getId().equals(EditorsUI.DEFAULT_TEXT_EDITOR_ID)) {
		    					HistoryViewImpl.this.addMenuItem(sub, editors[i].getLabel(), new OpenFileWithAction(editors[i].getId(), false));
		    				}
		    			}
							
						sub.add(new Separator("variousEditors"));
						HistoryViewImpl.this.addMenuItem(sub, SVNTeamUIPlugin.instance().getResource("HistoryView.TextEditor"), new OpenFileWithAction());
						HistoryViewImpl.this.addMenuItem(sub, SVNTeamUIPlugin.instance().getResource("HistoryView.SystemEditor"), new OpenFileWithExternalAction());
						HistoryViewImpl.this.addMenuItem(sub, SVNTeamUIPlugin.instance().getResource("HistoryView.InplaceEditor"), new OpenFileWithInplaceAction());
						HistoryViewImpl.this.addMenuItem(sub, SVNTeamUIPlugin.instance().getResource("HistoryView.DefaultEditor"), new OpenFileAction());
							
			        	manager.add(sub);
					}
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.CompareEachOther")) {
						public void run() {
							Object []selection = tSelection.toArray();
							IRepositoryResource left = HistoryViewImpl.this.getResourceForSelectedRevision(selection[0]);
							IRepositoryResource right = HistoryViewImpl.this.getResourceForSelectedRevision(selection[1]);
							try {
								if (left.getRevision() < right.getRevision()) {
									IRepositoryResource tmp = right;
									right = left;
									left = tmp;
								}
								UIMonitorUtility.doTaskScheduledActive(new CompareRepositoryResourcesOperation(right, left));
							}
							catch (SVNConnectorException ex) {
								UILoggedOperation.reportError("Compare", ex);
							}
						}
					});
	        		boolean isCompareAllowed = 
	        			(CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.COMPARE_FOLDERS) != 0 ||
	        			HistoryViewImpl.this.repositoryResource instanceof IRepositoryFile;
					tAction.setEnabled(tSelection.size() == 2 && isCompareAllowed);
					if (tSelection.size() == 1) {
						final Object []selection = tSelection.toArray();
						String revision = HistoryViewImpl.this.wcResource != null ? String.valueOf(((SVNLogEntry)selection[0]).revision) : SVNTeamUIPlugin.instance().getResource("HistoryView.HEAD");
						
						String msg = SVNTeamUIPlugin.instance().getResource("HistoryView.CompareCurrentWith", new String[] {revision});
						manager.add(tAction = new Action(msg) {
							public void run() {
								HistoryViewImpl.this.compareWithCurrent(selection[0]);
							}
						});
						tAction.setEnabled(tSelection.size() == 1 && isCompareAllowed);
					}
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.CreateUnifiedDiff")) {
						public void run() {
							HistoryViewImpl.this.createUnifiedDiff(tSelection);
						}
					});
					tAction.setEnabled(tSelection.size() == 1 || tSelection.size() == 2);
					
					if (HistoryViewImpl.this.repositoryResource instanceof IRepositoryFile) {
						manager.add(new Separator());
					}
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowProperties")) {
						public void run() {
							Object []selection = tSelection.toArray();
							IRepositoryResource resource = HistoryViewImpl.this.getResourceForSelectedRevision(selection[0]);
							IResourcePropertyProvider provider = new GetRemotePropertiesOperation(resource);
							ShowPropertiesOperation op = new ShowPropertiesOperation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), resource, provider);
							CompositeOperation composite = new CompositeOperation(op.getId());
							composite.add(provider);
							composite.add(op, new IActionOperation[] {provider});
							if (!op.isEditorOpened()) {
								UIMonitorUtility.doTaskScheduledActive(composite);
							}
						}
					});
					tAction.setEnabled(tSelection.size() == 1);
					tAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
					if (HistoryViewImpl.this.repositoryResource instanceof IRepositoryFile) {
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowAnnotation")) {
							public void run() {
								HistoryViewImpl.this.showRepositoryResourceAnnotation(tSelection.getFirstElement());
							}
						});
						tAction.setEnabled(tSelection.size() == 1);
					}
					if (HistoryViewImpl.this.wcResource != null || HistoryViewImpl.this.repositoryResource instanceof IRepositoryFile) {
						manager.add(new Separator());
					}
					if (HistoryViewImpl.this.wcResource != null) {
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.GetContents")) {
							public void run() {
								if (HistoryViewImpl.this.isIgnoreReplaceWarning()) {
									HistoryViewImpl.this.getRevisionContents(tSelection.getFirstElement());
								}
							}
						});
						tAction.setEnabled(tSelection.size() == 1);
					}
					if (HistoryViewImpl.this.wcResource != null)  {
						manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.UpdateTo")) {
							public void run() {
								if (HistoryViewImpl.this.isIgnoreReplaceWarning()) {
									HistoryViewImpl.this.updateTo(tSelection.getFirstElement());
								}
							}
						});
						tAction.setEnabled(tSelection.size() == 1);
					}
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Export")) {
						public void run() {
							HistoryViewImpl.this.doExport(tSelection.getFirstElement());
						}
					});
					tAction.setEnabled(tSelection.size() == 1);
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif"));
					manager.add(new Separator());
	
					String branchFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFromRevision");
					String tagFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.TagFromRevision");
					if (tSelection.size() == 1) {
						String revision = String.valueOf(((SVNLogEntry)tSelection.getFirstElement()).revision);
						branchFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String[] {revision});
						tagFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.TagFrom", new String[] {revision});
					}
					manager.add(tAction = new Action(branchFrom) {
						public void run() {
							PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(new IRepositoryResource[] {HistoryViewImpl.this.getResourceForSelectedRevision(tSelection.getFirstElement())}, UIMonitorUtility.getShell(), BranchTagAction.BRANCH_ACTION, false);
							if (op != null) {
								UIMonitorUtility.doTaskScheduledActive(op);
							}
						}
					});
					tAction.setEnabled(tSelection.size() == 1);
					tAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
					manager.add(tAction = new Action(tagFrom) {
						public void run() {
							PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(new IRepositoryResource[] {HistoryViewImpl.this.getResourceForSelectedRevision(tSelection.getFirstElement())}, UIMonitorUtility.getShell(), BranchTagAction.TAG_ACTION, false);
							if (op != null) {
								UIMonitorUtility.doTaskScheduledActive(op);
							}
						}
					});
					tAction.setEnabled(tSelection.size() == 1);
					tAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif"));
					
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.AddRevisionLink")) {
						public void run() {
							HistoryViewImpl.this.addRevisionLinks(tSelection);
						}
					});
					tAction.setEnabled(tSelection.size() == 1);
					manager.add(new Separator());
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.CopyHistory")) {
						public void run() {
							HistoryViewImpl.this.handleCopy();
						}
					});
					tAction.setEnabled(tSelection.size() > 0);
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/copy.gif"));
					manager.add(new Separator());
					if (HistoryViewImpl.this.wcResource != null) {
					    manager.add(tAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.QuickFilter")) {
					        public void run() {
					            if (HistoryViewImpl.this.quickFilter()) {
					            	HistoryViewImpl.this.showHistoryImpl(null, false);
					            }
					        }
					    });
					    tAction.setEnabled(HistoryViewImpl.this.logMessages != null);
					    tAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/filter.gif"));
					    manager.add(tAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.ClearFilter")) {
					        public void run() {
					        	HistoryViewImpl.this.clearFilter();
					        	HistoryViewImpl.this.showHistoryImpl(null, false);
					        }
					    });
					    tAction.setDisabledImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear.gif"));
					    tAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear_filter.gif"));
					    tAction.setEnabled(isFilterEnabled());
					}
				}
				if (!onlyLogEntries) {
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.CopyHistory")) {
						public void run() {
							HistoryViewImpl.this.handleCopy();
						}
					});
					tAction.setEnabled(tSelection.size() > 0);
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/copy.gif"));
				}
				manager.add(new Separator()); 
				manager.add(HistoryViewImpl.this.getRefreshAction());
			}
		});
        menuMgr.setRemoveAllWhenShown(true);
        table.setMenu(menu);
        this.getSite().registerContextMenu(menuMgr, this.history.getTreeViewer());
        
        this.history.getAffectedPathsComposite().registerMenuManager(this.getSite());
        
        //drop-down menu
        IActionBars actionBars = this.getActionBars();	    
	    IMenuManager actionBarsMenu = actionBars.getMenuManager();
	    this.showCommentViewerAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.ShowCommentViewer")) {
	        public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				boolean showMultiline = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME);
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME, !showMultiline);
				SVNTeamUIPlugin.instance().savePluginPreferences();
	        }	        
	    };
	    this.showAffectedPathsViewerAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowAffectedPathsViewer")) {
	        public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				boolean showAffected = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME);
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME, !showAffected);
				SVNTeamUIPlugin.instance().savePluginPreferences();
	        }
	    };
	    
	    this.hideUnrelatedDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.HideUnrelatedPaths")) {
	        public void run() {
	        	HistoryViewImpl.this.options ^= HistoryViewImpl.HIDE_UNRELATED;
	        	HistoryViewImpl.this.history.setShowRelatedPathsOnly((HistoryViewImpl.this.options & HistoryViewImpl.HIDE_UNRELATED) != 0);
	        	HistoryViewImpl.this.hideUnrelatedAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.HIDE_UNRELATED) != 0);
	        }	        
	    };
	    this.stopOnCopyDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.StopOnCopy")) {
	    	public void run() {
	    		HistoryViewImpl.this.options ^= HistoryViewImpl.STOP_ON_COPY;
	    		HistoryViewImpl.this.stopOnCopyAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
	    		HistoryViewImpl.this.refresh();
	        }
	    };
	    this.groupByDateDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.GroupByDate")) {
	    	public void run() {
	    		HistoryViewImpl.this.options ^= HistoryViewImpl.GROUP_BY_DATE;
	    		HistoryViewImpl.this.groupByDateAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
	    		HistoryViewImpl.this.history.setGroupByDate((HistoryViewImpl.this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
	    		HistoryViewImpl.this.history.setTableInput();
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				int type = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
				SVNTeamPreferences.setHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, type == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
	        }
	    };	    
	    this.filterDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.QuickFilter")) {
	    	public void run() {
	    		if (HistoryViewImpl.this.quickFilter()) {
	    			HistoryViewImpl.this.showHistoryImpl(null, false);
	        	}
	        }
	    };
	    this.clearFilterDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.ClearFilter")) {
	    	public void run() {
	    		HistoryViewImpl.this.clearFilter();
	    		HistoryViewImpl.this.showHistoryImpl(null, false);
	        }
	    };
	    this.compareModeDropDownAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.CompareMode")) {
	    	public void run() {
	    		HistoryViewImpl.this.options ^= HistoryViewImpl.COMPARE_MODE;
	            IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
	            SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE, (HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	            HistoryViewImpl.this.compareModeAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	        }
	    };
	    actionBarsMenu.add(this.showCommentViewerAction);
	    actionBarsMenu.add(this.showAffectedPathsViewerAction);
	    MenuManager sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("HistoryView.AffectedPathLayout"), IWorkbenchActionConstants.GROUP_MANAGING);
		sub.add(this.flatAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Flat"), Action.AS_RADIO_BUTTON) {
			public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, false);
			}
		});
		this.flatAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/flat_layout.gif"));		
		
		sub.add(this.hierarchicalAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Hierarchical"), Action.AS_RADIO_BUTTON) {
			public void run() {
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT, true);
				
			}
		});
		this.hierarchicalAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/tree_layout.gif"));
		actionBarsMenu.add(sub);
		actionBarsMenu.add(new Separator());
		actionBarsMenu.add(this.groupByDateDropDownAction);
	    actionBarsMenu.add(new Separator());
	    actionBarsMenu.add(this.hideUnrelatedDropDownAction);
	    actionBarsMenu.add(this.stopOnCopyDropDownAction);
	    actionBarsMenu.add(new Separator());
	    actionBarsMenu.add(this.filterDropDownAction);
	    actionBarsMenu.add(this.clearFilterDropDownAction);
	    actionBarsMenu.add(new Separator());
	    actionBarsMenu.add(this.compareModeDropDownAction);
	    
	    this.showToolBar();
        this.history.getTreeViewer().getControl().addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent event) {
        		if (event.keyCode == SWT.F5) {
        			HistoryViewImpl.this.refresh();
        		}
        		if (event.stateMask == SWT.CTRL && (event.keyCode == 'c' || event.keyCode == 'C')) {
        			HistoryViewImpl.this.handleCopy();
        		}
        	}
        });
        
        this.history.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection)selection;
					if (structured.size() == 1) {
						HistoryViewImpl.this.handleDoubleClick(structured.getFirstElement(), true);
					}
				}
			}
		});
				
		this.viewInfoProvider.setDescription(this.getResourceLabel());
		
	    this.refreshOptionButtons();
	    
	    //Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.historyViewContext");
	}
	
	public void refresh() {
		long revision = this.history.getSelectedRevision();
		
		if (this.wcResource == null) {
			this.showHistory(this.repositoryResource, true);
		}
		else {
			this.showHistory(this.wcResource, true);
		}
		this.history.setSelectedRevision(revision);		
	}
	
	public void selectRevision(long revision) {
		this.history.setSelectedRevision(revision);
	}

	public void showHistory(IResource resource, boolean background) {
		this.wcResource = resource;
		
		//must be called first, to initialize bug-track model
		this.history.getCommentView().usedFor(resource);
		
	    long currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
	    IRepositoryResource remote = null;
		if (resource != null) {
			remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			if (local != null) {
				currentRevision = local.getRevision();
				if (local.isCopied()) {
					remote = SVNUtility.getCopiedFrom(resource);
				}
			}
		}
		this.showHistoryImpl(currentRevision, remote, background);
	}
	
	public void showHistory(IRepositoryResource remoteResource) {
		this.showHistory(remoteResource, false);
	}
	
	public void showHistory(IRepositoryResource remoteResource, boolean background) {
		this.wcResource = null;
		
		//must be called first, to initialize backtrack model
		this.history.getCommentView().usedFor(remoteResource);
		long currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
		try {
			if (remoteResource != null) {
				currentRevision = remoteResource.getRevision();
			}
		} 
		catch (Exception e){

		} 
		this.showHistoryImpl(currentRevision, remoteResource, background);
	}
	
	public void updateViewInput(IRepositoryResource resource) {
		if (this.repositoryResource != null && this.repositoryResource.equals(resource)) {
			return;
		}
		if (resource != null) {
			if (this.repositoryResource != null && resource.getUrl().equals(this.repositoryResource.getUrl())) {
				if (resource.getSelectedRevision().getKind() == Kind.NUMBER) {
					SVNRevision currentRevision = resource.getSelectedRevision();
					this.repositoryResource.setSelectedRevision(SVNRevision.HEAD);
					this.showHistoryImpl(((SVNRevision.Number)currentRevision).getNumber(), this.repositoryResource, true);
					return;
				}
			}
			resource.setSelectedRevision(SVNRevision.HEAD);
			this.showHistory(resource, true);
		}
	}
	
	public void updateViewInput(IResource resource) {
		if (resource != null) {
			SVNRemoteStorage storage = SVNRemoteStorage.instance();
			ILocalResource local = storage.asLocalResource(resource);
			if (local != null && IStateFilter.SF_ONREPOSITORY.accept(local)) {
				if (local.getResource().equals(this.wcResource)) {
					IRepositoryResource remote = storage.asRepositoryResource(local.getResource());
					if (this.repositoryResource != null && !this.repositoryResource.equals(remote)) {
						this.updateViewInput(remote);
					}
					return;
				}
				this.showHistory((IResource)null, true);
				this.refresh();
				this.showHistory(resource, true);
			}
		}
	}
	
	protected void showHistoryImpl(long currentRevision, IRepositoryResource remote, boolean background) {
		if (this.repositoryResource != null) {
			if (this.repositoryResource.equals(remote)) {
				this.logMessages = null;
			}
			else {
				this.clearFilter();	
			}
		}
		this.repositoryResource = remote;
		this.currentRevision = currentRevision;

		this.viewInfoProvider.setDescription(this.getResourceLabel());
		
		if (this.repositoryResource == null) {
			this.history.setLogMessages(null, null, null);
			this.setPagingDisabled();
		}
		else {
			this.logMessages = null;
			GetLogMessagesOperation msgOp = new GetLogMessagesOperation(this.repositoryResource, this.stopOnCopyAction.isChecked());
			msgOp.setLimit(this.limit);
			this.showHistoryImpl(msgOp, background);
		}
	}
	
	protected void showHistoryImpl(final GetLogMessagesOperation msgsOp, boolean background) {
		TreeViewer treeTable = this.history.getTreeViewer();
		//final IStructuredSelection selected = (IStructuredSelection)treeTable.getSelection();
		final ISelection selected = treeTable.getSelection();
		IActionOperation showOp = new AbstractActionOperation("Operation.HShowHistory") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (HistoryViewImpl.this.historyForTheOtherResource(msgsOp)) {
							return;
						}
						if (msgsOp != null && msgsOp.getExecutionState() == IActionOperation.OK) {
							HistoryViewImpl.this.pagingEnabled = HistoryViewImpl.this.limit > 0 && HistoryViewImpl.this.logMessages == null ? msgsOp.getMessages().length == HistoryViewImpl.this.limit : msgsOp.getMessages().length == HistoryViewImpl.this.limit + 1;
							HistoryViewImpl.this.addPage(msgsOp.getMessages());
						}
						SVNLogEntry[] toShow = HistoryViewImpl.this.isFilterEnabled() && HistoryViewImpl.this.logMessages != null ? HistoryViewImpl.this.filterMessages(HistoryViewImpl.this.logMessages) : HistoryViewImpl.this.logMessages;
						SVNRevision current = HistoryViewImpl.this.currentRevision != -1 ? SVNRevision.fromNumber(HistoryViewImpl.this.currentRevision) : null;
			    		HistoryViewImpl.this.history.setGroupByDate((HistoryViewImpl.this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
						HistoryViewImpl.this.history.setLogMessages(current, toShow, HistoryViewImpl.this.repositoryResource);
						HistoryViewImpl.this.setPagingEnabled();
//						HistoryViewImpl.this.viewInfoProvider.setDescription(HistoryViewImpl.this.getResourceLabel());
					}
				});
			}
		};
		IActionOperation selectOp = new AbstractActionOperation("Operation.HSaveTableSelection") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						if (HistoryViewImpl.this.historyForTheOtherResource(msgsOp)) {
							return;
						}
					    TreeViewer treeTable = HistoryViewImpl.this.history.getTreeViewer();
					    if (!treeTable.getTree().isDisposed()) {
						   /* if (selected.size() == 0) {
						    	treeTable.getTree().setSelection(treeTable.getTree().getItem(0));
						    }
						    else {*/
						        treeTable.setSelection(selected, true);
						        HistoryViewImpl.this.history.getHistoryTableListener().selectionChanged(null);
						    //}
					    }
					}
				});
			}
		};
		CompositeOperation op = new CompositeOperation(showOp.getId());
		if (msgsOp != null) {
			op.add(new CorrectRevisionOperation(msgsOp, this.repositoryResource, this.currentRevision, this.wcResource));
			op.add(msgsOp);
		}
		op.add(showOp);
		op.add(selectOp, new IActionOperation[] {showOp});
		if (background) {
			ProgressMonitorUtility.doTaskScheduled(op, false);
		}
		else {
			UIMonitorUtility.doTaskScheduledDefault(this.getSite().getPart(), op);
		}
	}
	
	/*
	 * Checks if Get Log Messages Operation was called for the one resource, 
	 * but History View is already connected to another.
	 */
	protected boolean historyForTheOtherResource(GetLogMessagesOperation msgsOp) {
		return this.repositoryResource == null || this.repositoryResource != null && msgsOp != null && !this.repositoryResource.equals(msgsOp.getResource());
	}
	
	protected void getRevisionContents(Object item) {
		IRepositoryResource remote = this.getResourceForSelectedRevision(item);
		
		// propose user to lock the file if it needs lock
		boolean canWrite = true;
		
		if (this.wcResource != null && this.wcResource instanceof IFile) {
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(this.wcResource);
			if (local != null && !local.isLocked() && IStateFilter.SF_NEEDS_LOCK.accept(local)) {
				canWrite = LockProposeUtility.proposeLock(new IResource[] {this.wcResource}, this.getSite().getShell());
			}
		}
		if (canWrite) {
			GetRemoteContentsOperation mainOp = new GetRemoteContentsOperation(this.wcResource, remote);
			
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(mainOp);
			op.add(new RefreshResourcesOperation(new IResource[] {this.wcResource}));

			UIMonitorUtility.doTaskNowWorkspaceModify(this.getSite().getShell(), op, false);
		}
	}
	
	protected void showRepositoryResourceAnnotation(final Object item) {
		IWorkbenchPage page = this.getSite().getPage();
        IRepositoryResource remote = this.getResourceForSelectedRevision(item);
		UIMonitorUtility.doTaskBusyDefault(
			this.wcResource != null ? 
			(IActionOperation)new LocalShowAnnotationOperation(this.wcResource, page, remote.getSelectedRevision()) :
			new RemoteShowAnnotationOperation(remote, page));
	}
	
	protected void doExport(Object item) {
		IRepositoryResource resource = this.getResourceForSelectedRevision(item);
		ExportPanel panel = new ExportPanel(resource);
		DefaultDialog dialog = new DefaultDialog(UIMonitorUtility.getShell(), panel);
		if (dialog.open() == 0) {
			resource = SVNUtility.copyOf(resource);
			resource.setSelectedRevision(panel.getSelectedRevision());
	    	UIMonitorUtility.doTaskScheduledDefault(new ExportOperation(resource, panel.getLocation()));
	    }
	}
	
	protected void addRevisionLinks(IStructuredSelection tSelection) {
		IRepositoryLocation location = this.repositoryResource.getRepositoryLocation();
		
		CompositeOperation op = new CompositeOperation("Operation.HAddSelectedRevision");
		for (Iterator iter = tSelection.iterator(); iter.hasNext();) {
			SVNLogEntry item = (SVNLogEntry)iter.next();
			IRepositoryResource resource = SVNUtility.copyOf(this.repositoryResource);
			resource.setSelectedRevision(SVNRevision.fromNumber(item.revision));
			LocateResourceURLInHistoryOperation locateOp = new LocateResourceURLInHistoryOperation(new IRepositoryResource[] {resource}, true);
			op.add(locateOp);
			op.add(new AddRevisionLinkOperation(locateOp, item.revision), new IActionOperation[] {locateOp});
		}
		op.add(new SaveRepositoryLocationsOperation());
		op.add(new RefreshRepositoryLocationsOperation(new IRepositoryLocation [] {location}, true));
		UIMonitorUtility.doTaskScheduledDefault(op);
	}
	
	protected void updateTo(final Object item) {	    
		IResource []resources = new IResource[] {this.wcResource};
	    CompositeOperation op = new CompositeOperation("Operation.HUpdateTo");
		SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(resources);
		op.add(saveOp);
	    op.add(new UpdateOperation(resources, SVNRevision.fromNumber(this.history.getSelectedRevision()), true));
		op.add(new RestoreProjectMetaOperation(saveOp));
	    op.add(new RefreshResourcesOperation(resources));
	    op.add(new AbstractActionOperation("Operation.HRefreshView") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						HistoryViewImpl.this.refresh();
					}
				});
			}
			public ISchedulingRule getSchedulingRule() {
				return null;
			}
	    });
		UIMonitorUtility.doTaskNowWorkspaceModify(this.getSite().getShell(), op, false);
	}
	
	protected void handleDoubleClick(Object item, boolean doubleClick) {
		if (this.repositoryResource == null) {
			return;
		}
		if (item instanceof HistoryCategory) {
			if (this.history.getTreeViewer().getExpandedState(item)) {
				this.history.getTreeViewer().collapseToLevel(item, TreeViewer.ALL_LEVELS);
			}
			else {
				this.history.getTreeViewer().expandToLevel(item, TreeViewer.ALL_LEVELS);
			}
			return;
		}
		boolean isCompareAllowed = 
			(CoreExtensionsManager.instance().getSVNConnectorFactory().getSupportedFeatures() & ISVNConnectorFactory.OptionalFeatures.COMPARE_FOLDERS) != 0 ||
			HistoryViewImpl.this.repositoryResource instanceof IRepositoryFile;
		if ((this.options & HistoryViewImpl.COMPARE_MODE) != 0 && doubleClick && isCompareAllowed) {
			this.compareWithCurrent(item);
		}
		else if (!(this.repositoryResource instanceof IRepositoryContainer)) {
			UIMonitorUtility.doTaskScheduledActive(new OpenRemoteFileOperation(new IRepositoryFile[] {(IRepositoryFile)this.getResourceForSelectedRevision(item)}, OpenRemoteFileOperation.OPEN_DEFAULT));
		}
	}
	
	protected void compareWithCurrent(Object item) {
		IRepositoryResource resource = this.getResourceForSelectedRevision(item);
		if (this.wcResource != null || this.compareWith != null) {
			ILocalResource local = this.compareWith == null ? SVNRemoteStorage.instance().asLocalResource(this.wcResource) : this.compareWith;
			UIMonitorUtility.doTaskScheduledActive(new CompareResourcesOperation(local, resource));
		}
		else {
			UIMonitorUtility.doTaskScheduledActive(new CompareRepositoryResourcesOperation(this.getResourceForHeadRevision(), resource));
		}
	}
	
	protected void createUnifiedDiff(IStructuredSelection selection) {
		CreatePatchWizard wizard = new CreatePatchWizard(this.repositoryResource.getName());
		WizardDialog dialog = new WizardDialog(this.getSite().getShell(), wizard);
		if (dialog.open() == DefaultDialog.OK) {
			
			Object []selected = selection.toArray();
			IRepositoryResource left = this.getResourceForSelectedRevision(selected[0]);
			IRepositoryResource right = null;
			if (selection.size() == 1) {
				long revNum = ((SVNRevision.Number)left.getSelectedRevision()).getNumber() - 1;
				right =
					left instanceof IRepositoryFile ? 
					(IRepositoryResource)((IRepositoryRoot)left.getRoot()).asRepositoryFile(left.getUrl(), false) : 
					((IRepositoryRoot)left.getRoot()).asRepositoryContainer(left.getUrl(), false);
					right.setSelectedRevision(SVNRevision.fromNumber(revNum));
					right.setPegRevision(left.getPegRevision());
			}
			else {
				right = this.getResourceForSelectedRevision(selected[1]);
			}
			try {
				if (left.getRevision() > right.getRevision()) {
					IRepositoryResource tmp = right;
					right = left;
					left = tmp;
				}
				UIMonitorUtility.doTaskScheduledDefault(this.getSite().getPart(), CreatePatchAction.getCreatePatchOperation(left, right, wizard));
			}
			catch (SVNConnectorException ex) {
				UILoggedOperation.reportError(SVNTeamPlugin.instance().getResource("Operation.CreatePatchRemote"), ex);
			}
		}
	}
	
	protected void handleCopy() {
		String historyText = this.history.getSelectedMessagesAsString();
		Clipboard clipboard = new Clipboard(UIMonitorUtility.getDisplay());
		try {
			clipboard.setContents(new Object[] {historyText}, new Transfer[] {TextTransfer.getInstance()});
		}
		finally {
			clipboard.dispose();
		}
	}
	
	protected IRepositoryResource getResourceForSelectedRevision(Object item) {
		long revNum = ((SVNLogEntry)item).revision;
		IRepositoryResource res =
			this.repositoryResource instanceof IRepositoryFile ? 
			(IRepositoryResource)((IRepositoryRoot)this.repositoryResource.getRoot()).asRepositoryFile(this.repositoryResource.getUrl(), false) : 
			((IRepositoryRoot)this.repositoryResource.getRoot()).asRepositoryContainer(this.repositoryResource.getUrl(), false);
		res.setSelectedRevision(SVNRevision.fromNumber(revNum));
		res.setPegRevision(this.repositoryResource.getPegRevision());
		return res;
	}
	
	protected IRepositoryResource getResourceForHeadRevision() {
		IRepositoryResource res =
			this.repositoryResource instanceof IRepositoryFile ? 
			(IRepositoryResource)((IRepositoryRoot)this.repositoryResource.getRoot()).asRepositoryFile(this.repositoryResource.getUrl(), false) : 
			((IRepositoryRoot)this.repositoryResource.getRoot()).asRepositoryContainer(this.repositoryResource.getUrl(), false);
		res.setSelectedRevision(SVNRevision.HEAD);
		res.setPegRevision(SVNRevision.HEAD);
		return res;
	}
	
	protected String[] getSelectedAuthors() {
		List authors = new ArrayList();
		if (this.logMessages != null) {
			for (int i = 0; i < logMessages.length; i++) {
				String current = logMessages[i].author;
				if (current != null && !authors.contains(current)) {
					authors.add(current);
				}
			}
		}
		return (String[])authors.toArray(new String[authors.size()]);
	}
	
	protected boolean quickFilter() {
	    boolean okPressed = false;
		HistoryFilterPanel panel = new HistoryFilterPanel(this.filterByAuthor, this.filterByComment, this.getSelectedAuthors(), this.isCommentFilterEnabled);
	    DefaultDialog dialog = new DefaultDialog(this.getSite().getShell(), panel);
	    if (dialog.open() == 0) {
	    	okPressed = true;
	        this.filterByAuthor = panel.getAuthor(); 
	        this.filterByComment = panel.getComment();
	        this.isCommentFilterEnabled = panel.isCommentFilterEnabled();
	    }
	    return okPressed;
	}
	
	protected void clearFilter() {
	    this.filterByAuthor = ""; 
	    this.filterByComment = "";
        this.isCommentFilterEnabled = false;
	}
	
	protected boolean isFilterEnabled() {
	    return this.filterByAuthor.length() > 0 || this.isCommentFilterEnabled; 
	}
	
	protected SVNLogEntry[] filterMessages(SVNLogEntry[] msgs) {
		ArrayList filteredMessages = new ArrayList();
	    for (int i = 0; i < msgs.length; i++) {
			String author = msgs[i].author;
			String message = msgs[i].message;
			StringMatcher authorMatcher = new StringMatcher(this.filterByAuthor);
			StringMatcher commentMatcher = new StringMatcher(this.filterByComment);
			if ((this.filterByAuthor.length() > 0 ? authorMatcher.match(author) : true) 
					&& (this.isCommentFilterEnabled ? commentMatcher.match(message) : true)) {
				filteredMessages.add(msgs[i]);
			}
	    }
	    SVNLogEntry []result = (SVNLogEntry [])filteredMessages.toArray(new SVNLogEntry[filteredMessages.size()]);
	    return result.length > 0 ? result : null;	    
	}
	
	protected Action getFilterAction() {
	    this.filterAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.QuickFilter")) {
	        public void run() {
	        	if (HistoryViewImpl.this.quickFilter()) {
	        		HistoryViewImpl.this.showHistoryImpl(null, false);
	        	}
	        }
	    };
	    this.filterAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.QuickFilter"));
	    this.filterAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/filter.gif"));
	    return this.filterAction;
	}
	
	protected Action getClearFilterAction() {
		this.clearFilterAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.ClearFilter")) {
	        public void run() {
	        	HistoryViewImpl.this.clearFilter();
	        	HistoryViewImpl.this.showHistoryImpl(null, false);
	        }
	    };
	    this.clearFilterAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ClearFilter"));
	    this.clearFilterAction.setDisabledImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear.gif"));
	    this.clearFilterAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/clear_filter.gif"));
	    return this.clearFilterAction;
	}
	
	protected Action getCompareModeAction() {
		this.compareModeAction = new Action (SVNTeamUIPlugin.instance().getResource("HistoryView.CompareMode"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.options ^= HistoryViewImpl.COMPARE_MODE;
	            IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
	            SVNTeamPreferences.setHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE, (HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	            HistoryViewImpl.this.compareModeDropDownAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	        }
	    };
	    this.compareModeAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.CompareMode"));
	    this.compareModeAction.setDisabledImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/compare_mode_disabled.gif"));
	    this.compareModeAction.setHoverImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/compare_mode.gif"));
	    
	    IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
	    this.options = 
	    	SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_COMPARE_MODE) ?
			(this.options | HistoryViewImpl.COMPARE_MODE) :
			(this.options & ~HistoryViewImpl.COMPARE_MODE);
	    this.compareModeAction.setChecked((this.options & HistoryViewImpl.COMPARE_MODE) != 0);
	    
	    return this.compareModeAction;
	}
	
	protected void addPage(SVNLogEntry[] newMessages) {
		if (this.logMessages == null) {
			this.logMessages = newMessages;
		}
		else {
			List oldList = new ArrayList(Arrays.asList(this.logMessages));
			List newList = Arrays.asList(newMessages);
			if (newList.size() > 1) {
				newList = newList.subList(1, newList.size());
				oldList.addAll(newList);
			}		
			this.logMessages = (SVNLogEntry [])oldList.toArray(new SVNLogEntry[oldList.size()]);		
		}
	}
	
	protected Action getStopOnCopyAction() {
		this.stopOnCopyAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.StopOnCopy"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.refresh();
	        	HistoryViewImpl.this.options = this.isChecked() ? (HistoryViewImpl.this.options | HistoryViewImpl.STOP_ON_COPY) : (HistoryViewImpl.this.options & ~HistoryViewImpl.STOP_ON_COPY);
	        	HistoryViewImpl.this.stopOnCopyDropDownAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
	        }
	    };
	    this.stopOnCopyAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.StopOnCopy"));
	    this.stopOnCopyAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/stop_on_copy.gif"));
	    return this.stopOnCopyAction;		
	}
	
	protected Action getGroupByDateAction() {
		this.groupByDateAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.GroupByDate"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.options = this.isChecked() ? (HistoryViewImpl.this.options | HistoryViewImpl.GROUP_BY_DATE) : (HistoryViewImpl.this.options & ~HistoryViewImpl.GROUP_BY_DATE);
	        	HistoryViewImpl.this.groupByDateDropDownAction.setChecked((HistoryViewImpl.this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
	        	HistoryViewImpl.this.history.setGroupByDate((HistoryViewImpl.this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
	        	HistoryViewImpl.this.history.setTableInput();
				IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
				int type = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
				SVNTeamPreferences.setHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME, type == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE : SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
	        }
	    };
	    this.groupByDateAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.GroupByDate"));
	    this.groupByDateAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/group_by_date.gif"));
	    return this.groupByDateAction;		
	}
	
	protected Action getHideUnrelatedAction() {
		this.hideUnrelatedAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.HideUnrelatedPaths"), IAction.AS_CHECK_BOX) {
	        public void run() {
	        	HistoryViewImpl.this.history.setShowRelatedPathsOnly(this.isChecked());
	        	HistoryViewImpl.this.hideUnrelatedDropDownAction.setChecked(this.isChecked());
	        	HistoryViewImpl.this.options = this.isChecked() ? (HistoryViewImpl.this.options | HistoryViewImpl.HIDE_UNRELATED) : (HistoryViewImpl.this.options & ~HistoryViewImpl.HIDE_UNRELATED);
	        }
	    };
	    this.hideUnrelatedAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.HideUnrelatedPaths"));
	    this.hideUnrelatedAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/hide_unrelated.gif"));
	    return this.hideUnrelatedAction;		
	}
	
	protected Action getPagingAction() {
		this.getNextPageAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.GetNextPage")) {
	        public void run() {
	        	GetLogMessagesOperation msgOp = new GetLogMessagesOperation(HistoryViewImpl.this.repositoryResource, HistoryViewImpl.this.stopOnCopyAction.isChecked());
	        	msgOp.setLimit(HistoryViewImpl.this.limit + 1);
	    		if (HistoryViewImpl.this.logMessages != null) {
	    			SVNLogEntry lm = HistoryViewImpl.this.logMessages[HistoryViewImpl.this.logMessages.length - 1];
	    			msgOp.setSelectedRevision(SVNRevision.fromNumber(lm.revision));
	    		}
	    		HistoryViewImpl.this.showHistoryImpl(msgOp, false);
	        }
	    };
	    String msg = this.limit > 0 ? SVNTeamUIPlugin.instance().getResource("HistoryView.ShowNextX", new String[] {String.valueOf(this.limit)}) : SVNTeamUIPlugin.instance().getResource("HistoryView.ShowNextPage");
	    this.getNextPageAction.setToolTipText(msg);
	    this.getNextPageAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging.gif"));
	    return this.getNextPageAction;
	}
	
	protected Action getPagingAllAction() {
		this.getAllPagesAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowAll")) {
	        public void run() {
	        	GetLogMessagesOperation msgOp = new GetLogMessagesOperation(HistoryViewImpl.this.repositoryResource, HistoryViewImpl.this.stopOnCopyAction.isChecked());
	    		msgOp.setLimit(0);
	    		if (HistoryViewImpl.this.logMessages != null) {
	    			SVNLogEntry lm = HistoryViewImpl.this.logMessages[HistoryViewImpl.this.logMessages.length - 1];
	    			msgOp.setSelectedRevision(SVNRevision.fromNumber(lm.revision));
	    		}
	    		HistoryViewImpl.this.showHistoryImpl(msgOp, false);	        	
	        }
	    };
	    this.getAllPagesAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.ShowAll"));
	    this.getAllPagesAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history/paging_all.gif"));
	    return this.getAllPagesAction;		
	}
	
	public Action getRefreshAction() {
	    Action refreshAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Refresh")) {
			public void run() {
				HistoryViewImpl.this.refresh();
			}
		};
        refreshAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif"));
        refreshAction.setToolTipText(SVNTeamUIPlugin.instance().getResource("HistoryView.Refresh"));
        return refreshAction;
	}
	
	protected void showToolBar() {
	    IActionBars actionBars = this.getActionBars();

	    IToolBarManager tbm = actionBars.getToolBarManager();
	    tbm.add(new Separator());
	    tbm.add(this.getGroupByDateAction());
        tbm.add(new Separator());
        tbm.add(this.getHideUnrelatedAction());           
        tbm.add(this.getStopOnCopyAction());           
        tbm.add(new Separator());
        tbm.add(this.getFilterAction());           
        tbm.add(this.getClearFilterAction());
        tbm.add(new Separator());
        tbm.add(this.getPagingAction());
        tbm.add(this.getPagingAllAction());
        tbm.add(this.getCompareModeAction());
                
        tbm.update(true);
	}
	
    protected void refreshOptionButtons() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		boolean showMultiline = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME);
		boolean showAffected = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME);
		boolean hierarchicalAffectedView = SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT);
		int groupingType = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME);
		
		this.showCommentViewerAction.setChecked(showMultiline);
        this.history.setCommentViewerVisible(showMultiline);	          

        this.showAffectedPathsViewerAction.setChecked(showAffected);
        this.history.setAffectedPathsViewerVisible(showAffected);
        
        this.hideUnrelatedDropDownAction.setChecked((this.options & HistoryViewImpl.HIDE_UNRELATED) != 0);
        this.hideUnrelatedAction.setChecked((this.options & HistoryViewImpl.HIDE_UNRELATED) != 0);
        this.stopOnCopyDropDownAction.setChecked((this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
        this.stopOnCopyAction.setChecked((this.options & HistoryViewImpl.STOP_ON_COPY) != 0);
        this.options |= groupingType == SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE ? HistoryViewImpl.GROUP_BY_DATE : 0;
        this.groupByDateAction.setChecked((this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
        this.groupByDateDropDownAction.setChecked((this.options & HistoryViewImpl.GROUP_BY_DATE) != 0);
        this.compareModeDropDownAction.setChecked((this.options & HistoryViewImpl.COMPARE_MODE) != 0);
        this.compareModeAction.setChecked((this.options & HistoryViewImpl.COMPARE_MODE) != 0);
        
        this.flatAction.setChecked(!hierarchicalAffectedView);
        this.hierarchicalAction.setChecked(hierarchicalAffectedView);
        this.flatAction.setEnabled(showAffected);
        this.hierarchicalAction.setEnabled(showAffected);
        
        this.history.setResourceTreeVisible(hierarchicalAffectedView);

        if (SVNTeamPreferences.getHistoryBoolean(store, SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME)) {
    	    this.limit = SVNTeamPreferences.getHistoryInt(store, SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME);
        	this.getNextPageAction.setToolTipText("Show Next " + this.limit);
        	this.options |= HistoryViewImpl.PAGING_ENABLED;
        }
        else {
        	this.limit = 0;
    	    this.getNextPageAction.setToolTipText("Show Next Page");
    	    this.options &= ~HistoryViewImpl.PAGING_ENABLED;
    	}
        this.setPagingEnabled();
    }
    
    protected void setPagingEnabled() {
	    this.filterAction.setEnabled(this.repositoryResource != null && this.logMessages != null);
	    this.clearFilterAction.setEnabled(this.isFilterEnabled());
	    this.getNextPageAction.setEnabled(this.pagingEnabled & ((this.options & HistoryViewImpl.PAGING_ENABLED) != 0));
	    this.getAllPagesAction.setEnabled(this.pagingEnabled & ((this.options & HistoryViewImpl.PAGING_ENABLED) != 0));
    }
    
    protected void setPagingDisabled() {
	    this.filterAction.setEnabled(false);
	    this.clearFilterAction.setEnabled(false);
	    this.getNextPageAction.setEnabled(false);
	    this.getAllPagesAction.setEnabled(false);
    }
    
    protected void disconnectView() {
    	UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				HistoryViewImpl.this.showHistory((IResource)null, false);
			}
		});
    }
	
	protected void addMenuItem(MenuManager menuManager, String label, final AbstractRepositoryTeamAction action) {
		IStructuredSelection tSelection = (IStructuredSelection)this.history.getTreeViewer().getSelection();
		Action wrapper = new Action(label) {
			public void run() {
				action.run(this);
			}
		};
		IStructuredSelection resourceSelection;
		if (tSelection.size() == 1 && (tSelection.getFirstElement() instanceof SVNLogEntry)) {
			resourceSelection = new StructuredSelection(new RepositoryFile(null, this.getResourceForSelectedRevision(tSelection.getFirstElement())));
		}
		else {
			resourceSelection = new StructuredSelection(StructuredSelection.EMPTY);
		}
		action.selectionChanged(wrapper, resourceSelection);
		menuManager.add(wrapper);
	}
	
	protected boolean isIgnoreReplaceWarning() {
		if (FileUtility.checkForResourcesPresenceRecursive(new IResource[] {this.wcResource}, IStateFilter.SF_COMMITABLE)) {
			ReplaceWarningDialog dialog = new ReplaceWarningDialog(this.getSite().getShell());
			if (dialog.open() != 0) {
				return false;
			}
		}
		return true;
	}
	
	public String getResourceLabel() {
		String viewDescription = SVNTeamUIPlugin.instance().getResource("HistoryView.Name");
		String resourceName;
		if (this.wcResource != null) {
		    String path = this.wcResource.getFullPath().toString();
		    if (path.startsWith("/")) {
		    	path = path.substring(1);
		    }
			resourceName = SVNTeamUIPlugin.instance().getResource("SVNView.ResourceSelected", new String[] {viewDescription, path});
		}
		else if (this.repositoryResource != null) {
			resourceName = SVNTeamUIPlugin.instance().getResource("SVNView.ResourceSelected", new String[] {viewDescription, this.repositoryResource.getUrl()});
		}
		else {
			resourceName = SVNTeamUIPlugin.instance().getResource("SVNView.ResourceNotSelected");
		}
		return resourceName;
	}

}
