/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.central.internal.discovery.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.central.internal.ImageUtil;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager.ProxyWizardManagerListener;
import org.jboss.tools.central.internal.discovery.wizards.ProxyWizardManager.UpdateEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProxyWizardManagerTest extends AbstractProxyWizardDiscoveryTest implements ProxyWizardManagerListener {

	private static final long MAX_WAIT_TIME = 60*1000;
	private ProxyWizardManager pwm;
	private NullProgressMonitor monitor;
	private List<ProxyWizard> remoteWizards;
	private File cacheFolder;
	
	@Before
	public void setUp() throws IOException {
		monitor = new NullProgressMonitor();
		pwm = ProxyWizardManager.INSTANCE;

		cacheFolder = createRootCacheFolder();
		IPath rootCacheFolderPath = new Path(cacheFolder.getAbsolutePath());
		pwm.setRootCacheFolderPath(rootCacheFolderPath);
		
		pwm.registerListener(this);
	}
	

	@After
	public void tearDown() throws IOException {
		pwm.setRootCacheFolderPath(null);
		pwm.unRegisterListener(this);
		monitor = null;
		FileUtils.deleteDirectory(cacheFolder);
		cacheFolder = null;
	}
	
	private File createRootCacheFolder() throws IOException {
		cacheFolder = new File("target/cache/");
		FileUtils.deleteDirectory(cacheFolder);
		
		FileUtils.copyDirectory(new File("test-resources/cache"), new File(cacheFolder, "123"));
		return cacheFolder;
	}


	@Test
	public void testLoadProxyWizardsFromRemote() throws Exception {

		  int port = startServer();
		  
		  assertTrue("platform is not runing", Platform.isRunning());
		
		  assertEquals(1, cacheFolder.list().length);

		  createRemoteResources(port);
		  
		  pwm.setDiscoveryUrl("http://localhost:"+port+"/directory.xml");
			
		  List<ProxyWizard> wizards = pwm.getProxyWizards(true, monitor);
		  assertEquals("Cache was not loaded", 7, wizards.size());		
		  waitForDownload(MAX_WAIT_TIME);
		  assertNotNull("Remote wizards were not broadcast", remoteWizards);
		  assertEquals("Remote wizards were not loaded", 6, remoteWizards.size());			
		  
		  //Check we created a new cache folder
		  assertEquals(2, cacheFolder.list().length);
		  
		  //Cache has been used for the following request
		  wizards = pwm.getProxyWizards(false, monitor);
		  assertEquals("Cache not updated", 6, wizards.size());			

		  //Check new requests delete old cache 
		  pwm.getProxyWizards(true, monitor);
		  waitForDownload(MAX_WAIT_TIME);
		  pwm.getProxyWizards(true, monitor);
		  waitForDownload(MAX_WAIT_TIME);
		  String[] folderNames = cacheFolder.list(); 
		  assertEquals(2, folderNames.length);
		  for (String name : folderNames){
			  assertNotEquals("123", name);
		  }
	}
	
	private void waitForDownload(long limit) {
	  long start = System.currentTimeMillis();
      while(true) {
          Job job = getProxyWizardUpdateJob();
          if(job == null) {
            return;
          }
          boolean timeout = (System.currentTimeMillis() - start ) > limit;
          assertFalse("Timeout while waiting for completion of job: " + job, timeout);
          job.wakeUp();
          delay(100);
        } 
      }

	public static void delay(long waitTimeMillis) {
		Display display = Display.getCurrent();
		if(PlatformUI.isWorkbenchRunning() && display!= null) {
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.update();
		} else { // Otherwise, perform a simple sleep.
			try {
				Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}
	
	private Job getProxyWizardUpdateJob() {
      Job[] jobs = Job.getJobManager().find(null);
      for(int i = 0; i < jobs.length; i++ ) {
        if(jobs[i] instanceof ProxyWizardUpdateJob) {
          return jobs[i];
        }
      }		
      return null;
	}


	@Test
	public void testLoadProxyWizardsFromCache() throws Exception {
		
		List<ProxyWizard> wizards = pwm.getProxyWizards(false, monitor);
		assertNotNull("Not wizards could be loaded", wizards);
		assertFalse("Not wizards were found", wizards.isEmpty());
		assertEquals("Cache was not loaded", 7, wizards.size());
		
		for (ProxyWizard p : wizards) {
			Image img = getImage(p.getIconUrl());
			try {
				assertNotNull("Image "+p.getIconUrl() +" can't be loaded", img);
			} finally {
				if (img != null) img.dispose();
			}
		}
	}
	
	@Test
	public void testLoadProxyWizardsFromBundle() throws Exception {
		FileUtils.deleteDirectory(cacheFolder);
		List<ProxyWizard> wizards = pwm.getProxyWizards(false, monitor);
		assertNotNull("Not wizards could be loaded", wizards);
		assertNotEquals(7, wizards.size());
		for (ProxyWizard p : wizards) {
			Image img = getImage(p.getIconUrl());
			try {
				assertNotNull("Image "+p.getIconUrl() +" can't be loaded", img);
			} finally {
				if (img != null) img.dispose();
			}
		}
	}

	public static Display getDisplay() {
	      Display display = Display.getCurrent();
	      //may be null if outside the UI thread
	      if (display == null)
	         display = Display.getDefault();
	      return display;		
	   }

	private Image getImage(URL iconUrl) {
		return ImageUtil.createImageFromUrl(getDisplay(), iconUrl);
	}


	@Override
	public void onProxyWizardUpdate(UpdateEvent event) throws CoreException {
		remoteWizards = event.getProxyWizards();
	}

}
