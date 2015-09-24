package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.action.local.CompareWithWorkingCopyAction;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Compare with working copy action 
 * 
 * @author Igor Burilo
 */
public class CompareWithWorkingCopyPaneAction extends AbstractSynchronizeModelAction {
	
	public CompareWithWorkingCopyPaneAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);			
	}

	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource resource = this.getAllSelectedResources()[0];
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
			IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
			remote.setSelectedRevision(SVNRevision.BASE);
			CompareResourcesOperation op = new CompareResourcesOperation(local, remote, false, true);
			return op;
		}
		return null;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() == 1) {
				IResource[] selectedResources = this.getAllSelectedResources();
				return FileUtility.checkForResourcesPresence(selectedResources, CompareWithWorkingCopyAction.COMPARE_FILTER, IResource.DEPTH_ZERO);
			}	
		}
		return false;
	}		    		    		    		
}