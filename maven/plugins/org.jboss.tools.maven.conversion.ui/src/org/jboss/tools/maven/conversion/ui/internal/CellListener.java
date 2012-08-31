/*************************************************************************************
 * Copyright (c) 2012 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.conversion.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public abstract class CellListener implements Listener {

	protected Table table;
	
	public CellListener(Table table) {
		Assert.isNotNull(table);
		this.table = table;
	}
	
	@Override
	public void handleEvent(Event event) {
		Point pt = new Point(event.x, event.y);
		TableItem item = table.getItem(pt);
		if (item == null)
			return;
		
		int columnCount = table.getColumnCount();
		
		for (int i = 0; i < columnCount; i++) {
			Rectangle rect = item.getBounds(i);
			if (rect.contains(pt)) {
				handle(i, item);
			}
		}
	}

	abstract protected void handle(int columnIndex, TableItem item);

}
