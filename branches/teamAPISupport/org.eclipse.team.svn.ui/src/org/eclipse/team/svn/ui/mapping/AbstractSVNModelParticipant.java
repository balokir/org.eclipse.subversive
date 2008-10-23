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
import java.util.Iterator;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.core.mapping.SVNChangeSetModelProvider;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.SynchronizeLabelDecorator;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;

/**
 * @author Igor Burilo
 *
 */
public abstract class AbstractSVNModelParticipant extends ModelSynchronizeParticipant {

	protected ISynchronizePageConfiguration configuration;
	
	public AbstractSVNModelParticipant() {
		super();
	}

	public AbstractSVNModelParticipant(SynchronizationContext context) {
		super(context);
	}
	
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		
		this.configuration = configuration;

		Collection<AbstractSynchronizeActionGroup> actionGroups = this.getActionGroups();
		// menu groups should be configured before actionGroups is added
		for (Iterator<AbstractSynchronizeActionGroup> it = actionGroups.iterator(); it.hasNext(); ) {
			AbstractSynchronizeActionGroup actionGroup = it.next();
			actionGroup.configureMenuGroups(configuration);
		}
		for (Iterator<AbstractSynchronizeActionGroup> it = actionGroups.iterator(); it.hasNext(); ) {
			AbstractSynchronizeActionGroup actionGroup = it.next();
			configuration.addActionContribution(actionGroup);
		}
				
		configuration.addLabelDecorator(this.createLabelDecorator());

		configuration.setSupportedModes(this.getSupportedModes());
		configuration.setMode(this.getDefaultMode());
	}	
	
	protected ILabelDecorator createLabelDecorator() {
		return new SynchronizeLabelDecorator();
	}
	
    protected abstract int getSupportedModes();
    protected abstract int getDefaultMode();
    protected abstract Collection<AbstractSynchronizeActionGroup> getActionGroups();
    
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant#getEnabledModelProviders() 
	 * 
	 * see CVSModelSynchronizeParticipant
	 * 
	 */
	public ModelProvider[] getEnabledModelProviders() {
		ModelProvider[] enabledProviders =  super.getEnabledModelProviders();
		if (this instanceof IChangeSetProvider) {
			for (int i = 0; i < enabledProviders.length; i++) {
				ModelProvider provider = enabledProviders[i];
				if (provider.getId().equals(SVNChangeSetModelProvider.ID))
					return enabledProviders;
			}
			ModelProvider[] extended = new ModelProvider[enabledProviders.length + 1];
			for (int i = 0; i < enabledProviders.length; i++) {
				extended[i] = enabledProviders[i];
			}
			SVNChangeSetModelProvider provider = SVNChangeSetModelProvider.getProvider();
			if (provider == null) {
				return enabledProviders;
			}
			extended[extended.length - 1] = provider;
			return extended;
		}
		return enabledProviders;
	}
}
