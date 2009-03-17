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

package org.eclipse.team.svn.ui.synchronize.action.logicalmodel;

import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.ICache;
import org.eclipse.team.core.ICacheListener;
import org.eclipse.team.core.diff.IDiffChangeEvent;
import org.eclipse.team.core.diff.IDiffChangeListener;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeLogicalModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Abstract logical model toolbar action
 * 
 * @author Igor Burilo
 */
public abstract class AbstractModelToolbarAction extends AbstractSynchronizeLogicalModelAction implements IDiffChangeListener {

	public AbstractModelToolbarAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		
		//listen to diff tree changes and make enablement
		final IDiffTree tree = this.getSynchronizationContext().getDiffTree();
		tree.addDiffChangeListener(this);
		this.getSynchronizationContext().getCache().addCacheListener(new ICacheListener() {
			public void cacheDisposed(ICache cache) {
				tree.removeDiffChangeListener(AbstractModelToolbarAction.this);
			}
		});
		updateEnablement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#diffsChanged(org.eclipse.team.core.diff.IDiffChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		updateEnablement();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IDiffChangeListener#propertyChanged(org.eclipse.team.core.diff.IDiffTree, int, org.eclipse.core.runtime.IPath[])
	 */
	public void propertyChanged(IDiffTree tree, int property, IPath[] paths) {
		// Do nothing				
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ModelProviderAction#isEnabledForSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		// Enablement has nothing to do with selection
		return isEnabled();
	}
	
	public void updateEnablement() {
		setEnabled(this.getFilteredResources().length > 0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ResourceModelParticipantAction#getResourceTraversals(org.eclipse.jface.viewers.IStructuredSelection, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected ResourceTraversal[] getResourceTraversals(IStructuredSelection selection, IProgressMonitor monitor) throws CoreException {
		return this.getSynchronizationContext().getScope().getTraversals();
	}

}
