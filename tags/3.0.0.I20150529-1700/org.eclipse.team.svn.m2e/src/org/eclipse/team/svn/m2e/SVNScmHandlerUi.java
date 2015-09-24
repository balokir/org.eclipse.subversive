/*******************************************************************************
 * Copyright (c) 2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.m2e;

import org.eclipse.jface.window.Window;
import org.eclipse.m2e.scm.ScmUrl;
import org.eclipse.m2e.scm.spi.ScmHandlerUi;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.GetLogMessagesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNUIMessages;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.panel.common.RepositoryTreePanel;
import org.eclipse.team.svn.ui.panel.common.SelectRevisionPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * SVN-specific source control manager handler UI
 * 
 * @see ScmHandlerUi
 * 
 * @author Eugene Kuleshov
 */
public class SVNScmHandlerUi extends ScmHandlerUi {

	public boolean canSelectUrl() {
		return true;
	}

	public boolean canSelectRevision() {
		return true;
	}

	public ScmUrl selectUrl(Shell shell, ScmUrl scmUrl) {
		IRepositoryResource repositoryResource = this.getRepositoryResource(scmUrl);

		// if no URL selected (repositoryResource == null) then existing repository locations must be shown...
		RepositoryTreePanel panel = new RepositoryTreePanel(SVNUIMessages.RepositoryResourceSelectionComposite_Select_Title, M2ESVNPlugin.instance()
				.getResource("CheckoutAsMavenProjectWizard.OptionsPage.Selection.Title"), M2ESVNPlugin.instance().getResource(
				"CheckoutAsMavenProjectWizard.OptionsPage.Selection.Description"),
				repositoryResource == null ? new IRepositoryResource[0] : new IRepositoryResource[] { repositoryResource }, 
				true /* allowSourcesInTree */, false);
		panel.setAllowFiles(false);

		DefaultDialog browser = new DefaultDialog(shell, panel);
		if (browser.open() == Window.OK) {
			IRepositoryResource selectedResource = panel.getSelectedResource();
			return new ScmUrl(SVNScmHandler.SVN_SCM_ID + selectedResource.getUrl());
		}

		return null;
	}

	public String selectRevision(Shell shell, ScmUrl scmUrl, String scmRevision) {
		IRepositoryResource repositoryResource = getRepositoryResource(scmUrl);
		if (repositoryResource == null) {
			return null;
		}

		GetLogMessagesOperation msgsOp = SelectRevisionPanel.getMsgsOp(repositoryResource, true /* stopOnCopy */);
		if (!UIMonitorUtility.doTaskNowDefault(shell, msgsOp, true).isCancelled() && msgsOp.getExecutionState() == IActionOperation.OK) {
			long currentRevision = SVNRevision.INVALID_REVISION_NUMBER;
			try {
				//NOTE initially revision number is empty. Is it correct behaviour or not?
				if (scmRevision != null && scmRevision.length() > 0) {
					SVNRevision rev = SVNRevision.fromString(scmRevision);
					if (rev.getKind() == SVNRevision.Kind.NUMBER) {
						currentRevision = ((SVNRevision.Number)rev).getNumber();
					}
				}
			}
			catch (IllegalArgumentException ex) {
				UILoggedOperation.reportError("SVNScmHandlerUi.selectRevision", ex);
			}

			SelectRevisionPanel panel = new SelectRevisionPanel(msgsOp, false, false, currentRevision);

			DefaultDialog dialog = new DefaultDialog(shell, panel);
			if (dialog.open() == Window.OK) {
				long selectedRevisionNum = panel.getSelectedRevision();
				return Long.toString(selectedRevisionNum);
			}
		}

		return null;
	}

	private IRepositoryResource getRepositoryResource(ScmUrl scmUrl) {
		if (scmUrl == null) {
			return null;
		}
		String url = scmUrl.getUrl();
		//NOTE if user writes the URL by hands it does not start with SVNScmHandler.SVN_SCM_ID substring. Is it correct behaviour or not?
		if (url.startsWith(SVNScmHandler.SVN_SCM_ID)) {
			url = scmUrl.getUrl().substring(SVNScmHandler.SVN_SCM_ID.length());
		}
		return SVNUtility.asRepositoryResource(url, true);
	}

	public boolean isValidUrl(String scmUrl) {
		return scmUrl != null && scmUrl.startsWith(SVNScmHandler.SVN_SCM_ID) && SVNUtility.isValidSVNURL(scmUrl.substring(SVNScmHandler.SVN_SCM_ID.length()));
	}

	public boolean isValidRevision(ScmUrl scmUrl, String scmRevision) {
		try {
			SVNRevision revision = SVNRevision.fromString(scmRevision);
			SVNRevision.Kind kind = revision.getKind();
			return kind == SVNRevision.Kind.HEAD || kind == SVNRevision.Kind.NUMBER;	
		} catch (Exception e) {
			return false;
		}
	}

}
