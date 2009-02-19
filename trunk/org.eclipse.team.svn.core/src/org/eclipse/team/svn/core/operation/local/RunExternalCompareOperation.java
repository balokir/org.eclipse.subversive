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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.connector.SVNRevision.Kind;
import org.eclipse.team.svn.core.operation.AbstractActionOperation;
import org.eclipse.team.svn.core.operation.AbstractGetFileContentOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ExternalProgramParameters;
import org.eclipse.team.svn.core.operation.local.DiffViewerSettings.ResourceSpecificParameterKind;
import org.eclipse.team.svn.core.operation.remote.GetFileContentOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryFile;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResourceProvider;

/**
 * Run external compare editor operation implementation
 * 
 * @author Igor Burilo
 */
public class RunExternalCompareOperation extends CompositeOperation implements IExecutable {
	
	public final static String DOC_SCRIPT = "diff-doc.js"; //$NON-NLS-1$
	
	protected ExternalCompareOperation externalCompareOperation;
	
	public boolean isExecuted() {
		return this.externalCompareOperation.isExecuted();
	}
	
	public interface IExternalProgramParametersProvider {
		ExternalProgramParameters getExternalProgramParameters();
	}
	
	public static class DefaultExternalProgramParametersProvider implements IExternalProgramParametersProvider {
		protected ExternalProgramParameters externalProgramParameters;
		
		public DefaultExternalProgramParametersProvider(ExternalProgramParameters externalProgramParameters) {
			this.externalProgramParameters = externalProgramParameters;
		}

		public ExternalProgramParameters getExternalProgramParameters() {

			return this.externalProgramParameters;
		}				
	}	
	
	/**
	 * Determine what compare to use: external or internal
	 * 
	 * @author Igor Burilo
	 */
	public static class DetectExternalCompareOperationHelper {
		
		protected DiffViewerSettings diffSettings;
		protected IRepositoryResource repositoryResource;
		protected IResource resource;
		
		protected ExternalProgramParameters externalProgramParams;
		
		public DetectExternalCompareOperationHelper(IResource resource, DiffViewerSettings diffSettings) {
			this.diffSettings = diffSettings;
			this.resource = resource;
		}
		
		public DetectExternalCompareOperationHelper(IRepositoryResource repositoryResource, DiffViewerSettings diffSettings) {
			this.diffSettings = diffSettings;
			this.repositoryResource = repositoryResource;
		}
		
		public void execute(IProgressMonitor monitor) {
			if (this.resource != null) {
				this.detectWithResource(monitor);
			} else if (this.repositoryResource != null) {
				this.detectWithRepositoryResource(monitor);
			}			
		}
		
		protected void detectWithRepositoryResource(IProgressMonitor monitor) {
			//apply only to files
			if (this.repositoryResource instanceof IRepositoryFile) {
				//at first check extension
				IRepositoryFile file = (IRepositoryFile) this.repositoryResource;						
				ResourceSpecificParameterKind specificKind = DiffViewerSettings.getSpecificResourceKind(this.diffSettings, file, monitor);
				if (specificKind != null) {
					this.externalProgramParams = this.diffSettings.getResourceSpecificParameters(specificKind).params;	
				}								
				
				//check default external program
				if (this.externalProgramParams == null && this.diffSettings.isExternalDefaultCompare()) {
					this.externalProgramParams = this.diffSettings.getDefaultExternalParameters();	
				}
			}	
		}
		
		protected void detectWithResource(IProgressMonitor monitor) {
			//apply only to files
			if (this.resource instanceof IFile) {
				//at first check extension
				IFile file = (IFile) this.resource;						
				ResourceSpecificParameterKind specificKind = DiffViewerSettings.getSpecificResourceKind(this.diffSettings, file, monitor);
				if (specificKind != null) {
					this.externalProgramParams = this.diffSettings.getResourceSpecificParameters(specificKind).params;	
				}								
				
				//check default external program
				if (this.externalProgramParams == null && this.diffSettings.isExternalDefaultCompare()) {
					this.externalProgramParams = this.diffSettings.getDefaultExternalParameters();	
				}
			}	
		}
		
		public ExternalProgramParameters getExternalProgramParameters() {
			return this.externalProgramParams;
		}		
	}
		
	public static class DetectExternalCompareOperation extends AbstractActionOperation implements IExternalProgramParametersProvider{
		
		protected IResource resource;
		protected IRepositoryResource repositoryResource;
		protected IRepositoryResourceProvider provider;
		protected DiffViewerSettings diffSettings;
		
		protected DetectExternalCompareOperationHelper helper;
		
