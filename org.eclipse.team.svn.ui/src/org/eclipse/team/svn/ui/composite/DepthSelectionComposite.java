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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.connector.ISVNConnector.Depth;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;
import org.eclipse.team.svn.ui.verifier.IValidationManager;

/**
 * A UI component for selecting the recursion depth
 * 
 * @author Alexei Goncharov
 */
public class DepthSelectionComposite extends Composite {	
	
	//getting strings for options
	protected final static String empty = SVNUIMessages.RecurseDepthSelector_Empty;
	protected final static String files = SVNUIMessages.RecurseDepthSelector_Files;
	protected final static String immediates = SVNUIMessages.RecurseDepthSelector_Immediates;
	protected final static String infinity = SVNUIMessages.RecurseDepthSelector_Infinity;
	protected final static String exclude = SVNUIMessages.RecurseDepthSelector_Exclude;
	protected final static String unknown = SVNUIMessages.RecurseDepthSelector_Unknown;
			
	protected boolean useWorkingCopyDepth;
			
	protected boolean supportSetDepth;	
	//if there were selected several resources to update then there's not sense to allow to specify path
	protected boolean isShowUpdateDepthPath;
	protected IRepositoryResource resource;
	protected IValidationManager validationManager;
		
	//output
	protected int depth;
	protected boolean isStickyDepth;
	protected String updatePath;
	
	protected Combo depthSelector;
	protected Button updateDepthButton;
	protected Text pathInput;
	protected Button browseButton;
	protected RepositoryResourceSelectionComposite selectionComposite;	
	
	protected boolean svn15compatible;
	protected boolean svn16compatible;
	
	public DepthSelectionComposite(Composite parent, int style, boolean useWorkingCopyDepth) {
		this(parent, style, useWorkingCopyDepth, false, false, null, null);
	}
	
	public DepthSelectionComposite(Composite parent, int style, boolean useWorkingCopyDepth, boolean supportSetDepth, boolean canShowUpdateDepthPath, IRepositoryResource resource, IValidationManager validationManager) {
		super(parent, style);
		
		this.svn15compatible = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_5_x;
		this.svn16compatible = CoreExtensionsManager.instance().getSVNConnectorFactory().getSVNAPIVersion() >= ISVNConnectorFactory.APICompatibility.SVNAPI_1_6_x;

		this.supportSetDepth = supportSetDepth && this.svn15compatible;		
		this.isStickyDepth = false;		
		this.isShowUpdateDepthPath = this.supportSetDepth && canShowUpdateDepthPath;
		this.resource = resource;
		this.validationManager = validationManager;
		
		if (useWorkingCopyDepth && this.svn15compatible) {
			this.useWorkingCopyDepth = true;
			this.depth = this.isStickyDepth ? Depth.INFINITY : Depth.UNKNOWN;
		} else {
			this.useWorkingCopyDepth = false;
			this.depth = Depth.INFINITY;			
		}
		
		this.createControls();
	}
	
	protected void createControls() {		
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);
		
