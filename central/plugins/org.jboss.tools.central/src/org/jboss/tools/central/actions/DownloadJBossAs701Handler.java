package org.jboss.tools.central.actions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.central.DownloadRuntime;
import org.jboss.tools.central.JBossCentralActivator;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.filetransfer.ECFExamplesTransport;
import org.jboss.tools.runtime.core.JBossRuntimeLocator;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.IRuntimeDetector;
import org.jboss.tools.runtime.core.model.RuntimePath;
import org.jboss.tools.runtime.core.model.ServerDefinition;
import org.jboss.tools.runtime.ui.RuntimeUIActivator;

public class DownloadJBossAs701Handler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final DownloadRuntime runtime = JBossCentralActivator.getDefault().getDownloadJBossRuntimes().get(getId());
		if (runtime == null) {
			JBossCentralActivator.log("Invalid runtime");
		}
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setMessage("Select installation directory.");
		//dialog.setFilterPath("");
		final String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			Job job = new Job("Download '" + runtime.getName() + "' ...") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					return downloadAndInstall(runtime, selectedDirectory, monitor);
				}
			};
			job.setUser(true);
			job.schedule();
		}
		
		return null;
	}

	protected IStatus downloadAndInstall(DownloadRuntime runtime,
			String selectedDirectory, IProgressMonitor monitor) {
		FileInputStream in = null;
		OutputStream out = null;
		try {
			File file = File.createTempFile("JBossRuntime", "tmp");
			file.deleteOnExit();
			out = new BufferedOutputStream(
					new FileOutputStream(file));
			URL url = new URL(runtime.getUrl());
			String name = url.getPath();
			int slashIdx = name.lastIndexOf('/');
			if (slashIdx >= 0)
				name = name.substring(slashIdx + 1);
			
			IStatus result = ECFExamplesTransport.getInstance().download(name,
					url.toExternalForm(), out, monitor);
			out.flush();
			out.close();
			File directory = new File(selectedDirectory);
			directory.mkdirs();
			if (!directory.isDirectory()) {
				JBossCentralActivator.getDefault().getLog().log(result);
				// FIXME 
				return Status.CANCEL_STATUS;
			}
			ProjectExamplesActivator.extractZipFile(file, directory, monitor);
			if (!result.isOK()) {
				JBossCentralActivator.getDefault().getLog().log(result);
				// FIXME 
				return Status.CANCEL_STATUS;
			}
			createRuntimes(selectedDirectory, monitor);
		} catch (IOException e) {
			JBossCentralActivator.log(e);
			// FIXME 
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return Status.OK_STATUS;
	}

	private String getId() {
		return "org.jboss.tools.central.as.701";
	}
	
	private static void createRuntimes(String directory, IProgressMonitor monitor) {
		JBossRuntimeLocator locator = new JBossRuntimeLocator();
		Set<RuntimePath> runtimePaths = RuntimeUIActivator.getDefault()
				.getRuntimePaths();
		RuntimePath newPath = new RuntimePath(directory);
		runtimePaths.add(newPath);
		for (RuntimePath runtimePath : runtimePaths) {
			List<ServerDefinition> serverDefinitions = locator
					.searchForRuntimes(runtimePath.getPath(),
							monitor);
			runtimePath.getServerDefinitions().clear();
			for (ServerDefinition serverDefinition : serverDefinitions) {
				serverDefinition.setRuntimePath(runtimePath);
			}
			runtimePath.getServerDefinitions().addAll(serverDefinitions);
		}
		List<ServerDefinition> serverDefinitions = RuntimeUIActivator
				.getDefault().getServerDefinitions();
		Set<IRuntimeDetector> detectors = RuntimeCoreActivator
				.getRuntimeDetectors();
		for (IRuntimeDetector detector : detectors) {
			if (detector.isEnabled()) {
				detector.initializeRuntimes(serverDefinitions);
			}
		}
	}

}
