/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main;

import java.awt.geom.Line2D;

public class Geometry {
	
	/**
	 * this method calculate the determinate of a basic matrix 
	 */
	private static double det(double x00,double x01,double x10,double x11) {
		return x00*x11 - x01*x10;
	}
	
	/**
	 * find the intersection point of 2 lines
	 */
	public static boolean getLineLineIntersection(double x0,double y0 , double x1, double y1,
								double x2 ,double y2 ,double x3 ,double y3 ,double point[]) {
		
		double slope0 =(x1-x0)!= 0 ? (double)(y1-y0)/(double)(x1-x0) : 0;
		double slope1 =(x3-x2)!= 0 ? (double)(y3-y2)/(double)(x3-x2) : 0;
			
			/* parallel lines */
		if(slope0 == slope1) {
			return false;
		}
	
		point[0] = det(det(x0 ,y0 ,x1 ,y1) , x0 - x1 ,det(x2 ,y2 ,x3 ,y3) , x2 - x3 ) / 
			det(x0 - x1 ,y0 - y1 ,x2 - x3 ,y2 - y3);	   
						   
		point[1] = det(det(x0 ,y0 ,x1 ,y1) , y0 - y1 ,det(x2 ,y2 ,x3 ,y3) , y2 - y3 ) /
			det(x0 - x1 ,y0 - y1 ,x2 - x3 ,y2 - y3);	   
		
		return true;
	}
	
	/**
	 * return the distance from a point to a line 
	 */
	public static double pointToLineDist(int x0, int y0, int x1, int y1, int px, int py) {
		Line2D.Double line = new Line2D.Double(x0 ,y0 ,x1 ,y1);
		double noseToLineDist = Math.sqrt(line.ptLineDist(px, py));
		return noseToLineDist;
	}
}