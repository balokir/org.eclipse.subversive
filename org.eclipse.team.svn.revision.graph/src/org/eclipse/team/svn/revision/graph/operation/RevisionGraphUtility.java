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
import org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditorInput;
import org.eclipse.team.svn.revision.graph.graphic.RevisionRootNode;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/** 
 * Utility which builds revision graph operation 
 *     
 * @author Igor Burilo
 */
public class RevisionGraphUtility {

	protected final static String EDITOR_ID = "org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor";
	
	public static CompositeOperation getRevisionGraphOperation(IRepositoryResource resource) {
		CompositeOperation op = new CompositeOperation("Show Revision Graph Operation");							
		
		CheckRepositoryConnectionOperation checkConnectionOp = new CheckRepositoryConnectionOperation(resource);
		op.add(checkConnectionOp);
		
		final PrepareRevisionDataOperation prepareDataOp = new PrepareRevisionDataOperation(resource);
		op.add(prepareDataOp, new IActionOperation[]{checkConnectionOp});
					
		FetchSkippedRevisionsOperation fetchSkippedOp = new FetchSkippedRevisionsOperation(resource, checkConnectionOp, prepareDataOp);
		op.add(fetchSkippedOp, new IActionOperation[]{prepareDataOp});
		
		FetchNewRevisionsOperation fetchNewOp = new FetchNewRevisionsOperation(resource, checkConnectionOp, prepareDataOp);
		op.add(fetchNewOp, new IActionOperation[]{fetchSkippedOp});					
		
		//create model
		final CreateRevisionGraphModelOperation createModelOp = new CreateRevisionGraphModelOperation(resource, prepareDataOp);
		op.add(createModelOp, new IActionOperation[]{checkConnectionOp});		
		
		//visualize
		AbstractActionOperation showRevisionGraphOp = new AbstractActionOperation("Create Revision Graph Operation") {			
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						try {							
							Object modelObject = createModelOp.getModel() != null ? 
								new RevisionRootNode(createModelOp.getModel(), prepareDataOp.getDataContainer()) : 
								"There's no data";
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
