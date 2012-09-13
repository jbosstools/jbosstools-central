// Copyright 2012 Jeeeyul Lee, Seoul, Korea
// https://github.com/jeeeyul/pde-tools
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
// EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
// LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
// GPL, GNU General Public License, V2 or later, http://www.gnu.org/licenses/gpl.html
// AL, Apache License, V2.0 or later, http://www.apache.org/licenses
// BSD, BSD License, http://www.opensource.org/licenses/bsd-license.php
// MIT, MIT License, http://www.opensource.org/licenses/MIT
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.
package net.jeeeyul.eclipse.themes.rendering;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import net.jeeeyul.eclipse.themes.CSSClasses;
import net.jeeeyul.eclipse.themes.UpdateCTabFolderClassesJob;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ChromeTabRendering extends HackedCTabRendering {

	private CTabFolder tabFolder;
	private int lastKnownTabHeight = -1;

	private static Set<ChromeTabRendering> INSTANCES = new HashSet<ChromeTabRendering>();

	public static Set<ChromeTabRendering> getInstances() {
		return INSTANCES;
	}

	private UpdateCTabFolderClassesJob updateTags;

	private boolean showShineyShadow;

	@Inject
	public ChromeTabRendering(CTabFolder tabFolder) {
		super(tabFolder);
		this.tabFolder = tabFolder;
		updateTags = new UpdateCTabFolderClassesJob(tabFolder);

		tabFolder.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateEmptyClassIfNeeded();
			}
		});

		tabFolder.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				INSTANCES.remove(this);
			}
		});

		INSTANCES.add(this);
	}

	public void applyChromeThemePreference() {
	}

	@Override
	protected void dispose() {
		super.dispose();
	}

	@Override
	protected void draw(int part, int state, Rectangle bounds, GC gc) {
		if (parent.isDisposed() || gc.isDisposed()) {
			return;
		}

		updateEmptyClassIfNeeded();

		if (part == PART_BODY && !isPreviewingTab()) {
			/*
			 * 7: Editor area - Minimize / maximize look brocken
			 * https://github.com/jeeeyul/eclipse-themes/issues/issue/7
			 * 
			 * Calculated tab height of empty tab seens to cause this problems.
			 */
			if (tabFolder.getItemCount() == 0) {
				if (lastKnownTabHeight < 0) {
					lastKnownTabHeight = tabFolder.getFont().getFontData()[0]
							.getHeight() + 19;
				}
				tabFolder.setTabHeight(lastKnownTabHeight);
			} else {
				tabFolder.setTabHeight(-1);
				lastKnownTabHeight = tabFolder.getTabHeight();
			}
		}

		super.draw(part, state, bounds, gc);
	}

	private boolean isPreviewingTab() {
		CSSClasses tags = CSSClasses.getStyleClasses(tabFolder);
		return tags.contains("chrome-tabfolder-preview");
	}

	public boolean isShowShineyShadow() {
		return showShineyShadow;
	}

	public void setShowShineyShadow(boolean showShineyShadow) {
		this.showShineyShadow = showShineyShadow;
	}

	@Override
	protected boolean showUnselectedTabItemShadow() {
		return showShineyShadow;
	}

	private void updateEmptyClassIfNeeded() {
		CSSClasses tags = CSSClasses.getStyleClasses(tabFolder);

		boolean haveToSetEmpty = tabFolder.getItemCount() == 0;

		if (haveToSetEmpty && !tags.contains("empty")) {
			updateTags.schedule();
			updateItems();
		} else if (!haveToSetEmpty && !tags.contains("nonEmpty")) {
			updateTags.schedule();
			updateItems();
		}
	}

}