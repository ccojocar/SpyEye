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
import java.util.*;

public class ClustersManager { 
    private int labels[];
    private int nClusters;
    private int nLabels;
    private int clustersPoints[][];
    public  int nrPoints[];
    public  int indecies[];
    public  int clusters[];

   /**
	* the constructor of cluster manager 
	* @param labels          - the labels of clusters array 
	* @param clustersMembers - the clusters points matrix
	*/
    public ClustersManager(int labels[], int clustersPoints[][]) {
    	nLabels = 0;
	    this.labels = labels;
	    this.clustersPoints = clustersPoints;
    }
    
    /**
	 * the number of active clusters
	 * @return
	 */
	public int getClustersNumber() {
		return nClusters;
	}

	/**
	 * the number of active nLabels
	 * @return
	 */
	public int getLabelsNumber() {
	    return nLabels;
	}
	
   /**
    * determine the label of the cluster for a pixel with (x,y) coordinates 
    * 
	*  Algorithm:
	*  - if the pixel has no neighbor then a new label will be assigned to it 
	*  - if the pixel has only one neighbor then the label of this neighbor will be assigned to it
	*  - if the pixel has more than one neighbor then the label of one of these neighbors will be assigned to it. 
	*  The same label will be also assigned to all neighbors.   
	*  
    * @param x 		- pixel x coordinate 
    * @param y 		- pixel y coordinate 
    * @param width	- image width
    * @return
    */
    public int findPixelLabel(int x,int y,int width) {
      int vPoint,nPoint,nvPoint,nePoint;
      if(y == 0) {
    	  if (x == 0) {
    		  nLabels++;
    		  clustersPoints[x][y] = nLabels;
    		  labels[nLabels] = Integer.MIN_VALUE;
    	  } else {
    		  if (clustersPoints[x-1][y] == 0) {
    			  nLabels++;
    			  clustersPoints[x][y] = nLabels;
    			  labels[nLabels] = Integer.MIN_VALUE;
    		  } else {
    			  clustersPoints[x][y] = clustersPoints[x-1][y];
	      
    		  }
    	  }
      } else {
    	  nPoint = clustersPoints[x][y-1];
    	  if (x+1 == width ) {
    		  nePoint = 0;
    	  } else {
    		  nePoint = clustersPoints[x+1][y-1];
    	  }
    	  if (x != 0) {
    		  nvPoint = clustersPoints[x-1][y-1] ;
    		  vPoint  = clustersPoints[x-1][y] ;
    		  if ((vPoint == 0) && (nvPoint == 0) && (nPoint==0) && (nePoint==0)) {
    			  nLabels++;
    			  clustersPoints[x][y] = nLabels;
    			  labels[nLabels] = Integer.MIN_VALUE;
    		  } else {
    			  if (vPoint != 0) {
    				  clustersPoints[x][y] = vPoint;
    				  if (nvPoint != 0) {
    					  if (nvPoint != vPoint) {
    						  labels[nvPoint] = vPoint;
    					  }
    					  if ((nPoint == 0) && (nePoint != 0) && (nvPoint != nePoint) && (labels[nvPoint] != nePoint)) {
    						  labels[nePoint] = nvPoint;
    					  }
    				  } else {
    					  if (nPoint != 0) {
    						  if (vPoint != nPoint) {
    							  labels[nPoint] = vPoint;
    						  }
    					  } else {
    						  if ((nePoint != 0) && (nePoint != vPoint) && (labels[vPoint] != nePoint)) {
    							  labels[nePoint] = vPoint;
    						  }
    					  }
    				  }
    			  } else {	
    				  if (nvPoint != 0) {
    					  clustersPoints[x][y] = nvPoint;
    					  if ((nPoint == 0) && (nePoint != 0) && (nePoint != nvPoint) && (labels[nvPoint] != nePoint)) {
    						  labels[nePoint] = nvPoint;
    					  }
    				  } else {
    					  if (nPoint != 0) {
    						  clustersPoints[x][y] = nPoint;
    					  } else {
    						  if (nePoint != 0) {
    							  clustersPoints[x][y] = nePoint;
    						  }
    					  }
    				  }
    			  }
    		  }
    	  } else {
    		  if ((nPoint==0) && (nePoint==0)) {
    			  nLabels++;
    			  clustersPoints[x][y] = nLabels;
    			  labels[nLabels] = Integer.MIN_VALUE;
    		  } else {
    			  if (nPoint != 0) {
    				  clustersPoints[x][y] = nPoint;
    			  } else {
    				  clustersPoints[x][y] = nePoint;
    			  }
    		  }
    	  }	
      }
    	
      /* a flag that shows where the array ends */
      labels[nLabels+1] = Integer.MAX_VALUE; 
      return nLabels;
    }

