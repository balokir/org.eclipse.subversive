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

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings;
import org.eclipse.team.svn.core.operation.local.IExecutable;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ExternalProgramParameters;
import org.eclipse.team.svn.core.operation.local.RunExternalCompareOperation.DetectExternalCompareOperation;
import org.eclipse.team.svn.core.operation.local.RunExternalCompareOperation.ExternalCompareOperationHelper;
import org.eclipse.team.svn.core.operation.local.RunExternalCompareOperation.IExternalProgramParametersProvider;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider.DefaultRepositoryResourceProvider;

/**
 * Run external repository compare editor operation implementation
 *
 * @author Igor Burilo
 */
public class RunExternalRepositoryCompareOperation extends CompositeOperation implements IExecutable {

	protected ExternalCompareRepositoryOperation externalCompareOperation;
	
	public RunExternalRepositoryCompareOperation(final IRepositoryResourceProvider provider, DiffViewerSettings diffSettings) {
		super("Operation_ExternalRepositoryCompare"); //$NON-NLS-1$
		
		final DetectExternalCompareOperation detectOperation = new DetectExternalCompareOperation(provider, diffSettings);
		this.add(detectOperation);
		
		this.externalCompareOperation = new ExternalCompareRepositoryOperation(provider, detectOperation); 
		this.add(this.externalCompareOperation, new IActionOperation[]{detectOperation});
	}

	public boolean isExecuted() {
		return this.externalCompareOperation.isExecuted();
	}
	
	/**
	 * Prepare files and run external compare editor
	 *
	 * @author Igor Burilo
	 */
	public static class ExternalCompareRepositoryOperation extends CompositeOperation {
				
		protected IExternalProgramParametersProvider parametersProvider;
		protected ExternalProgramParameters externalProgramParams;
		protected boolean isExecuted;
		
		public ExternalCompareRepositoryOperation(IRepositoryResource left, IRepositoryResource right, IExternalProgramParametersProvider parametersProvider) {
			this(new DefaultRepositoryResourceProvider(new IRepositoryResource[]{left, right}), parametersProvider);																	
		}
		
		public ExternalCompareRepositoryOperation(final IRepositoryResourceProvider resourcesProvider, IExternalProgramParametersProvider parametersProvider) {
			super("Operation_ExternalRepositoryCompare"); //$NON-NLS-1$
			this.parametersProvider = parametersProvider;
			
			//get files operations
			final AbstractGetFileContentOperation nextFileGetOp = new GetFileContentOperation(new IRepositoryResourceProvider() {
				public IRepositoryResource[] getRepositoryResources() {
					return new IRepositoryResource[]{resourcesProvider.getRepositoryResources()[0]};
				}				
			});				
			this.add(nextFileGetOp);
			
			final AbstractGetFileContentOperation prevFileGetOp = new GetFileContentOperation(new IRepositoryResourceProvider() {
				public IRepositoryResource[] getRepositoryResources() {
					return new IRepositoryResource[]{resourcesProvider.getRepositoryResources()[1]};
				}				
			});				
			this.add(prevFileGetOp, new IActionOperation[] {nextFileGetOp});
			
			//Run external program operation
			this.add(new AbstractActionOperation("Operation_ExternalRepositoryCompare") { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {					
					ExternalCompareOperationHelper externalRunHelper = new ExternalCompareOperationHelper(
							prevFileGetOp.getTemporaryPath(),
							nextFileGetOp.getTemporaryPath(),							
							null,
							null,
							ExternalCompareRepositoryOperation.this.externalProgramParams);
					externalRunHelper.execute(monitor);										
				}				
			}, new IActionOperation[] {nextFileGetOp, prevFileGetOp});	
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			this.externalProgramParams = this.parametersProvider.getExternalProgramParameters();
			if (this.externalProgramParams != null) {	
				this.isExecuted = true;
				super.runImpl(monitor);	
			} else {
				this.isExecuted = false;
			}
		}
		
		public boolean isExecuted() {
			return this.isExecuted;
		}
	}
	
}