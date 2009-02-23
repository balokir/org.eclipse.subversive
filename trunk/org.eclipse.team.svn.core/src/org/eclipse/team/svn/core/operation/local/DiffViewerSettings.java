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

package org.eclipse.team.svn.core.operation.local;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.team.svn.core.connector.SVNProperty;
import org.eclipse.team.svn.core.operation.local.property.GetPropertiesOperation;
import org.eclipse.team.svn.core.operation.remote.GetRemotePropertiesOperation;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.ProgressMonitorUtility;

/**
 * Model for DiffViewer
 * 
 * @author Igor Burilo
 */
public class DiffViewerSettings {		
		
	public enum ResourceSpecificParameterKindEnum {MIME_TYPE, FILE_EXTENSION};
	
	public static class ResourceSpecificParameterKind {
		public ResourceSpecificParameterKindEnum kindEnum;
		public String kindValue;
		
		public ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum kindEnum, String kindValue) {
			this.kindEnum = kindEnum;
			this.kindValue = kindValue;
		}
		
		public boolean equals(Object ob) {
			boolean isEqual = false;
			if (ob != null && ob instanceof ResourceSpecificParameterKind) {
				ResourceSpecificParameterKind kind = (ResourceSpecificParameterKind) ob; 
				if (this.kindEnum == kind.kindEnum && this.kindValue.equals(kind.kindValue)) {
					isEqual = true;
				}
			}
			return isEqual;
		}
		
		public String formatKindValue() {
			String res = this.kindValue;
			if (ResourceSpecificParameterKindEnum.FILE_EXTENSION.equals(this.kindEnum)) {
				res = "." + this.kindValue; //$NON-NLS-1$
			}
			return res;
		}
		
		public static ResourceSpecificParameterKind getKind(String kindString) {			
			String kindValue = null;
			ResourceSpecificParameterKindEnum kindEnum = null;
			if (kindString.charAt(0) == '.') {
				kindEnum = ResourceSpecificParameterKindEnum.FILE_EXTENSION;
				kindValue = kindString.substring(1);
			} else {
				kindEnum = ResourceSpecificParameterKindEnum.MIME_TYPE;
				kindValue = kindString;
			}
			ResourceSpecificParameterKind kind = new ResourceSpecificParameterKind(kindEnum, kindValue);
			return kind;
		}
		
