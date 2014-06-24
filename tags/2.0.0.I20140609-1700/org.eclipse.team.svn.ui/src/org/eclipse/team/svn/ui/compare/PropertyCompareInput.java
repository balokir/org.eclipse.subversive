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

package org.eclipse.team.svn.ui.compare;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IContentChangeListener;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.team.svn.core.SVNMessages;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.operation.SVNProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Compare editor input for SVN properties comparison.
 * 
 * @author Alexei Goncharov
 */
public abstract class PropertyCompareInput extends CompareEditorInput {
	
	protected DiffTreeViewer viewer;
	
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
	
	public Viewer createDiffViewer(Composite parent) {
		this.viewer = (DiffTreeViewer)super.createDiffViewer(parent);
		this.viewer.addOpenListener(new IOpenListener () {
			public void open(OpenEvent event) {
				PropertyCompareNode selected = (PropertyCompareNode)((TreeSelection)event.getSelection()).getPaths()[0].getFirstSegment();
				CompareConfiguration conf = PropertyCompareInput.this.getCompareConfiguration();
				if (PropertyCompareInput.this.ancestor != null) {
					conf.setAncestorLabel(selected.getName() + " [" + PropertyCompareInput.this.getRevisionPart(PropertyCompareInput.this.ancestor) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				conf.setLeftLabel(selected.getName() + " [" + PropertyCompareInput.this.getRevisionPart(PropertyCompareInput.this.left) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				conf.setRightLabel(selected.getName() + " [" + PropertyCompareInput.this.getRevisionPart(PropertyCompareInput.this.right) + "]");	 //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(this.viewer.getControl());	
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.removeAll();
				TreeSelection selection = (TreeSelection)PropertyCompareInput.this.viewer.getSelection();
				if (selection.size() == 0) {
					return;
				}
				PropertyCompareInput.this.fillMenu(manager, selection);
			}
		});
		this.viewer.getControl().setMenu(menu);
		
		return this.viewer;
	}
	
	protected abstract void fillMenu(IMenuManager manager, TreeSelection selection);
	
	protected String getRevisionPart(SVNEntryRevisionReference reference) {
		if (reference == null) {
			return SVNUIMessages.ResourceCompareInput_PrejFile;
		}
		return SVNUIMessages.format(SVNUIMessages.ResourceCompareInput_RevisionSign, new String [] {String.valueOf(reference.revision)});
	}
	
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		this.leftProps = new HashMap<String, String>();
		this.ancestorProps = new HashMap<String, String>();
		this.rightProps = new HashMap<String, String>();
		this.propSet = new HashSet<String>();
		
		//read the properties
		GetPropertiesOperation leftPropOperation = new GetPropertiesOperation(this.left, this.location, false);
		GetPropertiesOperation rightPropOperation = new GetPropertiesOperation(this.right == null ? this.left : this.right, this.location, this.right == null);
		GetPropertiesOperation ancestorPropOperation = null;
		final CompositeOperation op = new CompositeOperation(leftPropOperation.getOperationName(), SVNMessages.class);
		op.add(leftPropOperation);
		op.add(rightPropOperation);
		if (this.ancestor != null) {
			ancestorPropOperation = new GetPropertiesOperation(this.ancestor, this.location, false);
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
			int diffKind = this.calculateDifference(leftValue, rightValue, ancestorValue);
			if (rightPropOperation.isConflicting(current)) {
				int tDiffKind = this.calculateDifference(leftValue, ancestorValue, ancestorValue);
				diffKind = (rightPropOperation.isAdded(current) ? Differencer.ADDITION : (rightPropOperation.isChanged(current) ? Differencer.CHANGE : Differencer.DELETION));
				diffKind = Differencer.CONFLICTING | (diffKind == Differencer.CHANGE ? tDiffKind : diffKind);
			}
			if (diffKind != Differencer.NO_CHANGE) {
				new PropertyCompareNode(
						root,
						diffKind,
						new PropertyElement(current, ancestorValue, false),
						new PropertyElement(current, leftValue, this.ancestor == null ? false : true),
						new PropertyElement(current, rightValue, false));
			}
		}
			
		if (root.getChildren().length > 0) {
			return root;
		}
		return null;
	}
	
	protected int calculateDifference(String leftValue, String rightValue, String ancestorValue) {
		int diffKind = Differencer.NO_CHANGE;
		if (this.ancestor == null) {
			if (leftValue != null && rightValue != null) {
				diffKind = rightValue.equals(leftValue) ? Differencer.NO_CHANGE : Differencer.CHANGE;
			}
			else if (leftValue == null) {
				diffKind = Differencer.ADDITION;
			}
			else {
				diffKind = Differencer.DELETION;
			}
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
					if (!rightValue.equals(ancestorValue)) {
						diffKind |= Differencer.CONFLICTING;
					}
				}
				else if (rightValue == null) {
					diffKind = Differencer.RIGHT | Differencer.DELETION;
					if (!leftValue.equals(ancestorValue)) {
						diffKind |= Differencer.CONFLICTING;
					}
				}
			}
		}
		return diffKind;
	}
	
