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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.variant.ResourceVariant;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;

/**
 * @author Igor Burilo
 *
 */
public class SynchronizeLabelDecorator extends LabelProvider implements ILabelDecorator {
	public static final int CONFLICTING_REPLACEMENT_MASK = SyncInfo.CONFLICTING | SyncInfo.CHANGE;
	public static final int REPLACEMENT_MASK = SyncInfo.CHANGE;

	protected Map<ImageDescriptor, Image> images;
    
    public SynchronizeLabelDecorator() {
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
			if (!(left.getResource() instanceof IContainer) && ((left.getChangeMask() & ILocalResource.PROP_MODIFIED) != 0 || (right.getChangeMask() & ILocalResource.PROP_MODIFIED) != 0)) {
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
