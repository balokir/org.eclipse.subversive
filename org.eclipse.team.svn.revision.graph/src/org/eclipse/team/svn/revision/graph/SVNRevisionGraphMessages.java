/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph;

import org.eclipse.osgi.util.NLS;
import org.eclipse.team.svn.core.BaseMessages;

/**
 * 
 * @author Igor Burilo
 */
public class SVNRevisionGraphMessages extends BaseMessages {

	protected static final String BUNDLE_NAME = "org.eclipse.team.svn.revision.graph.messages"; //$NON-NLS-1$
	
	public static String Operation_CreateCache;
	public static String Operation_CreateCache_Id;
	public static String Operation_RefreshCache;
	public static String Operation_RefreshCache_Id;
	public static String Operation_RefreshGraph;
	public static String Operation_RefreshGraph_Id;
	public static String Operation_CheckRepositoryConnection;
	public static String Operation_CheckRepositoryConnection_Id;
	public static String Operation_CreateCacheData;
	public static String Operation_CreateCacheData_Id;
	public static String Operation_CreateRevisionGraphModel;
	public static String Operation_CreateRevisionGraphModel_Id;
	public static String Operation_FetchNewRevisions;
	public static String Operation_FetchNewRevisions_Id;
	public static String Operation_FetchSkippedRevisions;
	public static String Operation_FetchSkippedRevisions_Id;
	public static String Operation_PrepareRevisionData;
	public static String Operation_PrepareRevisionData_Id;
	public static String Operation_ShowRevisionGraph;
	public static String Operation_ShowRevisionGraph_Id;
	public static String Operation_ExportCaches;
	public static String Operation_ExportCaches_Id;
	public static String Operation_ImportCaches;
	public static String Operation_ImportCaches_Id;
	public static String Operation_FetchNewMergeInfo;
	public static String Operation_FetchNewMergeInfo_Id;
	public static String Operation_FetchSkippedMergeInfo;
	public static String Operation_FetchSkippedMergeInfo_Id; 
	public static String Operation_AddMergeInfo;
	public static String Operation_AddMergeInfo_Id;
	
	public static String ShowRevisionGraphAction;
	public static String ShowRevisionsWithCopiesAction;
	public static String CompareWithHeadAction;
	public static String CompareWithPreviousRevisionAction;
	public static String RevisionTooltipFigure_Author;
	public static String RevisionTooltipFigure_Comment;
	public static String RevisionTooltipFigure_CopiedFrom;
	public static String RevisionTooltipFigure_Date;
	public static String RevisionGraphEditor_EditName;
	public static String Dialog_GraphTitle;
	public static String CheckRepositoryConnectionOperation_DialogMessage;
	public static String CreateCacheDataOperation_DialogMessage;
	public static String LogEntriesCallback_Message;
	public static String NoData;
	
	public static String RevisionFigure_Branch;
	public static String RevisionFigure_Copy;
	public static String RevisionFigure_Create;
	public static String RevisionFigure_Delete;
	public static String RevisionFigure_Edit;
	public static String RevisionFigure_NoChanges;
	public static String RevisionFigure_Rename;
	public static String RevisionFigure_Tag;
	public static String RevisionFigure_Trunk;
	
	public static String SVNTeamRevisionGraphPage_Description;
	public static String SVNTeamRevisionGraphPage_Export;
	public static String SVNTeamRevisionGraphPage_ExportDescription;
	public static String SVNTeamRevisionGraphPage_ExportTitle;
	public static String SVNTeamRevisionGraphPage_Group_CacheDirectory;
	public static String SVNTeamRevisionGraphPage_Import;
	public static String SVNTeamRevisionGraphPage_ImportDescription;
	public static String SVNTeamRevisionGraphPage_ImportTitle;
	public static String SVNTeamRevisionGraphPage_Remove;
	public static String SVNTeamRevisionGraphPage_RemoveConfirm_Description;
	public static String SVNTeamRevisionGraphPage_RemoveConfirm_Title;
	public static String SVNTeamRevisionGraphPage_Path_BrowseDialogDescription;
	public static String SVNTeamRevisionGraphPage_Path_BrowseDialogTitle;
	public static String SVNTeamRevisionGraphPage_Path_Field;
	public static String SVNTeamRevisionGraphPage_Path_Label;
	
	public static String ShowRevisionGraphPanel_Description;
	public static String ShowRevisionGraphPanel_Message;
	public static String ShowRevisionGraphPanel_ShowAllRevisions;
	public static String ShowRevisionGraphPanel_ShowMergeInfo;
	public static String ShowRevisionGraphPanel_Title;
	public static String CheckRepositoryConnectionOperation_MergeNotSupported;
	public static String RevisionFigure_Merges;
	public static String RevisionFigure_Revisions;
	public static String RevisionTooltipFigure_IncomingMerge;
	public static String RevisionTooltipFigure_OutgoingMerge;
	
	static {
		//load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SVNRevisionGraphMessages.class);
	}
	
	public static String getString(String key) {
		return BaseMessages.getString(key, SVNRevisionGraphMessages.class);
	}
	
	public static String getErrorString(String key) {
		return BaseMessages.getErrorString(key, SVNRevisionGraphMessages.class);
	}
	
	public static String formatErrorString(String key, Object[] args) {
		return BaseMessages.formatErrorString(key, args, SVNRevisionGraphMessages.class);
	}
}