		Composite parent;
		if (this.isShowUpdateDepthPath) {
			Group group = new Group(this, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 3;
			group.setLayout(layout);	
			GridData data = new GridData(GridData.FILL_HORIZONTAL);		
			group.setLayoutData(data);
			group.setText(SVNUIMessages.DepthSelectionComposite_DepthGroup);
			
			parent = group;
		} else {
			layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 0;
			layout.numColumns = this.supportSetDepth ? 3 : 2;
			this.setLayout(layout);
			
			parent = this;
		}
								
		Label label = new Label(parent, SWT.NONE);
		label.setText(SVNUIMessages.RecurseDepthSelector_Label);
		GridData data = new GridData();
		label.setLayoutData(data);			
		
		this.depthSelector = new Combo(parent, SWT.READ_ONLY);		
		this.depthSelector.setText(infinity);		
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.depthSelector.setLayoutData(data);
		this.depthSelector.setVisibleItemCount(6);
		
		if (this.supportSetDepth) {
			this.updateDepthButton = new Button(parent, SWT.CHECK);
			this.updateDepthButton.setLayoutData(new GridData());
			this.updateDepthButton.setText(SVNUIMessages.DepthSelectionComposite_UpdateDepth);
			this.updateDepthButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					DepthSelectionComposite.this.refreshStickyDepth();					
				}
			});
		}
		
		if (this.isShowUpdateDepthPath) {
			Label pathLabel = new Label(parent, SWT.NONE);
			pathLabel.setLayoutData(new GridData());
			pathLabel.setText(SVNUIMessages.DepthSelectionComposite_PathLabel);
			
			this.pathInput = new Text(parent, SWT.BORDER | SWT.SINGLE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.pathInput.setLayoutData(data);						
			this.pathInput.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					DepthSelectionComposite.this.updatePath = DepthSelectionComposite.this.pathInput.getText();
				}			
			});			
						
			this.browseButton = new Button(parent, SWT.PUSH);			
			this.browseButton.setText(SVNUIMessages.Button_Browse);
			data = new GridData();
			data.widthHint = DefaultDialog.computeButtonWidth(this.browseButton);
			this.browseButton.setLayoutData(data);
						
			this.browseButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					DepthSelectionComposite.this.showPathSelectionPanel();
				}				
			});
		}
		
		this.depthSelector.addSelectionListener(new SelectionListener() {
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
				else if (((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(DepthSelectionComposite.unknown)){
					DepthSelectionComposite.this.depth = Depth.UNKNOWN;
				}
				else if (((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()).equals(exclude)){
					DepthSelectionComposite.this.depth = Depth.EXCLUDE;
				}
				else {
					DepthSelectionComposite.this.depth = Depth.EMPTY;
				}
			}			
		});
		
		//init values
		if (this.svn15compatible) {
			this.depthSelector.add(empty);
		}
		this.depthSelector.add(files);
		if (this.svn15compatible) {
			this.depthSelector.add(immediates);
		}
		this.depthSelector.add(infinity);											
		
		if (this.supportSetDepth) {
			this.updateDepthButton.setSelection(this.isStickyDepth);
			this.refreshStickyDepth();
		} else if (this.useWorkingCopyDepth) {
			this.depthSelector.add(unknown);
		}
		
		//set depth
		this.setDepthComboValue();		
	}
	
	protected void setDepthComboValue() {
		String strDepth;
		switch (this.depth) {
			case Depth.INFINITY:
				strDepth = infinity;
			break;
			case Depth.IMMEDIATES:
				strDepth = immediates;
			break;
			case Depth.FILES:
				strDepth = files;
			break;
			case Depth.UNKNOWN:
				strDepth = unknown;
			break;
			case Depth.EXCLUDE:
				strDepth = exclude;
			break;
			default:
				strDepth = empty;
		}
		int index = this.depthSelector.indexOf(strDepth);
		if (index == -1) {
			index = 0;
		}
		this.depthSelector.select(index);
	}
	
	protected void refreshStickyDepth() {
		this.isStickyDepth = this.updateDepthButton.getSelection();
		
		if (this.isShowUpdateDepthPath) {
			this.pathInput.setEnabled(this.isStickyDepth);
			this.browseButton.setEnabled(this.isStickyDepth);	
		}
		
		//add or remove 'exclude'
		if (this.svn16compatible) {
			if (this.isStickyDepth) {
				this.depthSelector.add(exclude);	
			} else {
				int index = this.depthSelector.indexOf(exclude);
				if (index != -1) {
					int selectionIndex = this.depthSelector.getSelectionIndex();
					this.depthSelector.remove(index);
					if (index == selectionIndex) {
						this.depthSelector.select(0);
					}
				}
			}			
		}
		
		//add or remove 'working copy'
		if (this.useWorkingCopyDepth) {
			if (this.isStickyDepth) {
				int index = this.depthSelector.indexOf(unknown);
				if (index != -1) {
					int selectionIndex = this.depthSelector.getSelectionIndex();
					this.depthSelector.remove(index);
					if (index == selectionIndex) {
						this.depthSelector.select(0);
					}
				}
			} else {
				this.depthSelector.add(unknown);
			}	
		}
	}
	
	protected void showPathSelectionPanel() {
		RepositoryTreePanel panel = new RepositoryTreePanel(
	        	SVNUIMessages.RepositoryResourceSelectionComposite_Select_Title, 
				SVNUIMessages.DepthSelectionComposite_RepositoryPanelDescription,
				SVNUIMessages.DepthSelectionComposite_RepositoryPanelMessage,
				new IRepositoryResource[0], 
				false, this.resource, false);			
		DefaultDialog browser = new DefaultDialog(this.getShell(), panel);
		if (browser.open() == 0) {
			IRepositoryResource selected = panel.getSelectedResource();
			if (selected != null) {
				this.pathInput.setText(selected.getName());
			}			
		}			
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public boolean isStickyDepth() {
		return this.isStickyDepth;
	}
	
	public String getUpdatePath() {
		return this.updatePath != null ? this.updatePath.trim() : null;
	}

}
