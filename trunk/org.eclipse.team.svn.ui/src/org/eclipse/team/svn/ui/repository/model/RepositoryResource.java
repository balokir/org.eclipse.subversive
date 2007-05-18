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

package org.eclipse.team.svn.ui.repository.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.team.svn.core.client.Lock;
import org.eclipse.team.svn.core.client.Revision;
import org.eclipse.team.svn.core.operation.AbstractNonLockingOperation;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.LoggedOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryResource.Info;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.decorator.DecoratorVariables;
import org.eclipse.team.svn.ui.decorator.IVariable;
import org.eclipse.team.svn.ui.decorator.IVariableContentProvider;
import org.eclipse.team.svn.ui.decorator.UserVariable;
import org.eclipse.team.svn.ui.operation.GetRemoteResourceRevisionOperation;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.repository.RepositoriesView;
import org.eclipse.team.svn.ui.repository.RepositoryTreeViewer;
import org.eclipse.team.svn.ui.utility.DefaultOperationWrapperFactory;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Abstract repository element representation 
 * 
 * @author Alexander Gurov
 */
public abstract class RepositoryResource implements IWorkbenchAdapter, IWorkbenchAdapter2, IAdaptable, IResourceTreeNode, IToolTipProvider, IVariableContentProvider {
	public static RGB NOT_RELATED_NODES_FOREGROUND;
	public static RGB NOT_RELATED_NODES_BACKGROUND;
	public static Font NOT_RELATED_NODES_FONT;
	public static RGB STRUCTURE_DEFINED_NODES_FOREGROUND;
	public static RGB STRUCTURE_DEFINED_NODES_BACKGROUND;
	public static Font STRUCTURE_DEFINED_NODES_FONT;
	
    protected static Map images = new HashMap();
    protected ImageDescriptor lockDescriptor;
    protected GetRemoteResourceRevisionOperation revisionOp;
    protected IRepositoryResource resource;
	protected RepositoryTreeViewer repositoryTree;
	
	protected boolean relatesToLocation;
	protected IPropertyChangeListener configurationListener;
	
	protected DecoratorVariables toolTipDecorator;
	
    public RepositoryResource(IRepositoryResource resource) {
        this.resource = resource;
        this.relatesToLocation = this.resource.getSelectedRevision().getKind() == Revision.Kind.head && new Path(this.resource.getRepositoryLocation().getUrl()).isPrefixOf(new Path(this.resource.getUrl()));
        if (RepositoryResource.NOT_RELATED_NODES_FOREGROUND == null) {
        	this.initializeFontsAndColors();
        }
        this.toolTipDecorator = new DecoratorVariables(ToolTipVariableSetProvider.instance);
    }

    public void setViewer(RepositoryTreeViewer repositoryTree) {
    	this.repositoryTree = repositoryTree;
    }
    
    public IRepositoryResource getRepositoryResource() {
    	return this.resource;
    }
    
    public Object getData() {
    	return this.resource;
    }
    
    public void refresh() {
    	this.resource.refresh();
    	this.revisionOp = null;
    }
    
    public RGB getBackground(Object element) {
    	return this.relatesToLocation ? null : RepositoryResource.NOT_RELATED_NODES_BACKGROUND;
    }
    
    public RGB getForeground(Object element) {
    	return this.relatesToLocation ? null : RepositoryResource.NOT_RELATED_NODES_FOREGROUND;
    }
    
    public FontData getFont(Object element) {
    	return this.relatesToLocation ? null : RepositoryResource.NOT_RELATED_NODES_FONT.getFontData()[0];
    }
    
    public String getLabel(Object o) {
		String retVal = this.getLabel() + " ";
		try {
	        retVal += this.getRevision();
		}
		catch (Exception e) {
			LoggedOperation.reportError(SVNTeamUIPlugin.instance().getResource("Error.FormatLabel"), e);
			retVal += SVNTeamUIPlugin.instance().getResource(RepositoryError.ERROR_MSG);
		}
		return retVal;
	}

