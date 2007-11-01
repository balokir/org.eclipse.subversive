/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.core.operation.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.client.PropertyData.BuiltIn;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.IUnprotectedOperation;
import org.eclipse.team.svn.core.operation.local.change.IActionOperationProcessor;
import org.eclipse.team.svn.core.operation.local.change.IResourceChangeVisitor;
import org.eclipse.team.svn.core.operation.local.change.ResourceChange;
import org.eclipse.team.svn.core.operation.local.change.visitors.CompositeVisitor;
import org.eclipse.team.svn.core.operation.local.change.visitors.SavePropertiesVisitor;
import org.eclipse.team.svn.core.operation.local.property.SetPropertiesOperation;
import org.eclipse.team.svn.core.resource.ILocalFolder;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;

/**
 * Freeze svn:externals revisions. Useful for tags and, possibly, for branches.
 * 
 * @author Alexander Gurov
 */
public class FreezeExternalsOperation extends AbstractWorkingCopyOperation implements IActionOperationProcessor, IResourceProvider {
	protected ArrayList changes = new ArrayList();
	
	public FreezeExternalsOperation(IResource[] resources) {
		super("Operation.FreezeExternals", resources);
	}

	public FreezeExternalsOperation(IResourceProvider provider) {
		super("Operation.FreezeExternals", provider);
	}
	
	public IResource []getResources() {
		return this.operableData();
	}
	
	public Collection getChanges() {
		return this.changes;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		IResource []resources = this.operableData();
		
		final CompositeVisitor visitor = new CompositeVisitor();
		visitor.add(new SavePropertiesVisitor(true));
		visitor.add(new FreezeVisitor());
		
		for (int i = 0; i < resources.length && !monitor.isCanceled(); i++) {
			final IResource current = resources[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					ResourceChange change = ResourceChange.wrapLocalResource(null, SVNRemoteStorage.instance().asLocalResource(current), false);
					if (change != null) {
						change.traverse(visitor, IResource.DEPTH_INFINITE, FreezeExternalsOperation.this, monitor);
					}
				}
			}, monitor, resources.length);
		}
	}

	public void doOperation(IActionOperation op, IProgressMonitor monitor) {
    	this.reportStatus(op.run(monitor).getStatus());
	}

	protected class FreezeVisitor implements IResourceChangeVisitor {
		public void postVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
			
		}

		public void preVisit(ResourceChange change, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
			if (change.getLocal() instanceof ILocalFolder) {
				PropertyData []properties = change.getProperties();
				if (properties != null) {
					for (int i = 0; i < properties.length && !monitor.isCanceled(); i++) {
						if (properties[i].name.equals(BuiltIn.EXTERNALS)) {
							FreezeExternalsOperation.this.changes.add(change);
							this.processExternals(change, properties[i], processor, monitor);
						}
					}
				}
			}
		}
		
		protected void processExternals(ResourceChange change, PropertyData property, IActionOperationProcessor processor, IProgressMonitor monitor) throws Exception {
			// process externals
			String newValue = "";
			StringTokenizer tok = new StringTokenizer(property.value, "\n\r", false);
			while (tok.hasMoreTokens()) {
				String line = tok.nextToken();
				String []entries = line.split("\\s");
				if (entries.length == 2) {
					newValue += this.freezeExternal(change, entries[0], entries[1]);
				}
				else {
					newValue += line;
				}
				newValue += "\n";
			}
			
			SetPropertiesOperation setOp = new SetPropertiesOperation(new IResource[] {change.getLocal().getResource()}, property.name, newValue.getBytes(), false);
			processor.doOperation(setOp, monitor);
		}
		
		protected String freezeExternal(ResourceChange change, String name, String url) {
			IContainer container = (IContainer)change.getLocal().getResource();
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(container.findMember(name));
			if (local == null) {
				return name + "\t" + url;
			}
			return name + "\t-r" + local.getRevision() + "\t" + url;
		}
		
	}
	
}
