/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main;

import java.awt.image.*;
import java.awt.*;

public class ImageProcessing {
	
	public ImageProcessing() {}

	/**
	 * this method grabs the pixels from image and puts them in an array
	 * @param image  - image 
	 * @param sX     - start x coordinate 
	 * @param sY     - start y coordinate
	 * @param width  - image width 
	 * @param height - image height 
	 * @param pixels - array with image pixels  
	 * @return
	 */
	public static int[] getPixels(Image image,int startX,int startY, int width,
								  int height,int pixels[]) {
		PixelGrabber pg = new PixelGrabber(image, startX, startY, width, height, 
										   pixels, 0, width);
        
		/* the PixelGrabber class grabs the pixels from the image and puts them in the
		 array. */
		try {
	      pg.grabPixels(); 
	    } catch (Exception ex){}
	    
	    return pixels;
	}
	
	/**
	 * this method converts the image from RGB color space to HSB color space and get 
	 * the gray color for every pixel
	 * @param pixels     - the RGB pixels of image 
	 * @param grayPixels - the gray pixels of image  
	 * @return
	 */
	public static int[] toGrayscale(int pixels[],int grayPixels[]) {
        /* array that will contain the hew, saturation and brightness values */
		float hsb[] = new float[3]; 	    
		float brightness;        
	    int rgb,red,green,blue;
	    
	   /*
	    * get the red,green,blue bands of the image
	    * and convert them to the HSB color model.
	    */
	    for (int i = 0; i < pixels.length; i++) {                                       
	    	red   = (pixels[i]>>16) & 0xFF;
	    	green = (pixels[i]>> 8) & 0xFF;
	    	blue  = (pixels[i]    ) & 0xFF;
	      
	    	Color.RGBtoHSB(red, green, blue, hsb);
	    	brightness = hsb[2];
	    	
	    	/* set the hew and the saturation to zero to get the representative 
	    	 * gray color.*/
	    	rgb = Color.HSBtoRGB(0, 0, brightness);
          
	    	/* the red = green = blue after the conversion so we are storing one 
	    	 * of the bands.*/
	    	grayPixels[i] = rgb & 0xFF; 
	    }
	    
	    return grayPixels;
	}

	/**
	 * this method calculate integral image
	 * @param width       - image width 
	 * @param height      - image height
	 * @param grayPixels  - array with gray pixels of image  
	 * @param s           - cumulative sum for every pixel   
	 * @param ii          - integral image for every pixel 
	 * @return
	 */
	public static int[][] calculateIntegralImage(int width,int height,int grayPixels[],
												 int s[][],int ii[][]) {
	    /* calculate the cumulative sums */
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				s[x][y] = ( y-1 == -1 ? grayPixels[y*width+x] : 
										grayPixels[y*width+x]+s[x][y-1]);
			   ii[x][y] = ( x-1 == -1 ? s[x][y] : ii[x-1][y]+s[x][y]);
			}
		}
	    return ii;
	}

	/**
	 * this method makes the gray scale image binarization 
	 * @param grayPixels    - the gray pixel array  
	 * @param binaryPixels  - the array with image pixels after binarization, values can 
	 * 						  be 0 or 1
	 * @param threshold     - binarization threshold 
	 * @return
	 */
	public static int[] toThresholdedImage(int grayPixels[],int binaryPixels[], int threshold)
	{
		for (int i = 0; i < grayPixels.length; i++) {
			if (grayPixels[i] < threshold) {
				binaryPixels[i] = 1;
			} else {
				binaryPixels[i] = 0;
			}
		}
		return binaryPixels;
	}
	
   /**
    * this method do image binarization returning a buffered image with white pixels for 
    * all them under threshold
	* @param grayPixels - the array with grays pixels of image 
	* @param threshold  - the bnarization threshold
	* @param width      - image width
	* @param height     - image height
	* @return
    */
	public static BufferedImage toNegativeThresholdedImage(int grayPixels[], int threshold,
												      int width,int height) {
		int nrPixels = 0;
		
		if ((width <= 0 ) || (height <= 0)) {
			return null;
		}
		
		BufferedImage bufferedImage = new BufferedImage(width,height,
									      BufferedImage.TYPE_BYTE_GRAY);		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				if(grayPixels[y*width+x] < threshold) {
					bufferedImage.setRGB(x, y, Color.white.getRGB());
					nrPixels++;
				}
			}
		}
		if (nrPixels < 10) {
			return null;
	    }
	    
	    return bufferedImage;
	}
	
	/**
	 * this method checks if it is a skin pixel
	 * @param color - pixel color 
	 * @return
	 */
	public static boolean isSkinPixel(int color) {
		int R, G, B;
		double r, g, a, b;
	    
		R = (color >> 16) & 0xFF;
	    G = (color >>  8) & 0xFF;
	    B = (color      ) & 0xFF;
	    
	    r = (double) R / (double) (R + G + B);
	    g = (double) G / (double) (R + G + B);
	    
	    /* change color space */
	    a = r + (g / 2);
	    b = (Math.sqrt(3) / 2d) * g;
	    
	    if ((a > 0.51) && (a < 0.62) && (b > 0.26) && (b < 0.31)) {
	    	return true;
	    } else {
	    	return false;
	    }
	}
}