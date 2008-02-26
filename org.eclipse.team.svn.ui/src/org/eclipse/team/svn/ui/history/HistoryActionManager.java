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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryInfo;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNLogPath;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.connector.SVNEntry.Kind;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.ExportOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.operation.remote.PreparedBranchTagOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRevisionLinkOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.remote.BranchTagAction;
import org.eclipse.team.svn.ui.action.remote.CreatePatchAction;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.history.data.AffectedPathsNode;
import org.eclipse.team.svn.ui.history.data.SVNChangedPathData;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;
import org.eclipse.team.svn.ui.operation.OpenRemoteFileOperation;
import org.eclipse.team.svn.ui.operation.RefreshRemoteResourcesOperation;
import org.eclipse.team.svn.ui.operation.RefreshRepositoryLocationsOperation;
import org.eclipse.team.svn.ui.operation.RemoteShowAnnotationOperation;
import org.eclipse.team.svn.ui.operation.ShowHistoryViewOperation;
import org.eclipse.team.svn.ui.operation.ShowPropertiesOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.wizard.CreatePatchWizard;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Provides actions for affected paths window
 * 
 * @author Alexander Gurov
 */
public class HistoryActionManager {
	public interface IControlActionManager {
		public void installDefaultAction(StructuredViewer viewer);
		public void installMenuActions(StructuredViewer viewer, IWorkbenchPartSite site);
	}
	
	protected IRepositoryResource repositoryResource;
	protected long selectedRevision;
	
	public final IControlActionManager affectedTableManager;
	public final IControlActionManager affectedTreeManager;
	
	public HistoryActionManager() {
		this.affectedTableManager = new AffectedTableActionManager();
		this.affectedTreeManager = new AffectedTreeActionManager();
	}

	public IRepositoryResource getRepositoryResource() {
		return this.repositoryResource;
	}

	public void setRepositoryResource(IRepositoryResource repositoryResource) {
		this.repositoryResource = repositoryResource;
	}
	
	public long getSelectedRevision() {
		return this.selectedRevision;
	}

	public void setSelectedRevision(long selectedRevision) {
		this.selectedRevision = selectedRevision;
	}
	
