package com.dmagik.sheetrackimporter;

/***
 *  SheetRack Importer - can generate sheet files for SheetRack from PDF files
 *  Copyright (C) 2011 D-MagiK Inc. (support@sheetpad.com)
 *   
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *                                            
 */

import java.awt.AlphaComposite;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Text;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//import com.lowagie.text.Font;

public class Overview {

	// Needed private vars
	private TrayItem trayItem;
	private Image trayImage;
	private Display display;
	private Composite parent;
	public static Composite cmp_SheetViewer;
	public static Composite cmp_ComposerViewer;
	public static Composite cmp_PreviewViewer;
	public static CLabel clbl_PreviewImageHelp;
	private int previewHeight;
	private int composerHeight;
	private int composerWidth;
	private int previewWidth;
	public static int pageNum = 0;
	public static int totalPages = 9999;
	public static final ImagesEncapsulate selectedImagesEnc = new ImagesEncapsulate();
	public static ComboViewer viewer_genre;
	public static Text txt_Title;
	public static Text txt_Composer;
	public static String temporaryFolder = System.getProperty("java.io.tmpdir");
	public static int hh;
	public static int ww;
	public static CLabel clbl_Dummy55;
	public Listener picklis1;
	public static Composite cmp_SheetMetadata;
	public static Thread thread;
	// public static int progIncrease;
	public static ProgressBar progressBar;
	public static CLabel progressLabel;
	public static Composite clientArea;
	public static CLabel clbl_PreviewImage;
	public static CLabel clbl_SheetTitle;
	public static CLabel clbl_SheetComposer;
	public static CLabel clbl_Genre;
	public static CLabel clbl_ComposerImage;
	public static Button btn_Save;
	public static Button btn_Left;
	public static Button btn_Right;

	public static boolean DisableAll;

	@Inject
	public Overview(@Named(IServiceConstants.ACTIVE_SHELL) final Shell shell,
			final IPresentationEngine engine, final IEclipseContext context,
			MApplication application, Composite composite) {

		java.awt.Toolkit.getDefaultToolkit();

		// Screen res Detection Test:
		if (screenResDetect()) {

			MessageDialog
					.openError(
							shell,
							"Screen Resolution Error",
							"We have detected that your screen resolution is too low for the proper functioning of SheetRack Importer. \n\nPlease configure your desktop screen to have a resolution of at least 1024x768. \nThe program will now shut down. \n\n\nFor any questions please contact technical support. ");

			if (shell != null && !shell.isDisposed()) {
				shell.dispose();
			}

			engine.stop();
			System.exit(0); // shut down immeditaely to avoid subsequent errors

		}

		/*
		 * // Setup logging URL confURL =
		 * getBundle().getEntry("log4j.properties"); PropertyConfigurator
		 * .configure(FileLocator.toFileURL(confURL).getFile());
		 * log.info("Logging using log4j and configuration " +
		 * FileLocator.toFileURL(confURL).getFile());
		 * hookPluginLoggers(context); // You need to add this method to hook
		 * other // plugins, described later...
		 * 
		 * 
		 * private static org.apache.log4j.Logger log = org.apache.log4j.Logger
		 * .getLogger(MyClassInAnyPlugin.class);
		 */

		// Formatting the Main shell
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		shell.setLocation(shell.getDisplay().getBounds().width / 2 - 400, shell
				.getDisplay().getBounds().height / 2 - 300);

		shell.setMaximized(false);

		parent = composite;

		// init selectedImagesEnc
		Image defImage0 = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter",
				"/images/EmptyComposerImage.png").createImage();
		Image defImage1 = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter", "/images/emptypreview.png")
				.createImage();

		selectedImagesEnc.selectedImagePath = "N-A";
		selectedImagesEnc.newComposerImage = new Image(shell.getDisplay(),
				defImage0.getImageData());
		selectedImagesEnc.newPreviewImage = new Image(shell.getDisplay(),
				defImage1.getImageData());
		selectedImagesEnc.botrightX = 0;
		selectedImagesEnc.botrightY = 0;
		selectedImagesEnc.topleftX = 0;
		selectedImagesEnc.topleftY = 0;

		// Make parts uncloseable
		EModelService uielementService = context.get(EModelService.class);
		MPart partO = (MPart) uielementService
				.find("overviewPart", application);

		partO.getTags().add("NoClose");
		partO.getTags().add(IPresentationEngine.NO_MOVE);

		partO.setVisible(true);

		shell.setAlpha(240);
		trayImage = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter", "/icons/alt_window_32.gif")
				.createImage();

		shell.setImage(trayImage);

		// //////////////////////////////////////////////////////////
		// needed here for sheetViewer later

		parent.setLayout(new FillLayout());

		cmp_SheetViewer = new Composite(parent, SWT.NONE);// H_SCROLL |
															// SWT.V_SCROLL);
		cmp_ComposerViewer = new Composite(parent, SWT.NONE);
		cmp_PreviewViewer = new Composite(parent, SWT.NONE);

		if (cmp_SheetViewer != null || !cmp_SheetViewer.isDisposed()) {
			cmp_SheetViewer.dispose();
		}

		if (cmp_ComposerViewer != null || !cmp_ComposerViewer.isDisposed()) {
			cmp_ComposerViewer.dispose();
		}

