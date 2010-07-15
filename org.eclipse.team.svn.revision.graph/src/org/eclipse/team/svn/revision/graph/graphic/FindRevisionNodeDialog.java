/*******************************************************************************
 * Copyright (c) 2005-2010 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo (Polarion Software) - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.svn.revision.graph.graphic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.svn.revision.graph.SVNRevisionGraphMessages;
import org.eclipse.team.svn.revision.graph.graphic.editpart.GraphScalableRootEditPart;
import org.eclipse.team.svn.revision.graph.graphic.editpart.RevisionEditPart;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.UserInputHistory;

/**
 * Find revision nodes dialog
 * 
 * @author Igor Burilo
 */
public class FindRevisionNodeDialog extends Dialog {
	
	//used for bounds
	protected Composite parent;

	//can be null	
	protected RevisionGraphEditor graphEditor;
	
	protected SearchOptions searchOptions;
	protected NodesIterator<RevisionEditPart> searchResult;	

	protected Label statusLabel;
	protected Combo revisionCombo;
	protected Combo pathCombo;
	protected Button nextButton;
	protected Button previousButton;
	
	protected UserInputHistory pathHistory;
	protected UserInputHistory revisionHistory;	
	
	public FindRevisionNodeDialog(Shell shell) {
		super(shell);							
		
		setShellStyle(getShellStyle() ^ SWT.APPLICATION_MODAL | SWT.MODELESS | SWT.RESIZE);
		setBlockOnOpen(false);
		
		this.pathHistory = new UserInputHistory("findDialogPath"); //$NON-NLS-1$
		this.revisionHistory = new UserInputHistory("findDialogRevision"); //$NON-NLS-1$
	}	
	
	@Override
	protected Control createContents(Composite parent) {			
		GridLayout layout = null;
		GridData data = null;
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 7;
		layout.marginWidth = 7;
		layout.verticalSpacing = 0;
		mainComposite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		mainComposite.setLayoutData(data);
		Dialog.applyDialogFont(mainComposite);
		this.initializeDialogUnits(mainComposite);
		
		Composite mainPanel = this.createMainPanel(mainComposite);
		this.dialogArea = mainPanel;
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = this.getPrefferedSize().x;
		this.dialogArea.setLayoutData(data);
		this.parent = mainPanel;
		
		Composite buttonPanel = this.createButtonSection(mainComposite);
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		data.verticalAlignment = SWT.BOTTOM;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = false;
		buttonPanel.setLayoutData(data);
		
		Composite statusPanel = this.createStatusAndCloseButton(mainComposite);
		data = new GridData();		
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.BOTTOM;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = false;
		data.verticalIndent = 5;
		statusPanel.setLayoutData(data);
		
		//computing the best size for the dialog
		Point defaultSize = this.dialogArea.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data = (GridData)this.dialogArea.getLayoutData();
		int defaultHeightHint = data.heightHint;
		data.heightHint = this.getPrefferedSize().y;
		this.dialogArea.setLayoutData(data);
		Point prefferedSize = this.dialogArea.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		if (prefferedSize.y < defaultSize.y) {
			data.heightHint = defaultHeightHint;
			this.dialogArea.setLayoutData(data);
		}
				
		return mainComposite;
	}

