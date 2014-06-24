/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.mapping;

import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.diff.IDiffTree;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.internal.core.subscribers.DiffChangeSet;
import org.eclipse.team.internal.ui.mapping.ResourceModelLabelProvider;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

public class SVNChangeSetLabelProvider extends ResourceModelLabelProvider {

	private Image changeSetImage;

	public void init(ICommonContentExtensionSite site) {
		super.init(site);
	}

	public String getText(Object element) {
		if (element instanceof ActiveChangeSet && SVNTeamPlugin.instance().getModelChangeSetManager().isDefault((ActiveChangeSet)element)) {
			return super.getText(element) + " " + SVNUIMessages.ChangeSet_DefaultDecoration; //$NON-NLS-1$
		}
		return super.getText(element);
	}
	
	protected String getDelegateText(Object elementOrPath) {
		Object element = this.internalGetElement(elementOrPath);
		if (element instanceof ChangeSet) {
			ChangeSet set = (ChangeSet) element;
			return set.getName();
		}
		return super.getDelegateText(elementOrPath);
	}
	
	protected Image getDelegateImage(Object elementOrPath) {
		Object element = this.internalGetElement(elementOrPath);
		if (element instanceof ChangeSet) {
			return this.getChangeSetImage();
		}
		return super.getDelegateImage(elementOrPath);
	}

	private Image getChangeSetImage() {
		if (this.changeSetImage == null) {
			this.changeSetImage = SVNTeamUIPlugin.instance().getImageDescriptor("icons/objects/changeset.gif").createImage(); //$NON-NLS-1$
		}
		return this.changeSetImage;
	}
	
	public void dispose() {
		if (this.changeSetImage != null) {
			this.changeSetImage.dispose();
		}
		super.dispose();
	}
	
	protected boolean isBusy(Object elementOrPath) {
		Object element = this.internalGetElement(elementOrPath);
		if (element instanceof DiffChangeSet) {
			DiffChangeSet dcs = (DiffChangeSet) element;
			IResource[] resources = dcs.getResources();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (getContext().getDiffTree().getProperty(resource.getFullPath(), IDiffTree.P_BUSY_HINT)) {
					return true;
				}
			}
			return false;
		}
		return super.isBusy(elementOrPath);
	}
	
	protected boolean hasDecendantConflicts(Object elementOrPath) {
		Object element = this.internalGetElement(elementOrPath);
		if (element instanceof DiffChangeSet) {
			DiffChangeSet dcs = (DiffChangeSet) element;
			IResource[] resources = dcs.getResources();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (this.getContext().getDiffTree().getProperty(resource.getFullPath(), IDiffTree.P_HAS_DESCENDANT_CONFLICTS))
					return true;
			}
			return false;
		}
		if (elementOrPath instanceof TreePath && element instanceof IResource) {
			DiffChangeSet set = this.internalGetChangeSet(elementOrPath);
			if (set != null) {
				ResourceTraversal[] traversals = this.getTraversalCalculator().getTraversals(set, (TreePath)elementOrPath);
				return (this.getContext().getDiffTree().hasMatchingDiffs(traversals, CONFLICT_FILTER));
			}
		}
		return super.hasDecendantConflicts(elementOrPath);
	}
	
	private DiffChangeSet internalGetChangeSet(Object elementOrPath) {
		if (elementOrPath instanceof TreePath) {
			TreePath tp = (TreePath) elementOrPath;
			Object o = tp.getFirstSegment();
			if (o instanceof DiffChangeSet) {
				return (DiffChangeSet) o;
			}
		}
		return null;
	}

	protected int getMarkerSeverity(Object elementOrPath) {
		Object element = this.internalGetElement(elementOrPath);
		if (element instanceof DiffChangeSet) {
			DiffChangeSet dcs = (DiffChangeSet) element;
			HashSet<IProject> projects = new HashSet<IProject>();
			IResource[] resources = dcs.getResources();
			int severity = -1;
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				IProject project = resource.getProject();
				if (!projects.contains(project)) {
					projects.add(project);
					int next = super.getMarkerSeverity(project);
					if (next == IMarker.SEVERITY_ERROR) {
						return IMarker.SEVERITY_ERROR;
					}
					if (next == IMarker.SEVERITY_WARNING) {
						severity = next;
					}
				}
			}
			return severity;
		}
		return super.getMarkerSeverity(elementOrPath);
	}
	
	protected void updateLabels(Object[] elements) {
		super.updateLabels(this.addSetsContainingElements(elements));
	}

	private Object[] addSetsContainingElements(Object[] elements) {
		HashSet<Object> result = new HashSet<Object>();
		for (int i = 0; i < elements.length; i++) {
			Object object = elements[i];
			result.add(object);
			if (object instanceof IProject) {
				IProject project = (IProject) object;
				ChangeSet[] sets = this.getSetsContaing(project);
				for (int j = 0; j < sets.length; j++) {
					ChangeSet set = sets[j];
					result.add(set);
				}
			}
		}
		return result.toArray();
	}

	private ChangeSet[] getSetsContaing(IProject project) {
		return this.getContentProvider().getSetsShowingPropogatedStateFrom(project.getFullPath());
	}

	private SVNChangeSetContentProvider getContentProvider() {
		return (SVNChangeSetContentProvider)this.getExtensionSite().getExtension().getContentProvider();
	}
	
	private Object internalGetElement(Object elementOrPath) {
		if (elementOrPath instanceof TreePath) {
			TreePath tp = (TreePath) elementOrPath;
			return tp.getLastSegment();
		}
		return elementOrPath;
	}
	
	public Font getFont(Object element) {
		element = this.internalGetElement(element);
	    if (element instanceof ActiveChangeSet && this.isDefaultActiveSet((ActiveChangeSet)element)) {
			return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	    }
		return super.getFont(element);
	}

	private boolean isDefaultActiveSet(ActiveChangeSet set) {
		ChangeSetCapability changeSetCapability = this.getContentProvider().getChangeSetCapability();
		if (changeSetCapability != null) {
			ActiveChangeSetManager activeChangeSetManager = changeSetCapability.getActiveChangeSetManager();
			if (activeChangeSetManager != null) {
				return activeChangeSetManager.isDefault(set);
			}
		}
		return false;
	}
}
