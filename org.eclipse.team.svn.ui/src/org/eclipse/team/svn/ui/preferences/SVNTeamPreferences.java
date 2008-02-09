/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Dann Martens - [patch] Text decorations 'ascendant' variable
 *******************************************************************************/

package org.eclipse.team.svn.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.team.svn.core.extension.factory.ISVNConnectorFactory;
import org.eclipse.team.svn.ui.decorator.TextVariableSetProvider;
import org.eclipse.team.svn.ui.repository.RepositoryPerspective;

/**
 * SVN Team plugin preference names
 * 
 * @author Alexander Gurov 
 */
public final class SVNTeamPreferences {
	public static final String DECORATION_BASE = "preference.decoration.";
	public static final String REPOSITORY_BASE = "preference.repository.";
	public static final String SYNCHRONIZE_BASE = "preference.synchronize.";
	public static final String HISTORY_BASE = "preference.history.";
	public static final String PROPERTIES_BASE = "preference.properties.";
	public static final String MAILREPORTER_BASE = "preference.mailreporter.";
	public static final String COMMENT_TEMPLATES_BASE = "preference.templates";
	public static final String COMMIT_DIALOG_BASE = "preference.commitDialog.";
	public static final String MERGE_BASE = "preference.merge.";
	public static final String CHECKOUT_BASE = "preference.checkout.";
	public static final String CONSOLE_BASE = "preference.console.";
	public static final String KEYWORDS_BASE = "preference.keywords.";
	public static final String SHARE_BASE = "preference.share.";
	public static final String CORE_BASE = "preference.core.";
	public static final String ANNOTATE_BASE = "preference.annotate.";
	public static final String TABLE_SORTING_BASE = "preference.sorting.";
	public static final String AUTO_PROPERTIES_BASE = "preference.autoproperties";
	public static final String CUSTOM_PROPERTIES_BASE = "preference.customproperties";
	public static final String RESOURCE_SELECTION_BASE = "preference.resourceSelection";
	
	public static final String ANNOTATE_CHANGE_PERSPECTIVE_NAME = "changePerspective";
	public static final String ANNOTATE_PERSPECTIVE_NAME = "perspective";
	public static final String ANNOTATE_USE_QUICK_DIFF_NAME = "useQuickDiff";
	public static final String ANNOTATE_USE_ONE_RGB_NAME = "useOneRGB";
	public static final String ANNOTATE_RGB_BASE_NAME = "rgbBase";
	
	public static final int ANNOTATE_DEFAULT_VIEW = 0;
	public static final int ANNOTATE_QUICK_DIFF_VIEW = 1;
	public static final int ANNOTATE_PROMPT_VIEW = 2;
	
	public static final int ANNOTATE_DEFAULT_PERSPECTIVE = 0;
	public static final int ANNOTATE_CURRENT_PERSPECTIVE = 1;
	public static final int ANNOTATE_PROMPT_PERSPECTIVE = 2;
	
	public static final int ANNOTATE_USE_QUICK_DIFF_DEFAULT = SVNTeamPreferences.ANNOTATE_PROMPT_VIEW;
	public static final String ANNOTATE_PERSPECTIVE_DEFAULT = RepositoryPerspective.ID;
	public static final int ANNOTATE_CHANGE_PERSPECTIVE_DEFAULT = SVNTeamPreferences.ANNOTATE_PROMPT_PERSPECTIVE;
	public static final boolean ANNOTATE_USE_ONE_RGB_DEFAULT = true;
	public static final RGB ANNOTATE_RGB_BASE_DEFAULT = new RGB(186, 186, 186);
	
	public static final String CONSOLE_AUTOSHOW_TYPE_NAME = "autoshow";
	public static final String CONSOLE_ENABLED_NAME = "enabled";
	public static final String CONSOLE_HYPERLINKS_ENABLED_NAME = "hyperlinksEnabled";
	public static final String CONSOLE_FONT_NAME = "font";
	public static final String CONSOLE_WRAP_ENABLED_NAME = "wrapEnabled";
	public static final String CONSOLE_WRAP_WIDTH_NAME = "wrapWidth";
	public static final String CONSOLE_LIMIT_ENABLED_NAME = "limitEnabled";
	public static final String CONSOLE_LIMIT_VALUE_NAME = "limitRange";
	
	public static final String CORE_SVNCONNECTOR_NAME = "svnconnector";
	
	public static final String CORE_SVNCONNECTOR_DEFAULT = ISVNConnectorFactory.DEFAULT_ID;
	
	public static final String CONSOLE_ERR_COLOR_NAME = "error";
	public static final String CONSOLE_WRN_COLOR_NAME = "warning";
	public static final String CONSOLE_OK_COLOR_NAME = "ok";
	public static final String CONSOLE_CMD_COLOR_NAME = "command";
	
	public static final int CONSOLE_AUTOSHOW_TYPE_NEVER = 0;
	public static final int CONSOLE_AUTOSHOW_TYPE_ALWAYS = 1;
	public static final int CONSOLE_AUTOSHOW_TYPE_ERROR = 2;
	public static final int CONSOLE_AUTOSHOW_TYPE_WARNING_ERROR = 3;
	public static final int CONSOLE_AUTOSHOW_TYPE_DEFAULT = SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NEVER;
	
	public static final boolean CONSOLE_ENABLED_DEFAULT = true;
	public static final boolean CONSOLE_HYPERLINKS_ENABLED_DEFAULT = true;
	
	public static final boolean CONSOLE_WRAP_ENABLED_DEFAULT = false;
	public static final int CONSOLE_WRAP_WIDTH_DEFAULT = 80;
	public static final boolean CONSOLE_LIMIT_ENABLED_DEFAULT = true;
	public static final int CONSOLE_LIMIT_VALUE_DEFAULT = 500000;
	
	public static final RGB CONSOLE_ERR_COLOR_DEFAULT = new RGB(255, 0, 0);
	public static final RGB CONSOLE_WRN_COLOR_DEFAULT = new RGB(128, 0, 0);
	public static final RGB CONSOLE_OK_COLOR_DEFAULT = new RGB(0, 0, 255);
	public static final RGB CONSOLE_CMD_COLOR_DEFAULT = new RGB(0, 0, 0);
	
