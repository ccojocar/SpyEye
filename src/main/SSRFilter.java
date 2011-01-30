/*
 * This class implements the six segmented rectangular filter
 * 
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main;

public class SSRFilter {
	private int filterWidth;
	private int filterHeight;
	private int sectorWidth;
	private int sectorHeight;
	private int ii[][];
	private double area;
	
	public SSRFilter(int width,int height,int ii[][]) {
		this.filterWidth = width;
	    this.filterHeight = height;
	    this.sectorWidth = (int)(width/3);
	    this.sectorHeight = (int)(height/2);
	    
	    /* integral image */
	    this.ii = ii; 
	    area = width*height;
	}
	
	/**
	 * return the filter area
	 * @return
	 */
	public double getArea() {
	    return area;
	}
	
	/**
	 * return the filter height
	 * @return
	 */
	public int getHeight() {
	    return this.filterHeight;
	}
	
	/**
	 * return the filter width
	 * @return
	 */
	public int getWidth() {
	    return this.filterWidth;
	}
	
	/**
	 * this method calculate the SSR filter sectors, starting with  
	 * x,y coordinates and check the face condition
	 */
	public boolean isValidFaceCodition(int x,int y) {
		int s1, s2, s3, s4, s6;

	    /* calculate sectors */
	    s1 = ii[x+sectorWidth][y+sectorHeight] - ii[x+sectorWidth][y] - 
	    	 ii[x][y+sectorHeight] + ii[x][y];
	    s2 = ii[x+2*sectorWidth][y+sectorHeight] - ii[x+2*sectorWidth][y] - 
	    	 ii[x+sectorWidth][y+sectorHeight] + ii[x+sectorWidth][y];
	    s3 = ii[x+3*sectorWidth][y+sectorHeight] - ii[x+3*sectorWidth][y] - 
	         ii[x+2*sectorWidth][y+sectorHeight] + ii[x+2*sectorWidth][y];
	    s4 = ii[x+sectorWidth][y+2*sectorHeight] - ii[x+sectorWidth][y+sectorHeight] - 
	         ii[x][y+2*sectorHeight] + ii[x][y+sectorHeight];
	    s6 = ii[x+3*sectorWidth][y+2*sectorHeight] - ii[x+3*sectorWidth][y+sectorHeight] - 
	         ii[x+2*sectorWidth][y+2*sectorHeight] + ii[x+2*sectorWidth][y+sectorHeight];

	    /* face candidate conditions */
	    if ((s1 < s2 ) && (s1 < s4) && (s3 < s2) && (s3 < s6))
	      return true;
	    else
	      return false;
	}
	
	/**
	 * this method finds nose bridge candidate
	 * using a special filter consisting of 3 segments 
	 */
	public int getNoseBridgeFilterValue(int x,int y) {
	    int s1,s2,s3;

	    /* calculate sectors */
	    s1 = ii[x+sectorWidth][y]   - ii[x][y];
	    s2 = ii[x+2*sectorWidth][y] - ii[x+sectorWidth][y];
	    s3 = ii[x+3*sectorWidth][y] - ii[x+2*sectorWidth][y];

	    /* Nose bridge candidate conditions */
	    if ((s1 < s2) && (s3 < s2))
	      return s2;
	    else
	      return Integer.MIN_VALUE;
	}
}