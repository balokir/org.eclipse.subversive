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

package org.eclipse.team.svn.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;

/**
 * A UI component for selecting the recursion depth
 * 
 * @author Alexei Goncharov
 */
public class RecureDepthSelector extends Composite {

	protected int recureDepth;
	
	public RecureDepthSelector(Composite parent, int style) {
		super(parent, style);
		this.recureDepth = Depth.INFINITY;
		this.createControls();
	}
	
	protected void createControls() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);

		Label label = new Label(this, SWT.NONE);
		label.setText(SVNTeamUIPlugin.instance().getResource("RecureDepthSelector.Label"));
		GridData data = new GridData();
		label.setLayoutData(data);
		
		//getting strings for options
		final String empty = SVNTeamUIPlugin.instance().getResource("RecureDepthSelector.Empty");
		final String files = SVNTeamUIPlugin.instance().getResource("RecureDepthSelector.Files");
		final String immediates = SVNTeamUIPlugin.instance().getResource("RecureDepthSelector.Immediates");
		final String infinity = SVNTeamUIPlugin.instance().getResource("RecureDepthSelector.Infinity");
		
		Combo depthSelector = new Combo(this, SWT.READ_ONLY);
		depthSelector.add(empty);
		depthSelector.add(files);
		depthSelector.add(immediates);
		depthSelector.add(infinity);
		depthSelector.setText(infinity);
		depthSelector.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
			public void widgetSelected(SelectionEvent e) {
				if (((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(infinity)) {
					RecureDepthSelector.this.recureDepth = Depth.INFINITY;
				}
				else if(((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(immediates)) {
					RecureDepthSelector.this.recureDepth = Depth.IMMEDIATES;
				}
				else if(((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(files)) {
					RecureDepthSelector.this.recureDepth = Depth.FILES;
				}
				else {
					RecureDepthSelector.this.recureDepth = Depth.EMPTY;
				}
			}
			
		});
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 100;
		depthSelector.setLayoutData(data);
	}
	
	public int getRescureDepth() {
		return this.recureDepth;
	}

}