	public static final String MAILREPORTER_ENABLED_NAME = "enabled";
	public static final String MAILREPORTER_ERRORS_ENABLED_NAME = "errorsEnabled";
	
	public static final boolean MAILREPORTER_ENABLED_DEFAULT = true;
	public static final boolean MAILREPORTER_ERRORS_ENABLED_DEFAULT = true;

	public static final String MERGE_USE_JAVAHL_NAME = "useJavaHL";
	
	public static final boolean MERGE_USE_JAVAHL_DEFAULT = false;
	
	public static final String SHARE_ENABLE_AUTO_NAME = "enableAuto";
	
	public static final boolean SHARE_ENABLE_AUTO_DEFAULT = true;
	
	public static final String COMMIT_SELECT_NEW_RESOURCES_NAME = "selectNew";
	
	public static final boolean COMMIT_SELECT_NEW_RESOURCES_DEFAULT = true;
	
	public static final String USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME = "treatExternalAsLocal";
	
	public static final boolean USE_SUBVERSION_EXTERNAL_BEHAVIOUR_DEFAULT = true;
	
	public static final String DETECT_DELETED_PROJECTS_NAME = "detectDeleted";
	
	public static final boolean DETECT_DELETED_PROJECTS_DEFAULT = true;
	
	public static final String COMPUTE_KEYWORDS_NAME = "computeValues";
	
	public static final boolean COMPUTE_KEYWORDS_DEFAULT = true;
	
	public static final String CHECKOUT_USE_DOT_PROJECT_NAME = "useDotProject";
	
	public static final boolean CHECKOUT_USE_DOT_PROJECT_DEFAULT = true;
	
	public static final String HISTORY_PAGE_SIZE_NAME = "pageSize";
	public static final String HISTORY_PAGING_ENABLE_NAME = "pagingEnable";
	public static final String HISTORY_SHOW_MULTILINE_COMMENT_NAME = "multilineComment";
	public static final String HISTORY_SHOW_AFFECTED_PATHS_NAME = "affectedPaths";
	public static final String HISTORY_GROUPING_TYPE_NAME = "groupingType";
	public static final String HISTORY_HIERARCHICAL_LAYOUT = "hierarchicalLayout";
	public static final String HISTORY_COMPARE_MODE = "compareMode";
	public static final String HISTORY_LINK_WITH_EDITOR_NAME = "linkWithEditor";

	public static final int HISTORY_GROUPING_TYPE_NONE = 0;
	public static final int HISTORY_GROUPING_TYPE_DATE = 1;
	
	public static final int HISTORY_PAGE_SIZE_DEFAULT = 25;
	public static final int HISTORY_GROUPING_TYPE_DEFAULT = SVNTeamPreferences.HISTORY_GROUPING_TYPE_NONE;
	public static final boolean HISTORY_PAGING_ENABLE_DEFAULT = true;
	public static final boolean HISTORY_SHOW_MULTILINE_COMMENT_DEFAULT = true;
	public static final boolean HISTORY_SHOW_AFFECTED_PATHS_DEFAULT = true;
	public static final boolean HISTORY_HIERARCHICAL_LAYOUT_DEFAULT = true;
	public static final boolean HISTORY_COMPARE_MODE_DEFAULT = false;
	public static final boolean HISTORY_LINK_WITH_EDITOR_DEFAULT = false;
	
	public static final String PROPERTY_USE_VIEW_NAME = "useView";
	public static final String PROPERTY_LINK_WITH_EDITOR_NAME = "linkWithEditor";
	
	public static final boolean PROPERTY_USE_VIEW_DEFAULT = true;
	public static final boolean PROPERTY_LINK_WITH_EDITOR_DEFAULT = false;
	
	public static final String SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME = "fastReport";
	
	public static final boolean SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_DEFAULT = true;
	
	public static final String REPOSITORY_SHOW_BROWSER_NAME = "repositoryBrowser";
	public static final boolean REPOSITORY_SHOW_BROWSER_DEFAULT = true;
	public static final String REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME = "forceExternalsFreeze";
	public static final boolean REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT = true;
	public static final String REPOSITORY_HEAD_NAME = "head";
	public static final String REPOSITORY_BRANCHES_NAME = "branches";
	public static final String REPOSITORY_TAGS_NAME = "tags";
	public static final String REPOSITORY_SHOW_EXTERNALS_NAME = "showExternals";
	
	public static final String REPOSITORY_HEAD_DEFAULT = "trunk";
	public static final String REPOSITORY_BRANCHES_DEFAULT = "branches";
	public static final String REPOSITORY_TAGS_DEFAULT = "tags";
	public static final boolean REPOSITORY_SHOW_EXTERNALS_DEFAULT = true;
	
	public static final String BRANCH_TAG_CONSIDER_STRUCTURE_NAME = "tagConsideringProjectStructure";
	public static final boolean BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT = true;
	
	public static final String DECORATION_FORMAT_FILE_NAME = "format.file";
	public static final String DECORATION_FORMAT_FOLDER_NAME = "format.folder";
	public static final String DECORATION_FORMAT_PROJECT_NAME = "format.project";
	
//	{outgoing_flag} {name} {revision}
	public static final String DECORATION_FORMAT_FILE_DEFAULT = "{" + TextVariableSetProvider.NAME_OF_OUTGOING_FLAG + "} {" + TextVariableSetProvider.NAME_OF_NAME + "} {" + TextVariableSetProvider.NAME_OF_REVISION + "}";
//	{outgoing_flag} {name} {revision}
	public static final String DECORATION_FORMAT_FOLDER_DEFAULT = SVNTeamPreferences.DECORATION_FORMAT_FILE_DEFAULT;
//	{outgoing_flag} {name} {revision} [{location_label}{root_prefix}: {fullname}]
	public static final String DECORATION_FORMAT_PROJECT_DEFAULT = SVNTeamPreferences.DECORATION_FORMAT_FOLDER_DEFAULT + " [{" + TextVariableSetProvider.NAME_OF_LOCATION_LABEL + "}{" + TextVariableSetProvider.NAME_OF_ROOT_PREFIX + "}: {" + TextVariableSetProvider.NAME_OF_ASCENDANT + "}]";
	