	public ImageDescriptor getImageDescriptor(Object o) {
		ImageDescriptor originalDescriptor = this.getImageDescriptorImpl();
		if (this.resource.getInfo() != null && this.resource.getInfo().lock != null) {
			if (this.lockDescriptor == null) {
		        this.lockDescriptor = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/lock.gif");
			}
			synchronized (RepositoryResource.images) {
			    OverlayedImageDescriptor imgDescr = (OverlayedImageDescriptor)RepositoryResource.images.get(originalDescriptor);
			    if (imgDescr == null) {
			    	Image image = originalDescriptor.createImage();
		            CompareUI.disposeOnShutdown(image);
		            RepositoryResource.images.put(originalDescriptor, imgDescr = new OverlayedImageDescriptor(image, this.lockDescriptor, new Point(16, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.BOTTOM));
			    }
			    return imgDescr;
			}
		}
		return originalDescriptor;
	}
	
	public Object getParent(Object o) {
		return this.resource.getParent();
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class) || adapter.equals(IWorkbenchAdapter2.class)) {
			return this;
		}
		return null;
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof RepositoryResource) {
			return ((RepositoryResource)obj).resource.equals(this.resource);
		}
		return super.equals(obj);
	}
	
	public String getToolTipMessage(String formatString) {
		ToolTipMessage tooltipMessage = new ToolTipMessage();
		IVariable[] format = this.toolTipDecorator.parseFormatLine(formatString);
		this.toolTipDecorator.decorateText(tooltipMessage, format, this);
		return tooltipMessage.getMessage();
	}
	
	public String getValue(IVariable var) {
		IRepositoryResource resource = (IRepositoryResource)this.getRepositoryResource();
		Info info = resource.getInfo();
		Lock lock = info == null ? null : info.lock;
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
		if (var.equals(ToolTipVariableSetProvider.VAR_NAME)) {
			return RepositoryResource.this.formatToolTipLine(var, resource.getName());
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_URL)) {
			return RepositoryResource.this.formatToolTipLine(var, resource.getUrl());
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_LAST_CHANGE_DATE)) {
			if (info != null && info.lastChangedDate != null) {
				return RepositoryResource.this.formatToolTipLine(var, dateFormat.format(info.lastChangedDate));
			}
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_LAST_AUTHOR)) {
			if (info != null && info.lastAuthor != null && !info.lastAuthor.equals("")) {
				return RepositoryResource.this.formatToolTipLine(var, info.lastAuthor);
			}
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_SIZE)) {
			if (info != null) {
				return RepositoryResource.this.formatToolTipLine(var, String.valueOf(info.fileSize));
			}
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_LOCK_OWNER)) {
			if (lock != null && lock.owner != null) {
				return RepositoryResource.this.formatToolTipLine(var, lock.owner);
			}
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_LOCK_CREATION_DATE)) {
			if (lock != null && lock.creationDate != 0) {
				return RepositoryResource.this.formatToolTipLine(var, dateFormat.format(new Date(lock.creationDate)));
			}
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_LOCK_EXPIRATION_DATE)) {
			if (lock != null && lock.expirationDate != 0) {
				return RepositoryResource.this.formatToolTipLine(var, dateFormat.format(new Date(lock.expirationDate)));
			}
		}
		else if (var.equals(ToolTipVariableSetProvider.VAR_LOCK_COMMENT)) {
			if (lock != null && lock.comment != null && lock.comment.length() != 0) {
				return RepositoryResource.this.formatToolTipLine(var, lock.comment);
			}
		}
		if (var instanceof UserVariable) {
			return var.toString();
		}
		return "";
	}
	
	protected String formatToolTipLine(IVariable var, String value) {
		return var.getDescription() + " " + value + "\n";
	}
	
	protected void initializeFontsAndColors() {
		this.configurationListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().startsWith(SVNTeamPreferences.DECORATION_BASE)) {
					RepositoryResource.loadConfiguration();
					RepositoriesView repositoriesView = RepositoriesView.instance();
					if (repositoriesView != null) {
						repositoriesView.getRepositoryTree().refresh();
					}
				}
			}
		};
		RepositoryResource.loadConfiguration();
		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().addPropertyChangeListener(this.configurationListener);
	}
	
	protected static void loadConfiguration() {
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				ITheme current = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
				RepositoryResource.NOT_RELATED_NODES_FOREGROUND = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_NOT_RELATED_NODES_FOREGROUND_COLOR)).getRGB();
				RepositoryResource.NOT_RELATED_NODES_BACKGROUND = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_NOT_RELATED_NODES_BACKGROUND_COLOR)).getRGB();
				RepositoryResource.NOT_RELATED_NODES_FONT = current.getFontRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_NOT_RELATED_NODES_FONT));
				RepositoryResource.STRUCTURE_DEFINED_NODES_FOREGROUND = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_STRUCTURE_NODES_FOREGROUND_COLOR)).getRGB();
				RepositoryResource.STRUCTURE_DEFINED_NODES_BACKGROUND = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_STRUCTURE_NODES_BACKGROUND_COLOR)).getRGB();
				RepositoryResource.STRUCTURE_DEFINED_NODES_FONT = current.getFontRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_STRUCTURE_NODES_FONT));
			}
		});
	}
	
	protected String getLabel() {
		return this.resource.getName();
	}
	
	public String getRevision() throws Exception {
		if (this.revisionOp != null) {
			return this.revisionOp.getRevision() == Revision.SVN_INVALID_REVNUM ? SVNTeamUIPlugin.instance().getResource(this.revisionOp.getExecutionState() == IActionOperation.ERROR ? RepositoryError.ERROR_MSG : RepositoryPending.PENDING) : String.valueOf(this.revisionOp.getRevision());
		}
		
		if (this.resource.isInfoCached()) {
			return String.valueOf(this.resource.getRevision());
		}

		this.revisionOp = new GetRemoteResourceRevisionOperation(this.resource);
		CompositeOperation op = new CompositeOperation(this.revisionOp.getId());
		op.add(this.revisionOp);
		op.add(this.getRefreshOperation(this.getViewer()));

		UIMonitorUtility.doTaskScheduled(op, new DefaultOperationWrapperFactory() {
            public IActionOperation getLogged(IActionOperation operation) {
        		return new LoggedOperation(operation);
            }
        });
		
		return SVNTeamUIPlugin.instance().getResource(RepositoryPending.PENDING);
	}

	protected RepositoryTreeViewer getViewer() {
		return this.repositoryTree;
	}
	
	protected RefreshOperation getRefreshOperation(RepositoryTreeViewer viewer) {
		return new RefreshOperation(viewer);
	}

	protected abstract ImageDescriptor getImageDescriptorImpl();
	
	protected class RefreshOperation extends AbstractNonLockingOperation {
		protected RepositoryTreeViewer viewer;
		
		public RefreshOperation(RepositoryTreeViewer viewer) {
			super("Operation.RefreshView");
			this.viewer = viewer;
		}

		protected void runImpl(IProgressMonitor monitor) throws Exception {
			// TODO rework this using cancellation manager in order to make it thread-safe...
			if (this.viewer != null && !this.viewer.getControl().isDisposed()) {
				this.viewer.refresh(RepositoryResource.this, null);
			}
		}
	}
	
}
