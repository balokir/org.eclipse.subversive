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

package org.eclipse.team.svn.ui.synchronize;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.BaseMessages;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceChange;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;

/**
 * @author Igor Burilo
 *
 */
public class SynchronizeLabelDecorator extends LabelProvider implements ILabelDecorator {
	public static final int CONFLICTING_REPLACEMENT_MASK = SyncInfo.CONFLICTING | SyncInfo.CHANGE;
	public static final int REPLACEMENT_MASK = SyncInfo.CHANGE;

	protected Map<ImageDescriptor, Image> images;
    
	protected ISynchronizePageConfiguration configuration;
	
    public SynchronizeLabelDecorator(ISynchronizePageConfiguration configuration) {
        super();
        this.images = new HashMap<ImageDescriptor, Image>();
        this.configuration = configuration;
    }

	public Image decorateImage(Image image, Object element) {
	    AbstractSVNSyncInfo info = this.getSyncInfo(element);
		if (info != null) {
		    ILocalResource left = info.getLocalResource();
		    ILocalResource right = info.getRemoteChangeResource();
		    OverlayedImageDescriptor imgDescr = null;
		    if (IStateFilter.SF_OBSTRUCTED.accept(left)) {
			    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_OBSTRUCTED, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
		    }
		    else if ((info.getKind() & SynchronizeLabelDecorator.CONFLICTING_REPLACEMENT_MASK) == SynchronizeLabelDecorator.CONFLICTING_REPLACEMENT_MASK) {
			    if (IStateFilter.SF_PREREPLACEDREPLACED.accept(left) || IStateFilter.SF_PREREPLACEDREPLACED.accept(right)) {
				    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_REPLACED_CONF, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
			    }
		    }
		    else if ((info.getKind() & SynchronizeLabelDecorator.REPLACEMENT_MASK) == SynchronizeLabelDecorator.REPLACEMENT_MASK) {
			    if (IStateFilter.SF_PREREPLACEDREPLACED.accept(left)) {
				    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_REPLACED_OUT, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
			    }
			    else if (IStateFilter.SF_PREREPLACEDREPLACED.accept(right)) {
				    imgDescr = new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_REPLACED_IN, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V);
			    }
		    }
		    Image tmp = this.registerImageDescriptor(imgDescr);		  
		    if (!(left.getResource() instanceof IContainer) && (IStateFilter.SF_HAS_PROPERTIES_CHANGES.accept(left) || IStateFilter.SF_HAS_PROPERTIES_CHANGES.accept(right))) {
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
					return text + " " + variant.getContentIdentifier(); //$NON-NLS-1$
			    }
			} else {
				/* handle if resource was remotely deleted:
				 * we need such processing because if resource is remotely deleted then its
				 * remove variant is null 
				 */
				ILocalResource incoming = info.getRemoteChangeResource();					
				if (incoming instanceof IResourceChange && IStateFilter.SF_DELETED.accept(incoming)) {
					String result = text;
					result += " " + String.valueOf(incoming.getRevision()); //$NON-NLS-1$
					if (incoming.getAuthor() != null) {
						result += " " + BaseMessages.format(SVNMessages.SVNInfo_Author, new Object[] {incoming.getAuthor()});	 //$NON-NLS-1$
					}
					return result;
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
		IResource resource = getResource(element);
		if (resource != null) {
			ISynchronizeParticipant participant = this.configuration.getParticipant();
			if (participant instanceof ModelSynchronizeParticipant) {
				ModelSynchronizeParticipant msp = (ModelSynchronizeParticipant) participant;
				ISynchronizationContext context = msp.getContext();
				if (context instanceof SubscriberMergeContext) {
					SubscriberMergeContext smc = (SubscriberMergeContext) context;
					Subscriber subscriber = smc.getSubscriber();
					try {
						AbstractSVNSyncInfo syncInfo = (AbstractSVNSyncInfo) subscriber.getSyncInfo(resource);
						//don't return syncInfo for resources which don't have changes
						return syncInfo != null && SyncInfo.isInSync(syncInfo.getKind()) ? null : syncInfo; 
					} catch (TeamException e) {
						LoggedOperation.reportError(SynchronizeLabelDecorator.class.getName(), e);
					}
				}
			}
		}
		return null;
	}
	
	protected IResource getResource(Object element) {
		if (element instanceof ISynchronizeModelElement)
			return ((ISynchronizeModelElement) element).getResource();
		return Utils.getResource(internalGetElement(element));
	}
	
	protected Object internalGetElement(Object elementOrPath) {
		if (elementOrPath instanceof TreePath) {
			TreePath tp = (TreePath) elementOrPath;
			return tp.getLastSegment();
		}
		return elementOrPath;
	}
}