		protected DetectExternalCompareOperation(DiffViewerSettings diffSettings) {
			super("Operation_DetectExternalCompare"); //$NON-NLS-1$
			this.diffSettings = diffSettings;
		}
		public DetectExternalCompareOperation(IResource resource, DiffViewerSettings diffSettings) {
			this(diffSettings);
			this.resource = resource;
		}

		public DetectExternalCompareOperation(IRepositoryResource repositoryResource, DiffViewerSettings diffSettings) {
			this(diffSettings);			
			this.repositoryResource = repositoryResource;
		}
		
		public DetectExternalCompareOperation(IRepositoryResourceProvider provider, DiffViewerSettings diffSettings) {
			this(diffSettings);
			this.provider = provider;						
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			if (this.resource != null) {
				this.helper = new DetectExternalCompareOperationHelper(this.resource, this.diffSettings);	
			} else {
				IRepositoryResource reposResource = this.provider != null ? this.provider.getRepositoryResources()[0] : this.repositoryResource;
				this.helper = new DetectExternalCompareOperationHelper(reposResource, this.diffSettings);	
			}
						
			this.helper.execute(monitor);
		}
		
		public ExternalProgramParameters getExternalProgramParameters() {
			return this.helper.getExternalProgramParameters();
		}		
	} 
	
	/**
	 * Run external compare editor
	 *
	 * @author Igor Burilo
	 */
	public static class ExternalCompareOperationHelper {
		
		protected String baseFile;
		protected String currentFile;
		protected String newFile;
		protected String targetFile;
		protected ExternalProgramParameters externalProgramParams;
		
		public ExternalCompareOperationHelper(String baseFile, String currentFile, String newFile, ExternalProgramParameters externalProgramParams) {
			this(baseFile, currentFile, newFile, currentFile, externalProgramParams);
		}
		
		public ExternalCompareOperationHelper(String baseFile, String currentFile, String newFile, String targetFile, ExternalProgramParameters externalProgramParams) {
			this.externalProgramParams = externalProgramParams;
			this.baseFile = baseFile != null ? baseFile : ""; //$NON-NLS-1$
			this.currentFile = currentFile != null ? currentFile : ""; //$NON-NLS-1$
			this.newFile = newFile != null ? newFile : ""; //$NON-NLS-1$
			this.targetFile = targetFile != null ? targetFile : ""; //$NON-NLS-1$
		}

		public void execute(IProgressMonitor monitor) throws Exception {
			String processedCmd = this.externalProgramParams.programPath;						

			String parameters = this.prepareParameters(monitor);
			processedCmd += " " + parameters; //$NON-NLS-1$
			//System.out.println("Exec: " + processedCmd);
			
			/*Process process = */Runtime.getRuntime().exec(processedCmd);
			/*
			if (process != null) {															
				new ReaderThread(process.getInputStream(), System.out).start();
				new ReaderThread(process.getErrorStream(), System.err).start();
				process.getOutputStream().close();
			}
			
			//waits for external process to terminate
			//Note that we don't wait for Eclipse's compare editor
			process.waitFor();
			
			//update resource in Eclipse
			//local.getResource().refreshLocal(Depth.INFINITY, monitor);
			*/													
		}
		
		protected String prepareParameters(IProgressMonitor monitor) throws IOException {
			String paramaters = this.externalProgramParams.paramatersString;
			paramaters = paramaters.replace("${base}", this.baseFile);				 //$NON-NLS-1$
			paramaters = paramaters.replace("${mine}", this.currentFile);				 //$NON-NLS-1$
			paramaters = paramaters.replace("${theirs}", this.newFile);				 //$NON-NLS-1$
			paramaters = paramaters.replace("${merged}", this.targetFile);							 //$NON-NLS-1$
			if (paramaters.indexOf("${default-doc-program}") != -1) { //$NON-NLS-1$
				paramaters = paramaters.replace("${default-doc-program}", RunExternalCompareOperation.getScriptFile(RunExternalCompareOperation.DOC_SCRIPT, monitor).getAbsolutePath()); //$NON-NLS-1$
			}
			
			//TODO add other default programs
			
			return paramaters;
		}
	}
	
	/**
	 * Prepare files and run external compare editor
	 * 
	 * @author Igor Burilo
	 */
	public static class ExternalCompareOperation extends CompositeOperation {
		
		protected IExternalProgramParametersProvider parametersProvider;
		protected ExternalProgramParameters externalProgramParams;
		protected boolean isExecuted;
		
