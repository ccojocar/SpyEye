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
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * this class show a pgm image
 */
public class ImageWindow extends JFrame {
	
	private static final long serialVersionUID = 748890795752976529L;
		
	/**
	 * class constructor
	 * @param name - image file name
	 * @param p    - image BytePixmap
	 */
	public ImageWindow(String name, BytePixmap p) {
		super(name);
		setLocation(0,0);
		
		int[] pixels = new int[p.size];
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = 0xFF000000 + Pixmap.intValue(p.data[i]) * 0x010101; 
		    
		MemoryImageSource source = new MemoryImageSource(p.width,p.height,pixels, 0, p.width);
		Image img = Toolkit.getDefaultToolkit().createImage(source);
		add(new ImagePreview(img));
		pack();
		this.setVisible(true);
	} 
	
	/**
	 * class constructor
	 * @param filename  - image file name 
	 * @throws IOException
	 */
	public ImageWindow(String filename) throws IOException {
		this(filename, new BytePixmap(filename));
	}
}
