/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.training;

import java.io.*;

public class EyesCoordinatesReader extends FileInputStream {
	
	private char c;
	
	/**
	 * class constructor 
	 * @param fileName - file with eyes coordinates path
	 * @throws FileNotFoundException
	 */
	public EyesCoordinatesReader(String fileName) throws FileNotFoundException {
		super(fileName);
	}
	
	/** 
	 * close file reader
	 */
	public void close() {
	    try {
	    	super.close();
	    } 
	    catch (IOException e) {}
	}
	
	/**
	 * skip one line
	 * @throws IOException
	 */
	public void skipLine() throws IOException {
	    while (c != '\n')
	      getChar();
	}

	/**
	 * skip comment part 
	 * @param code
	 * @throws IOException
	 */
	public void skipComment(char code) throws IOException 
	{
	    getChar();
	    while (c == code) {
	      skipLine();
	      getChar();
	    }
	}
	/**
	 * read one character
	 * @throws IOException
	 */
	public void getChar() throws IOException {
		c = (char)read();
	}
	
	/**
	 * get int value from pgm
	 * @return
	 * @throws IOException
	 */
	public int getInt() throws IOException {
	    String s = "";
	    while ((c != '\n')  && (!Character.isWhitespace(c))) {
	    	s = s + c;
	    	getChar();
	    }
	    getChar();
	    return Integer.parseInt(s);
	}
	
	/**
	 * return eyes coordinates from *.eye files
	 * @return
	 * @throws IOException
	 */
	public int[] getCoordinates()throws IOException {
		int coordinates[] = new int [4];
		try {
			skipComment('#');
			coordinates[2]=getInt(); /* lX and lY because the image is mirrored */
			coordinates[3]=getInt();
			coordinates[0]=getInt(); 
		    coordinates[1]=getInt();
		} finally {
			close();
		}
		return coordinates;
	}
}
