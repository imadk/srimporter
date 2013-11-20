package com.dmagik.sheetrackimporter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
 * System.Text; using System.IO; using System.Collections; using
 * System.Globalization; using System.Drawing;
 */

public class SheetSerializer {
	public static boolean g_bNormalize = false;
	public static String strLoad_strError;

	static String NormalizeAccents(String regularString) {
		if (!g_bNormalize)
			return regularString; // leave the accents
		String normalizedString = regularString.replace("Ã©", "e");
		normalizedString = Normalizer.normalize(normalizedString, Form.NFD);

		StringBuilder sb = new StringBuilder(normalizedString);

		for (int i = 0; i < sb.length(); i++) {

			if (Character.getType(sb.charAt(i)) == Character.NON_SPACING_MARK) {
				sb.delete(i, 1);
			}
		}
		regularString = sb.toString();
		return regularString;
	}

	// Needs some refactoring.
	static public boolean Generate(
			String[] vInputFiles,
			Hashtable<Object, Object> vMetaData, // from String to filename
			Hashtable<String, ArrayList<Measure>> vMeasuresPerInputFile,
			ArrayList<String> vTags, String strOutputFile, String[] strError)
			throws IOException {

		strError[0] = "";
		boolean bSuccess = true;
		if (vInputFiles.length == 0) {
			strError[0] = "There are no images in that sheet";
			return false;
		}

		for (String strFile : vInputFiles) {
			if (!new File(strFile).exists()) {
				bSuccess = false;
				strError[0] = "File not found - " + strFile;
				return bSuccess;
			}
		}

		FileOutputStream writer = new FileOutputStream(strOutputFile);// ,
																		// FileMode.Create);

		// Version
		writeInt(writer, (int) 1);
//		writer.write();
	

		// System.err.println("Step 2");

		// Step 1 - insert the tags into the header.
		//writer.write((int) vTags.size());
		writeInt(writer, (int) vTags.size());
		byte[] vOutput = null;
		for (Object strTag : vTags) {
			String[] vParts = ((String) strTag).split("\t");
			if (vParts.length != 2) {
				bSuccess = false;
				strError[0] = "Invalid tag encountered - expected 2 fields";
				break;
			}
			// /vOutput = enc.GetBytes(NormalizeAccents(vParts[0]));
			vOutput = NormalizeAccents(vParts[0]).getBytes("UTF-8");

			//writer.write(vOutput.length);
			writeInt(writer,vOutput.length  );
			if (vParts[0].length() > 0) {
				writer.write(vOutput);
			}

			// /vOutput = enc.GetBytes(NormalizeAccents(vParts[1]));
			vOutput = NormalizeAccents(vParts[1]).getBytes("UTF-8");
			//writer.write(vOutput.length);
			writeInt(writer,vOutput.length  );
			if (vParts[1].length() > 0) {
				writer.write(vOutput);
			}
		}

		// Step 2 - insert the metadata information
	//	writer.write(vMetaData.size());
		writeInt(writer,vMetaData.size());

		Iterator<Map.Entry<Object, Object>> it = vMetaData.entrySet()
				.iterator();
		while (it.hasNext()) {

			Map.Entry<Object, Object> entry = it.next();

			// /vOutput = enc.GetBytes(NormalizeAccents((String)entry.Key));

			vOutput = NormalizeAccents((String) entry.getKey()).getBytes(
					"UTF-8");
			//writer.write(vOutput.length);
			writeInt(writer,vOutput.length);
			if (vOutput.length > 0) {
				writer.write(vOutput);
			}

			int dotPos = (NormalizeAccents((String) entry.getValue()))
					.lastIndexOf(".");
			String ext = (NormalizeAccents((String) entry.getValue()))
					.substring(dotPos);
			String strExtension = ext;
			vOutput = strExtension.getBytes("UTF-8");
			//writer.write(vOutput.length);
			writeInt(writer,vOutput.length);
			if (strExtension.length() > 0) {
				writer.write(vOutput);
			}

			WriteImage((String) entry.getValue(), writer);
		}

		// Step 3 - enter the skeleton for the pages
		//writer.write(vInputFiles.length);
		writeInt(writer,vInputFiles.length);
		long nCurrentPosition = writer.getChannel().position(); // /BaseStream.Position;
		for (int i = 0; i < vInputFiles.length; i++) {
			writeInt(writer, (int)0);
//			writer.write((int) 0);
		}

		int[] vPagePositions = new int[vInputFiles.length];
		// Step 4 - write the actual page contents and measures
		for (int i = 0; i < vInputFiles.length; i++) {
			vPagePositions[i] = (int) writer.getChannel().position();
			if (!vMeasuresPerInputFile.contains(vInputFiles[i]))
				//writer.write((int) 0);
				writeInt(writer, (int)0);
			else {
				ArrayList<Measure> list = (ArrayList<Measure>) vMeasuresPerInputFile
						.get(vInputFiles[i]);
				//writer.write(list.size());
				writeInt(writer,list.size());
				for (int j = 0; j < list.size(); j++) {
					Measure measure = (Measure) list.get(j);
					measure.Serialize(writer);
				}

			}

			// Next - insert the extension of the file in here.
			int dotPos = vInputFiles[i].lastIndexOf(".");
			String ext = vInputFiles[i].substring(dotPos);
			String strExtension = ext;

//			writer.write((int) strExtension.length());
			writeInt(writer, (int) strExtension.length());
			if (strExtension.length() > 0) {
				writer.write(strExtension.getBytes()); // / toCharArry???????
			}

			// Finally - insert the image contents in there.
			WriteImage(vInputFiles[i], writer);
		}

		writer.getChannel().position((int) nCurrentPosition);
		for (int k = 0; k < vInputFiles.length; k++) {
			//writer.write(vPagePositions[k]);
			writeInt(writer,vPagePositions[k]);
		}

		writer.close();
		if (!bSuccess) {
			new File(strOutputFile).delete();
		}
		return bSuccess;
	}

