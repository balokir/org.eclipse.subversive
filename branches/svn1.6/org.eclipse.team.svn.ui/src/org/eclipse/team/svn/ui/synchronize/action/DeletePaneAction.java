package org.eclipse.team.svn.ui.synchronize.action;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.RestoreProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.SaveProjectMetaOperation;
import org.eclipse.team.svn.core.operation.local.refactor.DeleteResourceOperation;
import org.eclipse.team.svn.core.svnstorage.ResourcesParentsProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.dialog.DiscardConfirmationDialog;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Delete action
 *
 * @author Igor Burilo
 */
public class DeletePaneAction extends AbstractSynchronizeModelAction {
	
	public DeletePaneAction(String text, ISynchronizePageConfiguration configuration) {
		super(text, configuration);
	}
	
	protected IActionOperation getOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		IResource[] selectedResources = this.getAllSelectedResources();
		DiscardConfirmationDialog dialog = new DiscardConfirmationDialog(UIMonitorUtility.getShell(), selectedResources.length == 1, DiscardConfirmationDialog.MSG_RESOURCE);
		if (dialog.open() == 0) {
			DeleteResourceOperation deleteOperation = new DeleteResourceOperation(selectedResources);
			CompositeOperation op = new CompositeOperation(deleteOperation.getId());
			SaveProjectMetaOperation saveOp = new SaveProjectMetaOperation(selectedResources);
			RestoreProjectMetaOperation restoreOp = new RestoreProjectMetaOperation(saveOp);
			op.add(saveOp);
			op.add(deleteOperation);
			op.add(restoreOp);
			op.add(new RefreshResourcesOperation(new ResourcesParentsProvider(selectedResources), IResource.DEPTH_INFINITE, RefreshResourcesOperation.REFRESH_CHANGES));
			return op;
		}
		return null;
	}
	
	protected boolean updateSelection(IStructuredSelection selection) {
		if (super.updateSelection(selection)) {
			if (selection.size() > 0) {
				IResource[] selectedResources = this.getAllSelectedResources();
				return !FileUtility.checkForResourcesPresence(selectedResources, IStateFilter.SF_DELETED, IResource.DEPTH_ZERO);
			}
		}
		return false;
	}
}