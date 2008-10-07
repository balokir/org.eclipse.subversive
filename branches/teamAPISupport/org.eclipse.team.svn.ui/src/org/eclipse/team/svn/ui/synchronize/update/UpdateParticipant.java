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

package org.eclipse.team.svn.ui.synchronize.update;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNParticipant;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSubscriber;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.ResourceScope;

/**
 * Synchronize view participant
 * 
 * @author Alexander Gurov
 */
public class UpdateParticipant extends AbstractSVNParticipant {
	public static final String PARTICIPANT_ID = "org.eclipse.team.svn.ui.synchronize.update.SynchronizeParticipant";

	public UpdateParticipant() {
        super();
    }

	public UpdateParticipant(ISynchronizeScope scope) {
		super(scope);
	}
	
    public AbstractSVNSubscriber getMatchingSubscriber() {
        return UpdateSubscriber.instance();
    }

	public String getName() {
		String name = SVNTeamUIPlugin.instance().getResource("SynchronizeParticipant");
		ISynchronizeScope scope = this.getScope();
		String scopeName = scope.getName();
		if (scope instanceof ResourceScope) {
			StringBuffer buffer = new StringBuffer();
			IResource []resources = scope.getRoots();
			for (int i = 0; i < resources.length; i++) {
				if (i > 0) {
					buffer.append(", "); //$NON-NLS-1$
				}
				buffer.append(resources[i].getFullPath().toString().substring(1));
			}
			scopeName = buffer.toString();
		}
		return NLS.bind(TeamUIMessages.SubscriberParticipant_namePattern, new String[] { name, scopeName }); 
	}
	
    protected String getParticipantId() {
        return UpdateParticipant.PARTICIPANT_ID;
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
    
}
