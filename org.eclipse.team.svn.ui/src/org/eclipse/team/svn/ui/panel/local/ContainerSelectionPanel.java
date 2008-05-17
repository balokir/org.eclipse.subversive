/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.SVNContainerSelectionGroup;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.IDialogManager;
import org.eclipse.team.svn.ui.verifier.AbstractFormattedVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.ResourceNameVerifier;
import org.eclipse.ui.internal.ide.misc.ContainerSelectionGroup;

/**
 * Container selection panel implementation
 * 
 * @author Sergiy Logvin
 */
public class ContainerSelectionPanel extends AbstractDialogPanel {
	protected ContainerSelectionGroup group;
	protected Button copyWithHistoryButton;
	protected IContainer initialRoot;
	protected Text nameBox;
	protected Button overrideResourceNameButton;
	
	protected IResource []resources;
	
	protected IPath selectedPath;
	protected String defaultConflictMessage;
	protected int numConflicts;
	protected boolean copyWithHistorySelected;
	protected String name;
	protected boolean overrideResourceName;
    
	public ContainerSelectionPanel(IResource []resources, HashSet conflicts) {
	 	super();
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.Title");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.Description");
		this.selectedPath = null;
		this.initialRoot = resources[0].getParent();
		this.resources = resources;
		this.numConflicts = conflicts.size();
		this.defaultConflictMessage = "";
		this.getDefaultConflictMessage(conflicts);
		this.defaultConflictMessage = this.getDefaultConflictMessage(conflicts);
		this.defaultMessage = conflicts.size() == 0 ? SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.Message") : this.defaultConflictMessage;
    }

    public IPath getSelectedPath() {
    	return this.selectedPath;
    }
    
    public boolean isOverrideResourceName() {
    	return this.overrideResourceName;
    }
    
    public String getOverridenName() {
    	return this.name;
    }
    
    public boolean isCopyWithHistorySelected() {
    	return this.copyWithHistorySelected;
    }
    
    public void createControlsImpl(Composite parent) {
        GridData data = null;
        
        this.group = new SVNContainerSelectionGroup(parent, new Listener() {
            public void handleEvent(Event event) {
            	ContainerSelectionPanel.this.validateContent();
            }
        });
        this.group.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.attachTo(this.group, new ContainerSelectionVerifier(this.defaultConflictMessage));
        
    	this.overrideResourceNameButton = new Button(parent, SWT.CHECK);
    	this.overrideResourceNameButton.setLayoutData(new GridData());
    	this.overrideResourceNameButton.setText(SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.NewName"));
    	this.overrideResourceNameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ContainerSelectionPanel.this.nameBox.setEnabled(((Button)e.widget).getSelection());
				ContainerSelectionPanel.this.validateContent();
			}
		});
    	this.overrideResourceNameButton.setSelection(false);
    	
    	this.nameBox = new Text(parent, SWT.BORDER | SWT.SINGLE);
    	data = new GridData(GridData.FILL_HORIZONTAL);
    	this.nameBox.setLayoutData(data);
    	this.nameBox.setText(this.resources[0].getName());
    	this.nameBox.setEnabled(false);
    	CompositeVerifier verifier = new CompositeVerifier();
    	String name = SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.NewName.Verifier");
    	verifier.add(new NonEmptyFieldVerifier(name));
    	verifier.add(new ResourceNameVerifier(name, true));
    	verifier.add(new AbstractFormattedVerifier(name) {
			protected String getWarningMessageImpl(Control input) {
				return null;
			}
			protected String getErrorMessageImpl(Control input) {
				IPath path = ContainerSelectionPanel.this.group.getContainerFullPath();
				if (path != null && ResourcesPlugin.getWorkspace().getRoot().findMember(path.append(this.getText(input))) != null) {
					return SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.NewName.Verifier.Error");
				}
				return null;
			}
		});
        this.attachTo(this.nameBox, new AbstractVerifierProxy(verifier) {
			protected boolean isVerificationEnabled(Control input) {
				return ContainerSelectionPanel.this.overrideResourceNameButton.getSelection();
			}
		});
        
        this.copyWithHistoryButton = new Button(parent, SWT.CHECK);
        this.copyWithHistoryButton.setLayoutData(new GridData());
        this.copyWithHistoryButton.setText(SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.KeepHistory"));
        this.copyWithHistoryButton.setSelection(true);
        this.copyWithHistoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ContainerSelectionPanel.this.validateContent();
			}
		});
    }
    
    public void postInit() {
    	super.postInit();
        if (this.initialRoot != null) {
     		this.group.setSelectedContainer(this.initialRoot);
     	}
        if (this.numConflicts == 0) {
        	this.manager.setMessage(IDialogManager.LEVEL_OK, null);
        }
        else {
        	this.manager.setMessage(IDialogManager.LEVEL_WARNING, null);
        }
    }
    
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.copyMoveToDialogContext";
	}
    
    protected void saveChangesImpl() {
    	this.selectedPath = this.group.getContainerFullPath();
    	this.copyWithHistorySelected = this.copyWithHistoryButton.getSelection();
    	this.name = this.nameBox.getText().trim();
    	this.overrideResourceName = this.overrideResourceNameButton.getSelection();
    }

    protected void cancelChangesImpl() {
    }
    
	protected String getDefaultConflictMessage(HashSet conflicts) {
		if (conflicts.size() == 0) {
			return null;
		}
		int numberOfConflicts = 0;
		String message = "";
		for (Iterator iter = conflicts.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			numberOfConflicts++;
			if (numberOfConflicts < 4) {
				message += numberOfConflicts == 1 ? "'" + element + "'" : ", '" + element + "'";
			}
		}
		if (numberOfConflicts >= 4) {
			message += "...";
    	}
		return SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.ConflictMessage", new String[] {message});
	}
	
    public class ContainerSelectionVerifier extends SVNContainerSelectionGroup.SVNContainerSelectionVerifier {
    	protected String SOME_RESOURCES_IN_CONFLICT_MESSAGE;
    	protected String SOME_RESOURCE_IN_CONFLICT_MESSAGE;
    	protected String ALL_RESOURCES_IN_CONFLICT_MESSAGE;
    	protected String conflictedResources;
    	protected String defaultConflictingResourcesNames;
    	
    	public ContainerSelectionVerifier(String conflictingResourcesNames) {
            super();
            this.conflictedResources = "";
            this.defaultConflictingResourcesNames = conflictingResourcesNames;
            this.ALL_RESOURCES_IN_CONFLICT_MESSAGE = SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.Selection.Verifier.AllInConflict");
            this.SOME_RESOURCE_IN_CONFLICT_MESSAGE = SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.Selection.Verifier.SomeInConflict.Single");
            this.SOME_RESOURCES_IN_CONFLICT_MESSAGE = SVNTeamUIPlugin.instance().getResource("ContainerSelectionPanel.Selection.Verifier.SomeInConflict.Multi");
        }
    	
        protected String getErrorMessageImpl(Control input) {
        	SVNContainerSelectionGroup control = (SVNContainerSelectionGroup)input;
        	if (this.findConflicts(control) == ContainerSelectionPanel.this.resources.length) {
        		return this.ALL_RESOURCES_IN_CONFLICT_MESSAGE;	
        	}
            return super.getErrorMessageImpl(input);
        }
        
        protected String getWarningMessageImpl(Control input) {
        	SVNContainerSelectionGroup control = (SVNContainerSelectionGroup)input;
        	int numberconflicts = this.findConflicts(control);
        	if (numberconflicts == 1) {
        		return MessageFormat.format(this.SOME_RESOURCE_IN_CONFLICT_MESSAGE, new Object[] {this.conflictedResources});
        	}
        	if (numberconflicts > 1) {
        		return MessageFormat.format(this.SOME_RESOURCES_IN_CONFLICT_MESSAGE, new Object[] {this.conflictedResources});
        	}
        	if (this.defaultConflictingResourcesNames != null) {
        		return this.defaultConflictingResourcesNames;
        	}
        	
        	return null;
        }
        
        protected int findConflicts(SVNContainerSelectionGroup control) {
        	IPath containerPath = control.getContainerFullPath();
        	if (containerPath == null) {
        		return 0;
        	}
        	if (ContainerSelectionPanel.this.overrideResourceNameButton.getSelection()) {
        		containerPath = containerPath.append(ContainerSelectionPanel.this.nameBox.getText().trim());
        		if (ContainerSelectionPanel.this.resources.length == 1) {
        			if (ResourcesPlugin.getWorkspace().getRoot().findMember(containerPath) != null) {
        	        	this.conflictedResources = containerPath.lastSegment();
        				return 1;
        			}
        		}
        	}
        	ArrayList<IResource> destResources = new ArrayList<IResource>();
        	int numberOfConflictedResources = 0;
        	for (int i = 0; i < ContainerSelectionPanel.this.resources.length; i++) {
    			IPath dest = containerPath.append(ContainerSelectionPanel.this.resources[i].getName());
    			IResource destinationResource = ResourcesPlugin.getWorkspace().getRoot().findMember(dest);
    			if (destinationResource != null) {
    				numberOfConflictedResources++;
    				destResources.add(destinationResource);
    			}
        	}
        	this.conflictedResources = FileUtility.getNamesListAsString(destResources.toArray());
        	return numberOfConflictedResources;
        }
        
        protected boolean isNonSVNCheckDisabled() {
        	return ContainerSelectionPanel.this.copyWithHistoryButton.getSelection();
        }
	
    }
	    
}
