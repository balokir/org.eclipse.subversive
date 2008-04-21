/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.internal.ui.synchronize.ScopableSubscriberParticipant;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantDescriptor;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

/**
 * Abstract SVN participant. Can be merge and synchronize participant.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNParticipant extends ScopableSubscriberParticipant implements IChangeSetProvider {
	protected static ImageDescriptor OVR_OBSTRUCTED;
	protected static ImageDescriptor OVR_REPLACED_OUT;
	protected static ImageDescriptor OVR_REPLACED_IN;
	protected static ImageDescriptor OVR_REPLACED_CONF;
	protected static ImageDescriptor OVR_PROPCHANGE;
	
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

		configuration.setSupportedModes(this.getSupportedModes());
		configuration.setMode(this.getDefaultMode());
	}
	
	protected ILabelDecorator createLabelDecorator() {
		return new LabelDecorator();
	}
	
	private void setDefaults() {
	    if (AbstractSVNParticipant.OVR_REPLACED_OUT == null) {
	        SVNTeamUIPlugin instance = SVNTeamUIPlugin.instance();
            AbstractSVNParticipant.OVR_OBSTRUCTED = instance.getImageDescriptor("icons/overlays/obstructed.gif");
            AbstractSVNParticipant.OVR_REPLACED_OUT = instance.getImageDescriptor("icons/overlays/replaced_out.gif");
            AbstractSVNParticipant.OVR_REPLACED_IN = instance.getImageDescriptor("icons/overlays/replaced_in.gif");
            AbstractSVNParticipant.OVR_REPLACED_CONF = instance.getImageDescriptor("icons/overlays/replaced_conf.gif");
            AbstractSVNParticipant.OVR_PROPCHANGE = instance.getImageDescriptor("icons/overlays/prop_changed.png");
	    }
	}	
    
    public abstract AbstractSVNSubscriber getMatchingSubscriber();
    protected abstract String getParticipantId();
    protected abstract Collection<AbstractSynchronizeActionGroup> getActionGroups();
    protected abstract int getSupportedModes();
    protected abstract int getDefaultMode();
	
	protected class LabelDecorator extends LabelProvider implements ILabelDecorator {
		public static final int CONFLICTING_REPLACEMENT_MASK = SyncInfo.CONFLICTING | SyncInfo.CHANGE;
		public static final int REPLACEMENT_MASK = SyncInfo.CHANGE;

		protected Map<ImageDescriptor, Image> images;
	    
	    public LabelDecorator() {
	        super();
	        this.images = new HashMap<ImageDescriptor, Image>();
	    }

		public Image decorateImage(Image image, Object element) {
		    AbstractSVNSyncInfo info = this.getSyncInfo(element);
			if (info != null) {
			    ILocalResource left = info.getLocalResource();
			    ILocalResource right = ((ResourceVariant)info.getRemote()).getResource();
			    OverlayedImageDescriptor imgDescr = null;
			    if (IStateFilter.SF_OBSTRUCTED.accept(left)) {
				    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_OBSTRUCTED, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
			    }
			    else if ((info.getKind() & LabelDecorator.CONFLICTING_REPLACEMENT_MASK) == LabelDecorator.CONFLICTING_REPLACEMENT_MASK) {
				    if (IStateFilter.SF_PREREPLACEDREPLACED.accept(left) || IStateFilter.SF_PREREPLACEDREPLACED.accept(right)) {
					    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_REPLACED_CONF, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
				    }
			    }
			    else if ((info.getKind() & LabelDecorator.REPLACEMENT_MASK) == LabelDecorator.REPLACEMENT_MASK) {
				    if (IStateFilter.SF_PREREPLACEDREPLACED.accept(left)) {
					    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_REPLACED_OUT, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
				    }
				    else if (IStateFilter.SF_PREREPLACEDREPLACED.accept(right)) {
					    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_REPLACED_IN, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
				    }
			    }
			    Image tmp = this.registerImageDescriptor(imgDescr);
				if ((left.getChangeMask() & ILocalResource.PROP_MODIFIED) != 0 || (right.getChangeMask() & ILocalResource.PROP_MODIFIED) != 0) {
				    if (tmp != null) {
				    	image = tmp;
				    }
					imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_PROPCHANGE, new Point(23, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.BOTTOM);
					return this.registerImageDescriptor(imgDescr);
				}
			    return tmp;
			}
			return null;
		}
		
		public String decorateText(String text, Object element) {
		    AbstractSVNSyncInfo info = this.getSyncInfo(element);
			if (info != null) {
				ResourceVariant variant = (ResourceVariant)info.getRemote();
				if (variant != null) {
				    ILocalResource remote = variant.getResource();
				    if (remote.getRevision() != SVNRevision.INVALID_REVISION_NUMBER) {
						return text + " " + variant.getContentIdentifier();
				    }
				}
			}
			return null;
		}
		
		protected Image registerImageDescriptor(OverlayedImageDescriptor imgDescr) {
		    if (imgDescr != null) {
		        Image img = this.images.get(imgDescr);
		        if (img == null) {
		            this.images.put(imgDescr, img = imgDescr.createImage());
		        }
				return img;
		    }
		    return null;
		}
		
		public void dispose() {
			for (Image img : this.images.values()) {
				img.dispose();
			}
		}
		
		protected AbstractSVNSyncInfo getSyncInfo(Object element) {
			if (element instanceof SyncInfoModelElement) {
			    return (AbstractSVNSyncInfo)((SyncInfoModelElement)element).getSyncInfo();
			}
			return null;
		}
		
	}

}
