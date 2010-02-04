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
 * TODO
 * 	1. Correctly define interface, e.g. whether we need to differ local and remote resources
 *  2. Add action to Sync view
 *  3. Add action to History View ?
 *  4. Add action correctly to SVN Repositories view:
 *     for repository location and RepositoryFileEditorInput ?
 *     
 * @author Igor Burilo
 */
public class ShowRevisionGraphUtility {
	
	public static CompositeOperation getRevisionGraphOperation(IRepositoryResource resource) {
		CompositeOperation op = new CompositeOperation("ShowRevisionGraphOperation");
		
		//fetch
		final FetchRevisionsOperation fetchRevisionsOp = new FetchRevisionsOperation(resource);
		op.add(fetchRevisionsOp);							
		
		//TODO ask user whether to continue if there's no connection, see fetchRevisionsOp.hasConnectionToRepository()
		
		//create model
		final CreateRevisionGraphModelOperation createModelOp = new CreateRevisionGraphModelOperation(fetchRevisionsOp, resource);
		op.add(createModelOp, new IActionOperation[]{fetchRevisionsOp});
		
		//validate model		TODO remain this operation for real usage ?
		final ValidateRevionGraphModelOperation validateModelOp = new ValidateRevionGraphModelOperation(createModelOp); 
		op.add(validateModelOp, new IActionOperation[]{createModelOp});
		
		//visualize
		AbstractActionOperation showRevisionGraphOp = new AbstractActionOperation("Create Revision Graph Operation") {			
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				UIMonitorUtility.getDisplay().syncExec(new Runnable() {
					public void run() {
						//TODO handle if model is null
						if (createModelOp.getModel() != null) {
							try {
								UIMonitorUtility.getActivePage().openEditor(new RevisionGraphEditorInput(new RevisionRootNode(createModelOp.getModel())),
									"org.eclipse.team.svn.revision.graph.graphic.RevisionGraphEditor");
							} catch (Exception e) {
								LoggedOperation.reportError(this.getClass().getName(), e);
							}
						}						
					}			
				});	
			}
		};
		op.add(showRevisionGraphOp, new IActionOperation[]{createModelOp});		
		
		return op;
	}
}
