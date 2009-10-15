/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.composite.RevisionComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.panel.participant.BasePaneParticipant;
import org.eclipse.team.svn.ui.panel.participant.PaneParticipantHelper;
import org.eclipse.team.svn.ui.panel.participant.PaneParticipantHelper.PaneVerifier;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.URLVerifier;
import org.eclipse.team.ui.synchronize.ResourceScope;

/**
 * Abstract Branch/Tag panel
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractBranchTagPanel extends AbstractDialogPanel {
	protected Button startWithCheck;
	protected Button freezeExternalsCheck;
	protected Combo destinationCombo;
	protected UserInputHistory resourceNameHistory;
	protected CommentComposite comment;
	protected String destinationUrl;
	
	protected IRepositoryRoot root;
	protected String nationalizationId;
	protected boolean startsWith;
	protected boolean freezeExternals;
	protected Set existingNodesNamesSet;
	protected boolean considerStructure;
	protected String historyName;
	
	protected ResourceSelectionComposite resourceSelection;
	protected IResource[] newResources;
	protected boolean disableSwitch;

	//used for revision selection for repository resources
	protected IRepositoryResource[] selectedRemoteResources;	
	protected RevisionComposite revisionComposite;
	
	protected PaneParticipantHelper paneParticipantHelper;
	/*
	 * As participant pane is not always present, we need to use this flag 
	 * (instead of paneParticipantHelper.isParticipantPane()) 
	 * to determine whether we can work with pane or not.
	 * (E.g. participant pane is not present if there are no new resources)
	 */
	protected boolean hasParticipantPane;
	
	public AbstractBranchTagPanel(IRepositoryRoot root, boolean showStartsWith, Set existingNames, String nationalizationId, String historyName, IRepositoryResource[] selectedRemoteResources) {
		this(root, showStartsWith, existingNames, nationalizationId, historyName, new IResource[0], selectedRemoteResources);
	}
	
	public AbstractBranchTagPanel(IRepositoryRoot root, boolean showStartsWith, Set existingNames, String nationalizationId, String historyName, IResource[] resources, IRepositoryResource[] selectedRemoteResources) {
		super();
		this.nationalizationId = nationalizationId;
		this.historyName = historyName;
		this.selectedRemoteResources = selectedRemoteResources;
		
		this.newResources = FileUtility.getResourcesRecursive(resources, IStateFilter.SF_NEW, IResource.DEPTH_INFINITE);
		this.disableSwitch = FileUtility.checkForResourcesPresence(resources, new IStateFilter.AbstractStateFilter() {
			protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
				return state == IStateFilter.ST_ADDED;
			}
			protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
				return true;
			}
		}, IResource.DEPTH_INFINITE);

		this.dialogTitle = SVNUIMessages.getString(this.nationalizationId + "_Title"); //$NON-NLS-1$
		this.dialogDescription = SVNUIMessages.getString(this.nationalizationId + "_Description"); //$NON-NLS-1$
		if (SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME)) {
			this.defaultMessage = SVNUIMessages.getString(this.nationalizationId + "_MessageAuto"); //$NON-NLS-1$
		}
		else {
			this.defaultMessage = SVNUIMessages.getString(this.nationalizationId + "_Message"); //$NON-NLS-1$
		}
		if (!showStartsWith) {
			this.defaultMessage += " " + SVNUIMessages.AbstractBranchTagPanel_Message; //$NON-NLS-1$
		}

		this.existingNodesNamesSet = existingNames;
		this.root = root;
		this.startsWith = showStartsWith;
		this.considerStructure = root.getRepositoryLocation().isStructureEnabled()
				&& SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
		
		this.paneParticipantHelper = new PaneParticipantHelper();
	}
	
	public SVNRevision getRevisionForRemoteResources() {
		return this.revisionComposite != null ? this.revisionComposite.getSelectedRevision() : null;
	}
	
	public IResource[] getSelectedResources() {
		if (this.hasParticipantPane) {			
			return this.paneParticipantHelper.getSelectedResources();
		} else {
			return this.resourceSelection == null ? new IResource[0] : this.resourceSelection.getSelectedResources();			
		}		
	}

	public IResource[] getNotSelectedResources() {
		if (this.hasParticipantPane) {    		
			return this.paneParticipantHelper.getNotSelectedResources();
    	} else {
    		return this.resourceSelection == null ? new IResource[0] : this.resourceSelection.getNotSelectedResources();
    	}
	}

	public boolean isFreezeExternals() {
		return this.freezeExternals;
	}

	public String getMessage() {
		return this.comment.getMessage();
	}

	public IRepositoryResource getDestination() {
		this.destinationUrl = this.destinationUrl.trim();
		while (this.destinationUrl.endsWith("/") || this.destinationUrl.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			this.destinationUrl = this.destinationUrl.substring(0, this.destinationUrl.length() - 1);
		}
		return this.root.getRepositoryLocation().asRepositoryContainer(this.destinationUrl, false);
	}

	public boolean isStartWithSelected() {
		return this.startsWith;
	}

	public Point getPrefferedSizeImpl() {
		return new Point(this.newResources != null && this.newResources.length > 0 ? 625 : 525, SWT.DEFAULT);
	}

	public void postInit() {
		super.postInit();
		this.comment.postInit(this.manager);
		if (this.hasParticipantPane) {
			this.paneParticipantHelper.expandPaneTree();
		}
	}

	public void createControlsImpl(Composite parent) {
		GridData data = null;

		GridLayout layout = new GridLayout();
		Composite select = null;
		String substitutionUppercase = SVNUIMessages.getString(this.nationalizationId + "_NodeName"); //$NON-NLS-1$
		if (this.startsWith) {
			select = new Group(parent, SWT.NULL);
			layout.numColumns = 2;
			((Group) select).setText(this.considerStructure ? substitutionUppercase : SVNUIMessages.getString(this.nationalizationId + "_Location_roup")); //$NON-NLS-1$
		}
		else {
			select = new Composite(parent, SWT.NONE);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 3;
		}
		select.setLayout(layout);
		select.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (!this.startsWith) {
			Label description = new Label(select, SWT.NONE);
			data = new GridData();
			description.setLayoutData(data);
			description.setText(this.considerStructure ? substitutionUppercase : SVNUIMessages.getString(this.nationalizationId + "_Location_Field")); //$NON-NLS-1$
		}
		this.createTopPart(select, substitutionUppercase);

		if (this.startsWith) {
			Composite inner = new Composite(select, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			inner.setLayoutData(data);
			layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 0;
			inner.setLayout(layout);

			this.startWithCheck = new Button(inner, SWT.CHECK);
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.startWithCheck.setLayoutData(data);
			this.startWithCheck.setText(SVNUIMessages.getString(this.nationalizationId + "_StartsWith")); //$NON-NLS-1$
			this.startWithCheck.setSelection(false);
			this.startWithCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					AbstractBranchTagPanel.this.validateContent();
				}
			});

			this.freezeExternalsCheck = new Button(inner, SWT.CHECK);
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.freezeExternalsCheck.setLayoutData(data);
			this.freezeExternalsCheck.setText(SVNUIMessages.getString(this.nationalizationId + "_FreezeExternals")); //$NON-NLS-1$
			this.freezeExternalsCheck.setSelection(false);
		}

		//revision selection
		if (!this.startsWith) {
			IRepositoryResource selectedRemoteResource;
			if (this.selectedRemoteResources.length == 1) {
				selectedRemoteResource = this.selectedRemoteResources[0];
			} else if (this.selectedRemoteResources.length > 1) {			
				selectedRemoteResource = this.selectedRemoteResources[0].getRoot();			
			} else {
				selectedRemoteResource = this.root.getRepositoryLocation().getRoot();
				selectedRemoteResource.setPegRevision(this.root.getPegRevision());
				selectedRemoteResource.setSelectedRevision(this.root.getSelectedRevision());
			}
			this.revisionComposite = new RevisionComposite(parent, this, false, new String[]{SVNUIMessages.RevisionComposite_Revision, SVNUIMessages.RevisionComposite_HeadRevision}, SVNRevision.HEAD, false);
			layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 0;
			data = new GridData(GridData.FILL_HORIZONTAL);		
			this.revisionComposite.setLayout(layout);
			this.revisionComposite.setLayoutData(data);			
			this.revisionComposite.setSelectedResource(selectedRemoteResource);			
		}

		SashForm splitter = new SashForm(parent, SWT.VERTICAL);
		data = new GridData(GridData.FILL_BOTH);
		splitter.setLayoutData(data);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = 3;
		splitter.setLayout(layout);

		Group group = new Group(splitter, SWT.NULL);
		group.setLayout(new GridLayout());
		data = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(data);
		group.setText(SVNUIMessages.getString(this.nationalizationId + "_Comment")); //$NON-NLS-1$

		this.comment = new CommentComposite(group, this);
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);

		if (this.startsWith && this.newResources != null && this.newResources.length > 0) {
			if (this.paneParticipantHelper.isParticipantPane()) {
				this.hasParticipantPane = true;
				this.paneParticipantHelper.init(this.createPaneParticipant());
				this.createPaneControls(splitter);				
			} else {
				this.createResourceSelectionCompositeControls(splitter);
			}	
			splitter.setWeights(new int[] { 1, 1 });
		}
		else {
			splitter.setWeights(new int[] { 1 });
		}
	}
	
	protected void createPaneControls(Composite parent) {		
		Control paneControl = this.paneParticipantHelper.createChangesPage(parent);
		GridData data = new GridData(GridData.FILL_BOTH);		
        paneControl.setLayoutData(data);      
        
        this.paneParticipantHelper.initListeners();        
                                          
        //add validator to pane
        this.attachTo(paneControl, new PanelPaneVerifier(this.paneParticipantHelper));            
	}
	
	protected BasePaneParticipant createPaneParticipant() {
		return new BasePaneParticipant(new ResourceScope(this.newResources), this) {
			protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
				Collection<AbstractSynchronizeActionGroup> actionGroups = new ArrayList<AbstractSynchronizeActionGroup>();
				actionGroups.add(new BasePaneActionGroup(this.validationManager));
		    	return actionGroups;
			}
		};	
	}
	
	protected void createResourceSelectionCompositeControls(Composite parent) {
		this.resourceSelection = new ResourceSelectionComposite(parent, SWT.NONE, this.newResources, true);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.resourceSelection.setLayoutData(data);
		this.resourceSelection.addResourcesSelectionChangedListener(new IResourceSelectionChangeListener() {
			public void resourcesSelectionChanged(ResourceSelectionChangedEvent event) {
				AbstractBranchTagPanel.this.validateContent();
			}
		});
		this.attachTo(this.resourceSelection, new AbstractVerifier() {
			protected String getWarningMessage(Control input) {
				IResource []resources = AbstractBranchTagPanel.this.resourceSelection.getSelectedResources();
				if ((resources != null && resources.length != 0 || AbstractBranchTagPanel.this.disableSwitch) && AbstractBranchTagPanel.this.startWithCheck.getSelection()) {
					return AbstractBranchTagPanel.this.defaultMessage + " " + SVNUIMessages.getString(AbstractBranchTagPanel.this.nationalizationId + "_Warning"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return null;
			}
			protected String getErrorMessage(Control input) {
				return null;
			}
		});		
	}
	
	protected Composite createTopPart(Composite select, final String substitutionUppercase) {
		this.destinationUrl = this.root.getUrl();

		this.destinationCombo = new Combo(select, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.destinationCombo.setLayoutData(data);

		Button browse = new Button(select, SWT.PUSH);
		browse.setText(SVNUIMessages.Button_Browse);
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(browse);
		browse.setLayoutData(data);

		CompositeVerifier verifier = new CompositeVerifier();

		if (!this.considerStructure) {
			this.resourceNameHistory = new UserInputHistory(this.historyName);
			this.destinationCombo.setText(this.destinationUrl);
			String name = SVNUIMessages.getString(this.nationalizationId + "_Location_Verifier"); //$NON-NLS-1$
			verifier.add(new URLVerifier(name));
			verifier.add(new AbsolutePathVerifier(name));
			verifier.add(new AbstractVerifier() {
				protected String getErrorMessage(Control input) {
					String url = AbstractBranchTagPanel.this.root.getRepositoryLocation().getUrl();
					if (!AbstractBranchTagPanel.this.destinationCombo.getText().startsWith(url)) {
						return SVNUIMessages.format(SVNUIMessages.getString(AbstractBranchTagPanel.this.nationalizationId + "_Location_Verifier_DoesNotCorresponds"), new String[] {AbstractBranchTagPanel.this.destinationCombo.getText(), url}); //$NON-NLS-1$
					}
					if (AbstractBranchTagPanel.this.startsWith) {
						if (!AbstractBranchTagPanel.this.destinationCombo.getText().startsWith(AbstractBranchTagPanel.this.root.getUrl())) {
							AbstractBranchTagPanel.this.startWithCheck.setSelection(false);
							AbstractBranchTagPanel.this.startWithCheck.setEnabled(false);
						}
						else {
							AbstractBranchTagPanel.this.startWithCheck.setEnabled(true);
						}
					}
					if (AbstractBranchTagPanel.this.root.getUrl().equals(SVNUtility.normalizeURL(AbstractBranchTagPanel.this.destinationCombo.getText()))) {
						return SVNUIMessages.getString(AbstractBranchTagPanel.this.nationalizationId + "_Location_Verifier_NoTagName"); //$NON-NLS-1$
					}
					return null;
				}

				protected String getWarningMessage(Control input) {
					return null;
				}
			});
			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RepositoryTreePanel panel = new RepositoryTreePanel(SVNUIMessages.getString(
							AbstractBranchTagPanel.this.nationalizationId + "_SelectionProposal"), //$NON-NLS-1$
							SVNUIMessages.RepositoryBrowsingPanel_Description,
							SVNUIMessages.RepositoryBrowsingPanel_Message,
							null,
							true,
							AbstractBranchTagPanel.this.root.getRepositoryLocation(), false);
					DefaultDialog browser = new DefaultDialog(AbstractBranchTagPanel.this.manager.getShell(), panel);
					if (browser.open() == 0) {
						IRepositoryResource selected = panel.getSelectedResource();
						if (selected != null) {
							AbstractBranchTagPanel.this.destinationCombo.setText(selected.getUrl());
						}
						AbstractBranchTagPanel.this.validateContent();
					}
				}
			});
		}
		else {
			this.resourceNameHistory = new UserInputHistory(this.historyName + "Name"); //$NON-NLS-1$

			String name = SVNUIMessages.getString(this.nationalizationId + "_NodeName_Verifier"); //$NON-NLS-1$
			verifier.add(new NonEmptyFieldVerifier(name) {
				protected String getErrorMessageImpl(Control input) {
					String msg = super.getErrorMessageImpl(input);
					if (msg == null) {
						if (new Path(this.getText(input)).segmentCount() == 0) {
							return NonEmptyFieldVerifier.ERROR_MESSAGE;
						}
					}
					return msg;
				}
			});
			verifier.add(new AbsolutePathVerifier(name));
			verifier.add(new AbstractVerifier() {
				protected String getErrorMessage(Control input) {
					return null;
				}
				
				protected String getWarningMessage(Control input) {
					String name = AbstractBranchTagPanel.this.destinationCombo.getText();
					if (AbstractBranchTagPanel.this.existingNodesNamesSet != null && AbstractBranchTagPanel.this.existingNodesNamesSet.contains(name)) {
						return SVNUIMessages.format(SVNUIMessages.getString(AbstractBranchTagPanel.this.nationalizationId + "_NodeName_Verifier_Error_Exists"), new String[] {name}); //$NON-NLS-1$
					}
					return null;
				}
			});
			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RepositoryTreePanel panel = new RepositoryTreePanel(SVNUIMessages.getString(
							AbstractBranchTagPanel.this.nationalizationId + "_SelectionProposal"), //$NON-NLS-1$
							SVNUIMessages.RepositoryBrowsingPanel_Description,
							SVNUIMessages.RepositoryBrowsingPanel_Message,
							null,
							true,
							AbstractBranchTagPanel.this.root.getRoot(), false);
					DefaultDialog browser = new DefaultDialog(AbstractBranchTagPanel.this.manager.getShell(), panel);
					if (browser.open() == 0) {
						IRepositoryResource selected = panel.getSelectedResource();
						if (selected != null) {
							AbstractBranchTagPanel.this.destinationCombo.setText(selected.getUrl().substring(AbstractBranchTagPanel.this.root.getUrl().length() + 1));
						}
						AbstractBranchTagPanel.this.validateContent();
					}
				}
			});
		}
		this.destinationCombo.setVisibleItemCount(this.resourceNameHistory.getDepth());
		this.destinationCombo.setItems(this.resourceNameHistory.getHistory());

		this.attachTo(this.destinationCombo, verifier);

		return select;
	}

	protected void saveChangesImpl() {
		if (!this.considerStructure) {
			this.destinationUrl = this.destinationCombo.getText();
			this.resourceNameHistory.addLine(this.destinationUrl);
		}
		else {
			this.destinationUrl = this.destinationUrl + "/" + this.destinationCombo.getText(); //$NON-NLS-1$
			this.resourceNameHistory.addLine(this.destinationCombo.getText());
		}

		this.comment.saveChanges();
		if (this.startWithCheck != null) {
			this.startsWith = this.startWithCheck.getSelection();
			this.freezeExternals = this.freezeExternalsCheck.getSelection();
		}
		else {
			this.startsWith = false;
			this.freezeExternals = false;
		}	
	}

	protected void cancelChangesImpl() {
		this.comment.cancelChanges();
	}

	public void dispose() {
		super.dispose();
    	if (this.hasParticipantPane) {
    		this.paneParticipantHelper.dispose();
    	}  	
	}	
	
	/*
	 * Pane validator
	 */
	protected class PanelPaneVerifier extends PaneVerifier {
		
		public PanelPaneVerifier(PaneParticipantHelper paneParticipantHelper) {
			super(paneParticipantHelper);
		}

		protected String getErrorMessage(Control input) {			
			return null;
		}
		
		protected String getWarningMessage(Control input) {
			IResource[] resourcesToProcess = this.paneParticipantHelper.getSelectedResources();
			
			if ((resourcesToProcess.length == 0 || AbstractBranchTagPanel.this.disableSwitch) && AbstractBranchTagPanel.this.startWithCheck.getSelection()) {
				return AbstractBranchTagPanel.this.defaultMessage + " " + SVNUIMessages.getString(AbstractBranchTagPanel.this.nationalizationId + "_Warning"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}	
	}	
	
	
}