	public void saveChanges(IProgressMonitor monitor) throws CoreException {
		super.saveChanges(monitor);
		PropertyCompareNode currentNode = (PropertyCompareNode)this.getSelectedEdition();
		PropertyElement left = (PropertyElement)currentNode.getLeft();
		PropertyElement right = (PropertyElement)currentNode.getRight();
		PropertyElement ancestor = (PropertyElement)currentNode.getAncestor();
		left.commit(monitor);
		currentNode.setKind(this.calculateDifference(left.getValue(), right.getValue(), ancestor.getValue()));
		currentNode.fireChange();
		this.viewer.refresh();
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
		
		public void fireChange() {
			super.fireChange();
		}
		
	}
	
	protected class PropertyElement implements ITypedElement, IEditableContent, IStreamContentAccessor, IContentChangeNotifier {

		protected String basedOnName;
		protected String basedOnValue;
		protected String currentInput;
		protected boolean isEditable;
		protected ArrayList<IContentChangeListener> listenersList;
		
		public PropertyElement(String name, String value, boolean isEditable) {
			this.basedOnName = name;
			this.basedOnValue = value;
			this.isEditable = isEditable;
			this.listenersList = new ArrayList<IContentChangeListener>();
		}
		
		public Image getImage() {
			return CompareUI.getImage(""); //$NON-NLS-1$
		}

		public String getName() {
			return this.basedOnName;
		}
		
		public String getValue() {
			return this.basedOnValue;
		}
		
		public void setValue(String value) {
			this.basedOnValue = value;
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
			this.currentInput = new String(newContent);
		}
		
		public void commit(IProgressMonitor pm) throws CoreException {
			this.basedOnValue = this.currentInput;
			new SavePropChangesOperation(PropertyCompareInput.this.left, new SVNProperty(this.basedOnName, this.basedOnValue), PropertyCompareInput.this.location).run(pm);
			this.fireContentChanged();
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(this.basedOnValue == null ? "".getBytes() : this.basedOnValue.getBytes());  //$NON-NLS-1$
		}

		public void addContentChangeListener(IContentChangeListener listener) {
			this.listenersList.add(listener);
		}

		public void removeContentChangeListener(IContentChangeListener listener) {
			this.listenersList.remove(listener);
		}
		
		protected void fireContentChanged() {
			IContentChangeListener []listeners = this.listenersList.toArray(new IContentChangeListener[0]);
			for (int i= 0; i < listeners.length; i++) {
				listeners[i].contentChanged(this);
			}
		}
		
	}
	
	protected class SavePropChangesOperation extends  AbstractActionOperation {
		protected SVNEntryRevisionReference reference;
		protected SVNProperty propToSet;
		protected IRepositoryLocation location;
		
