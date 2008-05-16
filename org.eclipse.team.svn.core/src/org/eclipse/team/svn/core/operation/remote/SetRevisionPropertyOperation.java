/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.ISVNConnector.Options;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.operation.local.property.IRevisionPropertiesProvider;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;

/**
 * Set revision property operation implementation
 * 
 * @author Alexei Goncharov
 */
public class SetRevisionPropertyOperation extends AbstractActionOperation {

	protected IRepositoryLocation location;
	protected SVNRevision revision;
	protected IRevisionPropertiesProvider provider;

	/**
	 * Creates an instance of SetRevisionPropertyOperation depending on the repository location.
	 * 
	 * @param location - repository location to set property to
	 * @param revision - revision to apply property to
	 * @param revProp - the revision property to set
	 * 
	 * @author Alexei Goncharov
	 */
	public SetRevisionPropertyOperation(IRepositoryLocation location, SVNRevision revision, final SVNProperty revProp) {
		this(location, revision,
				new IRevisionPropertiesProvider() {
					public SVNProperty[] getRevisionProperties() {
						return new SVNProperty [] {revProp};
					}
		});
	}
	
	/**
	 * Creates an instance of SetRevisionPropertyOperation depending on the repository location.
	 * 
	 * @param location - repository location to set property to
	 * @param revision - revision to apply property to
	 * @param revPropProvider - the revision property to set provider
	 * 
	 * @author Alexei Goncharov
	 */
	public SetRevisionPropertyOperation(IRepositoryLocation location, SVNRevision revision, IRevisionPropertiesProvider revPropProvider) {
		super("Operation.SetRevisionProperty");
		this.revision = revision;
		this.provider = revPropProvider;
		this.location = location;
	}
		
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		SVNProperty toSet = this.provider.getRevisionProperties()[0];
		ISVNConnector proxy =  this.location.acquireSVNProxy();
		try {
			proxy.setRevisionProperty(new SVNEntryReference(this.location.getUrl(), this.revision) , toSet.name, toSet.value, Options.FORCE, new SVNProgressMonitor(SetRevisionPropertyOperation.this, monitor, null));
		}
		finally {
			this.location.releaseSVNProxy(proxy);
		}
	}

}
