package com.dmagik.sheetrackimporter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

import net.sf.ghost4j.Ghostscript;
import net.sf.ghost4j.GhostscriptException;
import net.sf.ghost4j.document.DocumentException;
import net.sf.ghost4j.document.PDFDocument;
import net.sf.ghost4j.renderer.Renderer;
import net.sf.ghost4j.renderer.RendererException;
import net.sf.ghost4j.renderer.SimpleRenderer;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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

/*
 * using System; using System.Collections.Generic; using System.Linq; using
 * System.Text; using PdfPageTools; using PdfToImage; using System.IO;
 */

public class SheetPadHelper {

	private static String temporaryFolder = System
			.getProperty("java.io.tmpdir");

	public static imagesEncap imagesE = new imagesEncap();
	private static int totalP = 0;

	public static void ClearFiles() throws IOException {
		String[] vFiles = GetFiles();

		for (String strFile : vFiles) {
			File tmpF = new File(strFile);
			tmpF.delete();

		}
	}

	public static String[] GetFiles() throws IOException {

		String tempSheetsFolder = temporaryFolder;
		// System.err.println(tempSheetsFolder);

		FileFilter fileFilter = new WildcardFileFilter("sheetrack-temp*.jpg");

		List<File> listOfF = new ArrayList<File>();
		File[] listOfSheets = new File(tempSheetsFolder).listFiles(fileFilter);

		for (int u = 0; u < listOfSheets.length; u++) {

			listOfF.add(listOfSheets[u]);
		}

		Collections.sort(listOfF,  new Comparator<File>() {
			public int compare(File f1, File f2) {
				
				String name1 = new String();
				String name2 = new String();
				
				
				name1 = f1.getName().split("temp")[1];
				name2 = f2.getName().split("temp")[1];
				
				
				name1 = name1.split("\\.")[0];
				name2 = name2.split("\\.")[0];
				
				
				
				int indx1 = Integer.parseInt(name1);
				int indx2 = Integer.parseInt(name2);

				return Long.valueOf(indx1).compareTo((long) indx2);
			}
		});
		
	

		String[] returnSheets = new String[listOfSheets.length];

		for (int i = 0; i < listOfSheets.length; i++) {

		//	System.err.println(listOfF.get(i).toString());
			
			returnSheets[i] = ((File) listOfF.toArray()[i]).toString();

		}

		return returnSheets;
	}

	// Returns the list of filenames

