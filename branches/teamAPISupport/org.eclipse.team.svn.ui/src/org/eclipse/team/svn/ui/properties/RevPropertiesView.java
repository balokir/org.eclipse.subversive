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

package org.eclipse.team.svn.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.property.IRevisionPropertiesProvider;
import org.eclipse.team.svn.core.operation.remote.GetRevisionPropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.svnstorage.events.IRepositoriesStateChangedListener;
import org.eclipse.team.svn.core.svnstorage.events.IRevisionPropertyChangeListener;
import org.eclipse.team.svn.core.svnstorage.events.RepositoriesStateChangedEvent;
import org.eclipse.team.svn.core.svnstorage.events.RevisonPropertyChangeEvent;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.RevisionPropertiesComposite;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Revision properties view
 * 
 * @author Alexei Goncharov
 */
public class RevPropertiesView extends ViewPart implements IRepositoriesStateChangedListener, IRevisionPropertyChangeListener {

	public static final String VIEW_ID = RevPropertiesView.class.getName();
	
	protected IRepositoryLocation location;
	protected SVNRevision revision;
	protected IRevisionPropertiesProvider provider;
	protected IActionOperation toPerform;
	protected RevisionPropertiesComposite revPropComposite;
	
	public RevPropertiesView() {
		super();
		SVNRemoteStorage.instance().addRepositoriesStateChangedListener(this);
		SVNRemoteStorage.instance().addRevisionPropertyChangeListener(this);
	}
	
	public void setLocationAndRevision(IRepositoryLocation location, SVNRevision revision) {
		this.location = location;
		this.revision = revision;
		this.revPropComposite.setLocationAndRevision(location, revision);
		this.refreshView();
	}
	
	public void refreshView() {
		if (this.location == null || this.revision == null) {
			this.setContentDescription("");
			return;
		}
		this.setContentDescription(SVNTeamUIPlugin.instance().getResource("RevisionPropertyView.Decript", new String [] {String.valueOf(this.location), String.valueOf(this.revision)}));
		this.revPropComposite.setPending(true);
		CompositeOperation op = new CompositeOperation("ShowRevProp");
		op.add((IActionOperation)(this.provider = new GetRevisionPropertiesOperation(this.location, this.revision)));
		op.add(new AbstractActionOperation("ShowRevProp") {
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				RevPropertiesView.this.revPropComposite.setInput(RevPropertiesView.this.provider.getRevisionProperties());
				RevPropertiesView.this.revPropComposite.setPending(false);
			}
		});
		UIMonitorUtility.doTaskScheduledDefault(op);
	}
	
	public void createPartControl(Composite parent) {
		IToolBarManager tbm = this.getViewSite().getActionBars().getToolBarManager();
		tbm.removeAll();
        Action action = new Action(SVNTeamUIPlugin.instance().getResource("SVNView.Refresh.Label")) {
        	public void run() {
	    		RevPropertiesView.this.refreshView();
	    	}
        };
        action.setImageDescriptor(SVNTeamUIPlugin.instance().getImageDescriptor("icons/common/refresh.gif"));
        tbm.add(action);
        tbm.update(true);
		
		this.revPropComposite = new RevisionPropertiesComposite(parent);
		this.refreshView();

		//Setting context help
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.team.svn.help.revPropertiesViewContext");
	}
	
	protected void disconnectView() {
		this.location = null;
		this.revision = null;
		this.revPropComposite.setInput(new SVNProperty[0]);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				RevPropertiesView.this.setContentDescription("");
			}			
		});
	}

	public void dispose() {
		SVNRemoteStorage.instance().removeRepositoriesStateChangedListener(this);
		SVNRemoteStorage.instance().removeRevisionPropertyChangeListener(this);
		super.dispose();
	}
	
	public void setFocus() {
	}

	public void repositoriesStateChanged(RepositoriesStateChangedEvent event) {
		if (event.getLocation().equals(this.location)) {
			if (event.getAction() == RepositoriesStateChangedEvent.REMOVED) {
				RevPropertiesView.this.disconnectView();
			}
		}
	}

	public void revisionPropertyChanged(RevisonPropertyChangeEvent event) {
		if (this.location == null || !this.location.equals(event.getLocation())) {
			return;
		}
		ArrayList<SVNProperty> props = new ArrayList<SVNProperty>(Arrays.asList(this.revPropComposite.getSetProps()));
		boolean isNewProp = true;
		for (SVNProperty current : props) {
			if (current.name.equals(event.getProperty().name)) {
				int idx = props.indexOf(current);
				props.set(idx, event.getProperty());
				isNewProp = false;
			}
		}
		if (isNewProp) {
			props.add(event.getProperty());
		}
		final SVNProperty [] toSet = props.toArray(new SVNProperty[props.size()]);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				RevPropertiesView.this.revPropComposite.setInput(toSet);
			}
		});
	}

}
