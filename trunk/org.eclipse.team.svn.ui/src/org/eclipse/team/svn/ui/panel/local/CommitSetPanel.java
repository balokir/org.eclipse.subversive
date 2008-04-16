/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alessandro Nistico - [patch] Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.local;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.extension.factory.ICommentDialogPanel;
import org.eclipse.team.svn.ui.panel.common.CommentPanel;
import org.eclipse.team.svn.ui.panel.local.CommitPanel.CollectPropertiesOperation;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Commit set panel
 * 
 * @author Alessandro Nistico
 */
public class CommitSetPanel extends CommentPanel implements ICommentDialogPanel {
	public static final int MSG_CREATE = 0;
	public static final int MSG_EDIT = 1;
	
    private final ActiveChangeSet set;
	protected ResourceSelectionComposite selectionComposite;
    private Text nameText;
	protected IResource []resources;
	protected List changeListenerList;
	
	public CommitSetPanel(ActiveChangeSet set, IResource [] resources, int type) {
		super(SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Title"));
		this.set = set;
		this.resources = resources;
		switch (type) {
		case MSG_EDIT:
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Description.Edit");
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Message.Edit");
			break;
		default:
			this.dialogDescription = SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Description.New");
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Message.New");
		}
		
        if (resources == null) {
        	resources = set.getResources();
        }

		this.changeListenerList = new ArrayList();
    }
    
	public void createControlsImpl(Composite parent) {
    	GridData data = null;
    	GridLayout layout = null;

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout nameLayout = new GridLayout();
		nameLayout.marginWidth = nameLayout.marginHeight = 0;
		nameLayout.numColumns = 2;
		composite.setLayout(nameLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(parent.getFont());

		Label label = new Label(composite, SWT.NONE);
		label.setText(SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Name")); 
		label.setLayoutData(new GridData(GridData.BEGINNING));
		
		this.nameText = new Text(composite, SWT.BORDER);
		this.nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        String initialText = this.set.getTitle();
        if (initialText == null) {
        	initialText = "";
        }
        this.nameText.setText(initialText);
		this.attachTo(this.nameText, new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Name.Verifier")));
        
		Group group = new Group(parent, SWT.NULL);
    	layout = new GridLayout();
		group.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNTeamUIPlugin.instance().getResource("CommitSetPanel.Comment"));
		
		CommitPanel.CollectPropertiesOperation op = new CollectPropertiesOperation(this.resources);
    	UIMonitorUtility.doTaskNowDefault(op, true);
		
		this.bugtraqModel = op.getBugtraqModel();
    	this.comment = new CommentComposite(group, this.set.getComment(), this, op.getLogTemplates(), null, op.getMinLogSize(), op.getMaxLogWidth());
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);
    }
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.commitSetDialogContext";
    }
    
    public IResource []getSelectedResources() {
    	return this.selectionComposite.getSelectedResources();
    }
    
    public void addResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.changeListenerList.add(listener);
	}
	
	public void removeResourcesSelectionChangedListener(IResourceSelectionChangeListener listener) {
		this.changeListenerList.remove(listener);
	}
	
	public void fireResourcesSelectionChanged(ResourceSelectionChangedEvent event) {
		this.validateContent();
		IResourceSelectionChangeListener []listeners = (IResourceSelectionChangeListener [])this.changeListenerList.toArray(new IResourceSelectionChangeListener[this.changeListenerList.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].resourcesSelectionChanged(event);
		}
	}
        
    public Point getPrefferedSizeImpl() {
    	return new Point(525, SWT.DEFAULT);
    }
    
    protected void saveChangesImpl() {
    	super.saveChangesImpl();
    	this.set.setTitle(this.nameText.getText());
    	this.set.setComment(this.comment.getMessage());
    }
   
}
