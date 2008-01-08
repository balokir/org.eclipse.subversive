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

package org.eclipse.team.svn.ui.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;

/**
 * Universal table viewer sorter
 * 
 * @author Alexander Gurov
 */
public class TableViewerSorter extends ViewerSorter implements SelectionListener {
    public static final int INVALID_COLUMN = -1;
    public static final int ORDER_NONE = 0;
    public static final int ORDER_NORMAL = 1;
    public static final int ORDER_REVERSED = 2;
    
    public static boolean CASE_INSENSITIVE = true;
    
    protected List order;
    protected TableViewer basedOn;
    protected IColumnComparator comparator;
    protected IPropertyChangeListener configurationListener;
    protected long enabledColumns;
    
    public interface IColumnComparator {
        public int compare(Object row1, Object row2, int column);
    }
    
    public TableViewerSorter(TableViewer basedOn, IColumnComparator comparator) {
        super();
        this.basedOn = basedOn;
        this.comparator = comparator;
        this.order = new ArrayList();
        this.enabledColumns = -1L;
        final IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
        TableViewerSorter.CASE_INSENSITIVE = SVNTeamPreferences.getTableSortingBoolean(store, SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME);
        this.configurationListener = new IPropertyChangeListener() {
        	public void propertyChange(PropertyChangeEvent event) {
        		if (event.getProperty().equals(SVNTeamPreferences.fullTableSortingName(SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME))) {
        			TableViewerSorter.CASE_INSENSITIVE = SVNTeamPreferences.getTableSortingBoolean(store, SVNTeamPreferences.TABLE_SORTING_CASE_INSENSITIVE_NAME);
        			if (!TableViewerSorter.this.basedOn.getControl().isDisposed()) {
        				TableViewerSorter.this.basedOn.refresh();
        			}
        			else {
        				store.removePropertyChangeListener(this);
        			}
        		}
        	}
        };
        store.addPropertyChangeListener(this.configurationListener);
    }
    
    public long getEnabledColumns() {
    	return this.enabledColumns;
    }
    
    public void setEnabledColumns(long enabledColumns) {
    	this.enabledColumns = enabledColumns;
    }
    
    public static int compare(String first, String second) {
    	return TableViewerSorter.CASE_INSENSITIVE ? first.compareToIgnoreCase(second) : first.compareTo(second);
    }
    
    public void setDefaultColumn(int defaultColumn) {
        if (defaultColumn > TableViewerSorter.INVALID_COLUMN) {
        	if ((this.enabledColumns & (1 << defaultColumn)) == 0) {
        		for (int i = 0; i < 64; i++) {
        			if ((this.enabledColumns & (1 << i)) != 0) {
                		defaultColumn = i;
                		break;
        			}
        		}
        	}
        	this.showOrdering(this.basedOn.getTable().getColumn(defaultColumn), this.changeOrdering(defaultColumn, false));
        }
    }
    
    public TableViewer getBasedOn() {
        return this.basedOn;
    }
    
    public IColumnComparator getColumnComparator() {
        return this.comparator;
    }
    
    public void widgetSelected(SelectionEvent e) {
    	TableColumn widget = (TableColumn)e.widget;
	    int column = this.basedOn.getTable().indexOf(widget);
	    if ((this.enabledColumns & 1 << column) != 0) {
		    this.showOrdering(widget, this.changeOrdering(column, false));
		    this.basedOn.refresh();
	    }
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    	TableColumn widget = (TableColumn)e.widget;
	    int column = this.basedOn.getTable().indexOf(widget);
	    if ((this.enabledColumns & 1 << column) != 0) {
		    this.showOrdering(widget, this.changeOrdering(column, true));
		    this.basedOn.refresh();
	    }
    }
    
    public int compare(Viewer viewer, Object e1, Object e2) {
        for (Iterator it = this.order.iterator(); it.hasNext(); ) {
            OrderPair pair = (OrderPair)it.next();
            int cmpResult = this.comparator.compare(e1, e2, pair.column);
            if (cmpResult != 0) {
                return pair.order == TableViewerSorter.ORDER_NORMAL ? cmpResult : (cmpResult * -1);
            }
        }
        return 0;
    }
    
    public int getOrdering(int column) {
        OrderPair info = this.findOrderPair(column);
        return info == null ? TableViewerSorter.ORDER_NONE : info.order;
    }
    
    public void setOrdering(int column, int order) {
        OrderPair info = this.findOrderPair(column);
        if (info != null) {
            if (order == TableViewerSorter.ORDER_NONE) {
                this.order.remove(info);
            }
            else {
                info.order = order;
            }
        }
        else if (order != TableViewerSorter.ORDER_NONE) {
            this.order.add(new OrderPair(column, order));
        }
    }
    
    public OrderChangePair changeOrdering(int column, boolean drop) {
        OrderPair info = this.findOrderPair(column);
        
        this.order.clear();
        
        int oldOrder = info == null ? TableViewerSorter.ORDER_NONE : info.order;
        if (info == null) {
            info = new OrderPair(column, TableViewerSorter.ORDER_NORMAL);
        }
        else if (drop) {
            return new OrderChangePair(oldOrder, TableViewerSorter.ORDER_NONE);
        }
        else if (info.order == TableViewerSorter.ORDER_NORMAL) {
            info.order = TableViewerSorter.ORDER_REVERSED;
        }
        else if (info.order == TableViewerSorter.ORDER_REVERSED) {
        	info.order = TableViewerSorter.ORDER_NORMAL;
        }
        
    	this.order.add(info);
    	
        return new OrderChangePair(oldOrder, info.order);
    }
    
    public void sort(final Viewer viewer, Object []elements) {
    	Arrays.sort(elements, new Comparator() {
            public int compare(Object a, Object b) {
                return TableViewerSorter.this.compare(viewer, a, b);
            }
        });
    }
    
    protected void showOrdering(TableColumn column, OrderChangePair orderChange) {
    	int direction = orderChange.newOrder == TableViewerSorter.ORDER_NONE ? SWT.NONE : (orderChange.newOrder == TableViewerSorter.ORDER_NORMAL ? SWT.UP : SWT.DOWN);
    	this.basedOn.getTable().setSortColumn(column);
    	this.basedOn.getTable().setSortDirection(direction);
    }
    
    protected OrderPair findOrderPair(int column) {
        for (Iterator it = this.order.iterator(); it.hasNext(); ) {
            OrderPair pair = (OrderPair)it.next();
            if (pair.column == column) {
                return pair;
            }
        }
        return null;
    }
    
    public class OrderChangePair {
        public final int oldOrder;
        public final int newOrder;
        
        public OrderChangePair(int oldOrder, int newOrder) {
            this.oldOrder = oldOrder;
            this.newOrder = newOrder;
        }
    }
    
    protected class OrderPair {
        public int column;
        public int order;
        
        public OrderPair(int column, int order) {
            this.column = column;
            this.order = order;
        }
        
        public String toString() {
            return String.valueOf(this.column) + "=" + this.order;
        }
    }

}
