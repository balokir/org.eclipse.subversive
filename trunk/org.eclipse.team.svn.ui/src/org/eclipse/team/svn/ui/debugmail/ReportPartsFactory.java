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

package org.eclipse.team.svn.ui.debugmail;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.svn.core.extension.CoreExtensionsManager;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.ui.extension.factory.IReportingDescriptor;

/**
 * Mail reporter. Should implement only mail sending (non-UI part)
 * 
 * @author Alexander Gurov
 */
public class ReportPartsFactory {
	public static interface IStatusVisitor {
		public boolean visit(IStatus status);
	}
	
	public static boolean checkStatus(IStatus status, IStatusVisitor visitor) {
		if (!status.isMultiStatus()) {
			return visitor.visit(status);
		}
		IStatus []children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (ReportPartsFactory.checkStatus(children[i], visitor)) {
				return true;
			}
		}
		return false;
	}
	
	public static String getStackTrace(IStatus operationStatus) {
		final String []stackTrace = new String[] {""}; //$NON-NLS-1$
		ReportPartsFactory.checkStatus(operationStatus, new IStatusVisitor() {

			public boolean visit(IStatus status) {
				String trace = ReportPartsFactory.getOutput(status);
				stackTrace[0] += trace + "\n"; //$NON-NLS-1$
				return false;
			}
			
		});
		return stackTrace[0];
	}

	public static String getOutput(IStatus status) {
		Throwable t = status.getException();
		String message = ""; //$NON-NLS-1$
		if (t != null) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(output);
			try {
				t.printStackTrace(writer);
			}
			finally {
				writer.close();
			}
			message = output.toString();
		}
		return message;
	}
	
	public static String removeHTMLTags(String report) {
		report = report.replaceAll("<b>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		report = report.replaceAll("</b> ", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
		report = report.replaceAll("</b>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		report = report.replaceAll("<i>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		report = report.replaceAll("</i>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		report = report.replaceAll("<br>", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return report;
	}
	
	public static String getStatusPart(IStatus status) {
		String retVal = ""; //$NON-NLS-1$
		String []stackTraces = ReportPartsFactory.getStackTrace(status).split("\n\n"); //$NON-NLS-1$
		for (int i = 0; i < stackTraces.length; i++) {
			int idx = stackTraces[i].indexOf('\n');
			if (idx == -1) {
				retVal += stackTraces[i] + "<br><br>"; //$NON-NLS-1$
			}
			else {
				retVal += "<b>" + stackTraces[i].substring(0, idx) + "</b><br>" + stackTraces[i] + "<br><br>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		retVal += "<br>"; //$NON-NLS-1$
		return retVal;
	}
	
	public static String getReportIdPart(String id) {
		return "<b>" + id + "</b><br><br>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static String getProductPart(IReportingDescriptor provider) {
		return "<b>Product:</b> " + provider.getProductName() + "<br><br>"; //$NON-NLS-2$
	}
	
	public static String getVersionPart(IReportingDescriptor provider) {
		return "<b>Version:</b> " + provider.getProductVersion() + "<br><br>"; //$NON-NLS-2$
	}
	
	public static String getAuthorPart(String email, String name) {
		String author = (name != null ? name : "") + (email != null && email.trim().length() > 0 ? " &lt;" + email + "&gt;" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		author = author.trim().length() > 0 ? author : "<i>[not specified]</i>";
		return "<b>From:</b> " + author + "<br><br>"; //$NON-NLS-2$
	}
	
	public static String getUserCommentPart(String userComment) {
		userComment = (userComment != null && userComment.trim().length() > 0) ? userComment : "<i>[empty]</i>";
		return "<b>User comment:</b><br>" + userComment + "<br><br>"; //$NON-NLS-2$
	}
	
	public static String getSVNClientPart() {
		ISVNConnectorFactory factory = CoreExtensionsManager.instance().getSVNConnectorFactory();
		return "<b>SVN Client:</b> " + factory.getId() + " " + factory.getVersion() + " " + factory.getClientVersion() + "<br><br>"; //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	public static String getJVMPropertiesPart() {
		Properties props = (Properties)System.getProperties().clone();
		props.remove("org.osgi.framework.system.packages"); //$NON-NLS-1$
		props.remove("sun.boot.class.path"); //$NON-NLS-1$
		props.remove("osgi.bundles"); //$NON-NLS-1$
		return "<b>JVM Properties:</b><br>" + props.toString().replace('\n', ' ') + "<br><br>"; //$NON-NLS-2$
	}
	
}
