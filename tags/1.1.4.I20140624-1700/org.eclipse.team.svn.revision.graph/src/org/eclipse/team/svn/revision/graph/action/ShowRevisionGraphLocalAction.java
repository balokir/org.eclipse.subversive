package org.eclipse.team.svn.revision.graph.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.revision.graph.operation.RevisionGraphUtility;
import org.eclipse.team.svn.ui.action.AbstractWorkingCopyAction;

/**
 * Team services menu "show revision graph" action implementation
 * 
 * @author Igor Burilo
 */
public class ShowRevisionGraphLocalAction extends AbstractWorkingCopyAction {

	public ShowRevisionGraphLocalAction() {
		super();
	}
	
	public void runImpl(IAction action) {		
		IResource[] resources = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY);
		IRepositoryResource[] reposResources = new IRepositoryResource[resources.length];
		for (int i = 0; i < resources.length; i ++) {
			reposResources[i] = SVNRemoteStorage.instance().asRepositoryResource(resources[i]);
		}		
		IActionOperation op = RevisionGraphUtility.getRevisionGraphOperation(reposResources);
		if (op != null) {
			this.runScheduled(op);
		}
	}
		
	public boolean isEnabled() {
		return this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}
}
