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

public abstract class Pixmap {

	private static String MAGIC_PGM = "P5\n";
	public final int width;
	public final int height;
	public final int size;
	private PixmapReader reader;

   /**
    * class default constructor
    * @param w - image width
	* @param h - image height
	*/
	public Pixmap(int w, int h) {
		width = w;
		height = h;
		size = width * height;
	}

	/**
	 * this method loads a pgm image from file
	 * @param fileName     - file name 
	 * @param magic        - magic number
	 * @throws IOException
	 */
	Pixmap(String fileName, String magic) throws IOException {
		reader = new PixmapReader(fileName);
	    if (!reader.matchKey(magic))
	      throw new IOException(fileName + " : wrong magic number");
	    
	    reader.skipComment('#');
	    
	    /* get image haight and width */
	    reader.newLine();
	    
	    width = reader.getInt();
	    reader.newLine();
	    
	    height = reader.getInt();
	    size = width * height;
	    reader.newLine();
	    
	    reader.skipLine();
	}

	/**
	 * load a pgm image without magic number
	 * @param fileName     - file name
	 * @throws IOException
	 */
	public Pixmap(String fileName) throws IOException {
		this(fileName, MAGIC_PGM);
	}

	/**
	 * read a image bytes with given size
	 * @param size
	 * @return
	 * @throws IOException
	 */
	final byte[] readBytes(int size) throws IOException {
	    return reader.loadData(size);
	}

	/**
	 * read a image bytes without size
	 * @return
	 * @throws IOException
	 */
	final byte[] readBytes() throws IOException {
		return readBytes(size);
	}

	/**
	 * close file reader  
	 */
	final void close() {
	    reader.close();
	    reader = null;
	}

	/**
	 * return image bytes
	 * @return
	 */
	public abstract byte[] getBytes();
	  
	/**
	 * return pixel int value
	 * @param b
	 * @return
	 */
	public static int intValue(byte b) {
	    if (b < 0)
	    	return b + 256;
	    else
	    	return b;
	}
	
	/**
	 * return image bytes
	 * @param buffer
	 * @return
	 */
	public static byte[] getBytes(byte[] buffer) {
		byte[] data = new byte[buffer.length];
		for (int i = 0; i < buffer.length; i++)
			data[i] = buffer[i];
		return data;
	}
} 