    public final Point getPrefferedSize() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		int width = SVNTeamPreferences.getDialogInt(store, this.getDialogID() + ".width"); //$NON-NLS-1$
		int height = SVNTeamPreferences.getDialogInt(store, this.getDialogID() + ".height"); //$NON-NLS-1$
		Point prefSize = this.getPrefferedSizeImpl();
		width = Math.max(width, prefSize.x);
		height = Math.max(height, prefSize.y);
    	return new Point(width, height); 
	}
    
    protected Point getPrefferedSizeImpl() {
        return new Point(100, 80);
    }
    
    protected String getDialogID() {
    	return this.getClass().getName();
    }
	
	@Override
	public void create() {
		super.create();
		
		Shell shell= this.getShell();
		shell.setText(SVNRevisionGraphMessages.FindRevisionNodeDialog_Find);
		
		//set help context		
		
		this.validate();
	}
	
	protected Composite createMainPanel(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite panel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		panel.setLayout(layout);				
		
		ModifyListener listener = new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				FindRevisionNodeDialog.this.validate();
			}
		};
		
		//path
		Label pathLabel = new Label(panel, SWT.NONE);
		pathLabel.setLayoutData(new GridData());
		pathLabel.setText(SVNRevisionGraphMessages.FindRevisionNodeDialog_Path);
		
		this.pathCombo = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);		
		this.pathCombo.setLayoutData(data);
		this.pathCombo.addModifyListener(listener);
		this.pathCombo.setVisibleItemCount(this.pathHistory.getDepth());
		this.pathCombo.setItems(this.pathHistory.getHistory());
		if (this.pathHistory.getDepth() > 0) {
			this.pathCombo.select(0);	
		}		
		
		//revision
		Label revisionLabel = new Label(panel, SWT.NONE);
		revisionLabel.setLayoutData(new GridData());
		revisionLabel.setText(SVNRevisionGraphMessages.FindRevisionNodeDialog_Revision);
		
		this.revisionCombo = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		this.revisionCombo.setLayoutData(data);
		this.revisionCombo.addModifyListener(listener);
		this.revisionCombo.setVisibleItemCount(this.revisionHistory.getDepth());
		this.revisionCombo.setItems(this.revisionHistory.getHistory());				
		if (this.revisionHistory.getDepth() > 0) {
			this.revisionCombo.select(0);	
		}		
		
		return panel;
	}
	
	protected Composite createButtonSection(Composite parent) {
		GridLayout layout = null;
		//GridData data = null;
		
		Composite panel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		panel.setLayout(layout);		
		
		this.nextButton = this.createButton(panel, SVNRevisionGraphMessages.FindRevisionNodeDialog_Next, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FindRevisionNodeDialog.this.findNext();
			}
		}) ;			
		parent.getShell().setDefaultButton(this.nextButton);
		
		this.previousButton = this.createButton(panel, SVNRevisionGraphMessages.FindRevisionNodeDialog_Previous, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FindRevisionNodeDialog.this.findPrevious();
			}
		}) ;									
		
		return panel;
	}
	
	protected Composite createStatusAndCloseButton(Composite parent) {
		GridLayout layout = null;
		GridData data = null;
		
		Composite panel = new Composite(parent, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);		
		
		this.statusLabel = new Label(panel, SWT.LEFT);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.CENTER;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = false;
		this.statusLabel.setLayoutData(data);		

		Button closeButton = this.createButton(panel, SVNRevisionGraphMessages.FindRevisionNodeDialog_Close, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FindRevisionNodeDialog.this.close();
			}
		}); 
		data = (GridData) closeButton.getLayoutData();
		data.horizontalAlignment = SWT.RIGHT;
		data.verticalAlignment = SWT.BOTTOM;
		data.grabExcessHorizontalSpace = false;
		data.grabExcessVerticalSpace = false;
		
		return panel;
	}
	
	protected Button createButton(Composite parent, String text, SelectionListener listener) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		GridData data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(button);
		button.setLayoutData(data);				
		button.addSelectionListener(listener);		
		return button;
	}
	
	public void updateTarget(RevisionGraphEditor graphEditor) {
		if (this.graphEditor != graphEditor) {			
			this.graphEditor = graphEditor;			
			this.validate();
			
			//reset search data
			this.searchOptions = null;
			this.searchResult = null;
		}
	}
	
	public void changeGraphModel() {						
		//reset search data
		this.searchOptions = null;
		this.searchResult = null;
	}
	
	protected void findNext() {
		this.find(true);		
	}
	
	protected void findPrevious() {
		this.find(false);
	}
	
	protected void find(boolean goNext) {			
		this.doFind();	
		
		if (goNext) {
			if (this.searchResult.hasNext()) {
				RevisionEditPart editPart = this.searchResult.next();
				this.showResult(editPart);
			} else {
				this.showNoResults();
			}
		} else {
			if (this.searchResult.hasPrevious()) {
				RevisionEditPart editPart = this.searchResult.previous();
				this.showResult(editPart);
			} else {
				this.showNoResults();
			}
		}			
	}

	protected void doFind() {
		SearchOptions newOptions = this.getSearchOptions();		
		if (!newOptions.equals(this.searchOptions)) {			
			this.searchOptions = newOptions;
			
			//save find history: TODO currently if something is empty then it's not saved
			this.pathHistory.addLine(this.pathCombo.getText());							
			this.revisionHistory.addLine(this.revisionCombo.getText());
			
			RevisionRootNode rootNode = (RevisionRootNode) this.graphEditor.getModel(); 
			RevisionNode[] nodes = rootNode.search(newOptions);
			
			Map editPartRegistry = this.graphEditor.getViewer().getEditPartRegistry();			
			List<RevisionEditPart> result = new ArrayList<RevisionEditPart>();
			for (RevisionNode node : nodes) {
				RevisionEditPart editPart = (RevisionEditPart) editPartRegistry.get(node);
				if (editPart != null) {
					result.add(editPart);
				}
			}			
			
			//sort nodes by location on graph starting from left bottom
			Collections.sort(result, new Comparator<RevisionEditPart>() {
				public int compare(RevisionEditPart r1, RevisionEditPart r2) {
					org.eclipse.draw2d.geometry.Rectangle b1 = r1.getFigure().getBounds();
					org.eclipse.draw2d.geometry.Rectangle b2 = r2.getFigure().getBounds();										
					
					int y1 = b1.y + b1.height;
					int y2 = b2.y + b2.height;
					
					int x1 = b1.x;
					int x2 = b2.x;
					
					int result = y1 < y2 ? 1 : (y1 > y2 ? -1 : 0);										
					if (result == 0) {
						result = x1 < x2 ? -1 : (x1 > x2 ? 1 : 0);
					}
					return result;
				}
			});								
			this.searchResult = new NodesIterator<RevisionEditPart>(result);
		}
	}
	
	protected void showNoResults() {
		this.statusLabel.setText(SVNRevisionGraphMessages.FindRevisionNodeDialog_NotFound);
	}

	protected void showResult(RevisionEditPart editPart) {
		//select node
		this.graphEditor.getViewer().select(editPart);

		//move
		IFigure figure = editPart.getFigure();
		GraphScalableRootEditPart rootEditPart = (GraphScalableRootEditPart) this.graphEditor.getViewer().getRootEditPart();
		Viewport viewport = (Viewport) rootEditPart.getFigure();												
		org.eclipse.draw2d.geometry.Point viewportLocation = viewport.getViewLocation();
		
		org.eclipse.draw2d.geometry.Point point = figure.getBounds().getLocation();	
		figure.translateToAbsolute(point);
		
		org.eclipse.draw2d.geometry.Point newViewportLocation = viewportLocation.getTranslated(point);
		//center somehow result point ?
		viewport.setViewLocation(newViewportLocation);
		
		//reset status
		this.statusLabel.setText(""); //$NON-NLS-1$
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		this.closeImpl();
		return super.close();
	}
	
	protected void closeImpl() {
		this.retainSize();
	}
	
	protected void retainSize() {
		Point size = this.parent.getSize();		
		//TODO Need to remember location 		
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();		
		SVNTeamPreferences.setDialogInt(store, this.getDialogID() + ".width", size.x); //$NON-NLS-1$
		SVNTeamPreferences.setDialogInt(store, this.getDialogID() + ".height", size.y); //$NON-NLS-1$
	}
	
	protected void validate() {
		boolean enabled = this.graphEditor != null;
				
		if (enabled) {
			String path = this.getComboValue(this.pathCombo);
			String strRev = this.getComboValue(this.revisionCombo);				
			enabled = path.length() > 0 || strRev.length() > 0;						
		}
		
		if (enabled) {
			String strRev = this.getComboValue(this.revisionCombo);
			if (strRev.length() > 0) {
				try {
					Long.parseLong(strRev);				
				} catch (NumberFormatException e) {
					enabled = false;
				}		
			}		
		}
		
		if (this.nextButton != null && this.previousButton != null) {
			this.nextButton.setEnabled(enabled);
			this.previousButton.setEnabled(enabled);	
		}
	}
	
	protected SearchOptions getSearchOptions() {
		long revision = -1;		
		String strRev = this.getComboValue(this.revisionCombo);
		if (strRev.length() > 0) {
			revision = Long.parseLong(strRev);	
		}			
								
		String path = this.getComboValue(this.pathCombo);
		path = path.trim();
		if (path.length() == 0) {
			path = null;
		}
		
		SearchOptions options = new SearchOptions(revision, path);
		return options;
	}
	
	protected String getComboValue(Combo combo) {
		String str = combo.getText();
		str = str.trim();
		return str;
	}
	
	/**
	 * Iterate through revision nodes forward and backward.
	 * We can't use ListIterator because calls in order to next and
	 * previous return the same element
	 */
	protected static class NodesIterator<T> {
		
		protected List<T> list;
		protected int position;
		
		public NodesIterator(List<T> list) {
			this(list, -1);
		}
		
		public NodesIterator(List<T> list, int position) {
			if (position < -1 || position > list.size()) {
				throw new IllegalArgumentException("Position: " + position); //$NON-NLS-1$
			}			
			this.list = list;
			this.position = position;
		}
		
		public boolean hasNext() {
			return this.position + 1 < this.list.size();							
		}
		
		public T next() {
			try {
				return this.list.get(++ this.position);
			} catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}
		
		public boolean hasPrevious() {
			return this.position -1 >= 0;
		}
		
		public T previous() {
			try {
				return this.list.get(-- this.position);
			} catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}
	}

}
