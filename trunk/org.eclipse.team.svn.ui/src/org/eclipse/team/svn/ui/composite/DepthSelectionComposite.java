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

package org.eclipse.team.svn.ui.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;

/**
 * A UI component for selecting the recursion depth
 * 
 * @author Alexei Goncharov
 */
public class DepthSelectionComposite extends Composite {

	protected int depth;
	
	public DepthSelectionComposite(Composite parent, int style) {
		super(parent, style);
		this.depth = Depth.INFINITY;
		this.createControls();
	}
	
	protected void createControls() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);
				
		Label label = new Label(this, SWT.NONE);
		label.setText(SVNTeamUIPlugin.instance().getResource("RecurseDepthSelector.Label"));
		GridData data = new GridData();
		label.setLayoutData(data);
			
		//getting strings for options
		final String empty = SVNTeamUIPlugin.instance().getResource("RecurseDepthSelector.Empty");
		final String files = SVNTeamUIPlugin.instance().getResource("RecurseDepthSelector.Files");
		final String immediates = SVNTeamUIPlugin.instance().getResource("RecurseDepthSelector.Immediates");
		final String infinity = SVNTeamUIPlugin.instance().getResource("RecurseDepthSelector.Infinity");
		
		boolean svn15compatible = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
		
		Combo depthSelector = new Combo(this, SWT.READ_ONLY);
		if (svn15compatible) {
			depthSelector.add(empty);
		}
		depthSelector.add(files);
		if (svn15compatible) {
			depthSelector.add(immediates);
		}
		depthSelector.add(infinity);
		depthSelector.setText(infinity);
		depthSelector.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
			public void widgetSelected(SelectionEvent e) {
				if (((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(infinity)) {
					DepthSelectionComposite.this.depth = Depth.INFINITY;
				}
				else if(((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(immediates)) {
					DepthSelectionComposite.this.depth = Depth.IMMEDIATES;
				}
				else if(((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(files)) {
					DepthSelectionComposite.this.depth = Depth.FILES;
				}
				else {
					DepthSelectionComposite.this.depth = Depth.EMPTY;
				}
			}
			
		});
		data = new GridData(GridData.FILL_HORIZONTAL);
		depthSelector.setLayoutData(data);
	}
	
	public int getDepth() {
		return this.depth;
	}

}
