package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.local.ReplaceWithLatestRevisionAction;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Replace with latest revision action
 *
 * @author Igor Burilo
 */
public class ReplaceWithLatestRevisionPaneAction extends AbstractSynchronizeModelAction {
	
	public ReplaceWithLatestRevisionPaneAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource[] selectedResources = this.getAllSelectedResources();
		IResource []resources = FileUtility.getResourcesRecursive(selectedResources, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO);
		IActionOperation op = ReplaceWithLatestRevisionAction.getReplaceOperation(resources, UIMonitorUtility.getShell());
		return op;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			IResource[] selectedResources = this.getAllSelectedResources();
			return FileUtility.checkForResourcesPresenceRecursive(selectedResources, IStateFilter.SF_ONREPOSITORY);
		}
		return false;
	}			
}