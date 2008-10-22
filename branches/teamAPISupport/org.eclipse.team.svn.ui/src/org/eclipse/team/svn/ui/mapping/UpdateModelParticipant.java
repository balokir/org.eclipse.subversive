/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.mapping;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Igor Burilo
 *
 */
public class UpdateModelParticipant extends AbstractSVNModelParticipant implements IChangeSetProvider {

	protected ChangeSetCapability capability;
	
	public UpdateModelParticipant() {
		super();
	}

	public UpdateModelParticipant(SynchronizationContext context) {
		super(context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor("org.eclipse.team.svn.ui.synchronize.update.SynchronizeModelParticipant")); //$NON-NLS-1$
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
		//isConsultChangeSets = isConsultChangeSets(context.getScopeManager());
	}
	
	protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
		return ExtensionsManager.getInstance().getCurrentSynchronizeActionContributor().getUpdateContributions();
	}

    protected int getSupportedModes() {
        return ISynchronizePageConfiguration.ALL_MODES;
    }

    protected int getDefaultMode() {
        return ISynchronizePageConfiguration.BOTH_MODE;
    }
	
	//TODO
	public String getName() {
		return "UpdateModelParticipant";
	}

	public ChangeSetCapability getChangeSetCapability() {
		if (this.capability == null) {
			this.capability = new SVNModelParticipantChangeSetCapability();
        }
        return this.capability;
	}

}
