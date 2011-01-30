/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.training;

import libsvm.*;

public class TrainingDataManager {
	
	private int width;
	private int height;
	private int pixels[];
	
	public TrainingDataManager(int width,int height,int pixels[]) {
		this.width  = width;
		this.height = height;
		this.pixels = pixels;
	}
	
	/**
	 * get the face template pixels scaled to 35x21
	 * @param x0 - left  eye x
	 * @param y0 - left  eye y 
	 * @param x1 - right eye x
	 * @param y1 - right eye y
	 * @return
	 */
	public int[] getTemplatePixels(int x0, int y0, int x1, int y1) {
		int retPixels[] = new int [735]; /* 21*35 */
		int xLen, yLen, sX, sY, cX, cY, oX, oY;
	    double xInc, yInc, nY, scale;
	    
	    xLen = x1 - x0;
	    yLen = y1 - y0;
	    
	    /* distance between the eyes is 23 pixels from experiences */
	    xInc = (double)xLen / 23d; 
	    yInc = (double)yLen / 23d;
	    
	    /*scale factor */
	    scale = Math.sqrt(Math.pow(xLen,2)+Math.pow(yLen,2))/23d;
	    
	    /* 6 from (35 - 23) / 2 and 8 because the eyes are on the 8 row from experiments */
	    oX = x0 - (int)(6 * xInc) + (int)(8 * yInc);
	    nY = (yLen != 0) ? ((8 * yInc) * xLen / yLen) : (8 * scale);
	    oY = y0 - (int)nY;
	    
        /* rotate the template to a horizontal position */
	    sX = oX;
	    sY = oY; 
	    for (int y = 0; y < 21; y++) {
	    	cX = sX;
	    	cY = sY;
	    	for (int x = 0; x < 35; x++) {
	    		if ((cX >= 0) && (cX < width) && (cY < height) && (cY >= 0)) {
	    			retPixels[y * 35 + x]= pixels[cY * width + cX];
	    		}
	    		cX = sX + (int)((x + 1) * xInc);
	    		cY = sY + (int)((x + 1) * yInc);
	    	}
	    	sX = oX - (int)((y + 1) * yInc);
	    	sY = oY + (int)((y + 1) * xInc);
	    }
	    
		return retPixels;
	}
	
	/**
	 * get the face template pixels in SVM format
	 * @param x0 - left  eye x
	 * @param y0 - left  eye y
	 * @param x1 - right eye x
	 * @param y1 - right eye y
	 * @return
	 */
	public svm_node[] getTemplatePixelsInSVMFormat(int x0, int y0, int x1, int y1) {
		int xDistance,yDistance;
		int startX,startY;
		int currentX,currentY;
		int Ox,Oy;
	    double xRes,yRes;
	    double offsetY;
	    double scaleFactor;
	    
	    svm_node node;
	    svm_node template[] = new svm_node[735]; /* template size is 21*35 samples */
	    
	    /* distance between eyes on x and y axis */ 
	    xDistance = x1 - x0;
	    yDistance = y1 - y0;
	    
	    /* distance between the eyes is 23 pixels from experiences 
	     * calculate the scale factor for each axis
	     */
	    xRes = (double)xDistance / 23d; 
	    yRes = (double)yDistance / 23d;
	    
	    /* scale factor */
	    scaleFactor = Math.sqrt(Math.pow(xDistance, 2)+Math.pow(yDistance, 2)) / 23d;
	    
	    /* 6 from (35 -23)/2 and 8 because the eyes are on the 8 row from experiments */
	    Ox = x0 - (int)(6 * xRes) + (int)(8 * yRes);
	    offsetY = (yDistance != 0) ? ((8 * yRes * xDistance) / yDistance) : (8 * scaleFactor);
	    Oy = y0 -(int)offsetY;
	    
        /* rotate the template to a horizontal position */
	    startX = Ox;
	    startY = Oy; 
	    for (int y = 0; y < 21; y++) {
	    	currentX = startX;
	    	currentY = startY;
	    	for (int x = 0; x < 35; x++) {
	    		node = new svm_node();
	    		node.index = y * 35 + x + 1;
	    		if ((currentX >= 0) && (currentX < width) && 
	    			(currentY < height) && (currentY >= 0)) {
	    			/* convert data in svm format */
	    			node.value = pixels[currentY * width + currentX] / 255d;
	    		} else {
	    			if (y == 0 ) {
	    				node.value = 128d/255d;
	    			} else {
	    				node.value = template[ (y - 1) * 35 + x].value;
	    			}
	    		}
	    		
	    		template[y * 35 + x] = node;
	    		currentX = startX + (int)((x + 1) * xRes);
	    		currentY = startY + (int)((x + 1) * yRes);
	    	}
	    	
	    	startX = Ox - (int)((y + 1) * yRes);
	    	startY = Oy + (int)((y + 1) * xRes);
	    }
	    return template;
	}
}
