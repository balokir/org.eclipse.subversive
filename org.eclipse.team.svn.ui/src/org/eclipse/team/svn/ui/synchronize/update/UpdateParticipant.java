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

package org.eclipse.team.svn.ui.synchronize.update;

import java.util.Collection;

import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNParticipant;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNSubscriber;

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

    protected String getParticipantId() {
        return UpdateParticipant.PARTICIPANT_ID;
    }

	protected Collection getActionGroups() {
		return ExtensionsManager.getInstance().getCurrentSynchronizeActionContributor().getUpdateContributions();
	}

    protected int getSupportedModes() {
        return ISynchronizePageConfiguration.ALL_MODES;
    }

    protected int getDefaultMode() {
        return ISynchronizePageConfiguration.BOTH_MODE;
    }
    
}
