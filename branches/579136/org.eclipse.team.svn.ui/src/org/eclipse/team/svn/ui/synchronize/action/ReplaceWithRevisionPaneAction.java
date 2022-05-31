package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.action.local.ReplaceWithRevisionAction;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Replace with revision action 
 * 
 * @author Igor Burilo
 */
public class ReplaceWithRevisionPaneAction extends AbstractSynchronizeModelAction {

	public ReplaceWithRevisionPaneAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource[] selectedResources = this.getAllSelectedResources();
		IActionOperation op = ReplaceWithRevisionAction.getReplaceOperation(selectedResources, UIMonitorUtility.getShell());
		return op;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() == 1) {
				IResource[] selectedResources = this.getAllSelectedResources();
				return FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_ONREPOSITORY, IResource.DEPTH_ZERO);	
			}
		}
		return false;
	}
}