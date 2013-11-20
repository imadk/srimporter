package com.dmagik.sheetrackimporter;

/***
 * SheetRack Importer - can generate sheet files for SheetRack from PDF files
 * Copyright (C) 2011 D-MagiK Inc. (support@sheetpad.com)
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */


import java.awt.Dimension;
import java.awt.Toolkit;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;




public class SheetViewer {

	private Display display;
	
	
	public SheetViewer(final String title, final String imageToViewPath, Display display) {

		
		this.display = display;
		
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Shell viewer_shell = new Shell(display, SWT.CLOSE | SWT.MIN);

		Image iconimage = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter",
				"/icons/alt_window_16.gif").createImage();
		viewer_shell.setImage(iconimage);
		viewer_shell.setText(title);

		
		final Image SheetImage = new Image(display,
				imageToViewPath);

		final Image imageToView = new Image(Display.getDefault(), SheetImage
				.getImageData().scaledTo(
						(int) (SheetImage.getBounds().width * 0.5f),
						(int) (SheetImage.getBounds().height * 0.5f)));

		viewer_shell.setSize(imageToView.getBounds().width,
				imageToView.getBounds().height+25);
		viewer_shell.setLocation(800, 175);
		viewer_shell.setAlpha(255);

		viewer_shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {

				event.doit = false;
			}
		});

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;

		viewer_shell.setLayout(gridLayout);

		final Composite clientArea = new Composite(viewer_shell, SWT.NONE);
		GridData spec = new GridData();
		spec.horizontalAlignment = spec.FILL;
		spec.grabExcessHorizontalSpace = true;
		spec.verticalAlignment = spec.FILL;
		spec.grabExcessVerticalSpace = true;
		clientArea.setLayoutData(spec);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		clientArea.setLayout(layout);

		clientArea.setBackgroundImage(imageToView);

		viewer_shell.setAlpha(255); // so that we display the image correctly

		Menu m = new Menu(viewer_shell, SWT.BAR | SWT.RIGHT_TO_LEFT);

	

		Image importPhotoExitimg = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter",
				"/icons/alt_window_16.gif").createImage();

		final Image imgExit = new Image(display,
				importPhotoExitimg.getImageData());



		final Listener lis1 = new Listener() {
			Point origin;

			@Override
			public void handleEvent(Event e) {

				switch (e.type) {
				case SWT.MouseDown:
					origin = new Point(e.x, e.y);
					break;
				case SWT.MouseUp:
					origin = null;
					break;
				case SWT.Paint:
					if (origin != null) {
						// Point p = viewer_shell.getDisplay().map(viewer_shell,
						// null, e.x, e.y);
						e.gc.setBackground(viewer_shell.getDisplay()
								.getSystemColor(SWT.COLOR_BLACK));
						e.gc.setAlpha(200);
						e.gc.setAntialias(SWT.ON);
						e.gc.drawPoint(origin.x, origin.y);
					}
					break;
				}

			}
		};


		// TO EXIT from closing minimized window

		viewer_shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = true;
				
				
		

			}
		});

		viewer_shell.setMenuBar(m);

		// Adding ability to move viewer_shell around
		Listener l = new Listener() {
			Point origin;

			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.MouseDown:
					origin = new Point(e.x, e.y);
					break;
				case SWT.MouseUp:
					origin = null;
					break;
				case SWT.MouseDoubleClick:
			//		origin = null;
			//		viewer_shell.setLocation(0, 0);
					break;
				case SWT.Paint:
					if (origin != null) {
						// Point p = viewer_shell.getDisplay().map(viewer_shell,
						// null, e.x, e.y);
						e.gc.setBackground(viewer_shell.getDisplay()
								.getSystemColor(SWT.COLOR_BLACK));
						e.gc.setAlpha(200);
						e.gc.setAntialias(SWT.ON);
						e.gc.drawPoint(origin.x, origin.y);
					}
					break;

				}
			}
		};

		// Adding the listeners

		clientArea.addListener(SWT.MouseDown, l);
		clientArea.addListener(SWT.MouseUp, l);
		clientArea.addListener(SWT.MouseMove, l);
		clientArea.addListener(SWT.MouseDoubleClick, l);
	


		viewer_shell.open();



	}
	
	

}