	protected class AffectedTableActionManager implements IControlActionManager {
		public void installDefaultAction(StructuredViewer viewer) {
			//add double click listener for the table viewer
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent e) {
					ISelection selection = e.getSelection();
					if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
						IStructuredSelection structured = (IStructuredSelection)selection;
						HistoryActionManager.this.openRemoteResource((SVNChangedPathData)structured.getFirstElement(), OpenRemoteFileOperation.OPEN_DEFAULT, null);
					}
				}
			});
		}

		public void installMenuActions(final StructuredViewer viewer, IWorkbenchPartSite site) {
			MenuManager menuMgr = new MenuManager();
			Menu menu = menuMgr.createContextMenu(viewer.getControl());
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
					final IStructuredSelection affectedTableSelection = (IStructuredSelection)viewer.getSelection();
					if (affectedTableSelection.size() == 0) {
						return;
					}
					final SVNChangedPathData firstData = (SVNChangedPathData)affectedTableSelection.getFirstElement();
					Action tAction = null;
					
					IEditorRegistry editorRegistry = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry();
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.Open")) {
						public void run() {
							HistoryActionManager.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_DEFAULT, null);
						}
					});
					String name = firstData.resourceName;
					tAction.setImageDescriptor(editorRegistry.getImageDescriptor(name));
					tAction.setEnabled(affectedTableSelection.size() == 1);
					
					//FIXME: "Open with" submenu shouldn't be hardcoded after reworking of
					//       the HistoryView. Should be made like the RepositoriesView menu.
					MenuManager sub = new MenuManager(SVNTeamUIPlugin.instance().getResource("HistoryView.OpenWith"), "historyOpenWithMenu");
					sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
					
					sub.add(new Separator("nonDefaultTextEditors"));
					IEditorDescriptor[] editors = editorRegistry.getEditors(name);
					for (int i = 0; i < editors.length; i++) {
						final String id = editors[i].getId();
	    				if (!id.equals(EditorsUI.DEFAULT_TEXT_EDITOR_ID)) {
	    					sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource(editors[i].getLabel())) {
	    						public void run() {
	    							HistoryActionManager.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_SPECIFIED, id);
	    						}
	    					});
	    					tAction.setImageDescriptor(editors[i].getImageDescriptor());
	    					tAction.setEnabled(affectedTableSelection.size() == 1);
	    				}
	    			}
						
					sub.add(new Separator("variousEditors"));
					IEditorDescriptor descriptor = null;
					sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.TextEditor")) {
						public void run() {
							HistoryActionManager.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_SPECIFIED, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
						}
					});
					descriptor = editorRegistry.findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);
					tAction.setImageDescriptor(descriptor.getImageDescriptor());
					tAction.setEnabled(affectedTableSelection.size() == 1);
					sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.SystemEditor")) {
						public void run() {
							HistoryActionManager.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_EXTERNAL, null);
						}
					});
					if (editorRegistry.isSystemExternalEditorAvailable(name)) {
						tAction.setImageDescriptor(editorRegistry.getSystemExternalEditorImageDescriptor(name));
						tAction.setEnabled(affectedTableSelection.size() == 1);
					}
					else {
						tAction.setEnabled(false);
					}
					sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.InplaceEditor")) {
						public void run() {
							HistoryActionManager.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_INPLACE, null);
						}
					});
					if (editorRegistry.isSystemInPlaceEditorAvailable(name)) {
						tAction.setImageDescriptor(editorRegistry.getSystemExternalEditorImageDescriptor(name));
						tAction.setEnabled(affectedTableSelection.size() == 1);
					}
					else {
						tAction.setEnabled(false);
					}
					sub.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.DefaultEditor")) {
						public void run() {
							HistoryActionManager.this.openRemoteResource(firstData, OpenRemoteFileOperation.OPEN_DEFAULT, null);
						}
					});
					tAction.setImageDescriptor(editorRegistry.getImageDescriptor(name));
					tAction.setEnabled(affectedTableSelection.size() == 1);
					
		        	manager.add(sub);
		        	manager.add(new Separator());
		        	
					boolean isPreviousExists = false;
					if (affectedTableSelection.size() == 1) {
						//FIXME copied resources also must be handled
//						isPreviousExists = !(firstData.action == SVNLogPath.ChangeType.ADDED && firstData.copiedFromRevision == SVNRevision.INVALID_REVISION_NUMBER);
						isPreviousExists = firstData.action == SVNLogPath.ChangeType.MODIFIED || firstData.action == SVNLogPath.ChangeType.REPLACED;
					}
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.compareWithPreviousRevision(provider, provider);
						}
					});
					tAction.setEnabled(isPreviousExists);
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label")) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.createPatchToPrevious(provider, provider);
						}
					});
					tAction.setEnabled(isPreviousExists);
					manager.add(new Separator());
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label")) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.showProperties(provider, provider);
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
					tAction.setEnabled(affectedTableSelection.size() == 1);
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowResourceHistoryCommand.label")) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.showHistory(provider, provider);
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif"));
					tAction.setEnabled(affectedTableSelection.size() == 1);
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowAnnotationCommand.label")) {
						public void run() {
							HistoryActionManager.this.showAnnotation(firstData);
						}
					});
					tAction.setEnabled(affectedTableSelection.size() == 1);
					manager.add(new Separator());
					String branchFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(HistoryActionManager.this.selectedRevision)});
					String tagFrom = SVNTeamUIPlugin.instance().getResource("HistoryView.TagFrom", new String [] {String.valueOf(HistoryActionManager.this.selectedRevision)});
					manager.add(tAction = new Action(branchFrom) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.createBranchTag(viewer.getControl().getShell(), provider, provider, BranchTagAction.BRANCH_ACTION);
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
					tAction.setEnabled(affectedTableSelection.size() > 0);
					manager.add(tAction = new Action(tagFrom) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.createBranchTag(viewer.getControl().getShell(), provider, provider, BranchTagAction.TAG_ACTION);
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif"));
					tAction.setEnabled(affectedTableSelection.size() > 0);
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddRevisionLinkAction.label")) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.addRevisionLink(provider, provider);
						}
					});
					tAction.setEnabled(affectedTableSelection.size() > 0);
					manager.add(new Separator());
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ExportCommand.label")) {
						public void run() {
							FromChangedPathDataProvider provider = new FromChangedPathDataProvider(firstData, false);
							HistoryActionManager.this.doExport(viewer.getControl().getShell(), provider, provider);
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif"));
					tAction.setEnabled(affectedTableSelection.size() > 0);
				}
			});
			menuMgr.setRemoveAllWhenShown(true);
			viewer.getControl().setMenu(menu);
			site.registerContextMenu(menuMgr, viewer);
		}
	}
	
	protected class AffectedTreeActionManager implements IControlActionManager {
		public void installDefaultAction(StructuredViewer viewer) {
		}

		public void installMenuActions(final StructuredViewer viewer, IWorkbenchPartSite site) {
			MenuManager menuMgr = new MenuManager();
			Menu menu = menuMgr.createContextMenu(viewer.getControl());
	        menuMgr.addMenuListener(new IMenuListener() {
	            public void menuAboutToShow(IMenuManager manager) {
	                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	        		final IStructuredSelection affectedTableSelection = (IStructuredSelection)viewer.getSelection();
					if (affectedTableSelection.size() == 0) {
						return;
					}
	        		final AffectedPathsNode node = (AffectedPathsNode)affectedTableSelection.getFirstElement();
	        		Action tAction = null;
	        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.CompareWithPreviousRevision")) {
						public void run() {
							FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.compareWithPreviousRevision(provider, provider);
						}
	        		});
	        		boolean isCompareFoldersAllowed = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
	        		tAction.setEnabled(isCompareFoldersAllowed && HistoryActionManager.this.selectedRevision != 0 && affectedTableSelection.size() == 1 && (node.getStatus() == '\0' || node.getStatus() == SVNLogPath.ChangeType.MODIFIED));
	        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("CreatePatchCommand.label")) {
						public void run() {					
							FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.createPatchToPrevious(provider, provider);
						}
					});
	        		tAction.setEnabled(affectedTableSelection.size() == 1 && HistoryActionManager.this.selectedRevision != 0 && affectedTableSelection.size() == 1 && (node.getStatus() == '\0' || node.getStatus() == SVNLogPath.ChangeType.MODIFIED));
	        		manager.add(new Separator());
	        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowPropertiesAction.label")) {
						public void run() {
							FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.showProperties(provider, provider);
						}
	        		});
	        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/propertiesedit.gif"));
	        		tAction.setEnabled(HistoryActionManager.this.selectedRevision != 0 && affectedTableSelection.size() == 1 /*&& (node.getStatus() == null || node.getStatus().charAt(0) == 'M')*/);
	        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ShowResourceHistoryCommand.label")) {
						public void run() {
							FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.showHistory(provider, provider);
						}
	        		});
	        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/history.gif"));
	        		tAction.setEnabled(HistoryActionManager.this.selectedRevision != 0 && affectedTableSelection.size() == 1);
	        		manager.add(new Separator());
	        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.BranchFrom", new String [] {String.valueOf(HistoryActionManager.this.selectedRevision)})) {
	        			public void run() {
	        				FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.createBranchTag(viewer.getControl().getShell(), provider, provider, BranchTagAction.BRANCH_ACTION);
	        			}
	        		});
	        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/branch.gif"));
	        		tAction.setEnabled(affectedTableSelection.size() > 0);
	        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("HistoryView.TagFrom", new String [] {String.valueOf(HistoryActionManager.this.selectedRevision)})) {
	        			public void run() {
	        				FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.createBranchTag(viewer.getControl().getShell(), provider, provider, BranchTagAction.TAG_ACTION);
	        			}
	        		});
	        		tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/actions/tag.gif"));
	        		tAction.setEnabled(affectedTableSelection.size() > 0);
	        		manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("AddRevisionLinkAction.label")) {
						public void run() {
							FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.addRevisionLink(provider, provider);
						}
					});
					tAction.setEnabled(affectedTableSelection.size() > 0);
					manager.add(new Separator());
					manager.add(tAction = new Action(SVNTeamUIPlugin.instance().getResource("ExportCommand.label")) {
						public void run() {
							FromAffectedPathsNodeProvider provider = new FromAffectedPathsNodeProvider(node);
							HistoryActionManager.this.doExport(viewer.getControl().getShell(), provider, provider);
						}
					});
					tAction.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/export.gif"));
					tAction.setEnabled(affectedTableSelection.size() > 0);
	            }
	        });
	        menuMgr.setRemoveAllWhenShown(true);
	        viewer.getControl().setMenu(menu);
	        site.registerContextMenu(menuMgr, viewer);
		}
		
	}
	
	protected void createBranchTag(Shell shell, IActionOperation preOp, IRepositoryResourceProvider provider, int type) {
		ProgressMonitorUtility.doTaskExternal(preOp, new NullProgressMonitor());
		
		boolean respectProjectStructure = SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		
		IRepositoryResource []resources = provider.getRepositoryResources();
		PreparedBranchTagOperation op = BranchTagAction.getBranchTagOperation(resources, shell, type, respectProjectStructure);

		if (op != null) {
			CompositeOperation composite = new CompositeOperation(op.getId());
			composite.add(op);
			RefreshRemoteResourcesOperation refreshOp = new RefreshRemoteResourcesOperation(new IRepositoryResource[] {op.getDestination().getParent()});
			composite.add(refreshOp, new IActionOperation[] {op});
			UIMonitorUtility.doTaskScheduledDefault(op);
		}
	}
	
	protected void createPatchToPrevious(IActionOperation preOp, IRepositoryResourceProvider provider) {
		ProgressMonitorUtility.doTaskExternal(preOp, new NullProgressMonitor());
		IRepositoryResource current = provider.getRepositoryResources()[0];
		CreatePatchWizard wizard = new CreatePatchWizard(current.getName());
		WizardDialog dialog = new WizardDialog(UIMonitorUtility.getShell(), wizard);
		if (dialog.open() == DefaultDialog.OK) {
			IRepositoryResource previous = (current instanceof RepositoryFolder) ? current.asRepositoryContainer(current.getUrl(), false) : current.asRepositoryFile(current.getUrl(), false);
			previous.setSelectedRevision(SVNRevision.fromNumber(HistoryActionManager.this.selectedRevision - 1));
			previous.setPegRevision(SVNRevision.fromNumber(HistoryActionManager.this.selectedRevision));
			UIMonitorUtility.doTaskScheduledDefault(CreatePatchAction.getCreatePatchOperation(previous, current, wizard));
		}
	}
	
	protected void doExport(Shell shell, IActionOperation preOp, IRepositoryResourceProvider provider) {
		DirectoryDialog fileDialog = new DirectoryDialog(shell);
		fileDialog.setText(SVNTeamUIPlugin.instance().getResource("ExportPanel.ExportFolder"));
		fileDialog.setMessage(SVNTeamUIPlugin.instance().getResource("ExportPanel.ExportFolder.Msg"));
		String path = fileDialog.open();
		if (path != null) {
			ExportOperation mainOp = new ExportOperation(provider, path);
			CompositeOperation op = new CompositeOperation(mainOp.getId());
			op.add(preOp);
			op.add(mainOp, new IActionOperation[] {preOp});
	    	UIMonitorUtility.doTaskScheduledDefault(op);
		}		
	}
	
	protected void openRemoteResource(SVNChangedPathData selectedPath, int openType, String openWith) {
		FromChangedPathDataProvider provider = new FromChangedPathDataProvider(selectedPath, true);
		OpenRemoteFileOperation openOp = new OpenRemoteFileOperation(provider, openType, openWith);
		
		CompositeOperation composite = new CompositeOperation(openOp.getId(), true);
		composite.add(provider);
		composite.add(openOp, new IActionOperation[] {provider});
		UIMonitorUtility.doTaskScheduledActive(composite);
	}
	
	protected void showAnnotation(SVNChangedPathData selectedPath) {
		FromChangedPathDataProvider provider = new FromChangedPathDataProvider(selectedPath, true);
		RemoteShowAnnotationOperation mainOp = new RemoteShowAnnotationOperation(provider);
		CompositeOperation op = new CompositeOperation(mainOp.getId(), true);
		op.add(provider);
		op.add(mainOp, new IActionOperation[] {provider});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected void showHistory(IActionOperation preOp, IRepositoryResourceProvider provider) {
		ShowHistoryViewOperation mainOp = new ShowHistoryViewOperation(provider, 0, 0);
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(preOp);
		op.add(mainOp, new IActionOperation[] {preOp});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected void showProperties(IActionOperation preOp, IRepositoryResourceProvider provider) {
		IResourcePropertyProvider propertyProvider = new GetRemotePropertiesOperation(provider);
		ShowPropertiesOperation showOp = new ShowPropertiesOperation(UIMonitorUtility.getActivePage(), provider, propertyProvider);
		CompositeOperation op = new CompositeOperation(showOp.getId());
		op.add(preOp);
		op.add(propertyProvider, new IActionOperation[] {preOp});
		op.add(showOp, new IActionOperation[] {preOp, propertyProvider});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected void addRevisionLink(IActionOperation preOp, IRepositoryResourceProvider provider) {
		CompositeOperation op = new CompositeOperation("Operation.HAddSelectedRevision");
		op.add(preOp);
		IActionOperation []condition = new IActionOperation[] {preOp};
		op.add(new AddRevisionLinkOperation(provider, this.selectedRevision), condition);
		op.add(new SaveRepositoryLocationsOperation(), condition);
		op.add(new RefreshRepositoryLocationsOperation(new IRepositoryLocation [] {this.repositoryResource.getRepositoryLocation()}, true), condition);
		UIMonitorUtility.doTaskScheduledDefault(op);
	}
	
	protected void compareWithPreviousRevision(IActionOperation preOp, final IRepositoryResourceProvider provider) {
		CompareRepositoryResourcesOperation mainOp = new CompareRepositoryResourcesOperation(new IRepositoryResourceProvider() {
			public IRepositoryResource[] getRepositoryResources() {
				IRepositoryResource next = provider.getRepositoryResources()[0];
				IRepositoryResource prev = SVNUtility.copyOf(next);
				prev.setSelectedRevision(SVNRevision.fromNumber(((SVNRevision.Number)next.getSelectedRevision()).getNumber() - 1));
				return new IRepositoryResource[] {prev, next};
			}
		});
		CompositeOperation op = new CompositeOperation(mainOp.getId());
		op.add(preOp);
		op.add(mainOp, new IActionOperation[] {preOp});
		UIMonitorUtility.doTaskScheduledActive(op);
	}
	
	protected class FromAffectedPathsNodeProvider extends AbstractActionOperation implements IRepositoryResourceProvider {
		protected AffectedPathsNode affectedPathsItem;
		protected IRepositoryResource returnResource;
		
		public FromAffectedPathsNodeProvider(AffectedPathsNode affectedPathsItem) {
			super("Operation.GetRepositoryResource");
			this.affectedPathsItem = affectedPathsItem;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			String rootUrl = HistoryActionManager.this.repositoryResource.getRepositoryLocation().getRepositoryRootUrl();
			String path = this.affectedPathsItem.getFullPath();

			String resourceUrl = rootUrl + (path.startsWith("/") ? "" : "/") + path;
			SVNRevision revision = SVNRevision.fromNumber(HistoryActionManager.this.selectedRevision);
			
			IRepositoryLocation location = HistoryActionManager.this.repositoryResource.getRepositoryLocation();
			this.returnResource = location.asRepositoryContainer(resourceUrl, false);
			this.returnResource.setSelectedRevision(revision);
			this.returnResource.setPegRevision(revision);
		}
		public IRepositoryResource[] getRepositoryResources() {
			return new IRepositoryResource[] {this.returnResource};
		}
	}
	
	protected class FromChangedPathDataProvider extends AbstractActionOperation implements IRepositoryResourceProvider {
		protected IRepositoryResource []repositoryResources;
		protected SVNChangedPathData affectedPathsItem;
		protected boolean filesOnly;
		
		public FromChangedPathDataProvider(SVNChangedPathData affectedPathsItem, boolean filesOnly) {
			super("Operation.GetRepositoryResource");
			this.affectedPathsItem = affectedPathsItem;
			this.filesOnly = filesOnly;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			String affectedPath = this.affectedPathsItem.getFullResourcePath();
			String rootUrl = HistoryActionManager.this.repositoryResource.getRepositoryLocation().getRepositoryRootUrl();
			String resourceUrl = rootUrl + "/" + affectedPath;
			SVNRevision revision = SVNRevision.fromNumber(this.affectedPathsItem.action == SVNLogPath.ChangeType.DELETED ? HistoryActionManager.this.selectedRevision - 1 : HistoryActionManager.this.selectedRevision);
			
			SVNEntryInfo info = null;
			IRepositoryLocation location = HistoryActionManager.this.repositoryResource.getRepositoryLocation();
			ISVNConnector proxy = location.acquireSVNProxy();
			try {
				SVNEntryInfo []infos = SVNUtility.info(proxy, new SVNEntryRevisionReference(SVNUtility.encodeURL(resourceUrl), revision, revision), Depth.EMPTY, new SVNProgressMonitor(this, monitor, null));
				if (infos == null || infos.length == 0) {
					return;
				}
				info = infos[0];
			}
			finally {
				location.releaseSVNProxy(proxy);
			}
			
			if (info.kind == Kind.DIR && this.filesOnly) {
				final String message = SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Open.Message", new String[] {SVNUtility.decodeURL(info.url)});
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						MessageDialog dialog = new MessageDialog(UIMonitorUtility.getDisplay().getActiveShell(), 
								SVNTeamUIPlugin.instance().getResource("AffectedPathsComposite.Open.Title"), 
								null, 
								message,
								MessageDialog.INFORMATION, 
								new String[] {IDialogConstants.OK_LABEL}, 
								0);
						dialog.open();								
					}
				});
				this.reportStatus(new Status(IStatus.WARNING, SVNTeamPlugin.NATURE_ID, IStatus.OK, message, null));
				return;					
			}
			this.repositoryResources = new IRepositoryResource[1];
			this.repositoryResources[0] = info.kind == Kind.FILE ? (IRepositoryResource)location.asRepositoryFile(resourceUrl, false) : location.asRepositoryContainer(resourceUrl, false);
			this.repositoryResources[0].setSelectedRevision(revision);
			this.repositoryResources[0].setPegRevision(revision);
		}
		
		public IRepositoryResource[] getRepositoryResources() {
			return this.repositoryResources;
		}
		
	}

}
