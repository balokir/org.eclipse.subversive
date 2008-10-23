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

import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.team.ui.mapping.ISynchronizationCompareAdapter;

/**
 * @author Igor Burilo
 *
 */
public class SVNAdapterFactory implements IAdapterFactory {

	protected ChangeSetCompareAdapter compareAdapter;
	
	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (ISynchronizationCompareAdapter.class == adapterType) {
			if (this.compareAdapter == null) {
				this.compareAdapter = new ChangeSetCompareAdapter();
			}
			return this.compareAdapter;
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { ISynchronizationCompareAdapter.class };
	}

}