		public SavePropChangesOperation(SVNEntryRevisionReference reference, SVNProperty propToSet, IRepositoryLocation location) {
			super("Operation_SetProperties", SVNMessages.class); //$NON-NLS-1$
			this.propToSet = propToSet;
			this.reference = reference;
			this.location = location;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			ISVNConnector proxy = this.location.acquireSVNProxy();
			try {
				proxy.setPropertyLocal(new String[] {this.reference.path}, new SVNProperty(this.propToSet.name, this.propToSet.value), SVNDepth.EMPTY, ISVNConnector.Options.FORCE, null, new SVNProgressMonitor(this, monitor, null));
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
		protected ArrayList<SVNProperty> propsAdd = new ArrayList<SVNProperty>();
		protected ArrayList<SVNProperty> propsChange = new ArrayList<SVNProperty>();
		protected ArrayList<SVNProperty> propsDel = new ArrayList<SVNProperty>();
		protected boolean usePropsRej;
		
		public GetPropertiesOperation(SVNEntryRevisionReference reference, IRepositoryLocation location, boolean usePropsRej) {
			super("Operation_GetRevisionProperties", SVNMessages.class); //$NON-NLS-1$
			this.reference = reference;
			this.location = location;
			this.usePropsRej = usePropsRej;
		}
		
		public boolean isConflicting(String name) {
			return this.usePropsRej && 
				(this.findProperty(name, this.propsAdd) != null ||
				this.findProperty(name, this.propsChange) != null ||
				this.findProperty(name, this.propsDel) != null);
		}
		
		public boolean isAdded(String name) {
			return this.usePropsRej && this.findProperty(name, this.propsAdd) != null;
		}
		
		public boolean isChanged(String name) {
			return this.usePropsRej && this.findProperty(name, this.propsChange) != null;
		}
		
		public boolean isDeleted(String name) {
			return this.usePropsRej && this.findProperty(name, this.propsDel) != null;
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			ISVNConnector proxy = this.location.acquireSVNProxy();
			try {
				this.properties = SVNUtility.properties(proxy, this.reference, ISVNConnector.Options.NONE, new SVNProgressMonitor(this, monitor, null));				
				if (this.usePropsRej) {
					SVNChangeStatus []status = SVNUtility.status(proxy, this.reference.path, SVNDepth.EMPTY, ISVNConnector.Options.NONE, new SVNNullProgressMonitor());
					if (status.length > 0 && status[0].propStatus == SVNEntryStatus.Kind.CONFLICTED && 
						status[0].treeConflicts != null && status[0].treeConflicts.length > 0 && status[0].treeConflicts[0].remotePath != null) {
						File rejFile = new File(status[0].treeConflicts[0].remotePath);
						if (rejFile.exists()) {
							BufferedInputStream is = null;
							BufferedReader reader = null;
							try {
								is = new BufferedInputStream(new FileInputStream(rejFile));
								reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
								String line = null, pName = null, pValue = null;
								int state = 0;
								while ((line = reader.readLine()) != null) {
									if ((state == 0 || state == 2) && line.startsWith("Trying to add new property '")) {
										if (state == 2 || state == 8) {
											this.propsAdd.add(new SVNProperty(pName, pValue));
										}
										pName = line.substring("Trying to add new property '".length(), line.length() - 1);
										pValue = null;
										state = 1;
									}
									if ((state == 0 || state == 2) && line.startsWith("Trying to change property '")) {
										if (state == 2 || state == 8) {
											this.propsAdd.add(new SVNProperty(pName, pValue));
										}
										pName = line.substring("Trying to change property '".length(), line.length() - 1);
										pValue = null;
										state = 3;
									}
									if ((state == 0 || state == 2) && line.startsWith("Trying to delete property '")) {
										if (state == 2 || state == 8) {
											this.propsAdd.add(new SVNProperty(pName, pValue));
										}
										pName = line.substring("Trying to change property '".length(), line.length() - 1);
										this.propsDel.add(new SVNProperty(pName, ""));
										pName = null;
										pValue = null;
										state = 6;
									}
									if (state == 1 && line.equals("Incoming property value:")) {
										state = 2;
										continue;
									}
									if (state == 1 && line.equals("<<<<<<< (local property value)")) {
										state = 7;
										continue;
									}
									if (state == 2) {
										pValue = pValue != null ? pValue + "\n" + line : line;
									}
									if (state == 3 && line.equals("<<<<<<< (local property value)")) {
										state = 4;
										continue;
									}
									if (state == 4 && line.endsWith("=======")) {
										state = 5;
										continue;
									}
									if (state == 5) {
										if (line.endsWith(">>>>>>> (incoming property value)")) {
											line = line.substring(0, line.length() - ">>>>>>> (incoming property value)".length());
											pValue = pValue != null ? pValue + "\n" + line : line;
											this.propsChange.add(new SVNProperty(pName, pValue));
											state = 0;
										}
										else {
											pValue = pValue != null ? pValue + "\n" + line : line;
										}
									}
									if (state == 6 && line.endsWith(">>>>>>> (incoming property value)")) {
										state = 0;
									}
									if (state == 7 && line.endsWith("=======")) {
										state = 8;
										continue;
									}
									if (state == 8) {
										if (line.endsWith(">>>>>>> (incoming property value)")) {
											line = line.substring(0, line.length() - ">>>>>>> (incoming property value)".length());
											pValue = pValue != null ? pValue + "\n" + line : line;
											this.propsAdd.add(new SVNProperty(pName, pValue));
											state = 0;
										}
										else {
											pValue = pValue != null ? pValue + "\n" + line : line;
										}
									}
								}
								if (state == 2 || state == 8) {
									this.propsAdd.add(new SVNProperty(pName, pValue));
								}
								ArrayList<SVNProperty> props = new ArrayList<SVNProperty>();
								props.addAll(this.propsAdd);
								props.addAll(this.propsChange);
								if (this.properties != null) {
									for (int i = 0; i < this.properties.length; i++) {
										if (this.findProperty(this.properties[i].name, this.propsDel) == null &&
											this.findProperty(this.properties[i].name, this.propsChange) == null &&
											this.findProperty(this.properties[i].name, this.propsAdd) == null) {
											props.add(this.properties[i]);
										}
									}
								}
								this.properties = props.toArray(new SVNProperty[props.size()]);
							}
							catch (IOException ex) {
								// uninterested
							}
							finally {
								if (reader != null) try {reader.close();} catch (IOException ex) {};
								if (is != null) try {is.close();} catch (IOException ex) {};
							}
						}
					}
				}
			}
			finally {
				this.location.releaseSVNProxy(proxy);
			}
		}
		
		private SVNProperty findProperty(String name, ArrayList<SVNProperty> props) {
			for (Iterator<SVNProperty> it = props.iterator(); it.hasNext(); ) {
				SVNProperty p = it.next();
				if (name.equals(p.name)) {
					return p;
				}
			}
			return null;
		}
		
		public SVNProperty [] getProperties() {
			return this.properties == null ? new SVNProperty[0] : this.properties;
		}
	}
}
