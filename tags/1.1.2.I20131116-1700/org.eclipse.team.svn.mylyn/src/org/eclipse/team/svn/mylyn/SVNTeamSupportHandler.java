package org.eclipse.team.svn.mylyn;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.bugs.AbstractSupportHandler;
import org.eclipse.mylyn.tasks.bugs.ISupportRequest;
import org.eclipse.mylyn.tasks.bugs.ISupportResponse;
import org.eclipse.mylyn.tasks.bugs.ITaskContribution;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.ui.debugmail.ReportPartsFactory;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;

public class SVNTeamSupportHandler extends AbstractSupportHandler {
	
	@Override
	public void preProcess(ISupportRequest request) {
		super.preProcess(request);
		ITaskContribution contribution = request.getDefaultContribution();
		if (this.isSubversiveReport(contribution)) {
			this.appendToDescription(contribution);
		}
	}
	
	@Override
	public void process(ITaskContribution contribution, IProgressMonitor monitor) {
		super.process(contribution, monitor);
//		if (this.isSubversiveReport(contribution)) {
//			this.appendToDescription(contribution);
//		}
	}
	
	@Override
	public void postProcess(ISupportResponse response, IProgressMonitor monitor) {
		super.postProcess(response, monitor);
	}
	
	protected boolean isSubversiveReport(ITaskContribution contribution) {
		return SVNTeamPlugin.NATURE_ID.equals(contribution.getStatus().getPlugin());
	}
	
	protected void appendToDescription(ITaskContribution contribution) {
		IStatus status = contribution.getStatus();
		String plugin = status.getPlugin();
		if (!SVNTeamPlugin.NATURE_ID.equals(plugin)) {
			return;
		}
		
		IReportingDescriptor []providers = ExtensionsManager.getInstance().getReportingDescriptors();
		String report = "";
		for (int i = 0; i < providers.length; i++) {
			report += ReportPartsFactory.getProductPart(providers[i]);
			report += ReportPartsFactory.getVersionPart(providers[i]);
		}
		report += ReportPartsFactory.getSVNClientPart();
		
		if (status.getSeverity() != IStatus.OK) {
			report += ReportPartsFactory.getJVMPropertiesPart();
		}
		
		report = ReportPartsFactory.removeHTMLTags(report);
		
		contribution.appendToDescription(report);
	}
}