	public static final String DECORATION_FLAG_OUTGOING_NAME = "flag.outgoing";
	public static final String DECORATION_FLAG_ADDED_NAME = "flag.added";
	
	public static final String DECORATION_FLAG_OUTGOING_DEFAULT = ">";
	public static final String DECORATION_FLAG_ADDED_DEFAULT = "*";
	
	public static final String DECORATION_TRUNK_PREFIX_NAME = "trunk.branch";
	public static final String DECORATION_BRANCH_PREFIX_NAME = "prefix.branch";
	public static final String DECORATION_TAG_PREFIX_NAME = "prefix.tag";
	
	public static final String DECORATION_TRUNK_PREFIX_DEFAULT = ", Trunk";
	public static final String DECORATION_BRANCH_PREFIX_DEFAULT = ", Branch";
	public static final String DECORATION_TAG_PREFIX_DEFAULT = ", Tag";
	
	public static final String DECORATION_ICON_CONFLICTED_NAME = "icon.conflicted";
	public static final String DECORATION_ICON_MODIFIED_NAME = "icon.modified";
	public static final String DECORATION_ICON_REMOTE_NAME = "icon.remote";
	public static final String DECORATION_ICON_ADDED_NAME = "icon.added";
	public static final String DECORATION_ICON_NEW_NAME = "icon.new";
	public static final String DECORATION_ICON_LOCKED_NAME = "icon.locked";
	public static final String DECORATION_ICON_NEEDS_LOCK_NAME = "icon.needslock";
	public static final String DECORATION_ICON_SWITCHED_NAME = "icon.switched";
	
	public static final boolean DECORATION_ICON_CONFLICTED_DEFAULT = true;
	public static final boolean DECORATION_ICON_MODIFIED_DEFAULT = false;
	public static final boolean DECORATION_ICON_REMOTE_DEFAULT = true;
	public static final boolean DECORATION_ICON_ADDED_DEFAULT = true;
	public static final boolean DECORATION_ICON_NEW_DEFAULT = true;
	public static final boolean DECORATION_ICON_LOCKED_DEFAULT = true;
	public static final boolean DECORATION_ICON_NEEDS_LOCK_DEFAULT = false;
	public static final boolean DECORATION_ICON_SWITCHED_DEFAULT = true;
	
	public static final String DECORATION_COMPUTE_DEEP_NAME = "compute.deep";
	public static final boolean DECORATION_COMPUTE_DEEP_DEFAULT = true;
	
	public static final String DECORATION_ENABLE_CACHE_NAME = "enable.cache";
	public static final boolean DECORATION_ENABLE_CACHE_DEFAULT = true;
	
	public static final String DECORATION_USE_FONT_COLORS_DECOR_NAME = "use.fontdecor";
	public static final boolean DECORATION_USE_FONT_COLORS_DECOR_DEFAULT = false;
	
	public static final String NAME_OF_OUTGOING_FOREGROUND_COLOR = "outgoing_change_foreground_color";
	public static final String NAME_OF_OUTGOING_BACKGROUND_COLOR = "outgoing_change_background_color";
	public static final String NAME_OF_OUTGOING_FONT = "outgoing_change_font";
	public static final String NAME_OF_IGNORED_FOREGROUND_COLOR = "ignored_resource_foreground_color";
	public static final String NAME_OF_IGNORED_BACKGROUND_COLOR = "ignored_resource_background_color";
	public static final String NAME_OF_IGNORED_FONT = "ignored_resource_font";
	public static final String NAME_OF_NOT_RELATED_NODES_FOREGROUND_COLOR = "not_related_nodes_foreground_color";
	public static final String NAME_OF_NOT_RELATED_NODES_BACKGROUND_COLOR = "not_related_nodes_background_color";
	public static final String NAME_OF_NOT_RELATED_NODES_FONT = "not_related_nodes_font";
	public static final String NAME_OF_STRUCTURE_NODES_FOREGROUND_COLOR = "structure_nodes_foreground_color";
	public static final String NAME_OF_STRUCTURE_NODES_BACKGROUND_COLOR = "structure_nodes_background_color";
	public static final String NAME_OF_STRUCTURE_NODES_FONT = "structure_nodes_font";
	
	public static final String COMMENT_TEMPLATES_LIST_NAME = "comment.templates";
	public static final String COMMENT_TEMPLATES_LIST_ENABLED_NAME = "comment.templates.enabled";
	public static final String COMMENT_LOG_TEMPLATES_ENABLED_NAME = "comment.logTemplates.enabled";
	public static final String COMMENT_SAVED_COMMENTS_COUNT_NAME = "savedCommentsCount";
	
	public static final String COMMENT_TEMPLATES_LIST_DEFAULT = "";
	public static final boolean COMMENT_TEMPLATES_LIST_ENABLED_DEFAULT = true;
	public static final boolean COMMENT_LOG_TEMPLATES_ENABLED_DEFAULT = true;
	public static final int COMMENT_SAVED_COMMENTS_COUNT_DEFAULT = 10;
	
	public static final String COMMIT_DIALOG_WEIGHT_NAME = "CommitPanel.weight";
	public static final int COMMIT_DIALOG_WEIGHT_DEFAULT = 50;
	
	public static final String TABLE_SORTING_CASE_INSENSITIVE_NAME = "case.insensitive";
	public static final boolean TABLE_SORTING_CASE_INSENSITIVE_DEFAULT = true;
	
	public static final String AUTO_PROPERTIES_LIST_NAME = "autoproperties";
	public static final String AUTO_PROPERTIES_LIST_DEFAULT = "";
	
	public static final String CUSTOM_PROPERTIES_LIST_NAME = "customproperties";
	public static final String CUSTOM_PROPERTIES_LIST_DEFAULT = "";
	