	// strFilename is filename with extension
	public static int GetSong(final String strFilename, final Shell shell)
			throws FileNotFoundException, IOException, InterruptedException {

		if (com.dmagik.sheetrackimporter.Overview.progressBar.isDisposed()) {

			final CLabel progressLabel = new CLabel(
					com.dmagik.sheetrackimporter.Overview.cmp_SheetMetadata,
					SWT.NONE);
			RowData rowData_202 = new RowData();
			rowData_202.width = 220;
			progressLabel.setLayoutData(rowData_202);
			progressLabel.getDisplay().asyncExec(new Runnable() {

				public void run() {

					if (progressLabel.isDisposed())
						return;
					progressLabel.setText("Preparing...");

				}
			});
			progressLabel.setFont(new org.eclipse.swt.graphics.Font(shell
					.getDisplay(), "", 10, 1));

			progressLabel.setForeground(new org.eclipse.swt.graphics.Color(
					shell.getDisplay(), 200, 200, 200));

			progressLabel.setVisible(true);

			com.dmagik.sheetrackimporter.Overview.progressLabel = progressLabel;

			com.dmagik.sheetrackimporter.Overview.progressLabel.getDisplay()
					.asyncExec(new Runnable() {

						public void run() {

							if (com.dmagik.sheetrackimporter.Overview.progressLabel
									.isDisposed())
								return;
							com.dmagik.sheetrackimporter.Overview.progressLabel
									.setText("Preparing...");

						}
					});

			final ProgressBar progressBar = new ProgressBar(
					com.dmagik.sheetrackimporter.Overview.cmp_SheetMetadata,
					SWT.SMOOTH);

			final RowData rowData_644 = new RowData();
			rowData_644.width = 670;
			progressBar.setLayoutData(rowData_644);
			// progressBar.setLayoutData(cmp_SheetMetadata);
			progressBar.setLayoutData(shell);
			progressBar.setBounds(0, 0, 300, 50);
			progressBar.setMinimum(0);
			progressBar.setMaximum(100);

			com.dmagik.sheetrackimporter.Overview.progressBar = progressBar;
		}

		org.eclipse.swt.graphics.Image defImage1 = AbstractUIPlugin
				.imageDescriptorFromPlugin("com.dmagik.sheetrackimporter",
						"/images/emptypreview.png").createImage();
		com.dmagik.sheetrackimporter.Overview.selectedImagesEnc.newPreviewImage = new org.eclipse.swt.graphics.Image(
				shell.getDisplay(), defImage1.getImageData());

		int previewHeight = 70;
		int previewWidth = com.dmagik.sheetrackimporter.Overview.selectedImagesEnc.newPreviewImage
				.getImageData().width;

		ImageData newPreviewData = com.dmagik.sheetrackimporter.Overview.selectedImagesEnc.newPreviewImage
				.getImageData();
		org.eclipse.swt.graphics.Image newPreviewImageDisp = new org.eclipse.swt.graphics.Image(
				shell.getDisplay(), newPreviewData.scaledTo(previewWidth,
						previewHeight));

		final RowData rowData98 = new RowData();
		rowData98.width = 275;
		rowData98.height = 70;
		com.dmagik.sheetrackimporter.Overview.cmp_PreviewViewer
				.setLayoutData(rowData98);

		com.dmagik.sheetrackimporter.Overview.cmp_PreviewViewer.setSize(
				previewWidth, previewHeight);

		com.dmagik.sheetrackimporter.Overview.cmp_PreviewViewer
				.setBackgroundImage(newPreviewImageDisp);

		com.dmagik.sheetrackimporter.Overview.selectedImagesEnc.newPreviewImage = com.dmagik.sheetrackimporter.Overview.cmp_PreviewViewer
				.getBackgroundImage();

		// reinit prog bar
		com.dmagik.sheetrackimporter.Overview.progressBar.setVisible(true);
		final ProgressBar progressBar = com.dmagik.sheetrackimporter.Overview.progressBar; // new

		com.dmagik.sheetrackimporter.Overview.progressLabel.getDisplay()
				.asyncExec(new Runnable() {

					public void run() {

						if (com.dmagik.sheetrackimporter.Overview.progressLabel
								.isDisposed())
							return;
						com.dmagik.sheetrackimporter.Overview.progressLabel
								.setText("Preparing...");

					}
				});
		com.dmagik.sheetrackimporter.Overview.progressLabel
				.setFont(new org.eclipse.swt.graphics.Font(shell.getDisplay(),
						"", 10, 1));

		com.dmagik.sheetrackimporter.Overview.progressLabel
				.setForeground(new org.eclipse.swt.graphics.Color(shell
						.getDisplay(), 200, 200, 200));

		com.dmagik.sheetrackimporter.Overview.progressLabel.setVisible(true);
		final CLabel progressLabel = com.dmagik.sheetrackimporter.Overview.progressLabel; // new
		// ProgressBar(
		// com.dmagik.sheetrackimporter.Overview.cmp_SheetMetadata,
		// SWT.SMOOTH);

		progressLabel.getDisplay().asyncExec(new Runnable() {

			public void run() {

				if (progressLabel.isDisposed())
					return;
				progressLabel.setText("Preparing...");

			}
		});

		progressLabel.setFont(new org.eclipse.swt.graphics.Font(shell
				.getDisplay(), "", 10, 1));

		progressLabel.setForeground(new org.eclipse.swt.graphics.Color(shell
				.getDisplay(), 200, 0, 0));
		progressLabel.setVisible(true);

		// Documents larger than 30 pages take too long and too much heap
		// space
		// my Solution: reduce this overhead by splitting document

		progressLabel.getDisplay().asyncExec(new Runnable() {

			public void run() {

				if (progressLabel.isDisposed())
					return;
				progressLabel.setText("Processing PDF sheet...");

			}
		});

		progressBar.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				String string = (int) ((progressBar.getSelection() * 1.0
						/ (progressBar.getMaximum() - progressBar.getMinimum()) * 100))
						+ "%";
				Point point = progressBar.getSize();

				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * string.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(shell.getDisplay().getSystemColor(
						SWT.COLOR_WHITE));
				e.gc.drawString(string, (point.x - width) / 2,
						(point.y - height) / 2, true);
			}
		});

		// disable other buttons until completion
		com.dmagik.sheetrackimporter.Overview.btn_Left.setEnabled(false);
		com.dmagik.sheetrackimporter.Overview.btn_Right.setEnabled(false);
		com.dmagik.sheetrackimporter.Overview.btn_Save.setEnabled(false);

		Thread progThread = new Thread() {
			private int progIncrease;
			private int imageH;
			private int imageW;
			private ImageWriter writer;
			private FileImageOutputStream output;

			public void run() {

				PDFDocument document = new PDFDocument();
				int lastP;

				String[] outF;

				try {
					document.load(new File(strFilename));
					lastP = document.getPageCount();
					totalP = lastP;

					outF = new String[1 + ((int) lastP / 10)]; // split
					// into
					// 10-page-docs

					int dotPos = strFilename.lastIndexOf(".");
					final String ext = strFilename.substring(dotPos);
					final String strPrefix = strFilename.substring(0, dotPos);
					List<Image> images = new ArrayList<Image>();

					imagesE.imagesE = images;

					SimpleRenderer renderer;

					progIncrease = 0;
					List<Image> subImages;
					int firstP = 1;
					int cnter = -1;

					for (int j = 0; j < outF.length; j++) {

						if ((lastP % 10) == 0) {

							if (j >= (outF.length - 1)) {

								break;

							}

						}

						renderer = new SimpleRenderer();
						renderer.setResolution(150); // 150 is closest to 400KB
						int tmpLastP;
						if (outF.length > 1) {
							tmpLastP = ((int) lastP / (outF.length - 1))
									* (j + 1)
									+ ((int) ((lastP / (outF.length - 1)) * 10) - ((int) (lastP / (outF.length - 1)) * 10));

						} else {
							tmpLastP = lastP;

						}
						outF[j] = strPrefix + "-tmp-" + j + ext;

						System.err.println(firstP + "-" + tmpLastP);
						final int dispFirst = firstP;
						final int dispLast = tmpLastP;
						progressLabel.getDisplay().asyncExec(new Runnable() {

							public void run() {

								if (progressLabel.isDisposed())
									return;
								progressLabel.setText("Processing pp."
										+ dispFirst
										+ "-"
										+ (dispLast > totalP ? totalP
												: dispLast) + "  /   " + totalP
										+ "...");

							}
						});

						/*
						 * try { Thread.sleep(500); } catch
						 * (InterruptedException e2) {
						 * 
						 * e2.printStackTrace(); }
						 */
						splitPdf(strFilename, outF[j], String.valueOf(firstP),
								String.valueOf(tmpLastP));

						/*
						 * try { Thread.sleep(500); } catch
						 * (InterruptedException e2) {
						 * 
						 * e2.printStackTrace(); }
						 */

						// System.err.println("loading");
						try {
							document.load(new File(outF[j]));
						} catch (FileNotFoundException e) {

							e.printStackTrace();
						} catch (IOException e) {

							e.printStackTrace();
						}
						// System.err.println("loading done");

						// System.err.println("rendering");
						subImages = null;
						System.gc();

						/*
						 * try { Thread.sleep(500); } catch
						 * (InterruptedException e2) {
						 * 
						 * e2.printStackTrace(); }
						 */

						try {
							subImages = renderer.render(document);

						} catch (IOException e) {

							e.printStackTrace();
						} catch (RendererException e) {

							e.printStackTrace();
						} catch (DocumentException e) {

							e.printStackTrace();
						}

						/*
						 * try { Thread.sleep(500); } catch
						 * (InterruptedException e2) {
						 * 
						 * e2.printStackTrace(); }
						 */

						// System.err.println("rendering done");
						// System.err.println("adding");
						imagesE.imagesE.addAll(subImages);
						subImages = null;
						System.gc();
						// System.err.println("adding done");
						// System.err.println("deleting");

						try {
							new File(outF[j]).delete();
							Thread.sleep(500);

						} catch (InterruptedException e2) {

							e2.printStackTrace();
						}

						// System.err.println("deleting done");

						Iterator<ImageWriter> iter = ImageIO
								.getImageWritersByFormatName("jpeg");
						writer = (ImageWriter) iter.next();

						while (!imagesE.imagesE.isEmpty()) {

							cnter++;
							progIncrease = (int) ((float) (cnter + 1)
									/ (totalP) * 100);

							// System.err.println(progIncrease);

							// System.out.println("extFirst time: "
							// + String.valueOf(cnter + 1));
							try {

								// instantiate an ImageWriteParam object with
								// default compression options
								ImageWriteParam iwp = writer
										.getDefaultWriteParam();
								iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
								iwp.setCompressionQuality(1); // an integer
																// between 0 and
																// 1
								// 1 specifies minimum compression and maximum
								// quality
								File file = new File(temporaryFolder
										+ "\\sheetrack-temp" + (cnter + 1)
										+ ".jpg");
								output = new FileImageOutputStream(file);
								writer.setOutput(output);
								IIOImage image = new IIOImage(
										(RenderedImage) imagesE.imagesE.get(0),
										null, null);
								writer.write(null, image, iwp);

								imageH = image.getRenderedImage().getHeight();
								imageW = image.getRenderedImage().getWidth();

								imagesE.imagesE.remove(0);
							} catch (IOException e) {

								e.printStackTrace();
							}
							// System.out.println("Step-->3");

							System.gc();

							// sleep between conversion and resizing --
							// optional
							try {
								Thread.sleep(50);
							} catch (InterruptedException e1) {

								e1.printStackTrace();
							}

							// System.err.println(imageH + "-" + imageW);

							if ((imageH == 1024 && imageW == 768)) { // no need
																		// to
																		// resize
																		// if
																		// correct
																		// size

							} else {

								BufferedImage original;
								try {

									// instantiate an ImageWriteParam object
									// with
									// default compression options
									ImageWriteParam iwp = writer
											.getDefaultWriteParam();

									iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
									iwp.setCompressionQuality(1); // an integer
																	// between 0
																	// and
																	// 1
									// 1 specifies minimum compression and
									// maximum
									// quality
									File file = new File(temporaryFolder
											+ "\\sheetrack-temp" + (cnter + 1)
											+ ".jpg");
									FileImageOutputStream output = new FileImageOutputStream(
											file);
									writer.setOutput(output);

									BufferedImage resizedImage = resizeJpeg(
											new File(temporaryFolder
													+ "\\sheetrack-temp"
													+ (cnter + 1) + ".jpg"),
											768, 1024, 1.0f);

									IIOImage image = new IIOImage(
											(RenderedImage) resizedImage, null,
											null);
									writer.write(null, image, iwp);

									output.close();
								} catch (IOException e) {

									e.printStackTrace();
								}
							}
							try {
								Thread.sleep(5);
							} catch (Throwable th) {
							}

							if (progressBar.isDisposed())
								return;
							progressBar.getDisplay().asyncExec(new Runnable() {

								public void run() {

									if (progressBar.isDisposed())
										return;
									progressBar.setSelection(progIncrease);

									if (progIncrease >= 100) {
										// /progressBar.dispose();
										progressLabel.setVisible(false);
										progressBar.setVisible(false);
										progressBar.setSelection(0);
									}

								}
							});

						}
						writer.dispose();

						firstP = tmpLastP + 1;

						imagesE.imagesE = null;
						imagesE = new imagesEncap();
						imagesE.imagesE = new ArrayList<Image>();
						renderer = null;
						System.gc();
					}

				} catch (FileNotFoundException e2) {

					e2.printStackTrace();
				} catch (IOException e2) {

					e2.printStackTrace();
				}

				catch (DocumentException e2) {

					e2.printStackTrace();
				}

				System.err.println("out of loop");
				writer.dispose();
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		progThread.start();

		// images = renderer.render(document); // <---- THIS IS
		// HEAP
		// MEMORY
		// CONSUMING IF PDFS
		// ARE TOO LARGE

		while (progressBar.isVisible()) {
			if (!progressBar.getDisplay().readAndDispatch())
				progressBar.getDisplay().sleep();
		}

		return totalP;
	}

	public static final String GS_INSTALL = "\nPlease download, install GPL Ghostscript from http://sourceforge.net/projects/ghostscript/files\nand/or set the appropriate environment variable.";

	public static void splitPdf(String inputPdfFile, String outputPdfFile,
			String firstPage, String lastPage) {
		// get Ghostscript instance
		Ghostscript gs = Ghostscript.getInstance();

		// prepare Ghostscript interpreter parameters
		// refer to Ghostscript documentation for parameter usage
		// gs -sDEVICE=pdfwrite -dNOPAUSE -dQUIET -dBATCH -dFirstPage=m
		// -dLastPage=n -sOutputFile=out.pdf in.pdf
		List<String> gsArgs = new ArrayList<String>();
		gsArgs.add("-gs");
		gsArgs.add("-dNOPAUSE");
		gsArgs.add("-dQUIET");
		gsArgs.add("-dBATCH");
		gsArgs.add("-sDEVICE=pdfwrite");

		if (!firstPage.trim().isEmpty()) {
			gsArgs.add("-dFirstPage=" + firstPage);
		}

		if (!lastPage.trim().isEmpty()) {
			gsArgs.add("-dLastPage=" + lastPage);
		}

		gsArgs.add("-sOutputFile=" + outputPdfFile);
		gsArgs.add(inputPdfFile);

		// execute and exit interpreter
		try {
			gs.initialize(gsArgs.toArray(new String[0]));
			gs.exit();
		} catch (GhostscriptException e) {
			System.err.println("ERROR: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (UnsatisfiedLinkError ule) {
			throw new RuntimeException(getMessage(ule.getMessage()));
		} catch (NoClassDefFoundError ncdfe) {
			throw new RuntimeException(getMessage(ncdfe.getMessage()));
		}
	}

	public static BufferedImage resizeJpeg(File originalFile, int newWidth,
			int newHeight, float quality) throws IOException {

		if (quality > 1) {
			throw new IllegalArgumentException(
					"Quality has to be between 0 and 1");
		}

		ImageIcon ii = new ImageIcon(originalFile.getCanonicalPath());
		Image i = ii.getImage();
		Image resizedImage = null;

		int iWidth = i.getWidth(null);
		int iHeight = i.getHeight(null);

		if (iWidth > iHeight) {
			resizedImage = i.getScaledInstance(newHeight, newWidth,
					Image.SCALE_SMOOTH);
		} else {
			resizedImage = i.getScaledInstance(newWidth, newHeight,
					Image.SCALE_SMOOTH);
		}

		// This code ensures that all the pixels in the image are loaded.
		Image temp = new ImageIcon(resizedImage).getImage();

		// Create the buffered image.
		BufferedImage bufferedImage = new BufferedImage(temp.getWidth(null),
				temp.getHeight(null), BufferedImage.TYPE_INT_RGB);

		// Copy image to buffered image.
		Graphics g = bufferedImage.createGraphics();

		// Clear background and paint the image.
		g.setColor(Color.white);
		g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
		g.drawImage(temp, 0, 0, null);
		g.dispose();

		// Soften.
		float softenFactor = 0.05f;
		float[] softenArray = { 0, softenFactor, 0, softenFactor,
				1 - (softenFactor * 4), softenFactor, 0, softenFactor, 0 };
		Kernel kernel = new Kernel(3, 3, softenArray);
		ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		bufferedImage = cOp.filter(bufferedImage, null);

		return bufferedImage;

		/*
		 * // Encodes image as a JPEG data stream JPEGImageEncoder encoder =
		 * JPEGCodec.createJPEGEncoder(out);
		 * 
		 * JPEGEncodeParam param = encoder
		 * .getDefaultJPEGEncodeParam(bufferedImage);
		 * 
		 * param.setQuality(quality, true);
		 * 
		 * encoder.setJPEGEncodeParam(param); encoder.encode(bufferedImage);
		 */
	}

	public static int getPdfPageCount(String inputPdfFile) {
		// get Ghostscript instance
		Ghostscript gs = Ghostscript.getInstance();

		// prepare Ghostscript interpreter parameters
		// refer to Ghostscript documentation for parameter usage
		// gs -q -sPDFname=test.pdf pdfpagecount.ps
		List<String> gsArgs = new ArrayList<String>();
		gsArgs.add("-gs");
		gsArgs.add("-dNOPAUSE");
		gsArgs.add("-dQUIET");
		gsArgs.add("-dBATCH");
		gsArgs.add("-sPDFname=" + inputPdfFile);
		gsArgs.add("lib/pdfpagecount.ps");

		int pageCount = 0;
		ByteArrayOutputStream os = null;

		// execute and exit interpreter
		try {
			// output
			os = new ByteArrayOutputStream();
			gs.setStdOut(os);
			gs.initialize(gsArgs.toArray(new String[0]));
			pageCount = Integer
					.parseInt(os.toString().replace("%%Pages: ", ""));
			os.close();
		} catch (GhostscriptException e) {
			System.err.println("ERROR: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
		}

		return pageCount;
	}

	static String getMessage(String message) {
		if (message.contains("library 'gs") || message.contains("ghost4j")) {
			return message + GS_INSTALL;
		}
		return message;
	}

}

class imagesEncap {

	List<Image> imagesE;

}