   /**
	* find the representative labels of all clusters that would result 
	* after filtering  
	*/
	public void findMainLabels() {
	    clusters = new int[nLabels + 2];
	    nClusters = 0;
	    
	    /* only one label */
	    if (nLabels == 1) {
	    	nClusters++;
	    	clusters[nClusters] = 1;
	    	clusters[nClusters + 1] = Integer.MAX_VALUE;
	    } else {
	    	boolean reached[] = new boolean[nLabels+1];
	    	int j,i;
	      
	    	for (i=1; labels[i] != Integer.MAX_VALUE ; i++) {
	    		if (labels[i] != Integer.MIN_VALUE) {
	    			for (int k = 1; k < reached.length; k++) {
	    				reached[k] = false;
	    			}
	    		  
	    			reached[i] = true;
	    			j = i;
	    		  
	    			while (labels[j] != Integer.MIN_VALUE) {
	    				j = labels[j];
	    				if (reached[j]) {
	    					labels[i] = Integer.MIN_VALUE;
	    					break;
	    				} else {
	    					reached[j] = true;
	    				}
	    			}
	    			labels[i] = j;
	    		} else {
	    			nClusters++;
	    			clusters[nClusters] = i;
	    		}
	    	}
	      
	    	for (i = 1; labels[i] != Integer.MAX_VALUE ; i++) {
	    		if (labels[i] == Integer.MIN_VALUE) {
	    			labels[i] = i;
	    		}
	    	}
	    	
	    	clusters[nClusters + 1] = Integer.MAX_VALUE;
	    }
	 }

