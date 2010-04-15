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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditorInput;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/** 
 * Utility which builds revision graph operation 
 *     
 * @author Igor Burilo
 */
public class RevisionGraphUtility {

	protected final static String EDITOR_ID = "org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor";  //$NON-NLS-1$
	
	public static CompositeOperation getRevisionGraphOperation(final IRepositoryResource resource) {
		CompositeOperation op = new CompositeOperation("Operation_ShowRevisionGraph", SVNRevisionGraphMessages.class); //$NON-NLS-1$
		
		//create cache
		CreateCacheDataOperation createCacheOp = new CreateCacheDataOperation(resource, false);
		op.add(createCacheOp);
				
		//create model
		final CreateRevisionGraphModelOperation createModelOp = new CreateRevisionGraphModelOperation(resource, createCacheOp);
		op.add(createModelOp, new IActionOperation[] {createCacheOp} );		
		
		//visualize
		AbstractActionOperation showRevisionGraphOp = new AbstractActionOperation("Operation_ShowRevisionGraph", SVNRevisionGraphMessages.class) { //$NON-NLS-1$
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						try {							
							Object modelObject = createModelOp.getModel() != null ? 
								new RevisionRootNode(resource, createModelOp.getModel(), createModelOp.getRepositoryCache()) : 
								SVNRevisionGraphMessages.NoData;
							RevisionGraphEditorInput input = new RevisionGraphEditorInput(createModelOp.getResource(), modelObject);
							UIMonitorUtility.getActivePage().openEditor(input, RevisionGraphUtility.EDITOR_ID);														
						} catch (Exception e) {
							LoggedOperation.reportError(this.getClass().getName(), e);
						}						
					}			
				});	
			}
		};
		op.add(showRevisionGraphOp, new IActionOperation[]{createModelOp});		
		
		return op;
	}
}
