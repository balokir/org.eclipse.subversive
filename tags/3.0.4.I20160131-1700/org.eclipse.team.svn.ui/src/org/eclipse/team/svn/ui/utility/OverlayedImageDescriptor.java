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

package org.eclipse.team.svn.ui.utility;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * Overlayed image descriptor
 * 
 * @author Alexander Gurov
 */
public class OverlayedImageDescriptor extends CompositeImageDescriptor {
    
    public static int LEFT = 0x00000000;
    public static int RIGHT = 0x00000001;
    public static int CENTER_H = 0x00000003;
    public static int TOP = 0x00000000;
    public static int BOTTOM = 0x00000010;
    public static int CENTER_V = 0x00000030;
    public static int CENTER = CENTER_H | CENTER_V;
    
    protected Point size;
    protected Image base;
    protected ImageDescriptor overlay;
    protected int where;

    public OverlayedImageDescriptor(Image base, ImageDescriptor overlay, Point size, int where) {
        super();
        this.base = base;
        this.size = size;
        this.overlay = overlay;
        this.where = where;
    }

    protected void drawCompositeImage(int width, int height) {
        this.drawImage(this.base.getImageData(), 0, 0);
        
		ImageData overlayData = this.overlay.getImageData();
		int x = 0;
		int y = 0;
		if ((this.where & OverlayedImageDescriptor.CENTER_H) == OverlayedImageDescriptor.CENTER_H) {
		    x = (this.size.x - overlayData.width) / 2;
		}
		else if ((this.where & OverlayedImageDescriptor.RIGHT) == OverlayedImageDescriptor.RIGHT) {
		    x = this.size.x - overlayData.width;
		}
		if ((this.where & OverlayedImageDescriptor.CENTER_V) == OverlayedImageDescriptor.CENTER_V) {
		    y = (this.size.y - overlayData.height) / 2;
		}
		else if ((this.where & OverlayedImageDescriptor.BOTTOM) == OverlayedImageDescriptor.BOTTOM) {
		    y = this.size.y - overlayData.height;
		}
		this.drawImage(overlayData, x, y);
    }
    
    protected Point getSize() {
        return this.size;
    }

    public int hashCode() {
        return this.base.hashCode() ^ this.overlay.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof OverlayedImageDescriptor) {
            OverlayedImageDescriptor other = (OverlayedImageDescriptor)obj;
            return 
            	this.base.equals(other.base) && this.overlay.equals(other.overlay) &&
            	this.where == other.where && this.size.equals(other.size);
        }
        return super.equals(obj);
    }
    
}
