/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.training;

import java.awt.*;
 
class ImagePreview extends Canvas {
	 
	private static final long serialVersionUID = -6946634470905514404L;
	Image img;
	
	/**
	 * class constructor
	 * @param img
	 */
	public ImagePreview(Image img) {
	    this.img = img;
	    setSize(img.getWidth(this), img.getHeight(this));
	}

	/**
	 * paint image inside canvas
	 */
	public void paint(Graphics gr) {
	    gr.drawImage(img, 0, 0, this);
	}
} 