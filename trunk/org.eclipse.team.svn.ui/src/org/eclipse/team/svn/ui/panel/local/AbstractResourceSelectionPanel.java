/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;

/**
 * Abstract resource selection panel implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractResourceSelectionPanel extends AbstractDialogPanel {
	protected IResource []resources;
	protected CheckboxTableViewer tableViewer;
	protected ResourceSelectionComposite selectionComposite;
//	protected int subPathStart;	// common root length, unfortunately doesn't work with more than one repository location
	protected IResource[] userSelectedResources;

    public AbstractResourceSelectionPanel(IResource []resources, IResource[] userSelectedResources, String []buttonNames) {
        super(buttonNames);
		this.resources = resources;
		this.userSelectedResources = userSelectedResources;
    }

	public IResource []getSelectedResources() {
		return this.selectionComposite != null ? this.selectionComposite.getSelectedResources() : null;
	}

	public IResource []getNotSelectedResources() {
		return this.selectionComposite != null ? this.selectionComposite.getNotSelectedResources() : null;
	}

    public Point getPrefferedSizeImpl() {
        return new Point(600, SWT.DEFAULT);
    }
    
    public void createControls(Composite parent) {
    	this.parent = parent;
    	this.selectionComposite = new ResourceSelectionComposite(parent, SWT.NONE, this.resources, false, this.userSelectedResources);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 210;
		this.selectionComposite.setLayoutData(data);
		this.selectionComposite.addResourcesSelectionChangedListener(new IResourceSelectionChangeListener() {
			public void resourcesSelectionChanged(ResourceSelectionChangedEvent event) {
				AbstractResourceSelectionPanel.this.validateContent();
			}
		});
		this.attachTo(this.selectionComposite, new AbstractVerifier() {
			protected String getErrorMessage(Control input) {
				IResource []selection = AbstractResourceSelectionPanel.this.getSelectedResources();
				if (selection == null || selection.length == 0) {
					return SVNTeamUIPlugin.instance().getResource("ResourceSelectionComposite.Verifier.Error");
				}
				return null;
			}
			protected String getWarningMessage(Control input) {
				return null;
			}
		});
    }
    
    protected void saveChanges() {
    	retainSize();
    }

    protected void cancelChanges() {
    	retainSize();
    }

}
