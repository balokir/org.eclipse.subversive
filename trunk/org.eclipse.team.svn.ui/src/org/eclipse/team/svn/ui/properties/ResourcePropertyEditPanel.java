/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.extension.properties.PredefinedProperty;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.ApplyPropertyMethodComposite;
import org.eclipse.team.svn.ui.composite.PropertiesComposite;

/**
 * Edit property panel
 * 
 * @author Sergiy Logvin
 */
public class ResourcePropertyEditPanel extends AbstractPropertyEditPanel {
	public static int SINGLE_FILE = 0;
	public static int MULTIPLE_FILES = 1;
	public static int MIXED_RESOURCES = 2;
	
	protected Button recursiveButton;
	protected ApplyPropertyMethodComposite applyComposite;
	protected int resourcesType;
	
	protected boolean recursiveSelected;
	protected boolean applyToAll;
	protected boolean applyToFiles;
	protected boolean applyToFolders;
	protected IResource []selectedResources;
	protected boolean strict;
	protected int mask;

	public ResourcePropertyEditPanel(SVNProperty[] data, IResource []selectedResources, boolean strict) {
		super(data, data != null ? SVNUIMessages.PropertyEditPanel_Title_Edit : SVNUIMessages.PropertyEditPanel_Title_Add, SVNUIMessages.PropertyEditPanel_Description);
		this.strict = strict;	
		this.selectedResources = selectedResources;
		this.resourcesType = this.computeResourcesType();
		this.mask = PredefinedProperty.TYPE_NONE;
		for (IResource resource : this.selectedResources) {
			if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
				this.mask |= PredefinedProperty.TYPE_COMMON;
			}
			else if (resource.getType() == IResource.FILE) {
				this.mask |= PredefinedProperty.TYPE_FILE;
			}
		}
		this.fillVerifiersMap();
	}
	
	protected boolean isPropertyAccepted(PredefinedProperty property) {
		// is there any properties that could be used for both: revisions and resources?
		return (property.type & PredefinedProperty.TYPE_REVISION) == PredefinedProperty.TYPE_NONE && (property.type & this.mask) != PredefinedProperty.TYPE_NONE;
	}

	protected IRepositoryResource getRepostioryResource() {
		return SVNRemoteStorage.instance().asRepositoryResource(this.selectedResources[0]);
	}
	
	public boolean isStrict() {
		return this.strict;
	}
	
	public boolean isRecursiveSelected() {
		return this.recursiveSelected;
	}
	
	public int getApplyMethod() {
		return this.applyComposite == null ? PropertiesComposite.APPLY_TO_ALL : this.applyComposite.getApplyMethod();
	}
	
	public String getFilterMask() {
		return this.applyComposite == null ? "" : this.applyComposite.getFilterMask(); //$NON-NLS-1$
	}
	
	public boolean useMask() {
		return this.applyComposite == null ? false : this.applyComposite.useMask();
	}
		
	public void createControlsImpl (Composite parent) {
		super.createControlsImpl(parent);
		if (this.resourcesType != ResourcePropertyEditPanel.SINGLE_FILE) {
			if (this.resourcesType == ResourcePropertyEditPanel.MIXED_RESOURCES && !this.strict) {
				this.recursiveButton = new Button(parent, SWT.CHECK);
				this.recursiveButton.setText(SVNUIMessages.PropertyEditPanel_Recursively);
			
				this.recursiveButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						ResourcePropertyEditPanel.this.refreshControlsEnablement();
						ResourcePropertyEditPanel.this.validateContent();
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
			this.applyComposite = new ApplyPropertyMethodComposite(parent, SWT.NONE, this, this.resourcesType);
			this.applyComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		if (this.resourcesType == ResourcePropertyEditPanel.MIXED_RESOURCES && !this.strict) {
			this.refreshControlsEnablement();
		}
	}
	
    public String getHelpId() {
    	return "org.eclipse.team.svn.help.setPropsDialogContext"; //$NON-NLS-1$
    }
	
	protected int computeResourcesType() {
		boolean singleResource = this.selectedResources.length == 1;
		boolean allFiles = true;
		for (int i = 0; i < this.selectedResources.length; i++) {
			if (!(this.selectedResources[i] instanceof IFile)) {
				allFiles = false;
				break;
			}
		}
		if (allFiles) {
			return singleResource ? ResourcePropertyEditPanel.SINGLE_FILE : ResourcePropertyEditPanel.MULTIPLE_FILES; 
		}
		return ResourcePropertyEditPanel.MIXED_RESOURCES;
	}
	
	protected void saveChangesImpl() {
		super.saveChangesImpl();
		if (this.resourcesType != ResourcePropertyEditPanel.SINGLE_FILE) {
			if (this.resourcesType == ResourcePropertyEditPanel.MIXED_RESOURCES && !this.strict) {
				this.recursiveSelected = this.recursiveButton.getSelection();
			}
			if (this.applyComposite.isEnabled()) {
				this.applyComposite.saveChanges();
			}
		}
	}
	
	protected void refreshControlsEnablement() {
		this.applyComposite.setEnabled(this.recursiveButton.getSelection());
	}
	
	protected Point getPrefferedSizeImpl() {
        return new Point(590, SWT.DEFAULT);
    }	
}
