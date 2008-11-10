package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Open in compare editor action 
 *
 * @author Igor Burilo
 *     	
 */    	
public class OpenInComparePaneAction extends AbstractSynchronizeModelAction {
	
	public OpenInComparePaneAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		Utils.initAction(this, "action.openInCompareEditor."); //$NON-NLS-1$
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {				
		if (!this.canExecute(this.getAllSelectedResources())){
			return null;
		}								
		
		IResource resource = this.getSelectedResource();
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
			IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
			remote.setSelectedRevision(SVNRevision.HEAD);
			CompareResourcesOperation op = new CompareResourcesOperation(local, remote, true, true);
			return op;
		}	
		return null;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection) && selection.size() == 1) {
			return this.canExecute(this.getAllSelectedResources());				
		}
		return false;
	}
	
	protected boolean canExecute(IResource[] resources) {
		if (resources.length == 1) {
			IResource resource = this.getSelectedResource();
			return resource.getType() == IResource.FILE && !FileUtility.checkForResourcesPresence(new IResource[]{resource}, IStateFilter.SF_NOTONREPOSITORY, IResource.DEPTH_ZERO); 														
		}
		return false;
	}
}