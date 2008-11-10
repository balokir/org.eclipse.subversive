package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
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
 * Compare with latest revision action
 *
 * @author Igor Burilo
 */
public class CompareWithLatestRevisionPaneAction extends AbstractSynchronizeModelAction {

	public CompareWithLatestRevisionPaneAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource resource = this.getAllSelectedResources()[0];
		ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
		if (!IStateFilter.SF_INTERNAL_INVALID.accept(local)) {
			IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(resource);
			remote.setSelectedRevision(SVNRevision.HEAD);
			CompareResourcesOperation op = new CompareResourcesOperation(local, remote, false, true);
			return op;			
		}
		return null;
	}    	
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() == 1) {
				IResource[] selectedResources = this.getAllSelectedResources();
				return (CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() == ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x || 
						selectedResources[0].getType() == IResource.FILE) && FileUtility.checkForResourcesPresenceRecursive(selectedResources, CompareWithWorkingCopyAction.COMPARE_FILTER);					
			}	
		}
		return false;
	}
}