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
		IResource []resources = this.getSelectedResources(IStateFilter.SF_ONREPOSITORY);					
		IRepositoryResource reposResource = SVNRemoteStorage.instance().asRepositoryResource(resources[0]);
		IActionOperation op = RevisionGraphUtility.getRevisionGraphOperation(reposResource);
		if (op != null) {
			this.runScheduled(op);
		}
	}
		
	public boolean isEnabled() {
		return this.getSelectedResources().length == 1 && this.checkForResourcesPresence(IStateFilter.SF_ONREPOSITORY);
	}
}
