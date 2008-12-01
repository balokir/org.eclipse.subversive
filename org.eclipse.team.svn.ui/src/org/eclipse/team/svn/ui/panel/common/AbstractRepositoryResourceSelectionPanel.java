/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.composite.RepositoryResourceSelectionComposite;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;

/**
 * Abstract complementary URL selection panel
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractRepositoryResourceSelectionPanel extends AbstractDialogPanel {
	protected IRepositoryResource selectedResource;
	
	protected long currentRevision;
	protected boolean filterCurrentRevision;
	
	protected boolean toFilterCurrent;
	
	protected String historyKey;
	protected RepositoryResourceSelectionComposite selectionComposite;
	
	protected String selectionTitle;
	protected String selectionDescription;
	protected int defaultTextType;
	
    public AbstractRepositoryResourceSelectionPanel(IRepositoryResource baseResource, long currentRevision, String title, String proposal, String historyKey, String selectionTitle, String selectionDescription, int defaultTextType) {
        super();
        this.dialogTitle = title;
        this.dialogDescription = proposal;
        
		this.historyKey = historyKey;
		this.selectedResource = baseResource;
		this.currentRevision = currentRevision;
		this.selectionTitle = selectionTitle;
		this.selectionDescription = selectionDescription;
		this.defaultTextType = defaultTextType;
		this.toFilterCurrent = false;
    }
    
	public void setFilterCurrent(boolean toFilter) {
		this.toFilterCurrent = toFilter;
	}
    
	public IRepositoryResource []getSelection(IResource []to) {
		IRepositoryResource base = this.getSelectedResource();
		if (to.length == 1) {
			return new IRepositoryResource[] {base};
		}
		IRepositoryResource []retVal = new IRepositoryResource[to.length];
		String baseUrl = base.getUrl();
		for (int i = 0; i < retVal.length; i++) {
			String url = baseUrl + "/" + SVNRemoteStorage.instance().asRepositoryResource(to[i]).getName(); //$NON-NLS-1$
			retVal[i] = to[i].getType() == IResource.FILE ? (IRepositoryResource)base.asRepositoryFile(url, false) : base.asRepositoryContainer(url, false);
		}
		return retVal;
	}

	public IRepositoryResource getSelectedResource() {
		return this.selectedResource;
	}
	
    public void createControlsImpl(Composite parent) {
        GridData data = null;

        this.selectionComposite = new RepositoryResourceSelectionComposite(parent, SWT.NONE, this, this.historyKey, this.selectedResource, false, this.selectionTitle, this.selectionDescription, RepositoryResourceSelectionComposite.MODE_DEFAULT, this.defaultTextType);
        this.selectionComposite.setFilterCurrent(this.toFilterCurrent);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.selectionComposite.setLayoutData(data);
        this.selectionComposite.setCurrentRevision(this.currentRevision);
    }
    
    protected void saveChangesImpl() {
    	this.selectedResource = this.selectionComposite.getSelectedResource();
    	this.selectionComposite.saveHistory();
    }

    protected void cancelChangesImpl() {
    }

}
