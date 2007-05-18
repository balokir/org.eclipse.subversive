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

package org.eclipse.team.svn.ui.properties;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.core.operation.IResourcePropertyProvider;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;

/**
 * SVN properties CompareEditorInput implementation
 * 
 * @author Alexander Gurov
 */
public class PropertiesCompareEditorInput extends CompareEditorInput {
	public static final String SVN_PROPERTIES_TYPE = "svnproperties";
	
	protected IResourcePropertyProvider left;
	protected IResourcePropertyProvider right;
	protected IResourcePropertyProvider ancestor;
	
	public PropertiesCompareEditorInput(CompareConfiguration configuration, IResourcePropertyProvider left, IResourcePropertyProvider right, IResourcePropertyProvider ancestor) {
		super(configuration);
		this.left = left;
		this.right = right;
		this.ancestor = ancestor;
	}

	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		return null;
	}

	public class ResourcePropertiesElement implements ITypedElement {
		protected IResourcePropertyProvider provider;
		
		public ResourcePropertiesElement(IResourcePropertyProvider provider) {
			this.provider = provider;
		}
		
		public IResourcePropertyProvider getProvider() {
			return this.provider;
		}

		public String getName() {
			return this.provider.getRemote().getName();
		}

		public Image getImage() {
			return CompareUI.getImage(RepositoryFolder.wrapChild(this.provider.getRemote()));
		}

		public String getType() {
			return PropertiesCompareEditorInput.SVN_PROPERTIES_TYPE;
		}
		
	}
	
}
