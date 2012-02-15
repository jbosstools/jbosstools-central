package org.jboss.tools.project.examples;

import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.project.examples.dialog.DownloadRuntimeViewerDialog;
import org.jboss.tools.runtime.ui.IDownloadRuntimes;

public class DownloadRuntimes implements IDownloadRuntimes {

	@Override
	public void execute(Shell shell) {
		DownloadRuntimeViewerDialog dialog = new DownloadRuntimeViewerDialog(shell);
		dialog.open();
	}

}
