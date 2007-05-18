/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package org.eclipse.team.svn.ui.wizard.checkoutas;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.PatternProvider;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.verifier.AbstractVerifier;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.CompositeVerifier;
import org.eclipse.team.svn.ui.verifier.ExistingResourceVerifier;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;
import org.eclipse.team.svn.ui.wizard.AbstractVerifiedWizardPage;

/**
 * Project location selection page
 * 
 * @author Alexander Gurov
 */
public class ProjectLocationSelectionPage extends AbstractVerifiedWizardPage {
	protected static String DEFAULT_WORKING_SET;
	
	protected String location;
	protected String defaultLocation;
	protected String workingSetName;
	protected boolean useDefaultLocation;
	protected ProjectsSelectionPage projectsSelectionPage;
	
	protected Button browse;
	protected Button useDefaultLocationButton;
	protected Combo workingSetNameCombo;
	protected Text locationField;
	protected boolean sinceEclipse_3_2;

	public ProjectLocationSelectionPage(boolean multiple, ProjectsSelectionPage projectsSelectionPage) {
		super(ProjectLocationSelectionPage.class.getName(), 
			SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.Title"), 
			SVNTeamUIPlugin.instance().getImageDescriptor("icons/wizards/newconnect.gif"));
		
		ProjectLocationSelectionPage.DEFAULT_WORKING_SET = SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.DefaultWS");
		
		this.setDescription(SVNTeamUIPlugin.instance().getResource(multiple ? "ProjectLocationSelectionPage.Description.Multi" : "ProjectLocationSelectionPage.Description.Single"));
		String framework = System.getProperties().getProperty("osgi.framework.version");
		try {
			int version = Integer.parseInt(PatternProvider.replaceAll(framework, "\\.", "").substring(0, 2));
			this.sinceEclipse_3_2 = version >= 32;
		}
		catch (NumberFormatException e) {
			this.sinceEclipse_3_2 = false;
		}
		
		this.location = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		this.projectsSelectionPage = projectsSelectionPage;
		this.defaultLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
	}
	
	public String getLocation() {
		return this.useDefaultLocation ? this.defaultLocation : this.location;
	}

	public String getWorkingSetName() {
		return this.workingSetName.equals(ProjectLocationSelectionPage.DEFAULT_WORKING_SET) ? null : this.workingSetName;
	}
	
	public void setUseDefaultLocation(boolean defaultLocation) {
		if (!this.sinceEclipse_3_2) {
			this.useDefaultLocationButton.setSelection(defaultLocation);
			this.useDefaultLocationButton.setEnabled(defaultLocation);
			this.refreshControls();
		}
	}

	protected Composite createControlImpl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		// GridLayout
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		// GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		
		this.setControl(composite);
		
