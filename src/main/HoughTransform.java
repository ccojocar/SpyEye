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
import java.awt.image.*;

/**
 * This class implements the Line Hough transform. It is heavily based on the code written
 * by Timothy Sharman, resource site (http://homepages.inf.ed.ac.uk/rbf/HIPR2/hipr_top.htm)
 */
public class HoughTransform {
	
	/**
	 * the accumulator
	 */
	protected long houghAccumulator[][];
	
	/**
	 * max accumulator value
	 */
	
	protected long maxAccValue = -1;
	
	/**
     * the local peak neighborhood.
     */    
    protected int localPeakNeighbourhood = 5;
    
    /**
     * the theta step.
     */    
    protected double thetaStep = 0.0;
    
    /**
     * the line color used for the superimposed image (default WHITE).
     */    
    
    protected Color lineColor = Color.WHITE;
    
    public HoughTransform() {}
    
    /**
     * set the line color used for the superimposed image.
     * @param lc - the new line color
     */    
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }
    
    /**
     * get the line color 
     * @return the current line color 
     */    
    public Color getLineColor() {
        return lineColor;
    }
    
    /**
     * set the local peak neighborhood.
     * @param lpn - the new local peak neighborhood.
     */    
    public void setLocalPeakNeighbourhood(int localPeakNeighbourhood) {
        this.localPeakNeighbourhood = localPeakNeighbourhood;
    }
    
    /**
     * get the local peak neighborhood.
     * @return the current local peak neighborhood
     */    
    public int getLocalPeakNeighbourhood() {
        return localPeakNeighbourhood;
    }
    
    /**
     * get the maximum value in the accumulator.
     * @return the maximum value.
     */    
    protected long getMaximum() {
        if (houghAccumulator == null) return -1;
        
        long accumulatorMax = Long.MIN_VALUE;
        for (int i = 0; i<houghAccumulator.length; i++) {
            for (int j = 0; j < houghAccumulator[0].length; j++) {
                if (houghAccumulator[i][j] > accumulatorMax) 
                    accumulatorMax = houghAccumulator[i][j];                
            }
        }
        return accumulatorMax;
    }
    
    /**
     * runs the line Hough Transform. Note that the input image must be gray scaled and
     * thresholded
     * @param img - the input image.
     */    
    public void run(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_BYTE_GRAY)
            throw new IllegalArgumentException("input image must be greyscaled");

        int width  = image.getWidth();
        int height = image.getHeight();
        
        Raster source = image.getRaster();
        int tmp = Math.max(width, height);
        int hh = (int) (Math.sqrt(2)*tmp);
        int hw = 180;
        int rgb;
        
        houghAccumulator = new long[hw][2*hh];
        thetaStep = Math.PI / hw;

        int centreX = width  / 2;
        int centreY = height / 2;                        

        for (int i = 0; i < hw; i++) {
            for (int j = 0; j < 2*hh; j++) {
                houghAccumulator[i][j] = 0;
            }
        }
               
        // find the edge points and update the Hough array
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                rgb = source.getSample(i, j, 0);
                if (rgb != 0) {
                    // edge pixel found
                    for (int k = 0; k < hw; k++) {
                        //work out the r values for each theta step
                        tmp = (int) ((( i - centreX )*Math.cos(k*thetaStep)) +
                                     (( j - centreY )*Math.sin(k*thetaStep)));
                       
                        // move all values into positive range for display purposes
                        tmp = tmp + hh;
                        if (tmp < 0 || tmp >= 2*hh) continue;
                        
                        // increment the hough array
                        houghAccumulator[k][tmp]++;
                    }
                }
            }
        }
        
    }
    
    /**
     * returns the superimposed image. This can be a full-color version of the image
     * whom Hough transform has been run on.
     * @param img       - the input image.
     * @param threshold - the accumulator threshold.
     * @return the superimposed image.
     */    
    public BufferedImage getSuperimposedImage(BufferedImage image, double threshold) {
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), 
        						image.getType());        
        out.setData(image.copyData(null));
        Graphics2D g2d = (Graphics2D)out.getGraphics();

        g2d.setColor(lineColor);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        /* get max value of accumulator */
        maxAccValue = getMaximum();
        
        long thresh = (int)(threshold * maxAccValue);
        
        /* Search for local peaks above threshold to draw */
        int width = image.getWidth();
        int height = image.getHeight();
        int centreX = width  / 2;
        int centreY = height / 2;
        int tmp = Math.max(width, height);
        int hh = (int)(Math.sqrt(2)*tmp);
        int hw = 180;
        
        for (int i = 0; i < hw; i++) {
            for (int j = 0; j < 2*hh; j++) {
                if (houghAccumulator[i][j] >= thresh) {
                
                	if (localPeak(i, j, hw, hh, localPeakNeighbourhood) == false) continue;

	                int x1, x2, y1, y2;
	                double tsin = Math.sin(i*thetaStep);
	                double tcos = Math.cos(i*thetaStep);
	                
	                if (i <= hw/4 || i >= (3*hw)/4) {
	                	y1 = 0;
	                    x1 = (int) (((j-hh) - ((y1-centreY)*tsin)) / tcos) + centreX;
	                    y2 = height - 1;
	                    x2 = (int) (((j-hh) - ((y2-centreY)*tsin)) / tcos) + centreX;
	                } else {
	                	x1 = 0;
	                    y1 = (int) (((j-hh) - ((x1-centreX)*tcos)) / tsin) + centreY;
	                    x2 = width - 1;
	                    y2 = (int) (((j-hh) - ((x2-centreX)*tcos)) / tsin) + centreY;
	                }
	                g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
    
        return out;
    }
    
    /**
     * looking for neighborhood peaks
     * @param i - the current point x
     * @param j - the current point y
     * @param hw - the hough width
     * @param hh - the hough height
     * @param neighbourhood - numbers of neighborhoods
     * @return
     */
    private boolean localPeak(int i, int j, int hw, int hh, int neighbourhood) {
        int dt, dr;
        long peak = houghAccumulator[i][j];
        
        for (int k = -neighbourhood; k <= neighbourhood; k++) {
            for (int l = -neighbourhood; l <= neighbourhood; l++) {
                if (k == 0 && l == 0) 
                	continue;
                
                dt = i+k;
                dr = j+l;
                
                if (dr < 0 || dr >= 2*hh) 
                	continue;
                
                if (dt < 0) 
                	dt = dt + hw;
                
                if (dt >= hw) 
                	dt = dt - hw;
                
                if (houghAccumulator[dt][dr] > peak) {
                    return false;
                }
            }
        }
        
        return true;
    }
}