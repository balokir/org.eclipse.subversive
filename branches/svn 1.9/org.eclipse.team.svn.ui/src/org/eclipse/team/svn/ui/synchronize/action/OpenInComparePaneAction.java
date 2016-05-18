package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.operation.CompareResourcesOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Open in compare editor pane's action 
 *
 * @author Igor Burilo
 *     	
 */    	
public class OpenInComparePaneAction extends Action {	
	
	private final ISynchronizePageConfiguration configuration;
		
	public OpenInComparePaneAction(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
		Utils.initAction(this, "action.openInCompareEditor."); //$NON-NLS-1$
	}    		    	
		
	public void run() {
		ISelection selection = this.configuration.getSite().getSelectionProvider().getSelection();
		if(selection instanceof IStructuredSelection) {
			IStructuredSelection strSelection = (IStructuredSelection) selection;
			if (this.isOkToRun(strSelection)) {
				IResource resource = this.getResources(strSelection)[0];    					    					
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
				if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
					IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
					remote.setSelectedRevision(CompareResourcesOperation.getRemoteResourceRevisionForCompare(resource));
					UIMonitorUtility.doTaskScheduledDefault(new CompareResourcesOperation(local, remote, false, true));
				}	    					
			}
		}
	}
		
	protected boolean isOkToRun(IStructuredSelection selection) {
		if (selection.size() == 1) {
			IResource[] resources = this.getResources(selection);
			if (resources.length == 1) {
				IResource resource = resources[0];
				return resource.getType() == IResource.FILE && !FileUtility.checkForResourcesPresence(new IResource[]{resource}, IStateFilter.SF_NOTONREPOSITORY, IResource.DEPTH_ZERO);	
			}					 														
		}
		return false;
	}
		
	protected IResource[] getResources(IStructuredSelection selection) {
		Object[] elements = selection.toArray();
		IResource[] resources = Utils.getResources(elements);
		return resources;
	}
}