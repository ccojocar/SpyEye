/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main;

import java.awt.Color;

public class Drawer {
	
	final static byte red   = (byte)Color.red.getRed();
	final static byte green = (byte)Color.red.getGreen();
	final static byte blue  = (byte)Color.red.getBlue();
	
	final static byte ccRed   = (byte)Color.cyan.getRed();
	final static byte ccGreen = (byte)Color.cyan.getGreen();
	final static byte ccBlue  = (byte)Color.cyan.getBlue();
	
	/**
	 * this method draws a line inside a video frame 
	 * @param startX - first point x
	 * @param startY - first point y 
	 * @param endX - last point x 
	 * @param endY - last point y
	 * @param data - video frame data
	 * @return
	 */
	public static void drawLine(int startX , int startY , int endX , int endY , byte data[]) {
		int pos;
		double xLength = endX - startX;
		double yLength = endY - startY;
		
		double step = (yLength != 0 ? xLength/yLength : xLength);
		step = ((int)step == 0 ? 1 : (int)step);
		
		double slope = (xLength != 0 ? yLength/xLength : Math.tan(Math.toRadians(-90)));
		slope = (slope < 0 ? -1 : 1);
	
		for (int x=startX ; x<endX ; x++) {
			if ( x % (int)step == 0) {
				startY+= slope;
			}
			pos=(240 - startY)*960 + (x*3);
			if ((pos >=0)&&( pos + 2 < data.length)) {
				data[pos] = ccBlue;
				pos++;
				data[pos] = ccGreen;
				pos++;
				data[pos] = ccRed;
			} else {
				break;
			}
		}
	}
	
	/**
	 * this method draws a thick point 
	 * @param x0      - point x coordinate
	 * @param y0      - point y coordinate 
	 * @param radius  - point radius
	 * @param data    - image
	 * @return
	 */
	public static void drawCenter(int x0 , int y0 , int radius , byte data[]) {
		int pos;
		if (((y0 - radius) >= 0)&&((x0 - radius) >= 0) &&
		  ((y0 + radius) < 240 )&&((x0 + radius) < 960)) {
			for (int y = y0 - radius ; y <= y0 + radius ; y++) {
				for (int x = x0 - radius ; x <= x0 + radius ; x++) {
					if ((Math.pow(x-x0, 2)+Math.pow(y-y0, 2)) <= Math.pow(radius,2)) {
						pos=(240 - y)*960 + (x*3);
						if ((pos >= 0) &&(pos+2 < data.length)) {
							data[pos] = ccBlue;
							pos++;
							data[pos] = ccGreen;
							pos++;
							data[pos] = ccRed;
						}
					}
				}
			}
		}
	}
	
	/**
	 * this method draws a circle inside the video frame 
	 * @param x0      - center x coordinate 
	 * @param y0      - center y coordinate 
	 * @param radius  - circle radius
	 * @param data    - image 
	 * @return
	 */
	public static void drawCircle(int x0 , int y0 , int radius , byte data[]) {
		int pos;
		if(((y0 - radius) >= 0)  &&((x0 - radius) >= 0) &&
		  ((y0 + radius) < 240 )&&((x0 + radius) <  960)) {
			for (int y = y0 - radius ; y <= y0 + radius ; y++) {
				for (int x = x0 - radius ; x <= x0 + radius ; x++) {
					if ((Math.pow(x-x0, 2)+Math.pow(y-y0, 2)) <= (Math.pow(radius,2)+10) &&
					    (Math.pow(x-x0, 2)+Math.pow(y-y0, 2)) >= (Math.pow(radius,2)-10)) {
						pos=(240 - y)*960 + (x*3);
						if ((pos >= 0) && (pos+2 < data.length)) {
							data[pos] = blue;
							pos++;
							data[pos] = green;
							pos++;
							data[pos] = red;
						}
					}
				}
			}
		}
	}
	
	/**
	 * this method draws a rectangle inside the video frame
	 * @param x0 - upper corner x
	 * @param y0 - upper corner y
	 * @param width - rectangle width
	 * @param height - rectangle height
	 * @param data - video data 
	 * @return
	 */
	public static void drawRectangle(int x0 , int y0 , int width , int height , byte data[]) {
		int pos;
		/* first horizontal line */
		for (int x = x0 ; x < x0 + width; x++) {
			pos = (240 - y0) * 960 + (x*3);
			if ((pos >= 0) && (pos + 2 < data.length)) {
				data[pos] = blue;
				pos++;
				data[pos] = green;
				pos++;
				data[pos] = red;
			} else {
				break;
			}
		}
		
		/* second horizontal line */
		for(int x=x0; x < x0 + width; x++) {
			pos = (240 - (y0 + height)) * 960 + (x * 3);
			if ((pos >= 0) && (pos + 2 < data.length)) {
				data[pos] = blue;
				pos++;
				data[pos] = green;
				pos++;
				data[pos] = red;
			} else {
				break;
			}
		}
		
		/* first vertical line */
		for (int y = y0; y < y0 + height; y++) {
			pos = (240 - y) * 960 + (x0*3);
			if ((pos >= 0) && (pos + 2 < data.length)) {
				data[pos] = blue;
				pos++;
				data[pos] = green;
				pos++;
				data[pos] = red;
			} else {
				break;
			}
		}
		
		/* second vertical line */
		for (int y = y0 ; y < y0 + height; y++) {
			pos = (240 - y) * 960 + ((x0 + width) * 3);
			if ((pos >= 0) && (pos + 2 < data.length)) {
				data[pos] = blue;
				pos++;
				data[pos] = green;
				pos++;
				data[pos] = red;
			} else {
				break;
			}
		}
	}
}