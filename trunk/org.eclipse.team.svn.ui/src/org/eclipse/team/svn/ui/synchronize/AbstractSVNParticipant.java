/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Alessandro Nistico - [patch] Change Set's implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize;

import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSubscriber;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantDescriptor;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

/**
 * Abstract SVN participant. Can be merge and synchronize participant.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNParticipant extends SubscriberParticipant implements IChangeSetProvider {
	public static ImageDescriptor OVR_OBSTRUCTED;
	public static ImageDescriptor OVR_REPLACED_OUT;
	public static ImageDescriptor OVR_REPLACED_IN;
	public static ImageDescriptor OVR_REPLACED_CONF;
	public static ImageDescriptor OVR_PROPCHANGE;
	
	protected ISynchronizePageConfiguration configuration;
	
	private ChangeSetCapability capability;

	public AbstractSVNParticipant() {
        super();
        this.setDefaults();
    }

    public AbstractSVNParticipant(ISynchronizeScope scope) {
        super(scope);
		this.setSubscriber(this.getMatchingSubscriber());
		this.setDefaults();
    }
    
	public void init(String secondaryId, IMemento memento) throws PartInitException {
		super.init(secondaryId, memento);
		this.setSubscriber(this.getMatchingSubscriber());
	}
    
    public ISynchronizePageConfiguration getConfiguration() {
        return this.configuration;
    }

	// Change sets support
	public synchronized ChangeSetCapability getChangeSetCapability() {
		if (this.capability == null) {
			this.capability = new SVNChangeSetCapability();
		}
		return this.capability;
	}
	
	protected ISynchronizeParticipantDescriptor getDescriptor() {
		return TeamUI.getSynchronizeManager().getParticipantDescriptor(this.getParticipantId());
	}
	
    protected boolean isViewerContributionsSupported() {
        return true;
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
		
		if (this.isSetModes()) {
			configuration.setSupportedModes(this.getSupportedModes());
			configuration.setMode(this.getDefaultMode());	
		}
	}
	
	/**
	 * Flag which determines whether to set mode properties in synchronize page configuration
	 * 
	 * Can be overridden in sub classes
	 */
	protected boolean isSetModes() {
		return true;
	}
	
	protected ILabelDecorator createLabelDecorator() {
		return new SynchronizeLabelDecorator();
	}
	
	private void setDefaults() {
	    if (AbstractSVNParticipant.OVR_REPLACED_OUT == null) {
	        SVNTeamUIPlugin instance = SVNTeamUIPlugin.instance();
            AbstractSVNParticipant.OVR_OBSTRUCTED = instance.getImageDescriptor("icons/overlays/obstructed.gif"); //$NON-NLS-1$
            AbstractSVNParticipant.OVR_REPLACED_OUT = instance.getImageDescriptor("icons/overlays/replaced_out.gif"); //$NON-NLS-1$
            AbstractSVNParticipant.OVR_REPLACED_IN = instance.getImageDescriptor("icons/overlays/replaced_in.gif"); //$NON-NLS-1$
            AbstractSVNParticipant.OVR_REPLACED_CONF = instance.getImageDescriptor("icons/overlays/replaced_conf.gif"); //$NON-NLS-1$
            AbstractSVNParticipant.OVR_PROPCHANGE = instance.getImageDescriptor("icons/overlays/prop_changed.png"); //$NON-NLS-1$
	    }
	}	
    
    public abstract AbstractSVNSubscriber getMatchingSubscriber();
    protected abstract String getParticipantId();
    protected abstract Collection<AbstractSynchronizeActionGroup> getActionGroups();
    protected abstract int getSupportedModes();
    protected abstract int getDefaultMode();		
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant#setSubscriber(org.eclipse.team.core.subscribers.Subscriber)
	 */
	protected void setSubscriber(Subscriber subscriber) {
		super.setSubscriber(subscriber);
		try {
			ISynchronizeParticipantDescriptor descriptor = getDescriptor();
			setInitializationData(descriptor);
		} catch (CoreException e) {
			 LoggedOperation.reportError(this.getClass().getName(), e);
		}
		if (getSecondaryId() == null) {
			setSecondaryId(Long.toString(System.currentTimeMillis()));
		}
	}

}
