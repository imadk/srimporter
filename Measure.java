package com.dmagik.sheetrackimporter;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

public class Measure {

	public Rectangle m_Dimensions;
	public int m_nBeats = 4;
	public int m_nOver = 4;
	public short m_nVelocity = 120; // beats per second
	public short m_nPage = 0;

	/*
	 * FileInputStream in = new FileInputStream("c:\\myfile.dat"); byte buff[] =
	 * new byte[9]; in.read(buff, 0, 9); // Read first 9 bytes into buff String
	 * s = new String(buff); int num = in.read(); // Next is 123 in.close();
	 */

	public void Serialize(FileOutputStream writer) throws IOException {
		writer.write(m_Dimensions.Left);
		writer.write(m_Dimensions.Top);
		writer.write(m_Dimensions.Width);
		writer.write(m_Dimensions.Height);

		writer.write(m_nBeats);
		writer.write(m_nOver);
		writer.write((int) m_nPage);
		writer.write((int) m_nVelocity);
	}

	public void Deserialize(DataInputStream dreader) throws IOException {

		int nLeft = dreader.readInt();

		int nTop = dreader.readInt();

		int nWidth = dreader.readInt();

		int nHeight = dreader.readInt();

		m_Dimensions = new Rectangle(nLeft, nTop, nWidth, nHeight);

		int m_nBeats = dreader.readInt();
		int m_nOver = dreader.readInt();

		short m_nPage = (short) dreader.readInt();

		short m_nVelocity = (short) dreader.readInt();

	}
}