		public int hashCode() {		
			return this.kindEnum.hashCode() + this.kindValue.hashCode();
		}
	}
	
	public static class ResourceSpecificParameters {
		
		public final static int FIELDS_COUNT = 5;
		
		public ResourceSpecificParameterKind kind;
		public ExternalProgramParameters params;		
		public boolean isEnabled;
		
		public ResourceSpecificParameters(ResourceSpecificParameterKindEnum kindEnum, String kindValue, String programPath, String programParams) {
			this(new ResourceSpecificParameterKind(kindEnum, kindValue), new ExternalProgramParameters(programPath, programParams));
		}
		
		public ResourceSpecificParameters(ResourceSpecificParameterKind kind, ExternalProgramParameters params) {
			this(kind, params, true);
		}
		
		public ResourceSpecificParameters(ResourceSpecificParameterKind kind, ExternalProgramParameters params, boolean isEnabled) {
			this.kind = kind;
			this.params = params;
			this.isEnabled = isEnabled;
		}
		
		public String[] getAsStrings() {			
			String[] res = new String[ResourceSpecificParameters.FIELDS_COUNT];
			
			res[0] = this.isEnabled ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
			res[1] = ResourceSpecificParameterKindEnum.FILE_EXTENSION.equals(this.kind.kindEnum) ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
			res[2] = this.kind.kindValue == null ? "" : this.kind.kindValue; //$NON-NLS-1$
			res[3] = this.params.programPath == null ? "" : this.params.programPath; //$NON-NLS-1$
			res[4] = this.params.paramatersString == null ? "" :  this.params.paramatersString; //$NON-NLS-1$
			
			return res;
		}
		
		public static ResourceSpecificParameters createFromStrings(String[] strings) {
			if (strings.length != FIELDS_COUNT) {
				throw new RuntimeException("Failed to create " + ResourceSpecificParameters.class +  //$NON-NLS-1$
					" from string array. String array: " + Arrays.asList(strings)); //$NON-NLS-1$
			}
			
			boolean isEnabled = "1".equals(strings[0]); //$NON-NLS-1$
			ResourceSpecificParameterKindEnum kindEnum = "1".equals(strings[1]) ? ResourceSpecificParameterKindEnum.FILE_EXTENSION : ResourceSpecificParameterKindEnum.MIME_TYPE; //$NON-NLS-1$
			
			ResourceSpecificParameters res = new ResourceSpecificParameters(kindEnum, strings[2], strings[3], strings[4]);
			res.isEnabled = isEnabled;
			
			return res;
		}
	}	
	
	public static class ExternalProgramParameters {
		public String programPath;
		public String paramatersString;
		
		public ExternalProgramParameters(String programPath, String paramatersString) {
			this.programPath = programPath;
			this.paramatersString = paramatersString;
		}
	}
	
	protected boolean isExternalDefaultCompare;
	protected ExternalProgramParameters defaultExternalParameters;
	protected Map<ResourceSpecificParameterKind, ResourceSpecificParameters> specificParameters = new HashMap<ResourceSpecificParameterKind, ResourceSpecificParameters>();

	public static ResourceSpecificParameterKind getSpecificResourceKind(DiffViewerSettings diffSettings, IFile file, IProgressMonitor monitor) {
		ResourceSpecificParameterKind kind = null;

		//check file extension			
		String fileExtension = file.getFileExtension();
		if (fileExtension != null && diffSettings.specificParameters.containsKey(new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.FILE_EXTENSION, fileExtension))) {
			kind = new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.FILE_EXTENSION, fileExtension);
		}
		
		//check mime type
		if (kind == null) {
			String mimeType = null;
			
			GetPropertiesOperation op = new GetPropertiesOperation(file);
    		ProgressMonitorUtility.doTaskExternalDefault(op, monitor);
    		if (op.getExecutionState() == IStatus.OK) {
    			SVNProperty[] props = op.getProperties();
    			if (props != null) {
    				for (SVNProperty prop : props) {
    					if (SVNProperty.BuiltIn.MIME_TYPE.equals(prop.name)) {
    						mimeType = prop.value;
    						break;
    					}
    				}
    			}		    															
    		}	
    		
    		if (mimeType != null && diffSettings.specificParameters.containsKey(new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.MIME_TYPE, mimeType))) {
    			kind = new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.MIME_TYPE, mimeType);
			}
		}									
				
		return kind;
	}
	
	public static ResourceSpecificParameterKind getSpecificResourceKind(DiffViewerSettings diffSettings, IRepositoryFile file, IProgressMonitor monitor) {
		ResourceSpecificParameterKind kind = null;				
		
		//check file extension
		String fileExtension = null;
		String fileName = file.getName();
		int index = fileName.lastIndexOf("."); //$NON-NLS-1$
		if (index != -1 && index != fileName.length()) {
			fileExtension = fileName.substring(index + 1);
			if (diffSettings.specificParameters.containsKey(new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.FILE_EXTENSION, fileExtension))) {
				kind = new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.FILE_EXTENSION, fileExtension);
			}
		}
				
		//check mime type
		if (kind == null) {
			String mimeType = null;
			
			GetRemotePropertiesOperation op = new GetRemotePropertiesOperation(file);
    		ProgressMonitorUtility.doTaskExternalDefault(op, monitor);
    		if (op.getExecutionState() == IStatus.OK) {
    			SVNProperty[] props = op.getProperties();
    			if (props != null) {
    				for (SVNProperty prop : props) {
    					if (SVNProperty.BuiltIn.MIME_TYPE.equals(prop.name)) {
    						mimeType = prop.value;
    						break;
    					}
    				}
    			}		    															
    		}	
    		
    		if (mimeType != null && diffSettings.specificParameters.containsKey(new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.MIME_TYPE, mimeType))) {
    			kind = new ResourceSpecificParameterKind(ResourceSpecificParameterKindEnum.MIME_TYPE, mimeType);
			}
		}									
				
		return kind;
	}
	
	public void addResourceSpecificParameters(ResourceSpecificParameterKindEnum kindEnum, String kindValue, ExternalProgramParameters params) {
		this.addResourceSpecificParameters(kindEnum, kindValue, params, true);
	}
	
	public void addResourceSpecificParameters(ResourceSpecificParameterKindEnum kindEnum, String kindValue, ExternalProgramParameters params, boolean isEnabled) {
		ResourceSpecificParameterKind kind = new ResourceSpecificParameterKind(kindEnum, kindValue);
		ResourceSpecificParameters resourceParams = new ResourceSpecificParameters(kind, params, isEnabled);
		this.addResourceSpecificParameters(resourceParams);
	}
	
	public void addResourceSpecificParameters(ResourceSpecificParameters resourceParams) {
		this.specificParameters.put(resourceParams.kind, resourceParams);
		
		//fire event
		if (!this.listenersList.isEmpty()) {
			Object[] listeners = this.listenersList.getListeners();
			for (int i = 0; i < listeners.length; i ++) {
				IDiffViewerChangeListener listener = (IDiffViewerChangeListener) listeners[i];
				listener.addResourceSpecificParameters(resourceParams);
			}
		}
	}
	
	public void removeResourceSpecificParameters(ResourceSpecificParameters resourceParams) {				
		this.specificParameters.remove(resourceParams.kind);
		
		//fire event
		if (!this.listenersList.isEmpty()) {
			Object[] listeners = this.listenersList.getListeners();
			for (int i = 0; i < listeners.length; i ++) {
				IDiffViewerChangeListener listener = (IDiffViewerChangeListener) listeners[i];
				listener.removeResourceSpecificParameters(resourceParams);
			}
		}
	}
	
	public void updateResourceSpecificParameters(ResourceSpecificParameters resourceParams) {
		//fire event
		if (!this.listenersList.isEmpty()) {
			Object[] listeners = this.listenersList.getListeners();
			for (int i = 0; i < listeners.length; i ++) {
				IDiffViewerChangeListener listener = (IDiffViewerChangeListener) listeners[i];
				listener.changeResourceSpecificParameters(resourceParams);
			}
		}
	}
	
	public void setExternalDefaultCompare(boolean isExternalDefaultCompare) {
		this.isExternalDefaultCompare = isExternalDefaultCompare;
	}
	
	public boolean isExternalDefaultCompare() {
		return this.isExternalDefaultCompare;
	}

	public ExternalProgramParameters getDefaultExternalParameters() {
		return this.defaultExternalParameters;
	}
	
	public void setDefaultExternalParameters(String programPath, String paramatersString) {
		this.defaultExternalParameters = new ExternalProgramParameters(programPath, paramatersString);
	}
	
	public ResourceSpecificParameters getResourceSpecificParameters(ResourceSpecificParameterKind kind) {
		return this.specificParameters.get(kind);
	}
	
	public ResourceSpecificParameters[] getResourceSpecificParameters() {
		return this.specificParameters.values().toArray(new ResourceSpecificParameters[0]);
	}

	public static DiffViewerSettings getDefaultDiffViewerSettings() {		
		DiffViewerSettings diffSettings = new DiffViewerSettings();
		if (FileUtility.isWindows()) {			
			diffSettings.setExternalDefaultCompare(false);						
			
			//doc
			String programPath = "wscript.exe"; //$NON-NLS-1$
			String parametersString = "\"${default-doc-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "doc", new ExternalProgramParameters(programPath, parametersString));	//$NON-NLS-1$
			//docx
			programPath = "wscript.exe"; //$NON-NLS-1$
			parametersString = "\"${default-docx-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "docx", new ExternalProgramParameters(programPath, parametersString)); //$NON-NLS-1$
			
			//xls
			programPath = "wscript.exe"; //$NON-NLS-1$
			parametersString = "\"${default-xls-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "xls", new ExternalProgramParameters(programPath, parametersString)); //$NON-NLS-1$
			//xlsx
			programPath = "wscript.exe"; //$NON-NLS-1$
			parametersString = "\"${default-xlsx-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "xlsx", new ExternalProgramParameters(programPath, parametersString)); //$NON-NLS-1$
			
			//ppt
			programPath = "wscript.exe"; //$NON-NLS-1$
			parametersString = "\"${default-ppt-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "ppt", new ExternalProgramParameters(programPath, parametersString)); //$NON-NLS-1$
			//pptx
			programPath = "wscript.exe"; //$NON-NLS-1$
			parametersString = "\"${default-pptx-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "pptx", new ExternalProgramParameters(programPath, parametersString)); //$NON-NLS-1$
			
			//odt
			programPath = "wscript.exe"; //$NON-NLS-1$
			parametersString = "\"${default-odt-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "odt", new ExternalProgramParameters(programPath, parametersString));	//$NON-NLS-1$
			//ods
			programPath = "wscript.exe"; //$NON-NLS-1$
			parametersString = "\"${default-ods-program}\" \"${base}\" \"${mine}\" //E:vbscript"; //$NON-NLS-1$
			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "ods", new ExternalProgramParameters(programPath, parametersString)); //$NON-NLS-1$
			
//			//java			
//			String programPath = "C:/Program Files/TortoiseSVN/bin/TortoiseMerge.exe";
//			//String parametersString = "/theirs:\"${theirs}\" /base:\"${base}\" /mine:\"${mine}\" /merged:\"${merged}\"";
//			String parametersString = "/base:\"${base}\" /mine:\"${mine}\" /merged:\"${merged}\"";
//			diffSettings.addResourceSpecificParameters(ResourceSpecificParameterKindEnum.FILE_EXTENSION, "java", new ExternalProgramParameters(programPath, parametersString));						
		}
		return diffSettings;
	}
	
	//---- listeners
	
	public interface IDiffViewerChangeListener {
		void addResourceSpecificParameters(ResourceSpecificParameters pameters);
		void removeResourceSpecificParameters(ResourceSpecificParameters pameters);
		void changeResourceSpecificParameters(ResourceSpecificParameters pameters);
	}
	
	protected ListenerList listenersList = new ListenerList();
	
	public void addChangeListener(IDiffViewerChangeListener listener) {
		this.listenersList.add(listener);
	}
	
	public void removeChangeListener(IDiffViewerChangeListener listener) {
		this.listenersList.remove(listener);
	}
}
