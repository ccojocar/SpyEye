/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.testing;

import java.io.BufferedReader;
import java.io.FileReader;

public class ReadCoordinatesBioID {
	BufferedReader coordInFile;
	
	public ReadCoordinatesBioID(String fileName) throws Exception {
		coordInFile = new BufferedReader(new FileReader(fileName));
		
		/* skip first line */
		coordInFile.readLine();
	}
	
	public int[] getCoord() throws Exception {
		int coord[] = new int[4];
		String line = coordInFile.readLine();
		if (line != null) {
			line=line.trim();
			String attributes[]=line.split("\t\t*");
			for (int i=0 ; i<4 ; i++) {
				if (attributes[i]!= null) {
					coord[i]= Integer.parseInt(attributes[i]);
				}
			}
			return coord;
		} else {
			return null;
		}
	}
}