	private static void WriteImage(String strInputFile, FileOutputStream writer)
			throws IOException {
		FileInputStream reader = new FileInputStream(strInputFile); // /,
																	// FileMode.Open,
																	// FileAccess.Read,
																	// FileShare.Read);
																	// ????
		int nLength = (int) reader.getChannel().size();
	//	writer.write(nLength); // Cast to int.
		writeInt(writer, nLength);
		byte[] vData = new byte[nLength];
		reader.read(vData, 0, nLength);
		writer.write(vData, 0, nLength);
		reader.close();
	}

	public static boolean LoadSheet(String strInput, String[] vSheets,
			Hashtable<String, ArrayList<Measure>> vMeasures, Object[] vTags,
			boolean bActuallyLoad) throws IOException {
		vSheets = null;
		vMeasures = new Hashtable<String, ArrayList<Measure>>();
		Hashtable<Object, Object> vMetaData = new Hashtable<Object, Object>();
		vTags = null;
		strLoad_strError = "";

		FileInputStream reader = null;
		DataInputStream dreader = null;

		try {
			if (!(new File(strInput).exists())) {
				strLoad_strError = "File not found " + strInput;
				return false;
			}

			reader = new FileInputStream(strInput); // , FileMode.Open,
													// FileAccess.Read);
			dreader = new DataInputStream(reader);
			dreader.mark(Integer.MAX_VALUE);

			// byte[] buf = new byte[4];
			// reader.read(buf);
			// int nVersion = Integer.parseInt(buf.toString(), 10); //equivalent
			// of readInt32 ???
			int nVersion = dreader.readInt();

			// buf = new byte[4];
			// reader.read(buf);
			// int nTagCount = Integer.parseInt(buf.toString(), 10);
			// //equivalent of readInt32 ???

			int nTagCount = dreader.readInt();

			if (nTagCount > 1000) {
				strLoad_strError = "Too many tags - file likely corrupt";
				return false;
			}
			vTags = new String[nTagCount];
			for (int i = 0; i < nTagCount; i++) {

				// buf = new byte[4];
				// reader.read(buf);
				// int nLength = Integer.parseInt(buf.toString(), 10);
				// //equivalent of readInt32 ???

				int nLength = dreader.readInt();
				byte[] szData = null;
				dreader.read(szData, 0, nLength);
				String strTag = new String(szData, "UTF-8");

				nLength = dreader.readInt();
				dreader.read(szData, 0, nLength);
				String strValue = new String(szData, "UTF-8");

				vTags[i] = strTag + "\t" + strValue;
			}

			ArrayList<String> list = new ArrayList<String>();
			for (Object strTag : vTags) {

				list.add((String) strTag);

			}

			// Get the amount of metadata
			int nMetaData = dreader.readInt();
			for (int i = 0; i < nMetaData; i++) {
				int nLength = dreader.readInt();
				byte[] szData = null;
				dreader.read(szData, 0, nLength);
				String strFirst = new String(szData, "UTF-8");

				int nExtensionLength = dreader.readInt();
				dreader.read(szData, 0, nExtensionLength);
				String strExtension = new String(szData, "UTF-8");

				String strFilename = "Temp" + String.valueOf((10000000 + i))
						+ strExtension;

				FileOutputStream writer = new FileOutputStream(strFilename);
				nLength = dreader.readInt();
				byte[] vData = new byte[nLength];
				dreader.read(vData, 0, nLength);
				writer.write(vData, 0, nLength);
				writer.close();

				list.add(strFirst + "\t" + strFilename);
			}
			vTags = (String[]) list.toArray();

			// Get the positions
			int nFileCount = dreader.readInt();
			int[] vPositions = new int[nFileCount];
			vSheets = new String[nFileCount];
			for (int i = 0; i < nFileCount; i++) {
				vPositions[i] = dreader.readInt();
			}

			// Now get the images themselves
			for (int i = 0; i < nFileCount; i++) {

				dreader.read();

				dreader.skip(vPositions[i]);

				// Get the measures.
				int nMeasureCount = dreader.readInt();
				ArrayList<Measure> vLocalMeasures = new ArrayList<Measure>();
				if (nMeasureCount > 0) {
					for (int l = 0; l < nMeasureCount; l++) {
						Measure measure = new Measure();

						measure.Deserialize(dreader);

						vLocalMeasures.add(measure);
					}
				}

				int nExtensionLength = dreader.readInt();
				byte[] szData = null;
				dreader.read(szData, 0, nExtensionLength);
				String strExtension = new String(szData, "UTF-8");

				String strFilename = "Temp" + String.valueOf(i) + strExtension;
				if (bActuallyLoad) {
					FileOutputStream writer = new FileOutputStream(strFilename);

					int nLength = dreader.readInt();
					byte[] vData = new byte[nLength];
					dreader.read(vData, 0, nLength);
					writer.write(vData, 0, nLength);
					writer.close();
				}

				vSheets[i] = strFilename;
				vMeasures.put(strFilename, vLocalMeasures);
			}
		} catch (Exception e) {
			if (reader != null) {
				strLoad_strError = "Exception while reading the sheet "
						+ strInput + " - " + e.getMessage();

				dreader.close(); // /Check confirm order of closing
				reader.close();

			}
			return false;
		}

		return true;
	}

	public static void writeInt(FileOutputStream fOut, int nInteger) throws IOException {

		byte b[] = new byte[4];
		b[0] = (byte) (nInteger % 256);
		nInteger /= 256;

		b[1] = (byte) (nInteger % 256);
		nInteger /= 256;

		b[2] = (byte) (nInteger % 256);
		nInteger /= 256;

		b[3] = (byte) (nInteger % 256);
		nInteger /= 256;

		fOut.write(b);

	}

}
