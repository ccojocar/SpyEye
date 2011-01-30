/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.testing;

import java.io.*;

public class ReadCoordinatesYaleDB {
	BufferedReader leCoordInFile;
	BufferedReader reCoordInFile;
	int leLineCount=0;
	int reLineCount=1;
	
	/**
	 * @param fileName   - file path of eyes' coordinates 
	 * @throws Exception
	 */
	public ReadCoordinatesYaleDB(String fileName) throws Exception {
		leCoordInFile = new BufferedReader(new FileReader(fileName));
		reCoordInFile = new BufferedReader(new FileReader(fileName));
		/* skip firsts 65 lines */
		String line = reCoordInFile.readLine();
		int lnCount = 1;
		while ((line != null) && (lnCount < 65)) {
			line = reCoordInFile.readLine();
			lnCount++;
		}
	}
	
	/**
	 * returns next left eye's coordinate
	 * @return
	 * @throws Exception
	 */
	public double[] getNextLECoord() throws Exception {
		double coord[] = new double[2];
		String line = leCoordInFile.readLine();
		leLineCount++;
		if ((leLineCount <= 65) && (line != null)) {
			line=line.trim();
			String attributes[]=line.split("    *");
			if (attributes[0] != null) {
				coord[0]= Double.parseDouble(attributes[0]);
			}
			if (attributes[1] != null) {
				coord[1]= Double.parseDouble(attributes[1]);
			}
			return coord;
		} else {
			return null;
		}
	}
	
	/**
	 * returns next right eye's coordinate 
	 * @return
	 * @throws Exception
	 */
	public double[] getNextRECoord() throws Exception {
		double coord[] = new double[2];
		String line = reCoordInFile.readLine();
		reLineCount++;
		if ((reLineCount <= 65) && (line != null)) {
			line=line.trim();
			String attributes[]=line.split("    *");
			if (attributes[0] != null) {
				coord[0]= Double.parseDouble(attributes[0]);
			}
			if (attributes[1] != null) {
				coord[1]= Double.parseDouble(attributes[1]);
			}
			return coord;
		} else {
			return null;
		}
	}
}
