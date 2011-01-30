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

public class BytePixmap extends Pixmap {

	public final byte[] data;

	/**
	 * byte pixels constructor
	 * @param w   - image width
	 * @param h   - image height
	 * @param pixels - pixels values 
	 * @throws IllegalArgumentException
	 */
	public BytePixmap(int w, int h, byte[] pixels) throws IllegalArgumentException {
		super(w, h);
	    if (pixels == null)
	    	pixels = new byte[w * h];
	    
	    if (pixels.length != w * h)
	      throw new IllegalArgumentException("bad dimensions");
	    
	    data = pixels;
	}

	/**
	 * byte pixels constructor
	 * @param w - image width
	 * @param h - image height
	 */
	public BytePixmap(int w, int h) {
		this(w, h, null);
	}

	/**
	 * bytes pixels constructor
	 * @param p - pixels values using Pixmap
	 */
	public BytePixmap(Pixmap p) {
		this(p.width, p.height, p.getBytes());
	}

	/**
	 * bytes pixels constructor  
	 * @param fileName - image file name 
	 * @throws IOException
	 */
	public BytePixmap(String fileName) throws IOException {
		super(fileName);
	    data = readBytes();
	    close();
	}

	/**
	 * return pixels values
	 */
	public byte[] getBytes() {
		return getBytes(data);
	}
} 