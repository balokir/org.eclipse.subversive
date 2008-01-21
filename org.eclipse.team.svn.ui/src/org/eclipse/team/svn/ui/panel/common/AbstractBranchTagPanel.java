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

package org.eclipse.team.svn.ui.panel.common;

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
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.composite.CommentComposite;
import org.eclipse.team.svn.ui.composite.ResourceSelectionComposite;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.event.IResourceSelectionChangeListener;
import org.eclipse.team.svn.ui.event.ResourceSelectionChangedEvent;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbsolutePathVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.verifier.URLVerifier;

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

	public AbstractBranchTagPanel(IRepositoryRoot root, boolean showStartsWith, Set existingNames, String nationalizationId, String historyName) {
		this(root, showStartsWith, existingNames, nationalizationId, historyName, new IResource[0]);
	}
	
	public AbstractBranchTagPanel(IRepositoryRoot root, boolean showStartsWith, Set existingNames, String nationalizationId, String historyName, IResource[] resources) {
		super();
		this.nationalizationId = nationalizationId;
		this.historyName = historyName;

		this.newResources = FileUtility.getResourcesRecursive(resources, IStateFilter.SF_NEW, IResource.DEPTH_INFINITE);
		this.disableSwitch = FileUtility.checkForResourcesPresence(resources, new IStateFilter.AbstractStateFilter() {
			protected boolean acceptImpl(ILocalResource local, IResource resource, String state, int mask) {
				return state == IStateFilter.ST_ADDED;
			}
			protected boolean allowsRecursionImpl(ILocalResource local, IResource resource, String state, int mask) {
				return true;
			}
		}, IResource.DEPTH_INFINITE);

		this.dialogTitle = SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".Title");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".Description");
		if (SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME)) {
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".MessageAuto");
		}
		else {
			this.defaultMessage = SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".Message");
		}

		this.existingNodesNamesSet = existingNames;
		this.root = root;
		this.startsWith = showStartsWith;
		this.considerStructure = root.getRepositoryLocation().isStructureEnabled()
				&& SVNTeamPreferences.getRepositoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME);
	}
	
	public IResource[] getSelectedResources() {
		return this.resourceSelection == null ? null : this.resourceSelection.getSelectedResources();
	}

	public IResource[] getNotSelectedResources() {
		return this.resourceSelection == null ? null : this.resourceSelection.getNotSelectedResources();
	}

	public boolean isFreezeExternals() {
		return this.freezeExternals;
	}

	public String getMessage() {
		return this.comment.getMessage();
	}

	public IRepositoryResource getDestination() {
		this.destinationUrl = this.destinationUrl.trim();
		while (this.destinationUrl.endsWith("/") || this.destinationUrl.endsWith("\\")) {
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
	}

	public void createControlsImpl(Composite parent) {
		GridData data = null;

		GridLayout layout = new GridLayout();
		Composite select = null;
		String substitutionUppercase = SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".NodeName");
		if (this.startsWith) {
			select = new Group(parent, SWT.NULL);
			layout.numColumns = 2;
			((Group) select).setText(this.considerStructure ? substitutionUppercase : SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".Location.Group"));
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
			description.setText(this.considerStructure ? substitutionUppercase : SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".Location.Field"));
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
			this.startWithCheck.setText(SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".StartsWith"));
			this.startWithCheck.setSelection(false);
			this.startWithCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					AbstractBranchTagPanel.this.validateContent();
				}
			});

			this.freezeExternalsCheck = new Button(inner, SWT.CHECK);
			data = new GridData(GridData.FILL_HORIZONTAL);
			this.freezeExternalsCheck.setLayoutData(data);
			this.freezeExternalsCheck.setText(SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".FreezeExternals"));
			this.freezeExternalsCheck.setSelection(false);
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
		group.setText(SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".Comment"));

		this.comment = new CommentComposite(group, this);
		data = new GridData(GridData.FILL_BOTH);
		this.comment.setLayoutData(data);

		if (this.startsWith && this.newResources != null && this.newResources.length > 0) {
			this.resourceSelection = new ResourceSelectionComposite(splitter, SWT.NONE, this.newResources, true);
			data = new GridData(GridData.FILL_BOTH);
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
						return AbstractBranchTagPanel.this.defaultMessage + " " + SVNTeamUIPlugin.instance().getResource(AbstractBranchTagPanel.this.nationalizationId + ".Warning");
					}
					return null;
				}
				protected String getErrorMessage(Control input) {
					return null;
				}
			});
			splitter.setWeights(new int[] { 1, 1 });
		}
		else {
			splitter.setWeights(new int[] { 1 });
		}
	}

	protected Composite createTopPart(Composite select, final String substitutionUppercase) {
		this.destinationUrl = this.root.getUrl();

		this.destinationCombo = new Combo(select, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		this.destinationCombo.setLayoutData(data);

		Button browse = new Button(select, SWT.PUSH);
		browse.setText(SVNTeamUIPlugin.instance().getResource("Button.Browse"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(browse);
		browse.setLayoutData(data);

		CompositeVerifier verifier = new CompositeVerifier();

		if (!this.considerStructure) {
			this.resourceNameHistory = new UserInputHistory(this.historyName);
			this.destinationCombo.setText(this.destinationUrl);
			String name = SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".Location.Verifier");
			verifier.add(new URLVerifier(name));
			verifier.add(new AbsolutePathVerifier(name));
			verifier.add(new AbstractVerifier() {
				protected String getErrorMessage(Control input) {
					String url = AbstractBranchTagPanel.this.root.getRepositoryLocation().getUrl();
					if (!AbstractBranchTagPanel.this.destinationCombo.getText().startsWith(url)) {
						return SVNTeamUIPlugin.instance().getResource(AbstractBranchTagPanel.this.nationalizationId + ".Location.Verifier.DoesNotCorresponds", new String[] {AbstractBranchTagPanel.this.destinationCombo.getText(), url});
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
						return SVNTeamUIPlugin.instance().getResource(AbstractBranchTagPanel.this.nationalizationId + ".Location.Verifier.NoTagName");
					}
					return null;
				}

				protected String getWarningMessage(Control input) {
					return null;
				}
			});

			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RepositoryBrowsingPanel panel = new RepositoryBrowsingPanel(SVNTeamUIPlugin.instance().getResource(
							AbstractBranchTagPanel.this.nationalizationId + ".SelectionProposal"), AbstractBranchTagPanel.this.root.getRepositoryLocation());
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
			this.resourceNameHistory = new UserInputHistory(this.historyName + "Name");

			String name = SVNTeamUIPlugin.instance().getResource(this.nationalizationId + ".NodeName.Verifier");
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
					String name = AbstractBranchTagPanel.this.destinationCombo.getText();
					if (AbstractBranchTagPanel.this.existingNodesNamesSet != null && AbstractBranchTagPanel.this.existingNodesNamesSet.contains(name)) {
						return SVNTeamUIPlugin.instance().getResource(AbstractBranchTagPanel.this.nationalizationId + ".NodeName.Verifier.Error.Exists", new String[] {name});
					}
					return null;
				}

				protected String getWarningMessage(Control input) {
					return null;
				}
			});

			browse.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					RepositoryBrowsingPanel panel = new RepositoryBrowsingPanel(SVNTeamUIPlugin.instance().getResource(
							AbstractBranchTagPanel.this.nationalizationId + ".SelectionProposal"), AbstractBranchTagPanel.this.root);
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
			this.destinationUrl = this.destinationUrl + "/" + this.destinationCombo.getText();
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

}