		if (cmp_PreviewViewer != null || !cmp_PreviewViewer.isDisposed()) {
			cmp_PreviewViewer.dispose();
		}

		// Creating the composite which will contain
		// the sheet pages

		cmp_SheetViewer = new Composite(parent, SWT.NONE);// H_SCROLL |
															// SWT.V_SCROLL);

		clientArea = new Composite(cmp_SheetViewer, SWT.NONE);

		hh = 540;// 576;
		ww = 405;// 432;
		// pageNum = 1;
		// totalPages = 3;
		final RowData rowData184 = new RowData();
		rowData184.width = ww;
		rowData184.height = hh;
		clientArea.setLayoutData(rowData184);
		clientArea.setSize(ww, hh);

		if (new File(temporaryFolder + "\\sheetrack-temp" + pageNum + ".jpg")
				.exists()) {
			try {

				BufferedImage sampleSheetImageData = ImageIO
						.read(new File(temporaryFolder + "\\sheetrack-temp"
								+ pageNum + ".jpg"));

				int type = sampleSheetImageData.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
						: sampleSheetImageData.getType();
				BufferedImage resizedImage = new BufferedImage(ww, hh, type);
				Graphics2D g = resizedImage.createGraphics();
				g.drawImage(sampleSheetImageData, 0, 0, ww, hh, null);

				g.setComposite(AlphaComposite.Src);

				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.dispose();
				// System.err.println(sampleSheetImageData.getColorModel());

				// add some blurring
				// resizedImage = blurIt(resizedImage);

				if (resizedImage != null) {
					clientArea.setBackgroundImage(new Image(shell.getDisplay(),
							convertToSWT(resizedImage)));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// make it clickable
		final Listener picklis2 = new Listener() {

			@Override
			public void handleEvent(Event ev) {

				clientArea.removeListener(SWT.MouseDown, this);

				clbl_PreviewImageHelp
						.setText("Now please click on bottow right corner of the preview...");

				Point origin2 = new Point(ev.x, ev.y);

				selectedImagesEnc.botrightX = origin2.x;
				selectedImagesEnc.botrightY = origin2.y;

				/*
				 * System.out.println("---PICK2 Top LeftX: " +
				 * selectedImagesEnc.topleftX);
				 * System.out.println("PICK2 Top LeftY: " +
				 * selectedImagesEnc.topleftY); System.out
				 * .println("PICK2 Bot RightX: " + selectedImagesEnc.botrightX);
				 * System.out .println("PICK2 Bot RightY: " +
				 * selectedImagesEnc.botrightY);
				 */

				clientArea.removeListener(SWT.MouseDown, this);
				clientArea.addListener(SWT.MouseDown, picklis1);

			}
		};

		picklis1 = new Listener() {

			private float tx;
			private float ty;
			private float bx;
			private float by;

			@Override
			public void handleEvent(Event e) {

				clientArea.removeListener(SWT.MouseDown, this);

				Point origin = new Point(e.x, e.y);

				selectedImagesEnc.topleftX = origin.x;
				selectedImagesEnc.topleftY = origin.y;

				/*
				 * System.err.println("---PICK1 Top LeftX: " +
				 * selectedImagesEnc.topleftX);
				 * System.err.println("PICK1 Top LeftY: " +
				 * selectedImagesEnc.topleftY); System.err
				 * .println("PICK1 Bot RightX: " + selectedImagesEnc.botrightX);
				 * System.err .println("PICK1 Bot RightY: " +
				 * selectedImagesEnc.botrightY);
				 */

				// swap values due to interwining action between listeners:
				tx = 0.0f;
				ty = 0.0f;
				bx = 0.0f;
				by = 0.0f;

				tx = selectedImagesEnc.botrightX;
				ty = selectedImagesEnc.botrightY;
				bx = selectedImagesEnc.topleftX;
				by = selectedImagesEnc.topleftY;

				clbl_PreviewImageHelp
						.setText("Click on top left corner of the preview on the left image");

				// replace preview image with the one selected

				int deltay = (int) (by - ty);
				int deltax = (int) (bx - tx);

				if (deltax < 0) {
					deltax = -deltax;
					float swapx;
					swapx = tx;
					tx = bx;
					bx = swapx;

				}
				if (deltay < 0) {
					deltay = -deltay;
					float swapy;
					swapy = ty;
					ty = by;
					by = swapy;

				}

				if (deltay < 30)
					deltay = 30;
				if (deltax < 20)
					deltax = 20;

				// ////////////////////
				try {

					BufferedImage sampleSheetImageData = ImageIO.read(new File(
							temporaryFolder + "\\sheetrack-temp" + pageNum
									+ ".jpg"));

					int type = sampleSheetImageData.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
							: sampleSheetImageData.getType();
					BufferedImage resizedImage = new BufferedImage(ww, hh, type);
					Graphics2D g = resizedImage.createGraphics();
					g.drawImage(sampleSheetImageData, 0, 0, ww, hh, null);
					g.setComposite(AlphaComposite.Src);

					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					g.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

					g.dispose();

					// System.err.println(sampleSheetImageData.getColorModel());

					/*
					 * if (resizedImage != null) {
					 * clientArea.setBackgroundImage(new Image(shell
					 * .getDisplay(), convertToSWT(resizedImage))); }
					 */

					/*
					 * Image newCroppedImage = new Image(null, clientArea
					 * .getBackgroundImage().getBounds().width - (int) tx,
					 * deltay);
					 */

					type = resizedImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
							: resizedImage.getType();

					BufferedImage newCroppedImage = new BufferedImage(
							resizedImage.getWidth() - (int) tx, deltay, type);

					Graphics2D g1 = newCroppedImage.createGraphics();
					g1.drawImage(resizedImage.getSubimage((int) tx, (int) ty,
							resizedImage.getWidth() - (int) tx, deltay), 0, 0,
							resizedImage.getWidth() - (int) tx, deltay, null);

					g1.setComposite(AlphaComposite.Src);

					g1.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					g1.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);
					g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g1.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					// / selectedImagesEnc.newPreviewImage = new Image(
					// / shell.getDisplay(), convertToSWT(newCroppedImage));
					g1.dispose();

					previewHeight = 70;
					previewWidth = newCroppedImage.getWidth();

					type = newCroppedImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
							: newCroppedImage.getType();
					BufferedImage newPreviewImageDisp = new BufferedImage(
							(newCroppedImage.getWidth() * 70)
									/ newCroppedImage.getHeight(),
							previewHeight, type);

					Graphics2D g2 = newPreviewImageDisp.createGraphics();

					g2.drawImage(
							newCroppedImage,
							0,
							0,
							(newCroppedImage.getWidth() * 70)
									/ newCroppedImage.getHeight(),
							previewHeight, null);

					g2.setComposite(AlphaComposite.Src);

					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					g2.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

					g2.dispose();

					final RowData rowData98 = new RowData();
					rowData98.width = 275;
					rowData98.height = 70;
					cmp_PreviewViewer.setLayoutData(rowData98);

					cmp_PreviewViewer.setSize(java.lang.Math.min(
							(newPreviewImageDisp.getWidth() * 70)
									/ (newPreviewImageDisp.getHeight()), 275),
							70);

					// add some blurring

					// newPreviewImageDisp = blurIt(newPreviewImageDisp);

					cmp_PreviewViewer.setBackgroundImage(new Image(shell
							.getDisplay(), convertToSWT(newPreviewImageDisp)));

					selectedImagesEnc.newPreviewImage = cmp_PreviewViewer
							.getBackgroundImage();

					clientArea.removeListener(SWT.MouseDown, this);
					clientArea.addListener(SWT.MouseDown, picklis2);

				} catch (IOException ee) {
					ee.printStackTrace();
				}
				// ///////////////////

			}
		};

		clientArea.addListener(SWT.MouseDown, picklis1);

		Listener crosslis = new Listener() {

			@Override
			public void handleEvent(Event event) {

				Cursor cursor = new Cursor(display, SWT.CURSOR_CROSS);
				clientArea.setCursor(cursor);

			}
		};

		clientArea.addListener(SWT.MouseMove, crosslis);

		// Dummy label
		clbl_Dummy55 = new CLabel(cmp_SheetViewer, SWT.NONE);
		RowData rowData_320 = new RowData();
		rowData_320.width = 440;
		clbl_Dummy55.setLayoutData(rowData_320);
		clbl_Dummy55.setText("Page: " + " / ");
		clbl_Dummy55.setFont(new org.eclipse.swt.graphics.Font(display, "", 12,
				0));

		clbl_Dummy55.setForeground(new org.eclipse.swt.graphics.Color(display,
				255, 255, 255));

		// Left turn button
		final RowLayout rowLayout32 = new RowLayout();
		rowLayout32.fill = true;
		cmp_SheetViewer.setLayout(rowLayout32);

		btn_Left = new Button(cmp_SheetViewer, SWT.PUSH);
		RowData rowData_206 = new RowData();
		rowData_206.width = 50;
		btn_Left.setLayoutData(rowData_206);

		Image butImage4 = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter", "/images/left.png")
				.createImage();
		Image image4 = new Image(shell.getDisplay(), butImage4.getImageData());
		btn_Left.setImage(image4);
		btn_Left.setText(" ");
		// btn_Left.setAlignment(SWT.RIGHT);

		// Adding action to this button.
		btn_Left.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {

				if (pageNum > 1) {
					pageNum--;
					clbl_Dummy55.setText("Page: " + pageNum + " / "
							+ totalPages);
				}

				try {

					BufferedImage sampleSheetImageData = ImageIO.read(new File(
							temporaryFolder + "\\sheetrack-temp" + pageNum
									+ ".jpg"));

					int type = sampleSheetImageData.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
							: sampleSheetImageData.getType();
					BufferedImage resizedImage = new BufferedImage(ww, hh, type);
					Graphics2D g = resizedImage.createGraphics();
					g.drawImage(sampleSheetImageData, 0, 0, ww, hh, null);

					g.setComposite(AlphaComposite.Src);

					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					g.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

					g.dispose();
					// System.err.println(sampleSheetImageData.getColorModel());
					// add some blurring
					// resizedImage = blurIt(resizedImage);

					if (resizedImage != null) {
						clientArea.setBackgroundImage(new Image(shell
								.getDisplay(), convertToSWT(resizedImage)));
					}
				} catch (IOException ee) {
					ee.printStackTrace();
				}

			}
		});

