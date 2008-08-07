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

package org.eclipse.team.svn.ui.synchronize;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.action.AbstractSynchronizeModelAction;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * Abstract synchronize view action contribution implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSynchronizeActionGroup extends SynchronizePageActionGroup {
	public static final String GROUP_MANAGE_LOCALS = "manageLocalChanges";
	public static final String GROUP_PROCESS_ALL = "processAllItems";
	public static final String GROUP_TEAM = "team";
	
	protected ISynchronizePageConfiguration configuration;
	
	protected MenuManager outgoing;
	protected MenuManager incoming;
	
	public AbstractSynchronizeActionGroup() {
		super();
	}

	public final void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(this.configuration = configuration);
		this.configureActions(configuration);
	}
	
    public ISynchronizePageConfiguration getConfiguration() {
        return this.configuration;
    }

	public void dispose() {
		if (this.outgoing != null) {
			this.outgoing.removeAll();
			this.outgoing.dispose();
		}
		
		if (this.incoming != null) {
			this.incoming.removeAll();
			this.incoming.dispose();
		}
		
		super.dispose();
	}
	
    public abstract void configureMenuGroups(ISynchronizePageConfiguration configuration);
	protected abstract void configureActions(ISynchronizePageConfiguration configuration);
	
	protected void addSpecificActions(final AbstractSynchronizeModelAction selectionProvider, final ISynchronizePageConfiguration configuration) {
		this.outgoing = new MenuManager(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Outgoing"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				AbstractSynchronizeActionGroup.GROUP_TEAM, 
				this.outgoing);
		
		this.incoming = new MenuManager(SVNTeamUIPlugin.instance().getResource("SynchronizeActionGroup.Incoming"));
		this.appendToGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				AbstractSynchronizeActionGroup.GROUP_TEAM, 
				this.incoming);
		
		boolean isEuropa = false;
		String description = Platform.getProduct().getDescription();
		int idx = description.indexOf("Version:");
		if (idx != -1) {
			idx += "Version:".length() + 1;
			if (idx + 5 < description.length()) {
				description = description.substring(idx, idx + 5);
				isEuropa = "3.4.0".compareTo(description) > 0;
			}
		}
		if (isEuropa) {
			this.addLocalActions(this.outgoing, configuration);
			this.addRemoteActions(this.incoming, configuration);
		}
		else {
			this.outgoing.setRemoveAllWhenShown(true);
			this.outgoing.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					AbstractSynchronizeActionGroup.this.addLocalActions(manager, configuration);
					AbstractSynchronizeActionGroup.this.updateSelection(manager, selectionProvider.getStructuredSelection());
				}
			});
			this.incoming.setRemoveAllWhenShown(true);
			this.incoming.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					AbstractSynchronizeActionGroup.this.addRemoteActions(manager, configuration);
					AbstractSynchronizeActionGroup.this.updateSelection(manager, selectionProvider.getStructuredSelection());
				}
			});
		}
	}

	protected void addLocalActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {
		
	}
	protected void addRemoteActions(IMenuManager manager, ISynchronizePageConfiguration configuration) {
		
	}
	
	protected void updateSelection(IMenuManager manager, ISelection selection) {
		IContributionItem[] items = manager.getItems();
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if (item instanceof ActionContributionItem) {
				IAction actionItem = ((ActionContributionItem) item).getAction();
				if (actionItem instanceof SynchronizeModelAction) {
					((SynchronizeModelAction) actionItem).selectionChanged(selection);
				}
			}
		}
	}
}
