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

package org.eclipse.team.svn.ui.lock;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.LockResourceSelectionComposite;
import org.eclipse.team.svn.ui.lock.LockResource.LockStatusEnum;
import org.eclipse.team.svn.ui.utility.DateFormatter;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Table label provider for lock resources
 * 
 * @author Igor Burilo
 */
public class LockResourcesTableLabelProvider implements ITableLabelProvider, IFontProvider {
	
	protected Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();

	protected boolean hasCheckBoxes;	
	
	protected final static ImageDescriptor PENDING_IMAGE_DESCRIPTOR = SVNTeamUIPlugin.instance().getImageDescriptor("icons/views/repositories/browser_pending.gif"); //$NON-NLS-1$
	
	protected Font boldFont;
	
	public LockResourcesTableLabelProvider(boolean hasCheckBoxes) {		
		this.hasCheckBoxes = hasCheckBoxes;
		
		//init font
		Font defaultFont = JFaceResources.getDefaultFont();
		FontData[] data = defaultFont.getFontData();
		for (int i = 0; i < data.length; i++) {
			data[i].setStyle(SWT.BOLD);
		}
		this.boldFont = new Font(UIMonitorUtility.getDisplay(), data);		
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		LockResource node = (LockResource) element;
		if (LocksComposite.isFakePending(node)) {
			if (columnIndex == LockResourceSelectionComposite.COLUMN_NAME) {
				Image img = this.images.get(PENDING_IMAGE_DESCRIPTOR);
				if (img == null) {
					img = PENDING_IMAGE_DESCRIPTOR.createImage();
					this.images.put(PENDING_IMAGE_DESCRIPTOR, img);
				}						
				return img;
			}
			return null;
		} else if (LocksComposite.isFakeNoLocks(node)) {
			return null;
		}
		
		if (this.hasCheckBoxes && columnIndex == LockResourceSelectionComposite.COLUMN_NAME || !this.hasCheckBoxes && columnIndex == 0) {
			String fileName = node.getName();
			ImageDescriptor descr = SVNTeamUIPlugin.instance().getWorkbench().getEditorRegistry().getImageDescriptor(fileName);
			Image img = this.images.get(descr);
			if (img == null) {
				img = descr.createImage();
				this.images.put(descr, img);
			}			
			return img;
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		LockResource node = (LockResource) element;
		if (LocksComposite.isFakePending(node)) {
			if (columnIndex == LockResourceSelectionComposite.COLUMN_NAME) {
				return SVNUIMessages.RepositoriesView_Model_Pending;
			}
			return null;
		} else if (LocksComposite.isFakeNoLocks(node)) {
			if (columnIndex == LockResourceSelectionComposite.COLUMN_NAME) {
				return SVNUIMessages.LockResourcesTableLabelProvider_NoLocks;
			}
			return null;
		}								
		
		LockResource data = (LockResource)element;
		switch (columnIndex) {
			case LockResourceSelectionComposite.COLUMN_NAME: {
				return data.getName();
			}
			case LockResourceSelectionComposite.COLUMN_PATH: {
				return data.getPath();
			}
			case LockResourceSelectionComposite.COLUMN_STATE: {
				if (data.lockStatus == LockStatusEnum.LOCALLY_LOCKED) {
					return SVNUIMessages.LockResourcesTableLabelProvider_LocalLock;
				} else if (data.lockStatus == LockStatusEnum.OTHER_LOCKED) {
					return SVNUIMessages.LockResourcesTableLabelProvider_OtherLock;
				} else if (data.lockStatus == LockStatusEnum.BROKEN) {
					return SVNUIMessages.LockResourcesTableLabelProvider_BrokenLock;
				} else if (data.lockStatus == LockStatusEnum.STOLEN) {
					return SVNUIMessages.LockResourcesTableLabelProvider_StolenLock;
				}
				return ""; //$NON-NLS-1$
			}
			case LockResourceSelectionComposite.COLUMN_OWNER: {
				return data.getOwner();
			}
			case LockResourceSelectionComposite.COLUMN_DATE: {
				return DateFormatter.formatDate(data.getCreationDate());
			}
		}
		return ""; //$NON-NLS-1$
	}

	public void dispose() {
		for (Image img : this.images.values()) {
			img.dispose();
		}
		this.boldFont.dispose();
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void addListener(ILabelProviderListener listener) {
		
	}
	
	public void removeListener(ILabelProviderListener listener) {
		
	}
	
	public Font getFont(Object element) {
		return this.isRequireBoldFont(element) ? this.boldFont : null;
	}
	
	protected boolean isRequireBoldFont(Object element) {
		return LocksComposite.isFakeLockResource((LockResource) element);
	}

}
