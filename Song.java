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

// [System.Xml.Serialization.XmlTypeAttribute(Namespace =
// "http://tempuri.org/")]
class Song {
	public String FullName;
	public double RatingNumerator;
	public double RatingDenominator;
	public int RatingCount;
	public boolean IsPDF;
}
