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
				int index = table.indexOf(item);
				handle(index, item);
			}
		}
	}

	abstract protected void handle(int columnIndex, TableItem item);

}
