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
package net.jeeeyul.eclipse.themes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.progress.UIJob;

/**
 * Add "empty" class(CSS) into {@link CTabFolder} when there is no item.
 */
@SuppressWarnings("restriction")
public class UpdateCTabFolderClassesJob extends UIJob {

	private CTabFolder folder;

	public UpdateCTabFolderClassesJob(CTabFolder folder) {
		super("Update CTabFolder CSS");
		this.folder = folder;
		this.setSystem(true);
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor arg0) {
		if (folder == null || folder.isDisposed()) {
			return Status.OK_STATUS;
		}

		CSSClasses classes = CSSClasses.getStyleClasses(folder);
		boolean haveToSetEmpty = folder.getItemCount() == 0;

		if (haveToSetEmpty) {
			classes.add("empty");
			classes.remove("nonEmpty");
		} else {
			classes.remove("empty");
			classes.add("nonEmpty");
		}

		CSSClasses.setStyleClasses(folder, classes);
		getThemeEngine().applyStyles(folder, true);

		return Status.OK_STATUS;
	}

	private IThemeEngine getThemeEngine() {
		return (IThemeEngine) folder.getDisplay().getData(
				"org.eclipse.e4.ui.css.swt.theme");
	}

	@Override
	public boolean shouldSchedule() {
		return folder != null && !folder.isDisposed();
	}

	@Override
	public boolean shouldRun() {
		return shouldSchedule();
	}
}