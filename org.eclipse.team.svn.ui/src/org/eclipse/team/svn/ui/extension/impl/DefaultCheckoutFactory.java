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

package org.eclipse.team.svn.ui.extension.impl;

import java.util.HashMap;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.LocateProjectsOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.action.remote.CheckoutAction;
import org.eclipse.team.svn.ui.extension.factory.ICheckoutFactory;

/**
 * Default implementation
 * 
 * @author Alexander Gurov
 */
public class DefaultCheckoutFactory implements ICheckoutFactory {

	public DefaultCheckoutFactory() {

	}

	// for common reasons should be implemented as post-processing for checkout action, but while tracker is Eclipse project we should make it the best as we can...
	public LocateProjectsOperation.ILocateFilter getLocateFilter() {
		return new EclipseProjectsFilter();
	}
	
	public IActionOperation getCheckoutOperation(Shell shell, IRepositoryResource []remote, HashMap checkoutMap, boolean respectHierarchy, String location, boolean checkoutRecursively, boolean ignoreExternals) {
		return CheckoutAction.getCheckoutOperation(shell, remote, checkoutMap, respectHierarchy, location, checkoutRecursively, ignoreExternals);
	}

	protected class EclipseProjectsFilter implements LocateProjectsOperation.ILocateFilter {
		public boolean isProject(IRepositoryResource remote, IRepositoryResource[] children) {
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof IRepositoryFile && children[i].getName().equals(".project")) {
					return true;
				}
			}
			return false;
		}
		
	}

	public ITableLabelProvider getLabelProvider(HashMap resources2names) {
		return new LabelProvider(resources2names);
	}

	protected class LabelProvider implements ITableLabelProvider {
		protected HashMap resources2names;
		public LabelProvider(HashMap resources2names) {
			this.resources2names = resources2names;
		}
		public void removeListener(ILabelProviderListener listener) {
		}
		public void addListener(ILabelProviderListener listener) {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void dispose() {
		}
		public String getColumnText(Object element, int columnIndex) {
			IRepositoryResource resource = (IRepositoryResource)element;
			switch (columnIndex) {
				case 1: {
					return resource.getUrl();
				}
				case 2: {
					return FileUtility.formatResourceName((String)this.resources2names.get(resource));
				}
				case 3: {
					return SVNTeamUIPlugin.instance().getResource("DefaultCheckoutFactory.EclipseProject");
				}
			}
			return "";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	public IRepositoryResourceProvider additionalProcessing(CompositeOperation op, IRepositoryResourceProvider provider) {
		return provider;
	}

	public HashMap prepareName2resources(HashMap name2resources) {
		return name2resources;
	}
	
	public boolean findProjectsOptionEnabled() {
		return false;
	}
	
}
