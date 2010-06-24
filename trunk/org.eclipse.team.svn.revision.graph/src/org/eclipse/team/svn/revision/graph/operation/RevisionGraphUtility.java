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

import java.io.IOException;

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
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditorInput;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
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
	public static CompositeOperation getRevisionGraphOperation(final IRepositoryResource resource) {
		CompositeOperation op = new CompositeOperation("Operation_ShowRevisionGraph", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		
		//check if cache is calculating now
		try {
			RepositoryCacheInfo cacheInfo = SVNRevisionGraphPlugin.instance().getRepositoryCachesManager().getCache(resource);
			if (cacheInfo.isCacheDataCalculating()) {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {		
						MessageDialog dlg = RevisionGraphUtility.getCacheCalculatingDialog();
						dlg.open();
					}
				});
				return null;
			}	
		} catch (IOException e) {
			LoggedOperation.reportError(RevisionGraphUtility.class.getName(), e);
			return null;
		}
		
		//show dialog with options		
		final ShowRevisionGraphPanel panel = new ShowRevisionGraphPanel();
		DefaultDialog rDlg = new DefaultDialog(UIMonitorUtility.getShell(), panel);
		if (rDlg.open() != 0) {
			return null;
		}
		
		//check repository connection
		final CheckRepositoryConnectionOperation checkConnectionOp = new CheckRepositoryConnectionOperation(resource, panel.canIncludeMergeInfo(), true);
		op.add(checkConnectionOp);				
		
		//create cache
		CreateCacheDataOperation createCacheOp = new CreateCacheDataOperation(resource, false, checkConnectionOp, panel.isSkipFetchErrors());
		op.add(createCacheOp, new IActionOperation[]{checkConnectionOp});
				
		//create model
		final CreateRevisionGraphModelOperation createModelOp = new CreateRevisionGraphModelOperation(resource, createCacheOp);
		op.add(createModelOp, new IActionOperation[] {createCacheOp} );
		
		//add merge info
		AddMergeInfoOperation addMergeInfoOp = new AddMergeInfoOperation(createModelOp, checkConnectionOp);
		op.add(addMergeInfoOp, new IActionOperation[]{createModelOp});
		
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
								rootNode.setSkipFetchErrors(panel.isSkipFetchErrors());
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
		op.add(showRevisionGraphOp, new IActionOperation[]{createModelOp, addMergeInfoOp});		
		
		return op;
	}
	
	//dialog which says that cache is calculating now by another task
	public static MessageDialog getCacheCalculatingDialog() {
		MessageDialog dlg = new MessageDialog(
				UIMonitorUtility.getShell(), 
				SVNRevisionGraphMessages.Dialog_GraphTitle,
				null, 
				SVNRevisionGraphMessages.CreateCacheDataOperation_DialogMessage,
				MessageDialog.INFORMATION, 
				new String[] {IDialogConstants.OK_LABEL}, 
				0);
		return dlg;	
	}
}
