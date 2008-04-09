/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexei Goncharov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.compare;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Compare editor input for SVN properties comparison.
 * 
 * @author Alexei Goncharov
 */
public class PropertyCompareInput extends CompareEditorInput {
	
	protected IRepositoryLocation location;
	
	protected SVNEntryRevisionReference left;
	protected SVNEntryRevisionReference right;
	protected SVNEntryRevisionReference ancestor;

	protected HashMap<String, String> leftProps;
	protected HashMap<String, String> ancestorProps;
	protected HashMap<String, String> rightProps;
	
	protected HashSet<String> propSet;
	
	public PropertyCompareInput(CompareConfiguration configuration,
								SVNEntryRevisionReference left,
								SVNEntryRevisionReference right,
								SVNEntryRevisionReference ancestor,
								IRepositoryLocation location) {
		super(configuration);
		this.left = left;
		this.right = right;
		this.ancestor = ancestor;
		this.location = location;
	}
	
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		this.leftProps = new HashMap<String, String>();
		this.ancestorProps = new HashMap<String, String>();
		this.rightProps = new HashMap<String, String>();
		this.propSet = new HashSet<String>();
		
		//read the properties
		GetPropertiesOperation leftPropOperation = new GetPropertiesOperation(this.left, this.location);
		GetPropertiesOperation rightPropOperation = new GetPropertiesOperation(this.right, this.location);
		GetPropertiesOperation ancestorPropOperation = null;
		final CompositeOperation op = new CompositeOperation(leftPropOperation.getOperationName());
		op.add(leftPropOperation);
		op.add(rightPropOperation);
		if (this.ancestor != null) {
			ancestorPropOperation = new GetPropertiesOperation(this.ancestor, this.location);
			op.add(ancestorPropOperation);
		}
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				UIMonitorUtility.doTaskNowDefault(op, true);
			}
		});
		
		//gather found properties
		SVNProperty [] properties = leftPropOperation.getProperties();
		for (SVNProperty current : properties) {
			this.propSet.add(current.name);
			this.leftProps.put(current.name, current.value);
		}
		properties = null;
		properties = rightPropOperation.getProperties();
		for (SVNProperty current : properties) {
			this.propSet.add(current.name);
			this.rightProps.put(current.name, current.value);
		}
		if (this.ancestor != null) {
			properties = null;
			properties = ancestorPropOperation.getProperties();
			for (SVNProperty current : properties) {
				this.propSet.add(current.name);
				this.ancestorProps.put(current.name, current.value);
			}			
		}
		
		
		//prepare input
		RootCompareNode root = new RootCompareNode(Differencer.NO_CHANGE);
		for (String current : this.propSet) {
			String leftValue = this.leftProps.get(current);
			String rightValue = this.rightProps.get(current);
			String ancestorValue = this.ancestorProps.get(current);
			int diffKind = Differencer.NO_CHANGE;
			if (this.ancestor == null) {
			}
			else {
				if (ancestorValue == null) {
					if (rightValue != null && leftValue != null) {
						diffKind = Differencer.ADDITION | Differencer.CONFLICTING;
					}
					else if (rightValue != null) {
						diffKind = Differencer.RIGHT | Differencer.ADDITION;
					}
					else if (leftValue != null) {
						diffKind = Differencer.LEFT | Differencer.ADDITION;
					}
				}
				else {
					if (rightValue != null && leftValue != null)
					{
						if (!rightValue.equals(ancestorValue) && !leftValue.equals(ancestorValue)) {
							diffKind = Differencer.CHANGE | Differencer.CONFLICTING;
						}
						else if (!rightValue.equals(ancestorValue)) {
							diffKind = Differencer.RIGHT | Differencer.CHANGE;
						}
						else if (!leftValue.equals(ancestorValue)) {
							diffKind = Differencer.LEFT | Differencer.CHANGE;
						}
					}
					else if (leftValue == null && rightValue == null) {
						diffKind = Differencer.DELETION | Differencer.CONFLICTING;
					}
					else if (leftValue == null) {
						diffKind = Differencer.LEFT | Differencer.DELETION;
					}
					else if (rightValue == null) {
						diffKind = Differencer.RIGHT | Differencer.DELETION;
					}
				}
			}
			if (diffKind != Differencer.NO_CHANGE) {
				new PropertyCompareNode(
						root,
						diffKind,
						new PropertyElement(current, ancestorValue, false),
						new PropertyElement(current, leftValue, true),
						new PropertyElement(current, rightValue, false));
			}
		}
		
		//TODO move to resources, process different resources names
		this.setTitle("Property comparison (" +
				this.left.path.substring(this.left.path.lastIndexOf("/")+1)
				+ " ["
				+ String.valueOf(this.left.revision)
				+ " - "
				+ String.valueOf(this.ancestor.revision)
				+ " - "
				+ String.valueOf(this.right.revision)
				+ "] "
				+ ")");
		
		if (root.getChildren().length > 0) {
			return root;
		}
		return null;
	}
	
	public void saveChanges(IProgressMonitor monitor) throws CoreException {
		//PropertyCompareNode currentNode = (PropertyCompareNode)this.getSelectedEdition();
		super.saveChanges(monitor);
	}
	
	public boolean okPressed() {
		return false;
	}
	
	protected class RootCompareNode extends DiffNode {
		public RootCompareNode(int kind) {
			super(kind);
		}
	}
	
	protected class PropertyCompareNode extends DiffNode {

		public PropertyCompareNode(IDiffContainer parent, int kind,
				ITypedElement ancestor, ITypedElement left, ITypedElement right) {
			super(parent, kind, ancestor, left, right);
		}
		
	}
	
	protected class PropertyElement implements ITypedElement, IEditableContent, IStreamContentAccessor {

		protected String basedOnName;
		protected String basedOnValue;
		protected boolean isEditable;
		
		public PropertyElement(String name, String value, boolean isEditable) {
			this.basedOnName = name;
			this.basedOnValue = value;
			this.isEditable = isEditable;
		}
		
		public Image getImage() {
			return CompareUI.getImage("");
		}

		public String getName() {
			return this.basedOnName;
		}

		public String getType() {
			return ITypedElement.TEXT_TYPE;
		}

		public boolean isEditable() {
			return this.isEditable;
		}

		public ITypedElement replace(ITypedElement dest, ITypedElement src) {
			return dest;
		}

		public void setContent(byte[] newContent) {
		}

		public InputStream getContents() throws CoreException {
			String value = this.basedOnValue == null ? "" : this.basedOnValue;
			return new ByteArrayInputStream(value.getBytes());
		}
		
	}
	
	protected class SavePropChangesOperation extends  AbstractActionOperation {
		protected SVNEntryRevisionReference reference;
		protected SVNProperty propToSet;
		protected IRepositoryLocation location;
		
		public SavePropChangesOperation(SVNEntryRevisionReference reference, SVNProperty propToSet, IRepositoryLocation location) {
			super("Operation.SetProperties");
			this.propToSet = propToSet;
			this.reference = reference;
			this.location = location;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			ISVNConnector proxy = this.location.acquireSVNProxy();
			try {
				proxy.setProperty(this.reference.path, this.propToSet.name, this.propToSet.value, Depth.EMPTY, ISVNConnector.Options.NONE, null, new SVNProgressMonitor(this, monitor, null));
			}
			finally {
				this.location.releaseSVNProxy(proxy);
			}
		}
	}
	
	protected class GetPropertiesOperation extends AbstractActionOperation {
		protected SVNEntryRevisionReference reference;
		protected IRepositoryLocation location;
		protected SVNProperty [] properties;
		
		public GetPropertiesOperation(SVNEntryRevisionReference reference, IRepositoryLocation location) {
			super("Operation.GetRevisionProperties");
			this.reference = reference;
			this.location = location;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			ISVNConnector proxy = this.location.acquireSVNProxy();
			try {
				this.properties = SVNUtility.properties(proxy, this.reference, new SVNProgressMonitor(this, monitor, null));
			}
			finally {
				this.location.releaseSVNProxy(proxy);
			}
		}
		
		public SVNProperty [] getProperties() {
			return this.properties == null ? new SVNProperty[0] : this.properties;
		}
	}
}