	public static void setDefaultValues(IPreferenceStore store) {
		SVNTeamPreferences.setDefaultRepositoryValues(store);
		SVNTeamPreferences.setDefaultDecorationValues(store);
		SVNTeamPreferences.setDefaultPerformanceValues(store);
		SVNTeamPreferences.setDefaultSynchronizeValues(store);
		SVNTeamPreferences.setDefaultMailReporterValues(store);
		SVNTeamPreferences.setDefaultHistoryValues(store);
		SVNTeamPreferences.setDefaultPropertiesValues(store);
		SVNTeamPreferences.setDefaultCommentTemplatesValues(store);
		SVNTeamPreferences.setDefaultResourceSelectionValues(store);
		SVNTeamPreferences.setDefaultMergeValues(store);
		SVNTeamPreferences.setDefaultCheckoutValues(store);
		SVNTeamPreferences.setDefaultConsoleValues(store);
		SVNTeamPreferences.setDefaultKeywordsValues(store);
		SVNTeamPreferences.setDefaultShareValues(store);
		SVNTeamPreferences.setDefaultCoreValues(store);
		SVNTeamPreferences.setDefaultAnnotateValues(store);
		SVNTeamPreferences.setDefaultTableSortingValues(store);
		SVNTeamPreferences.setDefaultCommitDialogValues(store);
		SVNTeamPreferences.setDefaultAutoPropertiesValues(store);
		SVNTeamPreferences.setDefaultCustomPropertiesValues(store);
	}
	
	public static void setDefaultAutoPropertiesValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullAutoPropertiesName(SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME), SVNTeamPreferences.AUTO_PROPERTIES_LIST_DEFAULT);
	}
	
	public static void setDefaultCustomPropertiesValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullCustomPropertiesName(SVNTeamPreferences.CUSTOM_PROPERTIES_LIST_NAME), SVNTeamPreferences.AUTO_PROPERTIES_LIST_DEFAULT);
	}
	
	public static void setDefaultCommitDialogValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.COMMIT_DIALOG_WEIGHT_NAME, SVNTeamPreferences.COMMIT_DIALOG_WEIGHT_DEFAULT);
	}
	
	public static void setDefaultTableSortingValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullTableSortingName(SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME), SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_DEFAULT);
	}
	
	public static void setDefaultCheckoutValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullCheckoutName(SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_NAME), SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_DEFAULT);
	}
	
	public static void setDefaultMergeValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullMergeName(SVNTeamPreferences.MERGE_USE_JAVAHL_NAME), SVNTeamPreferences.MERGE_USE_JAVAHL_DEFAULT);
	}
	
	public static void setDefaultHistoryValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME), SVNTeamPreferences.HISTORY_PAGE_SIZE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME), SVNTeamPreferences.HISTORY_PAGING_ENABLE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME), SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME), SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME), SVNTeamPreferences.HISTORY_GROUPING_TYPE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT), SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_COMPARE_MODE), SVNTeamPreferences.HISTORY_COMPARE_MODE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_LINK_WITH_EDITOR_NAME), SVNTeamPreferences.HISTORY_LINK_WITH_EDITOR_DEFAULT);
	}
	
	public static void setDefaultSynchronizeValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullSynchronizeName(SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME), SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_DEFAULT);
	}
	
	public static void setDefaultPropertiesValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullPropertiesName(SVNTeamPreferences.PROPERTY_USE_VIEW_NAME), SVNTeamPreferences.PROPERTY_USE_VIEW_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullPropertiesName(SVNTeamPreferences.PROPERTY_LINK_WITH_EDITOR_NAME), SVNTeamPreferences.PROPERTY_LINK_WITH_EDITOR_DEFAULT);
	}
	
	public static void setDefaultRepositoryValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_HEAD_NAME), SVNTeamPreferences.REPOSITORY_HEAD_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_BRANCHES_NAME), SVNTeamPreferences.REPOSITORY_BRANCHES_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_TAGS_NAME), SVNTeamPreferences.REPOSITORY_TAGS_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_NAME), SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_NAME), SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME), SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT);
	}
	
	public static void setDefaultDecorationValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FORMAT_FILE_NAME), SVNTeamPreferences.DECORATION_FORMAT_FILE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FORMAT_FOLDER_NAME), SVNTeamPreferences.DECORATION_FORMAT_FOLDER_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FORMAT_PROJECT_NAME), SVNTeamPreferences.DECORATION_FORMAT_PROJECT_DEFAULT);
				
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FLAG_OUTGOING_NAME), SVNTeamPreferences.DECORATION_FLAG_OUTGOING_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FLAG_ADDED_NAME), SVNTeamPreferences.DECORATION_FLAG_ADDED_DEFAULT);
		
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_TRUNK_PREFIX_NAME), SVNTeamPreferences.DECORATION_TRUNK_PREFIX_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_BRANCH_PREFIX_NAME), SVNTeamPreferences.DECORATION_BRANCH_PREFIX_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_TAG_PREFIX_NAME), SVNTeamPreferences.DECORATION_TAG_PREFIX_DEFAULT);
		
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_CONFLICTED_NAME), SVNTeamPreferences.DECORATION_ICON_CONFLICTED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_MODIFIED_NAME), SVNTeamPreferences.DECORATION_ICON_MODIFIED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_REMOTE_NAME), SVNTeamPreferences.DECORATION_ICON_REMOTE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_ADDED_NAME), SVNTeamPreferences.DECORATION_ICON_ADDED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_NEW_NAME), SVNTeamPreferences.DECORATION_ICON_NEW_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_LOCKED_NAME), SVNTeamPreferences.DECORATION_ICON_LOCKED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_NEEDS_LOCK_NAME), SVNTeamPreferences.DECORATION_ICON_NEEDS_LOCK_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_SWITCHED_NAME), SVNTeamPreferences.DECORATION_ICON_SWITCHED_DEFAULT);
		
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_USE_FONT_COLORS_DECOR_NAME), SVNTeamPreferences.DECORATION_USE_FONT_COLORS_DECOR_DEFAULT);
	}
	
	public static void setDefaultPerformanceValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_COMPUTE_DEEP_NAME), SVNTeamPreferences.DECORATION_COMPUTE_DEEP_DEFAULT);		
		store.setDefault(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ENABLE_CACHE_NAME), SVNTeamPreferences.DECORATION_ENABLE_CACHE_DEFAULT);		
	}
	
	public static void setDefaultMailReporterValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullMailReporterName(SVNTeamPreferences.MAILREPORTER_ENABLED_NAME), SVNTeamPreferences.MAILREPORTER_ENABLED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullMailReporterName(SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME), SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_DEFAULT);
	}
	
	public static void setDefaultCommentTemplatesValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_TEMPLATES_LIST_NAME), SVNTeamPreferences.COMMENT_TEMPLATES_LIST_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME), SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME), SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_NAME), SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_DEFAULT);
	}
	
	public static void setDefaultConsoleValues(final IPreferenceStore store) {
		PreferenceConverter.setDefault(store, SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_ERR_COLOR_NAME), SVNTeamPreferences.CONSOLE_ERR_COLOR_DEFAULT);
		PreferenceConverter.setDefault(store, SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_WRN_COLOR_NAME), SVNTeamPreferences.CONSOLE_WRN_COLOR_DEFAULT);
		PreferenceConverter.setDefault(store, SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_OK_COLOR_NAME), SVNTeamPreferences.CONSOLE_OK_COLOR_DEFAULT);
		PreferenceConverter.setDefault(store, SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_CMD_COLOR_NAME), SVNTeamPreferences.CONSOLE_CMD_COLOR_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NAME), SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_ENABLED_NAME), SVNTeamPreferences.CONSOLE_ENABLED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_HYPERLINKS_ENABLED_NAME), SVNTeamPreferences.CONSOLE_HYPERLINKS_ENABLED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_WRAP_ENABLED_NAME), SVNTeamPreferences.CONSOLE_WRAP_ENABLED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_NAME), SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_WRAP_WIDTH_NAME), SVNTeamPreferences.CONSOLE_WRAP_WIDTH_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_LIMIT_VALUE_NAME), SVNTeamPreferences.CONSOLE_LIMIT_VALUE_DEFAULT);
	}
	
	public static void setDefaultAnnotateValues(IPreferenceStore store) {
		PreferenceConverter.setDefault(store, SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_RGB_BASE_NAME), SVNTeamPreferences.ANNOTATE_RGB_BASE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_NAME), SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_USE_QUICK_DIFF_NAME), SVNTeamPreferences.ANNOTATE_USE_QUICK_DIFF_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_NAME), SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_PERSPECTIVE_NAME), SVNTeamPreferences.ANNOTATE_PERSPECTIVE_DEFAULT);
	}
	
	public static void setDefaultResourceSelectionValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullResourceSelectionName(SVNTeamPreferences.DETECT_DELETED_PROJECTS_NAME), SVNTeamPreferences.DETECT_DELETED_PROJECTS_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullResourceSelectionName(SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_NAME), SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_DEFAULT);
		store.setDefault(SVNTeamPreferences.fullResourceSelectionName(SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME), SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_DEFAULT);
	}
	
	public static void setDefaultKeywordsValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullKeywordsName(SVNTeamPreferences.COMPUTE_KEYWORDS_NAME), SVNTeamPreferences.COMPUTE_KEYWORDS_DEFAULT);
	}
	
	public static void setDefaultShareValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullShareName(SVNTeamPreferences.SHARE_ENABLE_AUTO_NAME), SVNTeamPreferences.SHARE_ENABLE_AUTO_DEFAULT);
	}
	
	public static void setDefaultCoreValues(IPreferenceStore store) {
		store.setDefault(SVNTeamPreferences.fullCoreName(SVNTeamPreferences.CORE_SVNCONNECTOR_NAME), SVNTeamPreferences.CORE_SVNCONNECTOR_DEFAULT);
	}
	
	public static void resetToDefaultAutoPropsValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullAutoPropertiesName(SVNTeamPreferences.AUTO_PROPERTIES_LIST_NAME), SVNTeamPreferences.AUTO_PROPERTIES_LIST_DEFAULT);
	}
	
	public static void resetToDefaultCustomoPropsValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullCustomPropertiesName(SVNTeamPreferences.CUSTOM_PROPERTIES_LIST_NAME), SVNTeamPreferences.AUTO_PROPERTIES_LIST_DEFAULT);
	}
	
	public static void resetToDefaultTableSortingValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullTableSortingName(SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME), SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_DEFAULT);
	}
	
	public static void resetToDefaultCoreValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullCoreName(SVNTeamPreferences.CORE_SVNCONNECTOR_NAME), SVNTeamPreferences.CORE_SVNCONNECTOR_DEFAULT);
	}
	
	public static void resetToDefaultHistoryValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_PAGE_SIZE_NAME), SVNTeamPreferences.HISTORY_PAGE_SIZE_DEFAULT);
		store.setValue(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_PAGING_ENABLE_NAME), SVNTeamPreferences.HISTORY_PAGING_ENABLE_DEFAULT);
		store.setValue(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_NAME), SVNTeamPreferences.HISTORY_SHOW_MULTILINE_COMMENT_DEFAULT);
		store.setValue(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_NAME), SVNTeamPreferences.HISTORY_SHOW_AFFECTED_PATHS_DEFAULT);
		//FIXME uncomment this line when this options are added to plugin preferences 
		//store.setValue(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_GROUPING_TYPE_NAME), SVNTeamPreferences.HISTORY_GROUPING_TYPE_DATE);
		//store.setValue(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT), SVNTeamPreferences.HISTORY_HIERARCHICAL_LAYOUT_DEFAULT);
		//store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_NAME), SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_DEFAULT);
		//store.setValue(SVNTeamPreferences.fullHistoryName(SVNTeamPreferences.HISTORY_COMPARE_MODE), SVNTeamPreferences.HISTORY_COMPARE_MODE_DEFAULT);
	}
	
	public static void resetToDefaultMailReporterValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullMailReporterName(SVNTeamPreferences.MAILREPORTER_ENABLED_NAME), SVNTeamPreferences.MAILREPORTER_ENABLED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullMailReporterName(SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_NAME), SVNTeamPreferences.MAILREPORTER_ERRORS_ENABLED_DEFAULT);
	}
	
	public static void resetToDefaultCheckoutValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullCheckoutName(SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_NAME), SVNTeamPreferences.CHECKOUT_USE_DOT_PROJECT_DEFAULT);
	}
	
	public static void resetToDefaultMergeValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullMergeName(SVNTeamPreferences.MERGE_USE_JAVAHL_NAME), SVNTeamPreferences.MERGE_USE_JAVAHL_DEFAULT);
	}
	
	public static void resetToDefaultResourceSelectionValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullResourceSelectionName(SVNTeamPreferences.DETECT_DELETED_PROJECTS_NAME), SVNTeamPreferences.DETECT_DELETED_PROJECTS_DEFAULT);
		store.setValue(SVNTeamPreferences.fullResourceSelectionName(SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_NAME), SVNTeamPreferences.COMMIT_SELECT_NEW_RESOURCES_DEFAULT);
		store.setValue(SVNTeamPreferences.fullResourceSelectionName(SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_NAME), SVNTeamPreferences.USE_SUBVERSION_EXTERNAL_BEHAVIOUR_DEFAULT);
	}
	
	public static void resetToDefaultKeywordsValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullKeywordsName(SVNTeamPreferences.COMPUTE_KEYWORDS_NAME), SVNTeamPreferences.COMPUTE_KEYWORDS_DEFAULT);
	}
	
	public static void resetToDefaultSynchronizeValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullSynchronizeName(SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_NAME), SVNTeamPreferences.SYNCHRONIZE_SHOW_REPORT_CONTIGUOUS_DEFAULT);
	}
	
	public static void resetToDefaultPropertiesValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullPropertiesName(SVNTeamPreferences.PROPERTY_USE_VIEW_NAME), SVNTeamPreferences.PROPERTY_USE_VIEW_DEFAULT);
	}
	
	public static void resetToDefaultRepositoryValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_HEAD_NAME), SVNTeamPreferences.REPOSITORY_HEAD_DEFAULT);
		store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_BRANCHES_NAME), SVNTeamPreferences.REPOSITORY_BRANCHES_DEFAULT);
		store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_TAGS_NAME), SVNTeamPreferences.REPOSITORY_TAGS_DEFAULT);
		store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_NAME), SVNTeamPreferences.REPOSITORY_SHOW_EXTERNALS_DEFAULT);
		store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_NAME), SVNTeamPreferences.REPOSITORY_SHOW_BROWSER_DEFAULT);
		store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_NAME), SVNTeamPreferences.BRANCH_TAG_CONSIDER_STRUCTURE_DEFAULT);
		store.setValue(SVNTeamPreferences.fullRepositoryName(SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_NAME), SVNTeamPreferences.REPOSITORY_FORCE_EXTERNALS_FREEZE_DEFAULT);
	}
	
	public static void resetToDefaultDecorationValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FORMAT_FILE_NAME), SVNTeamPreferences.DECORATION_FORMAT_FILE_DEFAULT);		
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FORMAT_FOLDER_NAME), SVNTeamPreferences.DECORATION_FORMAT_FOLDER_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FORMAT_PROJECT_NAME), SVNTeamPreferences.DECORATION_FORMAT_PROJECT_DEFAULT);
		
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FLAG_OUTGOING_NAME), SVNTeamPreferences.DECORATION_FLAG_OUTGOING_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_FLAG_ADDED_NAME), SVNTeamPreferences.DECORATION_FLAG_ADDED_DEFAULT);
		
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_TRUNK_PREFIX_NAME), SVNTeamPreferences.DECORATION_TRUNK_PREFIX_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_BRANCH_PREFIX_NAME), SVNTeamPreferences.DECORATION_BRANCH_PREFIX_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_TAG_PREFIX_NAME), SVNTeamPreferences.DECORATION_TAG_PREFIX_DEFAULT);

		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_CONFLICTED_NAME), SVNTeamPreferences.DECORATION_ICON_CONFLICTED_DEFAULT);		
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_MODIFIED_NAME), SVNTeamPreferences.DECORATION_ICON_MODIFIED_DEFAULT);		
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_REMOTE_NAME), SVNTeamPreferences.DECORATION_ICON_REMOTE_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_ADDED_NAME), SVNTeamPreferences.DECORATION_ICON_ADDED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_NEW_NAME), SVNTeamPreferences.DECORATION_ICON_NEW_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_LOCKED_NAME), SVNTeamPreferences.DECORATION_ICON_LOCKED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_NEEDS_LOCK_NAME), SVNTeamPreferences.DECORATION_ICON_NEEDS_LOCK_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ICON_SWITCHED_NAME), SVNTeamPreferences.DECORATION_ICON_SWITCHED_DEFAULT);

		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_USE_FONT_COLORS_DECOR_NAME), SVNTeamPreferences.DECORATION_USE_FONT_COLORS_DECOR_DEFAULT);
	}
	
	public static void resetToDefaultPerformanceValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_COMPUTE_DEEP_NAME), SVNTeamPreferences.DECORATION_COMPUTE_DEEP_DEFAULT);
		store.setValue(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.DECORATION_ENABLE_CACHE_NAME), SVNTeamPreferences.DECORATION_ENABLE_CACHE_DEFAULT);
	}
	
	public static void resetToDefaultCommentTemplatesValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_TEMPLATES_LIST_NAME), SVNTeamPreferences.COMMENT_TEMPLATES_LIST_DEFAULT);
		store.setValue(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_NAME), SVNTeamPreferences.COMMENT_TEMPLATES_LIST_ENABLED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_NAME), SVNTeamPreferences.COMMENT_LOG_TEMPLATES_ENABLED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullCommentTemplatesName(SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_NAME), SVNTeamPreferences.COMMENT_SAVED_COMMENTS_COUNT_DEFAULT);
	}
	
	public static void resetToDefaultConsoleValues(IPreferenceStore store) {
		SVNTeamPreferences.setConsoleRGB(store, SVNTeamPreferences.CONSOLE_ERR_COLOR_NAME, SVNTeamPreferences.CONSOLE_ERR_COLOR_DEFAULT);
		SVNTeamPreferences.setConsoleRGB(store, SVNTeamPreferences.CONSOLE_WRN_COLOR_NAME, SVNTeamPreferences.CONSOLE_WRN_COLOR_DEFAULT);
		SVNTeamPreferences.setConsoleRGB(store, SVNTeamPreferences.CONSOLE_OK_COLOR_NAME, SVNTeamPreferences.CONSOLE_OK_COLOR_DEFAULT);
		SVNTeamPreferences.setConsoleRGB(store, SVNTeamPreferences.CONSOLE_CMD_COLOR_NAME, SVNTeamPreferences.CONSOLE_CMD_COLOR_DEFAULT);
		store.setValue(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_NAME), SVNTeamPreferences.CONSOLE_AUTOSHOW_TYPE_DEFAULT);
		store.setValue(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_ENABLED_NAME), SVNTeamPreferences.CONSOLE_ENABLED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_HYPERLINKS_ENABLED_NAME), SVNTeamPreferences.CONSOLE_HYPERLINKS_ENABLED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_WRAP_ENABLED_NAME), SVNTeamPreferences.CONSOLE_WRAP_ENABLED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_NAME), SVNTeamPreferences.CONSOLE_LIMIT_ENABLED_DEFAULT);
		store.setValue(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_WRAP_WIDTH_NAME), SVNTeamPreferences.CONSOLE_WRAP_WIDTH_DEFAULT);
		store.setValue(SVNTeamPreferences.fullConsoleName(SVNTeamPreferences.CONSOLE_LIMIT_VALUE_NAME), SVNTeamPreferences.CONSOLE_LIMIT_VALUE_DEFAULT);
	}
	
	public static void resetToDefaultAnnotateValues(IPreferenceStore store) {
		SVNTeamPreferences.setAnnotateRGB(store, SVNTeamPreferences.ANNOTATE_RGB_BASE_NAME, SVNTeamPreferences.ANNOTATE_RGB_BASE_DEFAULT);
		store.setValue(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_NAME), SVNTeamPreferences.ANNOTATE_USE_ONE_RGB_DEFAULT);
		store.setValue(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_USE_QUICK_DIFF_NAME), SVNTeamPreferences.ANNOTATE_USE_QUICK_DIFF_DEFAULT);
		store.setValue(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_NAME), SVNTeamPreferences.ANNOTATE_CHANGE_PERSPECTIVE_DEFAULT);
		store.setValue(SVNTeamPreferences.fullAnnotateName(SVNTeamPreferences.ANNOTATE_PERSPECTIVE_NAME), SVNTeamPreferences.ANNOTATE_PERSPECTIVE_DEFAULT);
	}
	
	public static void resetToDefaultShareValues(IPreferenceStore store) {
		store.setValue(SVNTeamPreferences.fullShareName(SVNTeamPreferences.SHARE_ENABLE_AUTO_NAME), SVNTeamPreferences.SHARE_ENABLE_AUTO_DEFAULT);
	}
	
	public static int getDialogInt(IPreferenceStore store, String name) {
		return store.getInt(name);
	}
	
	public static boolean getTableSortingBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullTableSortingName(shortName));
	}
	
	public static boolean getCheckoutBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullCheckoutName(shortName));
	}
	
	public static boolean getMergeBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullMergeName(shortName));
	}
	
	public static boolean getSynchronizeBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullSynchronizeName(shortName));
	}
	
	public static boolean getPropertiesBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullPropertiesName(shortName));
	}
	
	public static String getRepositoryString(IPreferenceStore store, String shortName) {
		return store.getString(SVNTeamPreferences.fullRepositoryName(shortName));
	}
	
	public static boolean getRepositoryBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullRepositoryName(shortName));
	}
	
	public static String getDecorationString(IPreferenceStore store, String shortName) {
		return store.getString(SVNTeamPreferences.fullDecorationName(shortName));
	}
	
	public static int getHistoryInt(IPreferenceStore store, String shortName) {
		return store.getInt(SVNTeamPreferences.fullHistoryName(shortName));
	}
	
	public static boolean getHistoryBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullHistoryName(shortName));
	}
	
	public static boolean getResourceSelectionBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullResourceSelectionName(shortName));
	}
	
	public static boolean getKeywordsBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullKeywordsName(shortName));
	}
	
	public static boolean getDecorationBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullDecorationName(shortName));
	}
	
	public static String getCommentTemplatesString(IPreferenceStore store, String shortName) {
		return store.getString(SVNTeamPreferences.fullCommentTemplatesName(shortName));
	}
	
	public static int getCommentTemplatesInt(IPreferenceStore store, String shortName) {
		return store.getInt(SVNTeamPreferences.fullCommentTemplatesName(shortName));
	}
	
	public static boolean getCommentTemplatesBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullCommentTemplatesName(shortName));
	}
	
	public static String getAutoPropertiesList(IPreferenceStore store, String shortName) {
		return store.getString(SVNTeamPreferences.fullAutoPropertiesName(shortName));
	}
	
	public static String getCustomPropertiesList(IPreferenceStore store, String shortName) {
		return store.getString(SVNTeamPreferences.fullCustomPropertiesName(shortName));
	}
	
	public static void setDialogInt(IPreferenceStore store, String name, int value) {
		store.setValue(name, value);
	}
	
	public static void setTableSortingBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullTableSortingName(shortName), value);
	}
	
	public static void setCheckoutBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullCheckoutName(shortName), value);
	}
	
	public static void setMergeBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullMergeName(shortName), value);
	}
	
	public static void setSynchronizeBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullSynchronizeName(shortName), value);
	}
	
	public static void setPropertiesBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullPropertiesName(shortName), value);		
	}
	
	public static void setRepositoryString(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNTeamPreferences.fullRepositoryName(shortName), value);
	}
	
	public static void setRepositoryBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullRepositoryName(shortName), value);
	}

	public static void setDecorationString(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNTeamPreferences.fullDecorationName(shortName), value);
	}
	
	public static void setDecorationBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullDecorationName(shortName), value);
	}
	
	public static void setHistoryBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullHistoryName(shortName), value);
	}
	
	public static void setResourceSelectionBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullResourceSelectionName(shortName), value);
	}
	
	public static void setKeywordsBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullKeywordsName(shortName), value);
	}
	
	public static void setHistoryInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(SVNTeamPreferences.fullHistoryName(shortName), value);
	}
	
	public static boolean getMailReporterBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullMailReporterName(shortName));
	}
	
	public static void setMailReporterBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullMailReporterName(shortName), value);
	}
	
	public static void setCommentTemplatesInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(SVNTeamPreferences.fullCommentTemplatesName(shortName), value);
	}
	
	public static void setCommentTemplatesString(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNTeamPreferences.fullCommentTemplatesName(shortName), value);
	}
	
	public static void setCommentTemplatesBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullCommentTemplatesName(shortName), value);
	}
	
	public static void setAutoPropertiesList(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNTeamPreferences.fullAutoPropertiesName(shortName), value);
	}
	
	public static void setCustomPropertiesList(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNTeamPreferences.fullCustomPropertiesName(shortName), value);
	}
	
	public static RGB getConsoleRGB(IPreferenceStore store, String shortName) {
		return PreferenceConverter.getColor(store, SVNTeamPreferences.fullConsoleName(shortName));
	}
	
	public static void setConsoleRGB(IPreferenceStore store, String shortName, RGB value) {
		PreferenceConverter.setValue(store, SVNTeamPreferences.fullConsoleName(shortName), value);
	}
	
	public static int getConsoleInt(IPreferenceStore store, String shortName) {
		return store.getInt(SVNTeamPreferences.fullConsoleName(shortName));
	}
	
	public static void setConsoleInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(SVNTeamPreferences.fullConsoleName(shortName), value);
	}
	
	public static boolean getConsoleBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullConsoleName(shortName));
	}
	
	public static void setConsoleBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullConsoleName(shortName), value);
	}
	
	public static boolean getShareBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullShareName(shortName));
	}
	
	public static void setShareBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullShareName(shortName), value);
	}
	
	public static String getCoreString(IPreferenceStore store, String shortName) {
		return store.getString(SVNTeamPreferences.fullCoreName(shortName));
	}
	
	public static void setCoreString(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNTeamPreferences.fullCoreName(shortName), value);
	}
	
	public static RGB getAnnotateRGB(IPreferenceStore store, String shortName) {
		return PreferenceConverter.getColor(store, SVNTeamPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateRGB(IPreferenceStore store, String shortName, RGB value) {
		PreferenceConverter.setValue(store, SVNTeamPreferences.fullAnnotateName(shortName), value);
	}
	
	public static int getAnnotateInt(IPreferenceStore store, String shortName) {
		return store.getInt(SVNTeamPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateInt(IPreferenceStore store, String shortName, int value) {
		store.setValue(SVNTeamPreferences.fullAnnotateName(shortName), value);
	}
	
	public static String getAnnotateString(IPreferenceStore store, String shortName) {
		return store.getString(SVNTeamPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateString(IPreferenceStore store, String shortName, String value) {
		store.setValue(SVNTeamPreferences.fullAnnotateName(shortName), value);
	}
	
	public static boolean getAnnotateBoolean(IPreferenceStore store, String shortName) {
		return store.getBoolean(SVNTeamPreferences.fullAnnotateName(shortName));
	}
	
	public static void setAnnotateBoolean(IPreferenceStore store, String shortName, boolean value) {
		store.setValue(SVNTeamPreferences.fullAnnotateName(shortName), value);
	}
	
	public static String fullCommitDialogName(String shortName) {
		return SVNTeamPreferences.COMMIT_DIALOG_BASE + shortName;
	}
	
	public static String fullTableSortingName(String shortName) {
		return SVNTeamPreferences.TABLE_SORTING_BASE + shortName;
	}
	
	public static String fullCheckoutName(String shortName) {
		return SVNTeamPreferences.CHECKOUT_BASE + shortName;
	}
	
	public static String fullMergeName(String shortName) {
		return SVNTeamPreferences.MERGE_BASE + shortName;
	}
	
	public static String fullDecorationName(String shortName) {
		return SVNTeamPreferences.DECORATION_BASE + shortName;
	}
	
	public static String fullRepositoryName(String shortName) {
		return SVNTeamPreferences.REPOSITORY_BASE + shortName;
	}
	
	public static String fullSynchronizeName(String shortName) {
		return SVNTeamPreferences.SYNCHRONIZE_BASE + shortName;
	}
	
	public static String fullPropertiesName(String shortName) {
		return SVNTeamPreferences.PROPERTIES_BASE + shortName;
	}
	
	public static String fullHistoryName(String shortName) {
		return SVNTeamPreferences.HISTORY_BASE + shortName;
	}
	
	public static String fullResourceSelectionName(String shortName) {
		return SVNTeamPreferences.RESOURCE_SELECTION_BASE + shortName;
	}
	
	public static String fullMailReporterName(String shortName) {
		return SVNTeamPreferences.MAILREPORTER_BASE + shortName;
	}
	
	public static String fullCommitSelectName(String shortName) {
		return SVNTeamPreferences.COMMENT_TEMPLATES_BASE + shortName;
	}
	
	public static String fullCommentTemplatesName(String shortName) {
		return SVNTeamPreferences.COMMENT_TEMPLATES_BASE + shortName;
	}
	
	public static String fullKeywordsName(String shortName) {
		return SVNTeamPreferences.KEYWORDS_BASE + shortName;
	}
	
	public static String fullConsoleName(String shortName) {
		return SVNTeamPreferences.CONSOLE_BASE + shortName;
	}
	
	public static String fullShareName(String shortName) {
		return SVNTeamPreferences.SHARE_BASE + shortName;
	}
	
	public static String fullCoreName(String shortName) {
		return SVNTeamPreferences.CORE_BASE + shortName;
	}
	
	public static String fullAnnotateName(String shortName) {
		return SVNTeamPreferences.ANNOTATE_BASE + shortName;
	}
	
	public static String fullAutoPropertiesName(String shortName) {
		return SVNTeamPreferences.AUTO_PROPERTIES_BASE + shortName;
	}
	
	public static String fullCustomPropertiesName(String shortName) {
		return SVNTeamPreferences.CUSTOM_PROPERTIES_BASE + shortName;
	}
	
	private SVNTeamPreferences() {
		
	}
	
}