		Group locationSelectionGroup = new Group(composite, SWT.NONE);
		locationSelectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		layout.numColumns = 2;
		locationSelectionGroup.setLayout(layout);
		locationSelectionGroup.setText(SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.Location"));

		this.useDefaultLocationButton = new Button(locationSelectionGroup, SWT.CHECK);

		this.locationField = new Text(locationSelectionGroup, SWT.SINGLE | SWT.BORDER);
		this.browse = new Button(locationSelectionGroup, SWT.PUSH);

		data = new GridData();
		data.horizontalSpan = 2;
		this.useDefaultLocationButton.setLayoutData(data);
		this.useDefaultLocationButton.setSelection(true);
		this.useDefaultLocationButton.setText(SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.UseDefaultLocation"));
		this.useDefaultLocationButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ProjectLocationSelectionPage.this.validateContent();
				ProjectLocationSelectionPage.this.refreshControls();
			}
		});
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 300;
		this.locationField.setLayoutData(data);
		this.locationField.setText(this.location);
		this.locationField.setEnabled(false);
		CompositeVerifier verifier = new CompositeVerifier();
		verifier.add(new ProjectLocationSelectionPage.LocationVerifier(this.projectsSelectionPage, this.defaultLocation, this.sinceEclipse_3_2, this.useDefaultLocationButton));
		verifier.add(new AbstractVerifierProxy(new ExistingResourceVerifier(SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.Location.Verifier"), false)) {
			protected boolean isVerificationEnabled(Control input) {
				return !ProjectLocationSelectionPage.this.useDefaultLocationButton.getSelection();
			}
		});
		this.attachTo(this.locationField, verifier);
		this.locationField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				ProjectLocationSelectionPage.this.location = ProjectLocationSelectionPage.this.locationField.getText();
			}
		});
		
		this.browse.setText(SVNTeamUIPlugin.instance().getResource("Button.Browse"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(this.browse);
		this.browse.setLayoutData(data);		
		this.browse.setEnabled(false);
		this.browse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog fileDialog = new DirectoryDialog(getShell());
				String res = fileDialog.open();
				if (res != null) {
					ProjectLocationSelectionPage.this.locationField.setText(res);
				}
			}
		});
		
		Composite workingSetComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.numColumns = 2;
		data = new GridData(GridData.FILL_HORIZONTAL);
		workingSetComposite.setLayout(layout);
		workingSetComposite.setLayoutData(data);
		
		Label wSetLabel = new Label(workingSetComposite, SWT.NONE);
		wSetLabel.setText(SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.SelectWS"));
		
		this.workingSetNameCombo = new Combo(workingSetComposite, SWT.NULL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.workingSetNameCombo.setLayoutData(data);
		String []wSetNames = this.getWorkingSetNames();
		List names = new ArrayList();
		names.add(ProjectLocationSelectionPage.DEFAULT_WORKING_SET);
		names.addAll(Arrays.asList(wSetNames));
		this.workingSetNameCombo.setItems((String[])names.toArray(new String[names.size()]));
		this.workingSetNameCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				ProjectLocationSelectionPage.this.workingSetName = ProjectLocationSelectionPage.this.workingSetNameCombo.getText();
			}
		});
		this.workingSetNameCombo.setText(ProjectLocationSelectionPage.DEFAULT_WORKING_SET);
		this.attachTo(this.workingSetNameCombo, new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.WorkingSet.Verifier")));
		
		return composite;
	}
	
	protected void refreshControls() {
		this.useDefaultLocation = this.useDefaultLocationButton.isEnabled() && useDefaultLocationButton.getSelection();
		this.locationField.setEnabled(!this.useDefaultLocation);
		this.browse.setEnabled(!this.useDefaultLocation);
	}
	
	protected String[] getWorkingSetNames() {
		List wSetNames = new ArrayList();
		IWorkingSetManager workingSetManager = SVNTeamUIPlugin.instance().getWorkbench().getWorkingSetManager();
		IWorkingSet []workingSets = workingSetManager.getWorkingSets();
		for (int i = 0; i < workingSets.length; i++) {
			wSetNames.add(workingSets[i].getName());
		}
		return (String[])wSetNames.toArray(new String[wSetNames.size()]);
	}
	
	public static class LocationVerifier extends AbstractVerifier {
		
		protected String defaultLocation;
		protected ProjectsSelectionPage projectsSelectionPage;
		protected boolean sinceEclipse_3_2;
		protected Button useDefaultLocationButton;
		
		public LocationVerifier(ProjectsSelectionPage projectsSelectionPage, String defaultLocation, boolean sinceEclipse3_2, Button useDefaultLocationButton) {
			this.defaultLocation = defaultLocation;
			this.projectsSelectionPage = projectsSelectionPage;
			this.sinceEclipse_3_2 = sinceEclipse3_2;
			this.useDefaultLocationButton = useDefaultLocationButton;
		}

		protected String getErrorMessage(Control input) {
			boolean respectHierarchy = (this.projectsSelectionPage != null && projectsSelectionPage.isRespectHierarchy());
			String parent = this.projectsSelectionPage != null && this.projectsSelectionPage.projects != null ? SVNUtility.getResourceParent(this.projectsSelectionPage.projects[0]) : "";
			String inputLocation = this.useDefaultLocationButton.getSelection() ? this.defaultLocation : FileUtility.formatPath(this.getText(input));
			if (inputLocation.startsWith(this.defaultLocation) && !this.sinceEclipse_3_2) {
				if (respectHierarchy || inputLocation.length() > this.defaultLocation.length()) {
					return SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.Location.Verifier.Error.Eclipse32");
				}
			}
			IProject []projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				IPath location = projects[i].getLocation();
				if (location != null && location.isPrefixOf(new Path(inputLocation + parent))) {
					String message = SVNTeamUIPlugin.instance().getResource("ProjectLocationSelectionPage.Location.Verifier.Error.ExistingProject");
					return MessageFormat.format(message, new String[] {projects[i].getLocation().toString()});
				}
			}
			return null;
		}

		protected String getWarningMessage(Control input) {
			return null;
		}
		
	}
	
}