		public ExternalCompareOperation(ILocalResource local, IRepositoryResource remote, final IExternalProgramParametersProvider parametersProvider) {
			super("Operation_ExternalCompare"); //$NON-NLS-1$
			this.parametersProvider = parametersProvider;							
				
			/* As external program allows only cmd params we should pre-create
			 * files for compare.
			 */	
			
			SVNRevision revision = remote.getSelectedRevision();
			boolean fetchRemote = revision.getKind() == Kind.HEAD || revision.getKind() == Kind.NUMBER;		
			
			//old file
			final AbstractGetFileContentOperation oldFileGetOp = new GetLocalFileContentOperation(local.getResource(), Kind.BASE);
			this.add(oldFileGetOp);
						
			//current
			final AbstractGetFileContentOperation currentFileGetOp = new GetLocalFileContentOperation(local.getResource(), Kind.WORKING);
			this.add(currentFileGetOp, new IActionOperation[]{oldFileGetOp});
			
			//remote: either from repository or BASE
			final AbstractGetFileContentOperation newFileGetOp = fetchRemote ? new GetFileContentOperation(remote) : new GetLocalFileContentOperation(local.getResource(), Kind.BASE);			
			this.add(newFileGetOp, new IActionOperation[]{currentFileGetOp});
					
			//Run external program					
			this.add(new AbstractActionOperation("Operation_ExternalCompare") { //$NON-NLS-1$
				protected void runImpl(IProgressMonitor monitor) throws Exception {
					ExternalCompareOperationHelper externalRunHelper = new ExternalCompareOperationHelper(
							oldFileGetOp.getTemporaryPath(),
							currentFileGetOp.getTemporaryPath(),
							newFileGetOp.getTemporaryPath(),
							ExternalCompareOperation.this.externalProgramParams);
					externalRunHelper.execute(monitor);										
				}						
			}, new IActionOperation[]{oldFileGetOp, currentFileGetOp, newFileGetOp});			
		}
		
		protected void runImpl(IProgressMonitor monitor) throws Exception {
			this.externalProgramParams = this.parametersProvider.getExternalProgramParameters();
			if (this.externalProgramParams != null) {	
				this.isExecuted = true;
				super.runImpl(monitor);	
			} else {
				this.isExecuted = false;
			}
		}
		
		public boolean isExecuted() {
			return this.isExecuted;
		}
	}
	
	public RunExternalCompareOperation(ILocalResource local, IRepositoryResource remote, DiffViewerSettings diffSettings) {		
		super("Operation_ExternalCompare");		 //$NON-NLS-1$
				
		DetectExternalCompareOperation detectOperation = new DetectExternalCompareOperation(local.getResource(), diffSettings);
		this.add(detectOperation);
		
		this.externalCompareOperation = new ExternalCompareOperation(local, remote, detectOperation); 
		this.add(externalCompareOperation, new IActionOperation[]{detectOperation});													
	} 
	
	public static File getScriptFile(String fileName, IProgressMonitor monitor) throws IOException {
		File stateArea = SVNTeamPlugin.instance().getStateLocation().toFile();
		File scriptFile = new File(stateArea, fileName);
		if (!scriptFile.exists()) {
			URL scriptUrl = FileLocator.find(SVNTeamPlugin.instance().getBundle(), new Path("/resources/" + fileName), null); //$NON-NLS-1$
			InputStream in = null;
			FileOutputStream out = null;
			try {
				in = scriptUrl.openStream();			
				out = new FileOutputStream(scriptFile);
				int loaded = 0;
				byte[] buf = new byte[2048];
				while ((loaded = in.read(buf)) > 0 && !monitor.isCanceled()) {
					out.write(buf, 0, loaded);
				}	
			} finally {
				if (in != null) {
					try {
						in.close();	
					} catch (IOException ie) {
						//ignore
					}					
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException ie) {
						//ignore
					} 
				}
			}
		}		
		return scriptFile;
	}
	
//	public static class ReaderThread extends Thread {
//
//		private final InputStream myInputStream;
//	    private final OutputStream myOutputStream;
//
//	    public ReaderThread(InputStream is, OutputStream os) {
//	        myInputStream = is;
//	        myOutputStream = os;
//	        setDaemon(true);            
//	    }
//
//	    public void run() {
//	        try {
//	            while(true) {
//	                int read = myInputStream.read();
//	                if (read < 0) {
//	                    return;
//	                }
//	                myOutputStream.write(read);
//	            }
//	        } catch (IOException e) {
//	        } finally {
//	            try {
//	            	myInputStream.close();
//	                myOutputStream.flush();
//	            } catch (IOException e) {
//	            	//Just ignore. Stream closing.
//	            }
//	        }
//	    }
//	}

}
