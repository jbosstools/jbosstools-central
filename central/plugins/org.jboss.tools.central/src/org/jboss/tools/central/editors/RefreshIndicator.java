package org.jboss.tools.central.editors;

import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jboss.tools.central.JBossCentralActivator;
import org.osgi.framework.Bundle;
public class RefreshIndicator extends Canvas {
	protected Image[] images;
	protected Image image;

	protected Thread busyThread;
	protected boolean stop;

	/**
	 * BusyWidget constructor comment.
	 * @param parent org.eclipse.swt.widgets.Composite
	 * @param style int
	 * @throws IOException 
	 */
	public RefreshIndicator(Composite parent, String imagePath, int style) throws IOException {
		super(parent, style);
	
		images = getImages(parent, imagePath);
	
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				onPaint(event);
			}
		});
	
		image = images[0];
	}
	
	private Image[] getImages(Composite parent, String imagePath) throws IOException {
		ImageLoader loader = new ImageLoader();
		Bundle bundle = JBossCentralActivator.getDefault().getBundle();
		URL entry = bundle.getEntry(imagePath);
		Image[] images = null;
		InputStream is = null;
		try {
			is = entry.openStream();
			ImageData[] datas = loader.load(is);
			images = new Image[datas.length];
			int i = 0;
			for (ImageData data:datas) {
				images[i++] = new Image(parent.getDisplay(), data);
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return images;
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(25, 25);
	}
	
	/**
	 * Creates a thread to animate the image.
	 */
	protected synchronized void createBusyThread() {
		if (busyThread != null)
			return;
	
		stop = false;
		busyThread = new Thread() {
			protected int count;
			public void run() {
				try {
					final int maxCount = images.length;
					count = 1;
					while (!stop) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								if (!stop) {
									if (count < maxCount)
										setImage(images[count]);
									count++;
									if (count > maxCount - 1)
										count = 1;
								}
							}
						});
						try {
							sleep(125);
						} catch (Exception e) {
							// ignore
						}
					}
					if (busyThread == null)
						Display.getDefault().syncExec(new Thread() {
							public void run() {
								setImage(images[0]);
							}
						});
				} catch (Exception e) {
					JBossCentralActivator.log(e);
				}
			}
		};
	
		busyThread.setPriority(Thread.NORM_PRIORITY + 2);
		busyThread.setDaemon(true);
		busyThread.start();
	}
	
	public void dispose() {
		stop = true;
		busyThread = null;
		if (images != null) {
			for (Image image:images) {
				image.dispose();
			}
		}
		super.dispose();
	}
	
	/**
	 * Return the image or <code>null</code>.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Returns true if it is currently busy.
	 *
	 * @return boolean
	 */
	public boolean isBusy() {
		return (busyThread != null);
	}

	/* 
	 * Process the paint event
	 */
	protected void onPaint(PaintEvent event) {
		Rectangle rect = getClientArea();
		if (rect.width == 0 || rect.height == 0)
			return;
	
		GC gc = event.gc;
		if (image != null)
			gc.drawImage(image, 2, 2);
	}

	/**
	 * Sets the indicators busy count up (true) or down (false) one.
	 *
	 * @param busy boolean
	 */
	public synchronized void setBusy(boolean busy) {
		if (busy) {
			if (busyThread == null)
				createBusyThread();
		} else {
			if (busyThread != null) {
				stop = true;
				busyThread = null;
			}
		}
	}

	/**
	 * Set the image.
	 * The value <code>null</code> clears it.
	 */
	public void setImage(Image image) {
		if (image != this.image && !isDisposed()) {
			this.image = image;
			redraw();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		setBusy(visible);
		super.setVisible(visible);
	}
}