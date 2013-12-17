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
package org.eclipse.team.svn.revision.graph.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphPlugin;
import org.eclipse.team.svn.revision.graph.ShowRevisionGraphPanel;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCacheInfo;
import org.eclipse.team.svn.revision.graph.cache.RepositoryCachesManager;
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditorInput;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.revision.graph.preferences.SVNRevisionGraphPreferences;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/** 
 * Utility which builds revision graph operation 
 *     
 * @author Igor Burilo
 */
public class RevisionGraphUtility {

	protected final static String EDITOR_ID = "org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor";  //$NON-NLS-1$
	
	//may return null
	public static CompositeOperation getRevisionGraphOperation(IRepositoryResource[] resources) {
		CompositeOperation mainOp = new CompositeOperation("Operation_ShowRevisionGraph", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		
		Map<String, List<IRepositoryResource>> splittedResources = splitResources(resources);
		filterResources(splittedResources);
		if (splittedResources.isEmpty()) {
			return null;
		}
		
		//show dialog with options		
		final ShowRevisionGraphPanel panel = new ShowRevisionGraphPanel(resources.length == 1 ? resources[0] : null);
		DefaultDialog rDlg = new DefaultDialog(UIMonitorUtility.getShell(), panel);
		if (rDlg.open() != 0) {
			return null;
		}
		
		//traverse resources
		boolean isSkipFetchErrors = SVNRevisionGraphPreferences.getGraphBoolean(SVNRevisionGraphPlugin.instance().getPreferenceStore(), SVNRevisionGraphPreferences.GRAPH_SKIP_ERRORS);
		for (List<IRepositoryResource> reposResources : splittedResources.values()) {
			CompositeOperation reposOp = new CompositeOperation(mainOp.getId(), SVNRevisionGraphMessages.class);
			mainOp.add(reposOp);
					
			/*
			 * get any resource to check connection and fetch data
			 * 
			 * Notes:
			 *   resources with the same repository root may belong to different repository locations,
			 *   as a result they may have different credentials. But in case of revision graph it's applicable
			 *   
			 *   if several resources belong to the same repository root, then create cache data operation
			 *   is called only once and so when we (internally) track resources for which cache is opened
			 *   we track it only for one resource, i.e. we don't track it for other resources, but this
			 *   is applicable
			 */
			IRepositoryResource anyResource = reposResources.get(0);
			
			//check repository connection
			final CheckRepositoryConnectionOperation checkConnectionOp = new CheckRepositoryConnectionOperation(anyResource, panel.canIncludeMergeInfo(), true);
			reposOp.add(checkConnectionOp);
			
			//create cache
			CreateCacheDataOperation createCacheOp = new CreateCacheDataOperation(anyResource, false, checkConnectionOp, isSkipFetchErrors);
			reposOp.add(createCacheOp, new IActionOperation[]{checkConnectionOp});
						
			CompositeOperation resourceOp = new CompositeOperation(mainOp.getId(), SVNRevisionGraphMessages.class);
			reposOp.add(resourceOp, new IActionOperation[] {checkConnectionOp, createCacheOp});
			for (final IRepositoryResource resource : reposResources) {
				//create model
				final CreateRevisionGraphModelOperation createModelOp = new CreateRevisionGraphModelOperation(resource, createCacheOp);
				resourceOp.add(createModelOp, new IActionOperation[] {createCacheOp} );
				
				//add merge info
				AddMergeInfoOperation addMergeInfoOp = new AddMergeInfoOperation(createModelOp, checkConnectionOp);
				resourceOp.add(addMergeInfoOp, new IActionOperation[]{createModelOp});
				
				//visualize
				AbstractActionOperation showRevisionGraphOp = new AbstractActionOperation("Operation_ShowRevisionGraph", SVNRevisionGraphMessages.class) { //$NON-NLS-1$
					@Override
					protected void runImpl(IProgressMonitor monitor) throws Exception {
						UIMonitorUtility.getDisplay().syncExec(new Runnable() {
							public void run() {
								try {					
									Object modelObject;
									if (createModelOp.getModel() != null) {
										RevisionRootNode rootNode = new RevisionRootNode(resource, createModelOp.getModel(), createModelOp.getRepositoryCache());
										rootNode.simpleSetMode(!panel.isShowAllRevisions());
										rootNode.setIncludeMergeInfo(checkConnectionOp.getRepositoryConnectionInfo().isSupportMergeInfo);
										modelObject = rootNode;
									} else {
										modelObject = SVNRevisionGraphMessages.NoData;
									}						
									RevisionGraphEditorInput input = new RevisionGraphEditorInput(createModelOp.getResource(), modelObject);
									UIMonitorUtility.getActivePage().openEditor(input, RevisionGraphUtility.EDITOR_ID);														
								} catch (Exception e) {
									LoggedOperation.reportError(this.getClass().getName(), e);
								}						
							}			
						});	
					}
				};
				resourceOp.add(showRevisionGraphOp, new IActionOperation[]{createModelOp, addMergeInfoOp});	
			}			
		}		
		
		return mainOp;
	}
	
	protected static Map<String, List<IRepositoryResource>> splitResources(IRepositoryResource[] resources) {
		//split resources by repository url
		Map<String, List<IRepositoryResource>> splittedResources = new HashMap<String, List<IRepositoryResource>>();
		for (IRepositoryResource resource : resources) {
			String reposRoot = RepositoryCachesManager.getRepositoryRoot(resource);
			List<IRepositoryResource> resourcesList = splittedResources.get(reposRoot);
			if (resourcesList == null) {
				resourcesList = new ArrayList<IRepositoryResource>();
				splittedResources.put(reposRoot, resourcesList);
 			}

			resourcesList.add(resource);
		}
		return splittedResources;
	}
	
	protected static void filterResources(Map<String, List<IRepositoryResource>> resources) {		
		//check if cache is calculating now
		final List<String> caclulatingCaches = new ArrayList<String>();
		Iterator<Map.Entry<String, List<IRepositoryResource>>> iter = resources.entrySet().iterator();
		RepositoryCachesManager cachesManager = SVNRevisionGraphPlugin.instance().getRepositoryCachesManager();
		while (iter.hasNext()) {
			Map.Entry<String, List<IRepositoryResource>> entry = iter.next();
			//it's enough to check only first resource
			IRepositoryResource resource = entry.getValue().get(0);			
			RepositoryCacheInfo cacheInfo = cachesManager.getCache(resource);
			if (cacheInfo != null && cacheInfo.isCacheDataCalculating()) {
				caclulatingCaches.add(entry.getKey());
				iter.remove();				
			}		
		}
		//show calculating caches
		if (!caclulatingCaches.isEmpty()) {
			UIMonitorUtility.getDisplay().syncExec(new Runnable() {
				public void run() {		
					MessageDialog dlg = RevisionGraphUtility.getCacheCalculatingDialog(caclulatingCaches.toArray(new String[0]));
					dlg.open();
				}
			});
		}
	}
	
	public static MessageDialog getCacheCalculatingDialog(String reposRoot) {
		return getCacheCalculatingDialog(new String[] {reposRoot});
	}
	
	//dialog which says that cache is calculating now by another task
	public static MessageDialog getCacheCalculatingDialog(String[] reposRoots) {
		StringBuilder strRepos = new StringBuilder();
		for (int i = 0, n = reposRoots.length; i < n; i ++) {
			String reposRoot = reposRoots[i];
			strRepos.append(reposRoot);
			if (i != n - 1) {			
				strRepos.append(", "); //$NON-NLS-1$
			}
		}
		
		MessageDialog dlg = new MessageDialog(
				UIMonitorUtility.getShell(), 
				SVNRevisionGraphMessages.Dialog_GraphTitle,
				null, 
				SVNRevisionGraphMessages.format(SVNRevisionGraphMessages.CreateCacheDataOperation_DialogMessage, strRepos.toString()),
				MessageDialog.INFORMATION, 
				new String[] {IDialogConstants.OK_LABEL}, 
				0);
		return dlg;	
	}
}
