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

/**
 * read the pixels of pgm image from file
 * @author cosmin.cojocar
 */
class PixmapReader extends FileInputStream {

	private char c;

	/**
	 * class constructor
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public PixmapReader(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	/**
	 * this method compares two strings
	 * @param key  - key string 
	 * @return
	 * @throws IOException
	 */
	public boolean matchKey(String key) throws IOException {
		byte[] buf = new byte[key.length()];
	    read(buf, 0, key.length());
	    return key.compareTo(new String(buf)) == 0;
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
	    
	    while ((c != '\n') && Character.isSpaceChar(c)) 
	      getChar();
	    
	    while ((c != '\n') && !Character.isSpaceChar(c)) {
	      s = s + c;
	      getChar();
	    }
	    
	    return Integer.parseInt(s);
	}
	
	/**
	 * skip new line character
	 * @throws IOException
	 */
	public void newLine() throws IOException {
		if (c == '\n') {
			getChar();
		}
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
	public void skipComment(char code) throws IOException {
	    getChar();
	    while (c == code) {
	      skipLine();
	      getChar();
	    }
	}

	/**
	 * load data pixels from image file
	 * @param size - buffer size
	 * @return
	 * @throws IOException
	 */
	public byte[] loadData(int size) throws IOException {
		byte[] data = new byte[size];
	    read(data, 0, size);
	    return data;
	}

	/** 
	 * close reader
	 */
	public void close() {
	    try {
	      super.close();
	    } 
	    catch (IOException e) {}
	}
} 