   /**
	* find the center of the clusters 
	* @param clusters - the array of clusters 
	* @param limit    - the threshold for cluster size
	* @param width    - image width
	* @param height   - image height
	* @return
	*/
	private int[][] findClustersCentersCoordinates(int clusters[],double limit, int width,int height) {
		/* only one cluster */
		if (nClusters == 1) {
			int clusterCenterX = 0;
			int clusterCenterY = 0;
			int nrPoints = 0;
			
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (clustersPoints[x][y] != 0) {
						clusterCenterX += x;
						clusterCenterY += y;
						nrPoints++;
					}
				}
			}
            /* if the cluster size is larger than threshold */
			if (nrPoints > limit) {
				int centers[][] = new int[nClusters+2][2];
				centers[1][0] = (int)clusterCenterX / nrPoints;
				centers[1][1] = (int)clusterCenterY / nrPoints;
				centers[nClusters+1][0] = Integer.MAX_VALUE;
				return centers;
			} else {
				return null;
			}
		} else {
	    	long clusterCenterX[] = new long[nClusters + 2];
	    	long clusterCenterY[] = new long[nClusters + 2];
	    	nrPoints = new int[nClusters + 2];
	    	indecies = new int[nLabels + 2];
	    	int index; 
	    	int clusterArea;
	    	Vector<Integer> vClusters = new Vector<Integer>();
	      
	    	for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++) {
	    		indecies[clusters[i]] = i;
	    	} for (int y = 0; y < height; y++) {
	    		for (int x = 0; x < width; x++) {
	    			if (clustersPoints[x][y] != 0) {
	    				index = indecies[labels[clustersPoints[x][y]]];
	    				clusterCenterX[index] += x;
	    				clusterCenterY[index] += y;
	    				nrPoints[index]++;
	    			}
	    		}
	    	}
	    	for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++) {
	    		clusterArea = nrPoints[indecies[clusters[i]]];
	    		if (clusterArea > limit) {
	    			vClusters.add(new Integer(clusters[i]));
	    		} else {
	    			nClusters--;
	    		}
	    	}
	        if ( nClusters != 0 ) {
	    		for (int i = 0; i < vClusters.size(); i++) {
	    			clusters[i + 1] = ((Integer) (vClusters.get(i))).intValue();
	    		}
	    		clusters[nClusters + 1] = Integer.MAX_VALUE;
	    		int centers[][] = new int[nClusters + 2][2];

	    		for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++) {
	    			index = indecies[clusters[i]];
	    			centers[i][0] = (int) (clusterCenterX[index] / nrPoints[index]);
	    			centers[i][1] = (int) (clusterCenterY[index] / nrPoints[index]);
	    		}
	    		centers[nClusters + 1][0] = Integer.MAX_VALUE;
	    		return centers;
	    	} else {
	    		return null;
	    	}
	    }
	}

   /**
    * return the center of the solitary cluster 
	* @param centerX - cluster center x
	* @param centerY - cluster center y
	* @param nPoints - number of points from cluster
	* @param start   - y start coordinate 
	* @param end     - y end coordinate 
	* @param width   - region width
	* @param limit   - threshold for cluster size
	* @return
    */
	private Point solitaryClusterCenterCoordinate(int centerX,int centerY,int nrPoints,int start,
	                                              int end,int width,double limit) {
        /* look in the specified region */
		for (int y = start; y < end; y++) {
			for (int x = 0; x < width; x++) {
				if (clustersPoints[x][y] != 0) {
					centerX += x;
					centerY += y;
					nrPoints++;
				}
			}
	    }
		/* if cluster size is larger than specified threshold */
		if (nrPoints > (double) limit / 2d) {
			Point center = new Point((int) (centerX / nrPoints), (int) (centerY / nrPoints));
			return center;
	    } else {
	    	return null;
	    }
	}

   /**
    * find the center of multiple clusters 
	* @param clusters   - cluster array
	* @param start      - y start coordinate 
	* @param end        - y end coordinate 
	* @param width      - region width
	* @param limit      - cluster threshold
	* @param grayPixels - image pixel in gray scale format
	* @param x0         - x offset for gray scale image
	* @param y0         - y offset for gray scale image 
	* @param fWidth     - frame width 
	* @return
	*/
	private Point multipleClustersCenterCoordinates(int clusters[],int start,int end,int width,
										double limit,int grayPixels[],int x0,int y0,int fWidth) {
		long centerX[]  = new long[nClusters + 2];
	    long centerY[]  = new long[nClusters + 2];
	    long colors[]   = new long[nClusters + 2];
	    int nrPoints[]  = new int[nClusters + 2];
	    int indecies[]  = new int[nLabels + 2];
	    int index = 0;
	    int clusterArea;
	    int darkestPixel = 256;
	    int darkestX = 0;
	    int darkestY = 0;
	    int clusterCenterX, clusterCenterY;
	    double distance = 0;
	    double max = Double.NEGATIVE_INFINITY;
	    double ratio;
	    
	    for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++) {
	    	indecies[clusters[i]] = i;
	    }
	    
	    for (int y = start; y < end; y++) {
	    	for (int x = 0; x < width; x++) {
	    		if (clustersPoints[x][y] != 0) {
	    			index = indecies[labels[clustersPoints[x][y]]];
	    			centerX[index] += x;
	    			centerY[index] += y;
	    			nrPoints[index]++;
	    			int pixelColor = grayPixels[ (y + y0) * fWidth + (x + x0)];
	    			colors[index] += pixelColor;
	    			if (pixelColor < darkestPixel) {
	    				darkestPixel = pixelColor;
	    				darkestX = x;
	    				darkestY = y;
	    			}
	    		}
	    	}
	    }
	    
	    index = Integer.MIN_VALUE;
	    for (int i = 1; clusters[i] != Integer.MAX_VALUE; i++) {
	    	clusterArea = nrPoints[indecies[clusters[i]]];
	    	if (clusterArea > (double) limit / 2d) {
	    		clusterCenterX = (int) (centerX[indecies[clusters[i]]] / 
	    								nrPoints[indecies[clusters[i]]]);
	    		clusterCenterY = (int) (centerY[indecies[clusters[i]]] / 
	    								nrPoints[indecies[clusters[i]]]);
	    		
	    		distance = Math.sqrt(Math.pow( (clusterCenterX - darkestX), 2) + 
	    				             Math.pow( (clusterCenterY - darkestY), 2));
	    		/* look for the largest,darkest,and closest to the darkest pixel, cluster */
	    		ratio = clusterArea / (colors[indecies[clusters[i]]] * distance); 
	    		if (ratio > max) {
	    			max = ratio;
	    			index = indecies[clusters[i]];
	    		}
	    	}
	    }
	    if (index != Integer.MIN_VALUE) {
	    	Point center = new Point((int) (centerX[index] / nrPoints[index]),
                    				 (int) (centerY[index] / nrPoints[index]));
	    	return center;
	    } else {
	    	return null;
	    }
	}

   /**
    * after the SSR filter center was found , the eyes regions from S1 and S3 sector
    * will be thresholded by 128 value . All points resulted from this operation will be
    * grouped in clusters whose centers are returned by this method.
	* @param x0
	* @param y0
	* @param clusters
	* @param limit
	* @param width
	* @param height
	* @param grayPixels
	* @param fWidth
	* @return
	*/
	public Point findEyesClustersCenters(int x0,int y0,int clusters[],double limit,int width,
										 int height,int grayPixels[],int fWidth) {
		/* only one cluster */
		if (nClusters == 1) {
			int clusterCenterX = 0;
			int clusterCenterY = 0;
			int nrPoints = 0;
			 
			/* look in the upper half */
			Point center = solitaryClusterCenterCoordinate(clusterCenterX,clusterCenterY,
												nrPoints,(int)height/2,height,width,limit);
			if (center != null) {
				return center;
			} else {
				clusterCenterX = 0;
				clusterCenterY = 0;
				nrPoints = 0; 
				return solitaryClusterCenterCoordinate(clusterCenterX,clusterCenterY,
												nrPoints,0,(int)height/2,width,limit);
			}
		} else { 
			/* look in the lower half */
			Point center = multipleClustersCenterCoordinates(clusters,(int)height/2,height,
													  width,limit,grayPixels,x0,y0,fWidth);
			if (center != null) {
				return center;
			} else {
				return multipleClustersCenterCoordinates(clusters,0,(int)height/2,
													  width,limit,grayPixels,x0,y0,fWidth);
			}
		}
	}

   /**
    * create the clusters and find their centers 
	* @param limit  - threshold for clusters size
	* @param width  - image width
	* @param height - image height 
	* @return
	*/
	public int[][] processClusters(double limit,int width,int height) {
		findMainLabels();
	    return findClustersCentersCoordinates(clusters,limit,width,height);
	}
}
