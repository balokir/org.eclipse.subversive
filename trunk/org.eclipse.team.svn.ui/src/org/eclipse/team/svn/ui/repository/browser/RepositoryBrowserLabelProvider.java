/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.repository.browser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNLock;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveNode;
import org.eclipse.team.svn.ui.repository.model.RepositoryFictiveWorkingDirectory;
import org.eclipse.team.svn.ui.repository.model.RepositoryPending;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;

/**
 * Repository browser label provider
 *
 * @author Sergiy Logvin
 */
public class RepositoryBrowserLabelProvider implements ITableLabelProvider {
	protected Map<ImageDescriptor, Image> images;
	
	protected static String hasProps;
	protected static String noAuthor;
	protected static String noDate;

	public RepositoryBrowserLabelProvider(RepositoryBrowserTableViewer tableViewer) {
		this.images = new HashMap<ImageDescriptor, Image>();
		RepositoryBrowserLabelProvider.noAuthor = SVNTeamPlugin.instance().getResource("SVNInfo.NoAuthor");
		RepositoryBrowserLabelProvider.noDate = SVNTeamPlugin.instance().getResource("SVNInfo.NoDate");
		RepositoryBrowserLabelProvider.hasProps = SVNTeamUIPlugin.instance().getResource("RepositoriesView.Browser.HasProps");
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == RepositoryBrowserTableViewer.COLUMN_NAME) {
			ImageDescriptor iDescr = null;
			if (element instanceof RepositoryResource) {
				iDescr = ((RepositoryResource)element).getImageDescriptor(null);
			}
			else if (element instanceof RepositoryFictiveNode) {
				iDescr = ((RepositoryFictiveNode)element).getImageDescriptor(null);
			}
			if (iDescr != null) {
				Image img = this.images.get(iDescr);
				if (img == null) {
					this.images.put(iDescr, img = iDescr.createImage());
				}
				return img;
			}
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof RepositoryResource) {
			return this.getColumnTextForElement(element, columnIndex);
		}
		else if (element instanceof RepositoryFictiveNode) {
			if (columnIndex == RepositoryBrowserTableViewer.COLUMN_NAME) {
				return ((RepositoryFictiveNode)element).getLabel(null);
			}
			if (element instanceof RepositoryFictiveWorkingDirectory) {
				return this.getColumnTextForElement(((RepositoryFictiveWorkingDirectory)element).getAssociatedDirectory(), columnIndex);
			}
		}
		return "";		
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
		for (Image img : this.images.values()) {
			img.dispose();
		}
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {								
	}

	protected String getColumnTextForElement(Object element, int columnIndex) {
		if (element instanceof RepositoryResource) {
			IRepositoryResource resource = ((RepositoryResource)element).getRepositoryResource();
			IRepositoryResource.Information resourceInfo = resource.getInfo();
			if (columnIndex == RepositoryBrowserTableViewer.COLUMN_NAME) {
				return ((RepositoryResource)element).getLabel();
			}
			else if (columnIndex == RepositoryBrowserTableViewer.COLUMN_REVISION) {
				String revision = "";
				try {
					if (resource.isInfoCached()) {
						revision = String.valueOf(((RepositoryResource)element).getRevision());
					}
					else {
						revision = SVNTeamUIPlugin.instance().getResource(RepositoryPending.PENDING);
					}
				}
				catch (Exception ex) {
					LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.GetColumnText"), ex);
				}
				return revision;
			}
			else if (resourceInfo != null) {			
				if (columnIndex == RepositoryBrowserTableViewer.COLUMN_LAST_CHANGE_DATE) {
					return (resourceInfo.lastChangedDate != 0) ? SVNTeamPreferences.formatDate(resourceInfo.lastChangedDate) : RepositoryBrowserLabelProvider.noDate;
				}
				else if (columnIndex == RepositoryBrowserTableViewer.COLUMN_LAST_CHANGE_AUTHOR) {
					String author = resourceInfo.lastAuthor;
					return (author != null) ? author : RepositoryBrowserLabelProvider.noAuthor;
				}
				else if (columnIndex == RepositoryBrowserTableViewer.COLUMN_LOCK_OWNER) {
					SVNLock lock = resourceInfo.lock;
					String lockOwner = (lock == null) ? "" : lock.owner;
					return lockOwner;
				}
				else if (columnIndex == RepositoryBrowserTableViewer.COLUMN_SIZE) {
					long size = resourceInfo.fileSize;
					return (resource instanceof IRepositoryFile) ? String.valueOf(size) : "";
				}
				else if (columnIndex == RepositoryBrowserTableViewer.COLUMN_HAS_PROPS) {
					boolean hasProps = resourceInfo.hasProperties;
					return (hasProps) ? RepositoryBrowserLabelProvider.hasProps : "";
				}
			}
		}
		return "";
	}
	
}
