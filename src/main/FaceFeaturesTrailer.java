/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */
package main;

import java.awt.*;

public class FaceFeaturesTrailer {
	
	public FaceFeaturesTrailer()
	{}
	
	/**
	 * this method checks if x coordinate is inside the 
	 * frame boundaries and adjusts the shift value if need it 
	 * 
	 * @param x       - the x coordinate
	 * @param shift   - the shift value 
	 * @param width   - the window width
	 * @param size    - the frame/image size
	 * @return
	 */
	public int checkHorizontalFrontier(int x , int shift , int width , int size) {
		if ((x - width/2 + shift) < 0) {
			return shift = width/2 - x ;
		} else {
			if ((x - width/2 + shift) >= size - width) {
				return shift = size - width/2 - x ;
			}
		}
		return shift;
	}
	
	/**
	 * this method checks if y coordinate is inside the 
	 * frame boundaries and adjusts the shift if need it
	 * 
	 * @param y      - the y coordinates
	 * @param shift  - the shift value
	 * @param height - the window height 
	 * @param size   - the frame/image size 
	 * @return
	 */
	public int checkVerticalFrontier(int y , int shift , int height , int size) {
		if ((y - height/2 + shift) < 0) {
			return shift = height/2 -y ;
		} else {
			if ((y - height/2 + shift) >= size - height) {
				return shift = size - height/2 - y ;
			}
		}
		return shift;
	}
	
	/**
	 * this method calculate the sum of squared differences between the template
	 * extracted from search area and original template 
	 * @param template - correct pixels
	 * @param roi      - a region that contains the feature pixels after motion 
	 * @param x0       - x offset 
	 * @param y0       - y offset
	 * @param tW       - template width
	 * @param tH       - template height
	 * @param min      - the current min value of sums of squared differences  
	 * @param sW       - the search area width
	 * @return
	 */
	private double sumOfSquaredDifferences(int template[][],int roi[],int x0 , int y0 , 
										 int tW , int tH ,int sW ,double min) {
		double error = 0 ;
		for (int y =0; y < tH; y++) {
			for(int x = 0; x < tW; x++) {
				error += Math.pow( template[x][y] - roi[(y + y0)*sW + (x + x0)] ,2);
				if (error > min) {
					return Double.MAX_VALUE;
				}
			}
		}
		return error;
	}
	
	/**
	 * this method detects the new position of the template, 
	 * using squared difference sum
	 * @param template - correct pixels 
	 * @param roi      - a region that contains the pixels after motion 
	 * @param tW       - template width
	 * @param tH       - template height
	 * @param sW       - the with of region inside which it will be done the search 
	 * @param sH       - the height of region inside which it will be done the search
	 * @return
	 */
	public Point templateMatching(int template[][] , int roi[], int tW , int tH , int sW, int sH) {
		double error;
		double min = Double.MAX_VALUE;
		int x0=0 , y0=0;
		
	   /* calculate the square difference between templates extracted from search area 
	    * and find the perfect fit 
	    */
		for (int y=0; y<(sH -tH); y++) {
			for (int x=0; x<(sW - tW); x++) {
				error = sumOfSquaredDifferences(template ,roi ,x ,y ,tW ,tH ,sW ,min);
				if(error < min) {
					x0=x;
					y0=y;
					min = error;
				}
			}
		}
		return new Point(x0 + tW/2 , y0 + tH/2);
	}

	/**
	 * detects the motion
	 *  
	 * @param newPix - the new pixels from detection region
	 * @param oldPix - the old pixels from detection region
	 * @param width  - the region width
	 * @param height - the region height
	 * @param x0     - the region origin x
	 * @param y0     - the region origin y
	 * @param data   - image  
	 * @param isNotShifted - this flags indicates if the face has been moved 
	 * @return
	 */
	public boolean checkMotions(int newPix[] ,int oldPix[] ,int width ,int height,
								int x0 ,int y0 ,byte data[] ,boolean bNotShifted ) {
		int pos;
		int nrMotions=0;
		boolean areMotions = false;
		final byte red   = (byte)Color.green.getRed();
		final byte green = (byte)Color.green.getGreen();
		final byte blue  = (byte)Color.green.getBlue();
		
		if (bNotShifted) {
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					/* if the pixels difference is great than 20 has been a motion */
					if (Math.abs(newPix[y*width +x] - oldPix[y*width +x]) > 20) {
						nrMotions++;
						pos = (240 - (y + y0))*960 + (x + x0)*3;
						if ((pos >= 0) && (pos+2 < data.length )) {
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
		
		if (nrMotions >= 25) {
			if (bNotShifted) {	
				areMotions=true;
			} else {
				areMotions=false;
			}
		}
		return areMotions;
	}
	
	/**
	 * this method will find the eye coordinate after dragging
	 * @param roi            - region of interest 
	 * @param template       - original eye template 
	 * @param ii             - integral image of region 
	 * @param regWidth       - region width 
	 * @param regHeight      - region height 
	 * @param templateWidth  - template width 
	 * @param templateHeight - template height 
	 * @return
	 */
	public Point findEye( int roi[] , int template[][],int ii[][],int regWidth,int regHeight,
						  int templateWidth,int templateHeight) {
		int darkestX=0,darkestY=0;
		int templateX=0,templateY=0;
		int filterSize=7;
		int pixSum;
		int minPixSum = Integer.MAX_VALUE;
		
		/* looking for darkest point from region , using a 7x7 filter */
		/* darkestX must be > templateWidth/2=15 and darkestY > templateHeight/2 = 10*/
		for(int y = 3; y < (regHeight - filterSize -3); y++) {
			for(int x = 5; x < (regWidth - filterSize -5); x++) {
				pixSum = ii[x+filterSize][y+filterSize] - ii[x][y+filterSize]-
						 ii[x+filterSize][y] + ii[x][y];
				if (pixSum < minPixSum) {
					minPixSum = pixSum;
					darkestX=x + filterSize/2;
					darkestY=y + filterSize/2;
				}
			}
		}
		
		/* find the coordinates of the most commensurate template inside region */
		double error;
		double minError = Double.MAX_VALUE;
		for (int y = 0; y < (regHeight - templateHeight); y++) {
			for (int x = 0; x < (regWidth - templateWidth); x++) {
				error = sumOfSquaredDifferences(template, roi, x , y ,templateWidth ,
						templateHeight,regWidth,minError);
				
				if (error < minError) {
					minError = error;
					templateX = x + templateWidth/2;
					templateY = y + templateHeight/2;
				}
			}
		}
		
		/* return the mean between darkest point and template point */
		return new Point( (darkestX + templateX)/2,(darkestY + templateY)/2);
	}
}
