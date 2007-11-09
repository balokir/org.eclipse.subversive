/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.properties;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.PropertiesComposite;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Properties editor
 *
 * @author Sergiy Logvin
 */

public class PropertiesEditor extends EditorPart implements IResourceStatesListener {
	public static final String PROPERTIES_EDITOR_ID = "org.eclipse.team.svn.ui.properties.PropertiesEditor";
	
	protected PropertiesComposite propertiesComposite;
	protected PropertiesEditorInput input;
	
	public PropertiesEditor() {
		super();
		SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, this);
	}
	
	public void createPartControl(final Composite parent) {
		GridLayout layout;
		
        layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        parent.setLayout(layout);
        
        this.propertiesComposite = new PropertiesComposite(parent, null);
        this.propertiesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.propertiesComposite.setResource(this.input.getResource(), this.input.getPropertyProvider());
        
        UIMonitorUtility.doTaskBusyDefault(this.propertiesComposite.getRefreshViewOperation());
	}
	
	public void dispose() {
		SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, PropertiesEditor.this);
		super.dispose();
	}
	
	public void doSave(IProgressMonitor monitor) {	
		
	}

	public void doSaveAs() {
		
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		this.input = (PropertiesEditorInput)input;
		this.setSite(site);
		this.setInput(input);
		this.setPartName(this.getPartTitle());
	}

	public boolean isDirty() {
		return false;
	}
	
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void setFocus() {
		
	}

	public void resourcesStateChanged(ResourceStatesChangedEvent event) {
		if (this.input.getResource() instanceof IResource) {
			IResource resource = (IResource)this.input.getResource();
			if (event.contains(resource) || event.contains(resource.getProject())) {
				ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
				if (!resource.exists() || !FileUtility.isConnected(resource) || local == null || IStateFilter.SF_UNVERSIONED.accept(resource, local.getStatus(), local.getChangeMask())) {
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							PropertiesEditor.this.getEditorSite().getPage().closeEditor(PropertiesEditor.this, false);
						}
					});
				}
				else {
					UIMonitorUtility.doTaskBusyDefault(this.propertiesComposite.getRefreshViewOperation());
					UIMonitorUtility.getDisplay().syncExec(new Runnable() {
						public void run() {
							PropertiesEditor.this.setPartName(PropertiesEditor.this.getPartTitle());
						}
					});
				}
			}
		}
	}
	
	protected String getPartTitle() {
		String title = SVNTeamUIPlugin.instance().getResource("PropertiesEditor.PartTitle");
		return MessageFormat.format(title, new String[] {this.input.getName()});
	}

} 
