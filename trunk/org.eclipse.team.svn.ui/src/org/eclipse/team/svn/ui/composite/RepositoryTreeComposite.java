/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Rene Link - [patch] NPE in Interactive Merge UI
 *******************************************************************************/

package org.eclipse.team.svn.ui.composite;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;
import org.eclipse.team.svn.ui.repository.model.IRepositoryContentFilter;
import org.eclipse.team.svn.ui.repository.model.RepositoriesRoot;
import org.eclipse.team.svn.ui.repository.model.RepositoryContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownAdapter;

/**
 * Repositories tree composite
 * 
 * @author Alexander Gurov
 */
public class RepositoryTreeComposite extends Composite {
	protected RepositoryTreeViewer repositoryTree;
	protected DrillDownAdapter ddAdapter;
	protected RepositoryContentProvider provider;
	
	public RepositoryTreeComposite(Composite parent, int style) {
		this(parent, style, false);
	}
	
	public RepositoryTreeComposite(Composite parent, int style, boolean multiSelect) {
		this(parent, style, multiSelect, new RepositoriesRoot());
	}
	
	public RepositoryTreeComposite(Composite parent, int style, boolean multiSelect, Object input) {
		super(parent, style);
        this.createControls(multiSelect ? SWT.MULTI : SWT.SINGLE, input);
	}
	
	public RepositoryTreeViewer getRepositoryTreeViewer() {
		return this.repositoryTree;
	}
	
	public Object getModelRoot() {
		return this.repositoryTree.getInput();
	}
	
	public void setModelRoot(Object root) {
		this.repositoryTree.setInput(root);
	}

	public IRepositoryContentFilter getFilter() {
		return this.provider.getFilter();
	}
	
	public void setFilter(IRepositoryContentFilter filter) {
		this.provider.setFilter(filter);
		this.repositoryTree.refresh();
	}
	
	private void createControls(int style, Object input) {
		GridData data = null;
		GridLayout layout = null;
		
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		
		ToolBarManager toolBarMgr = new ToolBarManager(SWT.FLAT);
        ToolBar toolBar = toolBarMgr.createControl(this);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.BEGINNING;
        toolBar.setLayoutData(data);
		
        this.repositoryTree = new RepositoryTreeViewer(this, style | SWT.H_SCROLL | SWT.V_SCROLL);
        this.repositoryTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        this.provider = new RepositoryContentProvider(this.repositoryTree);
		this.repositoryTree.setContentProvider(this.provider);
		this.repositoryTree.setLabelProvider(new WorkbenchLabelProvider());

		this.repositoryTree.setInput(input);
		this.repositoryTree.setAutoExpandLevel(2);
		this.ddAdapter = new DrillDownAdapter(this.repositoryTree);
		this.ddAdapter.addNavigationActions(toolBarMgr);
		toolBarMgr.update(true);
	}
	
}