		// Right turn button
		final RowLayout rowLayout2 = new RowLayout();
		rowLayout2.fill = true;
		cmp_SheetViewer.setLayout(rowLayout2);

		btn_Right = new Button(cmp_SheetViewer, SWT.PUSH);
		RowData rowData_205 = new RowData();
		rowData_205.width = 50;
		btn_Right.setLayoutData(rowData_205);

		// Adding action to this button.
		btn_Right.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {

				if (pageNum < totalPages) {
					pageNum++;
					clbl_Dummy55.setText("Page: " + pageNum + " / "
							+ totalPages);
				}

				try {

					BufferedImage sampleSheetImageData = ImageIO.read(new File(
							temporaryFolder + "\\sheetrack-temp" + pageNum
									+ ".jpg"));

					int type = sampleSheetImageData.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
							: sampleSheetImageData.getType();
					BufferedImage resizedImage = new BufferedImage(ww, hh, type);
					Graphics2D g = resizedImage.createGraphics();
					g.drawImage(sampleSheetImageData, 0, 0, ww, hh, null);

					g.setComposite(AlphaComposite.Src);

					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					g.setRenderingHint(RenderingHints.KEY_RENDERING,
							RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					// System.err.println(sampleSheetImageData.getColorModel());
					g.dispose();

					// add some blurring
					// resizedImage = blurIt(resizedImage);

					if (resizedImage != null) {
						clientArea.setBackgroundImage(new Image(shell
								.getDisplay(), convertToSWT(resizedImage)));
					}
				} catch (IOException ee) {
					ee.printStackTrace();
				}

			}
		});

		Image butImage3 = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter", "/images/right.png")
				.createImage();
		Image image3 = new Image(shell.getDisplay(), butImage3.getImageData());
		btn_Right.setImage(image3);
		btn_Right.setText(" ");
		// btn_Right.setAlignment(SWT.RIGHT);

		// ////////////////////////////////////////////////////////
		// Creating the composite
		cmp_SheetMetadata = new Composite(parent, SWT.NONE);
		final RowLayout rowLayout = new RowLayout();
		rowLayout.fill = true;
		cmp_SheetMetadata.setLayout(rowLayout);

		for (int i = 1; i < 2; i++) {
			// Dummy label
			CLabel clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			RowData rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));
		}

		// Label for the heading
		clbl_SheetTitle = new CLabel(cmp_SheetMetadata, SWT.NONE);
		final RowData rowData = new RowData();
		rowData.width = 120;
		clbl_SheetTitle.setLayoutData(rowData);
		clbl_SheetTitle.setText("&Title: *");
		clbl_SheetTitle.setFont(new org.eclipse.swt.graphics.Font(display, "",
				11, 1));

		txt_Title = new Text(cmp_SheetMetadata, SWT.BORDER);
		// txt_Title.setFont(new Font(display, "", 28, 1));
		final RowData rowData_2 = new RowData();
		rowData_2.width = 360;
		txt_Title.setLayoutData(rowData_2);

		// add a focus listener
		FocusListener focusListener = new FocusListener() {
			public void focusGained(FocusEvent e) {
				Text t = (Text) e.widget;
				t.selectAll();
			}

			public void focusLost(FocusEvent e) {
				Text t = (Text) e.widget;
				if (t.getSelectionCount() > 0) {
					t.clearSelection();
				}
			}
		};

		// add a selection listener
		SelectionListener selectListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				Text t = (Text) e.widget;
				t.selectAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				Text t = (Text) e.widget;
				t.selectAll();
			}

		};

		// add a selection listener
		Listener mLis = new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				Text t = (Text) arg0.widget;
				t.selectAll();

			}
		};

		txt_Title.addFocusListener(focusListener);
		txt_Title.addSelectionListener(selectListener);
		txt_Title.addListener(SWT.MouseDown, mLis);

		CLabel clbl_Dummy;
		RowData rowData_20;
		for (int i = 1; i < 2; i++) {
			// Dummy label
			clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));
		}
		// Label
		clbl_SheetComposer = new CLabel(cmp_SheetMetadata, SWT.NONE);
		final RowData rowData_1 = new RowData();
		rowData_1.width = 120;
		clbl_SheetComposer.setLayoutData(rowData_1);
		clbl_SheetComposer.setText("&Composer *: ");
		clbl_SheetComposer.setVisible(true);
		clbl_SheetComposer.setFont(new org.eclipse.swt.graphics.Font(display,
				"", 11, 1));

		txt_Composer = new Text(cmp_SheetMetadata, SWT.BORDER);
		// txt_Composer.setFont(new Font(display, "", 28, 1));
		final RowData rowData_3 = new RowData();
		rowData_3.width = 360;
		txt_Composer.setLayoutData(rowData_3);

		// add a focus listener
		FocusListener focusListener2 = new FocusListener() {
			public void focusGained(FocusEvent e) {
				Text t = (Text) e.widget;
				t.selectAll();
			}

			public void focusLost(FocusEvent e) {
				Text t = (Text) e.widget;
				if (t.getSelectionCount() > 0) {
					t.clearSelection();
				}
			}
		};

		// add a selection listener
		SelectionListener selectListener2 = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				Text t = (Text) e.widget;
				t.selectAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				Text t = (Text) e.widget;
				t.selectAll();
			}

		};
		txt_Composer.addFocusListener(focusListener2);
		txt_Composer.addSelectionListener(selectListener2);
		txt_Composer.addListener(SWT.MouseDown, mLis);

		for (int i = 1; i < 2; i++) {
			// Dummy label
			clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));
		}

		// Label for the genre
		clbl_Genre = new CLabel(cmp_SheetMetadata, SWT.NONE);
		RowData rowData_22 = new RowData();
		rowData_22.width = 120;
		clbl_Genre.setLayoutData(rowData_22);
		clbl_Genre.setText("&Genre *");
		clbl_Genre
				.setFont(new org.eclipse.swt.graphics.Font(display, "", 11, 1));

		// Choice for the gender
		viewer_genre = new ComboViewer(cmp_SheetMetadata, SWT.READ_ONLY);
		viewer_genre.setContentProvider(new ArrayContentProvider());
		viewer_genre.setInput(new String[] { "Blues", "Classical", "Jazz",
				"Pop", "Rock" });
		viewer_genre
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						event.getSelection();

					}

				});

		viewer_genre.setSelection(new StructuredSelection("Classical"));

		// Composer Image
		for (int i = 1; i < 4; i++) {
			// Dummy label
			clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));
		}

		// Label for the heading
		clbl_ComposerImage = new CLabel(cmp_SheetMetadata, SWT.NONE);
		final RowData rowData77 = new RowData();
		rowData77.width = 300;
		clbl_ComposerImage.setLayoutData(rowData77);
		clbl_ComposerImage.setText("Composer Image (click to change)");
		clbl_ComposerImage.setFont(new org.eclipse.swt.graphics.Font(display,
				"", 11, 1));

		clbl_ComposerImage.setForeground(new org.eclipse.swt.graphics.Color(
				display, 255, 255, 255));

		cmp_ComposerViewer = new Composite(cmp_SheetMetadata, SWT.NONE);

		final RowData rowData18 = new RowData();
		rowData18.width = 68;
		rowData18.height = 68;
		cmp_ComposerViewer.setLayoutData(rowData18);

		// set default image
		Image defImage = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter",
				"/images/EmptyComposerImage.png").createImage();
		Image image = new Image(shell.getDisplay(), defImage.getImageData());
		cmp_ComposerViewer.setBackgroundImage(image);

		// make it clickable
		Listener weblis = new Listener() {

			@Override
			public void handleEvent(Event event) {

				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Please select Composer Image...");
				fd.setFilterPath("C:/");
				String[] filterExt = { "*.jpg", "*.png", "*.bmp", "*.gif",
						"*.tif", "*.*" };
				fd.setFilterExtensions(filterExt);
				String sel = fd.open();

				if (sel != null) {
					selectedImagesEnc.selectedImagePath = sel;

					// replace compsoer image with selected one and adjust size
					// accordingly
					ImageData selImage = new ImageData(
							selectedImagesEnc.selectedImagePath);
					composerHeight = 68;
					composerWidth = java.lang.Math.min((selImage.width * 68)
							/ (selImage.height), 68);
					selImage = selImage.scaledTo(composerWidth, composerHeight);
					selectedImagesEnc.newComposerImage = new Image(
							shell.getDisplay(), selImage);

					rowData18.width = composerWidth;
					rowData18.height = composerHeight;

					cmp_ComposerViewer.setSize(composerWidth, composerHeight);
					cmp_ComposerViewer
							.setBackgroundImage(selectedImagesEnc.newComposerImage);

				}

			}
		};

		Listener handlis = new Listener() {

			@Override
			public void handleEvent(Event event) {

				Cursor cursor = new Cursor(display, SWT.CURSOR_HAND);

				cmp_ComposerViewer.setCursor(cursor);

			}
		};

		cmp_ComposerViewer.addListener(SWT.MouseDown, weblis);
		cmp_ComposerViewer.addListener(SWT.MouseMove, handlis);

		// Preview Image
		for (int i = 1; i < 2; i++) {
			// Dummy label
			clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));

		}

		// Label for the heading
		clbl_PreviewImage = new CLabel(cmp_SheetMetadata, SWT.NONE);
		final RowData rowData78 = new RowData();
		rowData78.width = 200;
		clbl_PreviewImage.setLayoutData(rowData78);
		clbl_PreviewImage.setText("Preview Image");
		clbl_PreviewImage.setFont(new org.eclipse.swt.graphics.Font(display,
				"", 11, 1));

		clbl_PreviewImage.setForeground(new org.eclipse.swt.graphics.Color(
				display, 255, 255, 255));

		cmp_PreviewViewer = new Composite(cmp_SheetMetadata, SWT.NONE);

		previewHeight = 70;
		previewWidth = selectedImagesEnc.newPreviewImage.getImageData().width;

		ImageData newPreviewData = selectedImagesEnc.newPreviewImage
				.getImageData();
		Image newPreviewImageDisp = new Image(shell.getDisplay(),
				newPreviewData.scaledTo(previewWidth, previewHeight));

		final RowData rowData98 = new RowData();
		rowData98.width = 275;
		rowData98.height = 70;
		cmp_PreviewViewer.setLayoutData(rowData98);

		cmp_PreviewViewer.setSize(previewWidth, previewHeight);

		cmp_PreviewViewer.setBackgroundImage(newPreviewImageDisp);

		selectedImagesEnc.newPreviewImage = cmp_PreviewViewer
				.getBackgroundImage();

		for (int i = 1; i < 2; i++) {
			// Dummy label
			clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));
		}

		// Label for preview instruction
		clbl_PreviewImageHelp = new CLabel(cmp_SheetMetadata, SWT.NONE);
		final RowData rowData75 = new RowData();
		rowData75.width = 490;
		clbl_PreviewImageHelp.setLayoutData(rowData75);
		clbl_PreviewImageHelp
				.setText("Click on top left corner of the preview on the left image");
		clbl_PreviewImageHelp.setFont(new org.eclipse.swt.graphics.Font(
				display, "", 9, 0));
		clbl_PreviewImageHelp.setAlignment(SWT.RIGHT);

		clbl_PreviewImageHelp.setForeground(new org.eclipse.swt.graphics.Color(
				display, 255, 255, 255));

		for (int i = 1; i < 2; i++) {
			// Dummy label
			clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));
		}

		// Dummy label
		clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
		rowData_20 = new RowData();
		rowData_20.width = 500;
		clbl_Dummy.setLayoutData(rowData_20);
		clbl_Dummy.setText("");
		clbl_Dummy
				.setFont(new org.eclipse.swt.graphics.Font(display, "", 12, 1));

		// save output button
		btn_Save = new Button(cmp_SheetMetadata, SWT.PUSH);

		Image butImage = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter", "/images/saveimg.png")
				.createImage();
		Image image2 = new Image(shell.getDisplay(), butImage.getImageData());
		btn_Save.setImage(image2);
		btn_Save.setText("&Save Output");
		btn_Save.setAlignment(SWT.RIGHT);

		// Adding action to this button.
		btn_Save.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {

				// Validate empty fields:
				if (!textHasContent(txt_Title.getText())

						|| !textHasContent(txt_Composer.getText())

						|| ("" + viewer_genre.getSelection())
								.equals("<empty selection>")
						|| viewer_genre.getSelection() == null

				) {

					MessageDialog.openError(shell, "Required Fields",
							"One or more required (*) fields were not filled");

				} else {

					String[] m_vFiles = null;

					// Save Output .sheet file
					try {

						m_vFiles = SheetPadHelper.GetFiles();

						// System.err.println(m_vFiles.length);
						if (GenerateMetadata(m_vFiles, shell)) {
							// nothing

						}
					} catch (IOException e1) {

						e1.printStackTrace();
					} catch (InterruptedException e1) {

						e1.printStackTrace();
					}

					// //////////////////////////////////////////////

					// //////////////////////////////////////////////
					// Open Temp Folder
					if (Desktop.isDesktopSupported()) {

						if (Desktop.getDesktop() != null) {

							try {

								Desktop.getDesktop().open(
										new File(temporaryFolder + "Sheets"));

							} catch (IOException ex) {
								MessageDialog
										.openError(
												shell,
												"Error opening Folder",
												"Oops! We could not open the Folder where your sheet output resides . Please contact technical support");
								ex.printStackTrace();
							}
						}
					}

					MessageDialog
							.openInformation(
									shell,
									"Last Step!!",
									"Your sheet has been saved in the folder that just opened. Please drag and drop it to SheetRack app on your iTunes. \n\nIf in difficulty, try the Help from Menu->About or contact technical support.");

				}

			}
		});

		progressLabel = new CLabel(cmp_SheetMetadata, SWT.NONE);
		RowData rowData_202 = new RowData();
		rowData_202.width = 220;
		progressLabel.setLayoutData(rowData_202);
		progressLabel.setText("To start press on 'Choose a PDF'");
		progressLabel.setFont(new org.eclipse.swt.graphics.Font(display, "",
				10, 1));

		progressLabel.setForeground(new org.eclipse.swt.graphics.Color(display,
				200, 200, 200));

		progressLabel.setVisible(true);

		// init state For UI elements
		txt_Composer.setVisible(false);
		txt_Title.setVisible(false);
		viewer_genre.getCombo().setVisible(false);
		cmp_ComposerViewer.setEnabled(false);
		btn_Left.setVisible(false);
		btn_Right.setVisible(false);
		btn_Save.setVisible(false);
		clbl_Dummy55.setVisible(true);
		cmp_ComposerViewer.setVisible(false);
		clbl_SheetComposer.setVisible(false);
		clbl_SheetTitle.setVisible(false);
		cmp_PreviewViewer.setVisible(false);
		clbl_Genre.setVisible(false);
		clbl_PreviewImageHelp.setVisible(false);
		clbl_PreviewImage.setVisible(false);
		clbl_ComposerImage.setVisible(false);
		cmp_SheetViewer.setVisible(false);

		// COPYRIGHT MESSAGE
		for (int i = 1; i < 1; i++) {
			// Dummy label
			clbl_Dummy = new CLabel(cmp_SheetMetadata, SWT.NONE);
			rowData_20 = new RowData();
			rowData_20.width = 800;
			clbl_Dummy.setLayoutData(rowData_20);
			clbl_Dummy.setText("");
			clbl_Dummy.setFont(new org.eclipse.swt.graphics.Font(display, "",
					12, 1));
		}

		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		// Adding progress bar:
		// cmp_SheetMetadata.setLayout(new GridLayout());

		progressBar = new ProgressBar(cmp_SheetMetadata, SWT.SMOOTH);

		// final RowData rowData_644 = new RowData();
		// rowData_644.width = 670;
		// progressBar.setLayoutData(rowData_644);

		// progressBar.setLayoutData(cmp_SheetMetadata);
		progressBar.setBounds(0, 0, 500, 50);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setVisible(false);

		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		// Copyright label
		// Label for copyright info
		final CLabel clbl_Message = new CLabel(cmp_SheetMetadata, SWT.NONE);
		clbl_Message.setAlignment(SWT.LEFT);
		final RowData rowData_6 = new RowData();
		rowData_6.width = 670;
		clbl_Message.setLayoutData(rowData_6);
		String copyrightMsg = new String();
		if (Calendar.getInstance().get(Calendar.YEAR) == 2012) {

			copyrightMsg = "SheetRack Importer \u00A9" + " 2012"

			+ ", a product of D-magiK Inc. \u00AE (support@sheetpad.com) ";

		} else if (Calendar.getInstance().get(Calendar.YEAR) > 2012) {

			copyrightMsg = "SheetRack Importer \u00A9"
					+ " 2012-"
					+ Calendar.getInstance().get(Calendar.YEAR)
					+ ", a product of D-magiK Inc. \u00AE (support@sheetpad.com) ";
		}

		clbl_Message.setText(copyrightMsg);
		clbl_Message.setForeground(new org.eclipse.swt.graphics.Color(display,
				200, 200, 200));

		clbl_Message.setAlignment(SWT.RIGHT);

		// Setting up the tray minimize function
		trayItem = initTaskItem(shell);
		// Some OS might not support tray items
		if (trayItem != null) {
			minimizeBehavior(shell);
			// Create exit and about action on the icon
			hookPopupMenu(shell, engine);
		}

		// Handle also when user closes the app from the OS
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {

				if (MessageDialog.openQuestion(shell, "Exit Application?",
						"Do you really want to exit SheetRack Importer?")) {

					// -- uninitDatabase(shell);
					if (shell != null && !shell.isDisposed()) {
						shell.dispose();
					}
					event.doit = true;

				} else {

					event.doit = false;
				}

			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {

				try {
					SheetPadHelper.ClearFiles();
				} catch (IOException e1) {

					e1.printStackTrace();
				}

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("The end.");
			}
		});

	}

	private void minimizeBehavior(
			@Named(IServiceConstants.ACTIVE_SHELL) final Shell shell) {
		shell.addShellListener(new ShellAdapter() {
			// If the window is minimized hide the window
			public void shellIconified(ShellEvent e) {
				shell.setVisible(false);
			}
		});
		// If user double-clicks on the tray icons the application will be
		// visible again
		trayItem.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event event) {

				if (!shell.isVisible()) {
					shell.setMinimized(false);
					shell.setVisible(true);
					// shell.setFullScreen(true); //is making bugs in displaying
					// of menu
				}
			}
		});
	}

	// We hook up on menu entry which allows to close the application
	private void hookPopupMenu(
			@Named(IServiceConstants.ACTIVE_SHELL) final Shell shell,
			final IPresentationEngine engine) {
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				Menu menu = new Menu(shell, SWT.POP_UP);

				// Creates a new menu item that terminates the program
				// when selected
				MenuItem exit = new MenuItem(menu, SWT.NONE);
				exit.setText("E&xit SheetRack Importer");
				exit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {

						if (shell != null && !shell.isDisposed()) {
							shell.dispose();
						}

						engine.stop();
					}
				});
				// We need to make the menu visible
				menu.setVisible(true);
			}
		});
	}

	// This methods create the tray item and return a reference
	private TrayItem initTaskItem(Shell shell) {
		final Tray tray = shell.getDisplay().getSystemTray();
		TrayItem trayItem = new TrayItem(tray, SWT.NONE);
		trayImage = AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.dmagik.sheetrackimporter", "/icons/alt_window_16.gif")
				.createImage();
		trayItem.setImage(trayImage);
		trayItem.setToolTipText("Double-click to maximize SheetRack Importer");
		return trayItem;

	}

	public boolean screenResDetect() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		System.out.println("Screen resolution detected: " + screenSize.width);

		if (screenSize.width < 1024) {

			return true;

		}

		return false;

	}

	private boolean textHasContent(String aText) {
		return (aText != null) && (aText.trim().length() > 0);
	}

	@PreDestroy
	public void dipose() {

		while (!progressBar.isDisposed()) {
			if (!progressBar.getDisplay().readAndDispatch())
				progressBar.getDisplay().sleep();
		}

		progressBar.getDisplay().dispose();

		while (!parent.getDisplay().isDisposed()) {
			if (!parent.getDisplay().readAndDispatch())
				parent.getDisplay().sleep();
		}

		parent.dispose();

		if (trayItem != null) {
			trayItem.dispose();
		}

		if (trayImage != null) {
			trayImage.dispose();
		}
	}

	public static BufferedImage blurIt(BufferedImage imSrc) {

		// add some blurring
		float data[] = { 0.0625f * 1f, 0.125f * 1f, 0.0625f * 1f, 0.125f * 1f,
				0.25f * 1f, 0.125f * 1f, 0.0625f * 1f, 0.125f * 1f,
				0.0625f * 1f };
		Kernel kernel = new Kernel(3, 3, data);
		ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP,
				null);

		int type = imSrc.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : imSrc
				.getType();
		BufferedImage imDest = new BufferedImage((imSrc.getWidth()),
				imSrc.getHeight(), type);

		convolve.filter(imSrc, imDest);

		return imDest;

	}

	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
					.getColorModel();

			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());

			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);

			WritableRaster raster = bufferedImage.getRaster();

			int[] pixelArray = new int[4];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
					.getColorModel();

			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);

			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];

			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}

			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
			ComponentColorModel colorModel = (ComponentColorModel) bufferedImage
					.getColorModel();

			// ASSUMES: 3 BYTE BGR IMAGE TYPE

			PaletteData palette = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);

			// This is valid because we are using a 3-byte Data model with no
			// transparent pixels
			data.transparentPixel = -1;

			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[4];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		}

		return null;
	}

	private boolean GenerateMetadata(String[] vFiles, Shell shell)
			throws IOException, InterruptedException {
		if (vFiles != null && vFiles.length > 0) {
			com.dmagik.sheetrackimporter.Sheet finalSheet = new Sheet();
			finalSheet.m_strComposer = txt_Composer.getText().trim(); // item.m_strComposerName;
			finalSheet.m_strTitle = txt_Title.getText().trim();

			finalSheet.m_strGenre = "" + viewer_genre.getSelection(); // TODO -
																		// see
																		// what
																		// happens
																		// if an
																		// incorrect
																		// genre
																		// is
																		// specified.
			// remove brackets
			finalSheet.m_strGenre = finalSheet.m_strGenre.replaceAll("\\[", "");
			finalSheet.m_strGenre = finalSheet.m_strGenre.replaceAll("\\]", "");

			ArrayList<String> vTags = new ArrayList<String>();
			vTags.add("Title\t" + finalSheet.m_strTitle);
			vTags.add("Composer\t" + finalSheet.m_strComposer);
			vTags.add("Genre\t" + finalSheet.m_strGenre);
			vTags.add("Velocity\t100");

			Hashtable<String, ArrayList<Measure>> vMeasures = new Hashtable<String, ArrayList<Measure>>();

			String[] strError = new String[1];
			strError[0] = "No Errors";

			if (selectedImagesEnc.newPreviewImage != null) {

				// Save temp preview image
				Image tempPreview = selectedImagesEnc.newPreviewImage;
				ImageLoader imageLoader = new ImageLoader();
				imageLoader.data = new ImageData[] { tempPreview.getImageData() };
				imageLoader.save(temporaryFolder + "\\" + "tempPrev.jpg",
						SWT.IMAGE_JPEG);

			}

			// optional? check
			Thread.sleep(500);

			if (selectedImagesEnc.newComposerImage != null) {
				// Save the composer image
				Image tempCompsoer = selectedImagesEnc.newComposerImage;
				ImageLoader imageLoader2 = new ImageLoader();
				imageLoader2.data = new ImageData[] { tempCompsoer
						.getImageData() };
				imageLoader2.save(temporaryFolder + "\\" + "tempComposer.jpg",
						SWT.IMAGE_JPEG);

			}

			// optional? check
			Thread.sleep(500);

			Hashtable<Object, Object> vMetaData = new Hashtable<Object, Object>();
			vMetaData.put("ComposerImage", (Object) temporaryFolder + "\\"
					+ "tempComposer.jpg");

			vMetaData.put("Preview", temporaryFolder + "\\" + "tempPrev.jpg");

			// Create Sheets folder in temp directory where output will be saved
			if (!new File(temporaryFolder + "\\Sheets\\").exists()) {

				new File(temporaryFolder + "\\Sheets\\");
			}

			SheetSerializer
					.Generate(vFiles, vMetaData, vMeasures, vTags,
							(temporaryFolder + "\\Sheets\\"
									+ finalSheet.m_strTitle + ".sheet"),
							strError);

			System.err.println(strError[0]);

			/*Thread.sleep(1000);
			String[] vSheets = null;
			com.dmagik.sheetrackimporter.SheetSerializer.LoadSheet(
					temporaryFolder + "\\Sheets\\" + finalSheet.m_strTitle
							+ ".sheet", vSheets, vMeasures, vTags.toArray(),
					true);

			System.err
					.println(com.dmagik.sheetrackimporter.SheetSerializer.strLoad_strError);
					*/
			return true;
		}
		return false;

	}
}

class ImagesEncapsulate {

	public String selectedImagePath;
	public Image newComposerImage;
	public Image newPreviewImage;
	public float topleftX;
	public float topleftY;
	public float botrightX;
	public float botrightY;

}