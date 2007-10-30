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

package org.eclipse.team.svn.ui.repository;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.team.svn.ui.repository.model.IDataTreeNode;
import org.eclipse.team.svn.ui.repository.model.IParentTreeNode;
import org.eclipse.team.svn.ui.repository.model.IToolTipProvider;
import org.eclipse.team.svn.ui.repository.model.RepositoryBranches;
import org.eclipse.team.svn.ui.repository.model.RepositoryFile;
import org.eclipse.team.svn.ui.repository.model.RepositoryFolder;
import org.eclipse.team.svn.ui.repository.model.RepositoryResource;
import org.eclipse.team.svn.ui.repository.model.RepositoryRoot;
import org.eclipse.team.svn.ui.repository.model.RepositoryTags;
import org.eclipse.team.svn.ui.repository.model.RepositoryTrunk;
import org.eclipse.team.svn.ui.repository.model.ToolTipVariableSetProvider;

/**
 * Repository TreeViewer implementation
 * 
 * @author Alexander Gurov
 */
public class RepositoryTreeViewer extends TreeViewer {

	public static final String FMT_REPOSITORY_RESOURCE = "{" + ToolTipVariableSetProvider.NAME_OF_NAME + "}" +
	                                                     "{" + ToolTipVariableSetProvider.NAME_OF_LAST_CHANGE_DATE + "}" +
	                                                     "{" + ToolTipVariableSetProvider.NAME_OF_LAST_AUTHOR + "}";
	public static final String FMT_REPOSITORY_FILE = RepositoryTreeViewer.FMT_REPOSITORY_RESOURCE +  
												     "{" + ToolTipVariableSetProvider.NAME_OF_SIZE + "}" +
												     "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_OWNER + "}" +
												     "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_CREATION_DATE + "}" +
												     "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_EXPIRATION_DATE + "}" +
												     "{" + ToolTipVariableSetProvider.NAME_OF_LOCK_COMMENT + "}";
	public static final String FMT_REPOSITORY_FOLDER =  RepositoryTreeViewer.FMT_REPOSITORY_RESOURCE;
	public static final String FMT_REPOSITORY_BRANCHES = RepositoryTreeViewer.FMT_REPOSITORY_FOLDER;
	public static final String FMT_REPOSITORY_ROOT = RepositoryTreeViewer.FMT_REPOSITORY_FOLDER;
	public static final String FMT_REPOSITORY_TAGS = RepositoryTreeViewer.FMT_REPOSITORY_FOLDER;
	public static final String FMT_REPOSITORY_TRUNK = RepositoryTreeViewer.FMT_REPOSITORY_FOLDER;
	
	private static final Map class2Format = new HashMap();
	
	static {
		RepositoryTreeViewer.class2Format.put(RepositoryResource.class, RepositoryTreeViewer.FMT_REPOSITORY_RESOURCE);
		RepositoryTreeViewer.class2Format.put(RepositoryFile.class, RepositoryTreeViewer.FMT_REPOSITORY_FILE);
		RepositoryTreeViewer.class2Format.put(RepositoryFolder.class, RepositoryTreeViewer.FMT_REPOSITORY_FOLDER);
		RepositoryTreeViewer.class2Format.put(RepositoryBranches.class, RepositoryTreeViewer.FMT_REPOSITORY_BRANCHES);
		RepositoryTreeViewer.class2Format.put(RepositoryRoot.class, RepositoryTreeViewer.FMT_REPOSITORY_ROOT);
		RepositoryTreeViewer.class2Format.put(RepositoryTags.class, RepositoryTreeViewer.FMT_REPOSITORY_TAGS);
		RepositoryTreeViewer.class2Format.put(RepositoryTrunk.class, RepositoryTreeViewer.FMT_REPOSITORY_TRUNK);
	}

	public static interface IRefreshVisitor {
		public void visit(Object data);
	}
	
	public static interface IRefreshListener {
		public void refreshed(Object element);
	}
	
	protected List refreshListeners = new ArrayList();

	public RepositoryTreeViewer(Composite parent) {
		super(parent);
		this.initialize();
	}

	public RepositoryTreeViewer(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}

	public RepositoryTreeViewer(Tree tree) {
		super(tree);
		this.initialize();
	}
	
	public synchronized void addRefreshListener(IRefreshListener listener) {
		if (!this.refreshListeners.contains(listener)) {
			this.refreshListeners.add(listener);
		}
	}
	
	public synchronized void removeRefreshListener(IRefreshListener listener) {
		this.refreshListeners.remove(listener);
	}
	
	public void setExpandedState(Object element, boolean expanded) {
		TreeItem []items = this.getIdenticalNodes(element, true);
		if (items != null && items.length > 0) {
            if (expanded) {
                createChildren(items[0]);
            }
    		this.setExpanded(items[0], expanded);
		}
		else {
			TreeItem []nodes = this.getIdenticalNodes(element, false);
			if (nodes != null && nodes.length > 0) {
				super.setExpandedState(nodes[0].getData(), expanded);
			}
		}
	}
	
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection tmp = (IStructuredSelection)selection;
			TreeItem []nodes = this.getIdenticalNodes(tmp.getFirstElement(), false);
			if (nodes != null && nodes.length > 0) {
				selection = new StructuredSelection(nodes[0].getData());
			}
		}
		super.setSelection(selection);
	}
	
	public void refresh(final Object element, final IRefreshVisitor visitor, final boolean exact) {
		this.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				TreeItem []nodes = RepositoryTreeViewer.this.getIdenticalNodes(element, exact);
				if (nodes != null && nodes.length != 0) {
					for (int i = 0; i < nodes.length; i++) {
						Object data = nodes[i].getData();
						if (visitor != null) {
							visitor.visit(data);
						}
						RepositoryTreeViewer.this.internalRefresh(nodes[i], data, true, true);
						RepositoryTreeViewer.this.fireRefresh(data);
					}
				}
				else {
					Object input = RepositoryTreeViewer.this.getInput();
					if (input instanceof IDataTreeNode) {
						Object data = ((IDataTreeNode)input).getData();
						if (data != null && data.equals(element) && visitor != null) {
							visitor.visit(input);
						}
					}
					RepositoryTreeViewer.super.refresh(null);
				}
			}
		});
	}
	
	public void fireEmptySelectionEvent() {
		this.fireSelectionChanged(new SelectionChangedEvent(this, StructuredSelection.EMPTY));
	}
	
	// fix the problem with refresh of identical nodes in the tree
    protected void internalRefresh(Widget widget, Object element, boolean doStruct, boolean updateLabels) {
    	if (widget instanceof Item) {
			if (doStruct) {
				this.updatePlus((Item) widget, element);
			}
			if (updateLabels || !this.equals(element, widget.getData())) {
				this.doUpdateItem(widget, element, true);
			} else {
				this.associate(element, (Item) widget);
			}
		}

		if (doStruct) {
			this.internalRefreshStruct(widget, element, updateLabels); 
		} else {
			Item[] children = this.getChildren(widget);
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					Widget item = children[i];
					Object data = item.getData();
					if (data != null) {
						this.internalRefresh(item, data, doStruct, updateLabels);
					}
				}
			}
		}
	}

    protected void internalRefreshStruct(Widget widget, Object element, boolean updateLabels) {
//		this.updateChildren(widget, element, null);
		try {
			//updateChildren(Widget widget, Object parent, Object[] elementChildren, boolean updateLabels)
			Method m = AbstractTreeViewer.class.getDeclaredMethod("updateChildren", new Class[] {Widget.class, Object.class, Object [].class, boolean.class});
			m.setAccessible(true);
			m.invoke(this, new Object[] {widget, element, null, Boolean.valueOf(updateLabels)});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		Item[] children = getChildren(widget);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Widget item = children[i];
				Object data = item.getData();
				if (data != null) {
					this.internalRefreshStruct(item, data, updateLabels);
				}
			}
		}
	}

    public void refresh() {
		this.getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
			    RepositoryTreeViewer.super.refresh();
			}
		});
    }
    
	public TreeItem []getIdenticalNodes(Object sample, boolean exact) {
		if (sample != null) {
			return this.findUnfreshNodes(this.getTree().getItems(), sample, exact);
		}
		return null;
	}
	
	protected synchronized void fireRefresh(Object data) {
		Object[] listeners = this.refreshListeners.toArray();
		for (int i = 0; i < listeners.length; i++) {
			((IRefreshListener)listeners[i]).refreshed(data);
		}
	}
	
	protected TreeItem []findUnfreshNodes(TreeItem []items, Object obj, boolean exact) {
		List retVal = this.findUnfreshNodesImpl(items, obj, exact);
		return retVal == null ? null : (TreeItem [])retVal.toArray(new TreeItem[retVal.size()]);
	}
	
	protected List findUnfreshNodes(TreeItem item, Object obj, boolean exact) {
		Object data = item.getData();
		if (obj == data || !exact && obj.equals(data)) {
			return Arrays.asList(new TreeItem[] {item});
		}
		if (data instanceof IDataTreeNode) {
			IDataTreeNode dataNode = (IDataTreeNode)data;
			if (obj == dataNode.getData() || !exact && obj.equals(dataNode.getData())) {
				return Arrays.asList(new TreeItem[] {item});
			}
		}
		return this.findUnfreshNodesImpl(item.getItems(), obj, exact);
	}
	
	protected List findUnfreshNodesImpl(TreeItem []items, Object obj, boolean exact) {
		if (items != null) {
			List retVal = new ArrayList();
			for (int i = 0; i < items.length; i++) {
				List tmp = this.findUnfreshNodes(items[i], obj, exact);
				if (tmp != null) {
					retVal.addAll(tmp);
				}
			}
			return retVal;
		}
		return null;
	}
	
	protected void handleDoubleClick(IStructuredSelection selection) {
    	Object node = selection.getFirstElement();
    	if (node instanceof IParentTreeNode) {
    		TreeItem []items = this.getIdenticalNodes(node, true);
    		if (items != null && items.length > 0) {
    			boolean expanded = !this.getExpanded(items[0]);
                if (expanded) {
                    createChildren(items[0]);
                }
        		this.setExpanded(items[0], expanded);
    		}
    	}
	}

	private void initialize() {
		this.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				ISelection selection = e.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured = (IStructuredSelection)selection;
					if (structured.size() == 1) {
						RepositoryTreeViewer.this.handleDoubleClick(structured);
					}
				}
			}
		});
		
		this.getTree().addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseHover(MouseEvent e) {
				String tooltipText = "";
				Tree tree = RepositoryTreeViewer.this.getTree();
				TreeItem item = tree.getItem(new Point(e.x, e.y));
				if (item != null) {
					Object data = item.getData();
					if (data != null && data instanceof IToolTipProvider) {
						tooltipText = ((IToolTipProvider)data).getToolTipMessage((String)RepositoryTreeViewer.class2Format.get(data.getClass()));
					}
				}
				tree.setToolTipText(tooltipText);
			}
			
			public void mouseExit(MouseEvent e) {
				RepositoryTreeViewer.this.getTree().setToolTipText("");
			}
		});
	}
	
}
