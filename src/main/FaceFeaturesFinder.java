/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */
package main;

import libsvm.*;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FaceFeaturesFinder {
	
	final   int frameWidth  = 320;
	final   int frameHeight = 240;
	private int labels[];
	private int binaryPixels[];
	public  int grayPixels[];
	public  int pixels[];
	private int s[][];
	private int ii[][];
	private int clustersPoints[][];
	private int eyes[][];
	private int numberOfFacesFound;
	private int nrClusters;
	private int beginIndex[];
	svm_model model;
	svm_node nodes[];
	private HoughTransform houghTransform;
	private int lEBrX0,lEBrY0;
	private int rEBrX0,rEBrY0;
	
   /**
    * class constructor for face detector
	*  @param model - svm model
    */
	public FaceFeaturesFinder(svm_model model) {
		pixels = new int[frameWidth * frameHeight];
	    grayPixels = new int[frameWidth * frameHeight];
	    binaryPixels = new int[frameWidth * frameHeight];
	    
	    s = new int[frameWidth][frameHeight];
	    ii = new int[frameWidth][frameHeight];
	    
	    clustersPoints = new int[frameWidth][frameHeight];
	    labels = new int[frameWidth * frameHeight];
	    
	    /* 21*35 template size used for training */
	    nodes = new svm_node[735];
	    this.model = model;
	    
	    /* the true faces begin from index 0 and are followed by false faces */
	    beginIndex = new int[model.nr_class];
	    beginIndex[0] = 0;
	    for (int i = 1; i < model.nr_class; i++) {
	    	beginIndex[i] = beginIndex[i - 1] + model.nSV[i - 1];
	    }
	    
	    /* hough transform set up*/
	    houghTransform = new HoughTransform();
		houghTransform.setLineColor(Color.red);
		houghTransform.setLocalPeakNeighbourhood(8);
	}

   /**
	* detect face's features : eyes coordinates  and nose tip coordinate
	* @param image  - source image
	* @return
	*/
	public int[] findFaceFeatures(Image image) {
	    /* grab image pixels */
	    pixels = ImageProcessing.getPixels(image,0,0,frameWidth, frameHeight, pixels);
	    
	    /* convert pixels to gray scale format */
	    grayPixels = ImageProcessing.toGrayscale(pixels, grayPixels);
	    
	    /* make image binarization */
	    binaryPixels = ImageProcessing.toThresholdedImage(grayPixels,binaryPixels,128);
	    ii = ImageProcessing.calculateIntegralImage(frameWidth, frameHeight, grayPixels, s, ii);
	    
	    numberOfFacesFound = 0;
	    int coordinates[];
	    
	    /* apply first filter */
	    SSRFilter filter1 = new SSRFilter(84, 54, ii);
	    coordinates = findFeaturesCoordinates(filter1);
	    
	    /* if no results found , apply second filter a little bit small for reduced size faces */
	    if (numberOfFacesFound == 0) {
	      SSRFilter filter2 = new SSRFilter(60, 36, ii);
	      coordinates = findFeaturesCoordinates(filter2);
	    }
	    
	    return coordinates;
	}

	/**
	 * this method detect face features using the given filter 
	 * @param filter - a SSR filter
	 * @return
	 */
	private int[] findFeaturesCoordinates(SSRFilter filter) {
		int absXFilterCenter, absYFilterCenter;
		int nLabels = 0;
		
		for (int i = 0; i < frameWidth; i++) {
			for (int j = 0; j < frameHeight; j++) {
				clustersPoints[i][j] = 0;
			}
		}
		
		/* build the clusters manager */
	    ClustersManager clustersManager = new ClustersManager(labels, clustersPoints);
	    
	    /* apply the SSR Filter */
	    for (int y = 0; y < frameHeight - filter.getHeight(); y++) {
	    	for (int x = 0; x < frameWidth - filter.getWidth(); x++) {
	    		absXFilterCenter = x + (filter.getWidth()  / 2) + 1; 
	    		absYFilterCenter = y + (filter.getHeight() / 2) + 1;
	    		
	    		if (filter.isValidFaceCodition(x, y) &&
	    			ImageProcessing.isSkinPixel(pixels[absYFilterCenter * frameWidth + absXFilterCenter])) {
	    			nLabels = clustersManager.findPixelLabel(absXFilterCenter,absYFilterCenter,frameWidth);
	    		}
	    	}
	    }
	    
	    /* there are face candidates */
	    if (nLabels != 0) {
	    	/* find root labels, and their centers */
	    	int centers[][] = clustersManager.processClusters(filter.getArea()/48d,
	    			frameWidth,frameHeight);
	    	
	    	/* are valid clusters */
	    	if (centers != null) {
	    		/* find left and right eyes coordinates for each cluster found */
	    		nrClusters = clustersManager.getClustersNumber();
	    		int nrPossibleFaces = findEyesOfPossibleFaces(filter,centers);
	    		
	    		if (nrPossibleFaces != -1) {
	    			/* load templates into an array in order to classify them */
	    			Vector<svm_node[]> templates = loadEyesTemplates(nrPossibleFaces);

	    			/* classifying templates */
	    			double clasfResults[] = classifyTemplates(templates);

	    			/* find the face's template with the highest score */
	    			int EyesCoordinates[] = findBestEyesFitting(clasfResults);
	    			
	    			/* find nose's top */
	    			if (EyesCoordinates != null) {	
	    				Point noseTip = findNoseTop(EyesCoordinates);
	    				if (noseTip != null) {
	    					int coordinates[] = new int[6];
	    					coordinates[0] = EyesCoordinates[0];
	    					coordinates[1] = EyesCoordinates[1];
	    					coordinates[2] = EyesCoordinates[2];
	    					coordinates[3] = EyesCoordinates[3];
	    					coordinates[4] = (int)noseTip.getX();
	    					coordinates[5] = (int)noseTip.getY();
	    					return coordinates;
	    				}
	    			}
	    		}
	    	}
	    }
	    
	    return null;
	}

   /**
    * will find the eye's pupil center
	* @param pupilsMembers - eye candidates 
	* @param x0            - region x offset 
	* @param y0            - region y offset
	* @param width         - region width 
	* @param height        - region height
	* @param labels        - labels of clusters array
	* @param limit         - clusters area threshold
	* @return
	*/
	private  Point findEye(int eyePoints[][],int x0, int y0,int width,int height,int labels[],int limit)
	{
	    int nLabel = 0; 
	    
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		eyePoints[x][y] = 0;
	    	}
	    }
	    
	    /* build the clusters manager */
	    ClustersManager clustersManager = new ClustersManager(labels,eyePoints);
        
	    /*  binarize the sector and look for the appropriate clusters */
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		if (binaryPixels[ (y + y0) * frameWidth + (x + x0)] == 1) {
	    			nLabel = clustersManager.findPixelLabel(x, y, width);	
	    		}
	    	}
	    }
	    
	    if (nLabel != 0) {
	    	clustersManager.findMainLabels();
	    	return clustersManager.findEyesClustersCenters(x0,y0,clustersManager.clusters,limit,
	    			width,height,grayPixels,frameWidth);
	    } else {
	    	return null;
	    }
	}

   /**
	* find the pupil coordinates  
	* @param filter   - private SSR filter
	* @param centers  - centers of all clusters 
	* @return
	*/
	private int findEyesOfPossibleFaces(SSRFilter filter, int centers[][]) {
		eyes = new int[nrClusters][5];
		
	    /* size of one filter sector */
		int width  = filter.getWidth()/3;
	    int height = filter.getHeight()/2;
	    
	    int EyeAreaPoints[][] = new int[width][height];
	    int labels[] = new int[width*height];
	    int treshold = (int)filter.getArea()/(144*6);
	    
	    int nrPossibleFaces = -1;
	    Point le,re;
	    int x, y;
	    
	    /* loop through all clusters */
	    for (int i = 1; centers[i][0] != Integer.MAX_VALUE; i++) {
	    	x = centers[i][0];
	    	y = centers[i][1];
	      
	    	/* find left pupil in sector s1 of SSR filter */
	    	le = findEye(EyeAreaPoints,x - (filter.getWidth()  / 2),y - (filter.getHeight() / 2),width,height ,labels ,treshold);
	    	
	    	if ( le == null)
	    		continue;

	    	/* find right pupil in sector s3 of SSR filter */
	    	re = findEye(EyeAreaPoints,x + (filter.getWidth() / 6),y - (filter.getHeight() / 2),width ,height ,labels ,treshold);
	    	
	    	if (re == null)
	    		continue;

	    	nrPossibleFaces++;
	    	eyes[nrPossibleFaces][0] = (int)le.getX() + x - (filter.getWidth()  / 2);
	    	eyes[nrPossibleFaces][1] = (int)le.getY() + y - (filter.getHeight() / 2);
	    	eyes[nrPossibleFaces][2] = (int)re.getX() + x + (filter.getWidth()  / 6);
	    	eyes[nrPossibleFaces][3] = (int)re.getY() + y - (filter.getHeight() / 2);
	    	eyes[nrPossibleFaces][4] = i;
	    }
	    
	    return nrPossibleFaces;
	}

   /**
	* extract between the eyes template from a gray scale image
	* @param x0 - left eye x coordinate
	* @param y0 - left eye y coordinate
	* @param x1 - right eye x coordinate
	* @param y1 - right eye y coordinate
	* @return - the svm nodes for this template
	*/
	public svm_node[] extractEyesTemplate(int x0,int y0,int x1,int y1) {
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
	     * */
	    xRes = (double)xDistance / 23d; 
	    yRes = (double)yDistance / 23d;
	    
	    /* scale factor */
	    scaleFactor = Math.sqrt(Math.pow(xDistance,2)+Math.pow(yDistance,2))/23d;
	    
	    /* 6 from (35 - 23)/2 and 8 because the eyes are on the 8 row from experiments*/
	    Ox = x0 - (int)(6*xRes) + (int)(8*yRes);
	    offsetY = (yDistance != 0 ? (8*yRes*xDistance) / yDistance : 8*scaleFactor);
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
	    		if ( (currentX >= 0) && (currentX < frameWidth) && 
	    			 (currentY < frameHeight) && (currentY >= 0) ) {
	    			/* convert data in the svm format */
	    			node.value = grayPixels[currentY * frameWidth + currentX] / 255d;
	    		} else {
	    			if (y == 0) {
	    				node.value = 128d / 255d;
	    			} else {
	    				node.value = template[ (y - 1) * 35 + x].value;
	    			}
	    		}
	    		template[y * 35 + x] = node;
	    		currentX = startX+(int)((x+1)*xRes);
	    		currentY = startY+(int)((x+1)*yRes);
	    	}
	    	startX = Ox - (int)((y+1)*yRes);
	    	startY = Oy + (int)((y+1)*xRes);
	    }
	    
	    return template;
	  }

	/**
	* extract all between the eyes templates  
	* @param nrOfPossibleFaces - number of templates
	* @return
	*/
	private Vector<svm_node[]> loadEyesTemplates(int nrOfPossibleFaces) {
		svm_node[] template;
		Vector<svm_node[]> templates = new Vector<svm_node[]>();

		/* extract the templates and convert them in the svm format */
		for (int i = 0; i <= nrOfPossibleFaces; i++) {
			template = extractEyesTemplate(eyes[i][0], eyes[i][1],
	                                       eyes[i][2], eyes[i][3]);
			templates.add(template);
		}
		return templates;
	}

   /**
	* check all templates using the SVM pattern recognition 
	* @param templates - BTE templates 
	* @return
	*/
	public double[] classifyTemplates(Vector<svm_node[]> templates) {
		double   clasfResults[] = new double[templates.size()];
		double   predictResult[] = new double[1];
		
		for (int i = 0; i < templates.size(); i++) {
			svm.svm_predict_values(model, (svm_node[]) templates.get(i),predictResult, beginIndex);
			clasfResults[i] = predictResult[0] * model.label[0];
	    }
	    return clasfResults;
	}

	/**
	 * detect the most representative cluster candidate
	 * the max result
	 * @param cResults - the classification results 
	 * @return
	 */
	private int[] findBestEyesFitting(double clasfResults[]) {
		double max = 0d;
	    int index = 0;

	    for (int i=0; i < clasfResults.length; i++) {
	    	if( clasfResults[i] > max ) {
	    		max = clasfResults[i];
	    	    index = i;
	    	}
	    }

	    if (max > 0) {
	    	numberOfFacesFound++;
	    	int eyesCoordinates[] = new int[4];
	    	eyesCoordinates[0] = eyes[index][0];
	    	eyesCoordinates[1] = eyes[index][1];
	    	eyesCoordinates[2] = eyes[index][2];
	    	eyesCoordinates[3] = eyes[index][3];
	    	return eyesCoordinates;
	    } else {
	    	return null;
	    }
	}

	/**
	 * find all points that compose the nose bridge
	 * @param length - region length
	 * @param ii     - integral image
	 * @return
	 */
	private Point[] findNoseBridgeLine(int length,int ii[][]) {
		int max;
		int noseBridgeX = 0;
		int nrPoints = 0;
		
		/* the 3 segments rectangular filter */
	    SSRFilter filter = new SSRFilter(length/2,1,ii);
	    Point nbCoordinates[] = new Point[length];

	    for (int y=0; y < length; y++) {
	    	max = Integer.MIN_VALUE;
	    	for (int x=0; x < length-filter.getWidth(); x++) {
	    		int s2 = filter.getNoseBridgeFilterValue(x,y);
	    		
	    		/* get only the max value onto horizontal */
	    		if (s2 > max) {
	    			max = s2;
	    			noseBridgeX = x + 1 + (filter.getWidth() / 2);
	    		}
	    	}
	    	if (max != Integer.MIN_VALUE) {
	    		nrPoints++;
	    		/* save x coordinate and the filter value */ 
	    		nbCoordinates[y] = new Point(noseBridgeX, max);
	    	}
	    }
	    if (nrPoints > ( length /5 )) {
	    	return nbCoordinates;
	    } else {
	    	return null;
	    }
	}

   /**
	* Detect the first derivative by calculating the gradient between 
	* tow nearest points, in order to find the nose's tip. 
	* @param candidates  - the points that made up the nose bridge 
	* @param length      - the region length 
	* @return
	*/
	private int[] calculateGradiants(Point nbPoints[],int length) {
		int gradiants[] = new int[length];
		int index1=0;
		int index2=0;
		Point point1,point2; 
		
		for (int i = 0; i < gradiants.length; i++) {
			gradiants[i] = Integer.MAX_VALUE;
		}
		
		while ((index1+1 < length) && (index2+1 < length)) {
			index1 = index2;
			while ((nbPoints[index1]==null) && (index1+1 < length)) {
				index1++;
			}
			
			if (index1 +1 < length) {
				index2 = index1+1;
				while ((nbPoints[index2]==null) && (index2+1 < length)) {
					index2++;
				}
			} else {
				break;
			}
			
			point1=nbPoints[index1];
			point2=nbPoints[index2];
			
			if ((point1 != null) && (point2 != null)) {
				gradiants[index2] = (int)(point2.getY() - point1.getY());
			}
		}
		return gradiants;
	}
	
	/**
	* Find the coordinate of the nose peak coordinates 
	* @param face - eyes coordinates 
	* @return
	 * @throws IOException 
	*/
	private Point findNoseTop(int eyesCoordinates[]) {
		/* the eyes' coordinates */
		int lx,ly;
		int rx,ry;
		
		int xDistance,yDistance;
		int distance;
		
		int step;
		double slope;
		
		int startX,startY;
		int currentX,currentY;
	    
	    lx = eyesCoordinates[0];
	    ly = eyesCoordinates[1];
	    rx = eyesCoordinates[2];
	    ry = eyesCoordinates[3];
	    
	    xDistance = rx - lx;
	    yDistance = ry - ly;
	    
	    /* the distance between the eyes */
	    distance = (int)Math.sqrt(Math.pow(xDistance,2) + Math.pow(yDistance,2));
	    step  = Math.abs(yDistance != 0 ? xDistance/yDistance : xDistance);
	    step  = ( step < 3 ? 3 : step );
	    slope = ( yDistance < 0 ? -1 : 1);
	    
	    /* the region of interest for nose finding*/
	    int ROI[] = new int[distance*distance];
	    
	    /* extract the region of interest */
	    startX = lx;
	    startY = ly;
	    for (int y = 0; y < distance; y++) {
	    	currentX = startX;
	    	currentY = startY;
	    	for (int x = 0; x < distance; x++) {
	    		if ((currentX >= 0) && (currentX < frameWidth) && (currentY < frameHeight)) {
	    			ROI[y * distance + x] = grayPixels[currentY * frameWidth + currentX];
	    		} else {
	    			ROI[y * distance + x] = ROI[(y-1)*distance + x];
	    		}
	    		currentX++;
	    		if ((x + 1) % step == 0) {
	    			currentY = (int) (currentY + slope);
	    		}
	    	}
	    	
	    	if ((y + 1) % step == 0) {
	    		startX = (int) (startX - slope);
	    	}
	    	startY++;
	    }
		
	    /* the integral image of ROI */
	    int ii[][] = new int[distance][distance];
	    
	    /* the cumulative sum of ROI */
	    int s[][] = new int[distance][distance];
	    
	    /* calculate integral image */
	    ii = ImageProcessing.calculateIntegralImage(distance,distance,ROI,s,ii);
	    
	    /* find the nose bridge points */
	    Point nbPoints[] = findNoseBridgeLine(distance,ii); 
	    
	    if (nbPoints != null) {
	    	/* calculate gradient */
	    	int gradiants[] = calculateGradiants(nbPoints, distance);
	    	
	    	int min[] = new int[4];
	    	int minIndices[] = new int[4];
	    	for(int i=0 ; i < 4 ; i++) {
	    		min[i] = Integer.MAX_VALUE;
	    		minIndices[i] = 0;
	    	}
	    	
	    	/* split the nose bridge in 4 regions and find the local minimum */ 
	    	for (int i = 0; i < gradiants.length/4; i++) {
	    		if (gradiants[i] < min[0]) {
	    			min[0] = gradiants[i];
	    			minIndices[0] = i;
	    		}
	    	}
	    	
	    	for (int i = gradiants.length/4; i < gradiants.length/2; i++) {
	    		if (gradiants[i] < min[1]) {
	    			min[1] = gradiants[i];
	    			minIndices[1] = i;
	    		}
	    	}
	    	
	    	for (int i = gradiants.length/2; i < 3 * gradiants.length/4; i++) {
	    		if (gradiants[i] < min[2]) {
	    			min[2] = gradiants[i];
	    			minIndices[2] = i;
	    		}
	    	}
	    	
	    	for (int i = 3*gradiants.length/4; i < gradiants.length; i++) {
	    		if (gradiants[i] < min[3]) {
	    			min[3] = gradiants[i];
	    			minIndices[3] = i;
	    		}
	    	}
	    	
	    	int min1 = Math.min(min[0], min[1]);
	    	int min2 = Math.min(min[2], min[3]);
	    	
	    	int minIndex1;
	    	if (min1 == min[0]) {
	    		minIndex1=minIndices[0];
	    	} else {
	    		minIndex1=minIndices[1];
	    	}
	    	
	    	int minIndex2;
	    	if (min2 == min[2]) {
	    		minIndex2=minIndices[2];
	    	} else {
	    		minIndex2=minIndices[3];
	    	}
	    	
	    	int minIndex;
	    	if (Math.min(min1, min2) == min1) {
	    		minIndex=minIndex1;
	    	} else {
	    		minIndex=minIndex2;
	    	}
	    	
	    	int startIndex;
	    	if (minIndex >= gradiants.length/2) {
	    		if (minIndex < 3 * gradiants.length / 4) {
	    			startIndex = gradiants.length / 2;
	    		} else {
	    			startIndex = 3 * gradiants.length / 4;
	    		}
	    	} else {
	    		startIndex = 0;
	    	}
	    	
	    	/* after finding the smallest gradient (nose thrills) find the largest
	    	 * gradient above it (nose tip)
	    	 */
	    	int max = 0;
	    	int nosePeakIndex = 0;
	    	for(int i=startIndex ; i <= minIndex ; i++) {
	    		if ((gradiants[i] > max) && (gradiants[i] != Integer.MAX_VALUE)) {
	    			max = gradiants[i];
	    			nosePeakIndex = i;
	    		}
	    	}
	    	
	    	if (nbPoints[nosePeakIndex] == null)
	    		return null;
	      
	    	Point nosePeak = new Point((int)nbPoints[nosePeakIndex].getX(),nosePeakIndex);
	    	
	    	/* rotate the ROI to its original state */
	    	slope = (double)yDistance / (double)xDistance;
	    	double angle = Math.atan(slope); 
	    	double x = Math.cos(angle)*nosePeak.getX() - Math.sin(angle)*nosePeak.getY();
	    	double y = Math.sin(angle)*nosePeak.getX() + Math.cos(angle)*nosePeak.getY();
	    	
	    	x += eyesCoordinates[0];
	    	y += eyesCoordinates[1];
	    	nosePeak.setLocation(x,y);
	    	return nosePeak;
	    }
	    return null;
	}
	
	/**
	 * this method extracts the eye region and then converts them 
	 * in the gray scale format 
	 * @param eyeType   - the eye type (left = 0, right = 1)
	 * @param lX        - left eye x
	 * @param lY        - left eye y
	 * @param rX        - right eye x
	 * @param rY        - right eye y
	 * @param eyesDistance  - the distance between the eyes
	 * @param image     - frame 
	 * @param slope     - the slope of line between the eyes
	 * @param threshold - the binary threshold
	 * @return the thresholded image
	 */
	private BufferedImage getEybrowROI(int eyeType,int lX , int lY , int rX , int rY,
							double eyesDistance,Image image,double slope,int threshold) {
		int startX,startY;
		int endX,endY;
		int x0,y0;
		
		/* left eye */
		if (eyeType ==0) {
			x0=lX;
			y0=lY;
		} else { /* right eye */
			x0=rX;
			y0=rY;
		}
		
		/* these ratios have been empirically discovered */
		startX = x0 - (int)(eyesDistance/4d);
		endX   = startX + (int)((eyesDistance/2d) * Math.cos(Math.atan(slope)));
		endY   = y0 - 15;
		startY = endY - (int)((eyesDistance/4d) * Math.sin(Math.atan(slope)));
		
		int width  = endX - startX;
		int height = endY - startY;
		
		if (height < 20) {
			startY -= 5;
			  endY += 5;
			 height = endY - startY;
		}
		
		if ( width == 0) {
			startX -= 5;
			  endX += 5;
			 width = endX - startX;
		}
		
		if (eyeType ==0) {
			lEBrX0=startX;
			lEBrY0=startY;
		} else {
			rEBrX0=startX;
			rEBrY0=startY;
		}
		
		int ebPixels[] = new int [width*height];
		int ebPixGrayFormat[] = new int [width*height];
		
		ebPixels = ImageProcessing.getPixels(image, startX, startY, width, height, ebPixels);
		ebPixGrayFormat = ImageProcessing.toGrayscale(ebPixels, ebPixGrayFormat); 
		return ImageProcessing.toNegativeThresholdedImage(ebPixGrayFormat, threshold, width, height);
	}
	 
   /**
	 * this method finds the coordinates of the left and the right eyebrow
	 * @param eyeType   - the eye type (left = 0, right = 1) 
	 * @param lX        - left eye x
	 * @param lY        - left eye y
	 * @param rX        - right eye x
	 * @param rY        - right eye y
	 * @param eyesDist  - the distance between the eyes
	 * @param image     - frame 
	 * @param slope     - the slope of line between the eyes
	 * @param threshold - binary threshold
	 * @return eyebrow coordinates 
	 */
	public int[] getEyebrow(int eyeType,int lX , int lY , int rX , int rY,double eyesDistance,
			                Image image,double slope,int treshold) {
		BufferedImage eyebrowRegion = getEybrowROI(eyeType,lX , lY , rX , rY, eyesDistance,
												   image,slope,treshold);
		
		if (eyebrowRegion == null) {
			return null;
		}
		
		BufferedImage tmpEyebrow = new BufferedImage(eyebrowRegion.getWidth(),
				eyebrowRegion.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		houghTransform.run(eyebrowRegion);
		eyebrowRegion = houghTransform.getSuperimposedImage(tmpEyebrow, 1d);
		
		boolean foundLine = false;
		int x0 = 0, y0 = 0, x1 = 0, y1 = 0;
		
		for (int x=0; x < eyebrowRegion.getWidth()/2 && !foundLine; x++) {
			for (int y=0; y< eyebrowRegion.getHeight(); y++) {
				if(eyebrowRegion.getRGB(x,y) != Color.black.getRGB()) {
					x0=x;
					y0=y;
					foundLine = true;
					break;
				}
			}
		}
		if (!foundLine)
			return null;
		
		foundLine = false;
		for (int x = eyebrowRegion.getWidth()-1; x >= eyebrowRegion.getWidth()/2 && !foundLine; x--) {
			for (int y = eyebrowRegion.getHeight()-1; y >= 0; y--) {
				if (eyebrowRegion.getRGB(x, y) != Color.black.getRGB()) {
					x1=x;
					y1=y;
					foundLine=true;
					break;
				}
			}
		}
		if(!foundLine)
			return null;
		
		
		int eyebrowCoordinates[] = new int [6];
		eyebrowCoordinates[0]=x0;
		eyebrowCoordinates[1]=y0;
		eyebrowCoordinates[2]=x1;
		eyebrowCoordinates[3]=y1;
		
		/* left eye */
		if(eyeType == 0) {
			eyebrowCoordinates[4]=this.lEBrX0;
			eyebrowCoordinates[5]=this.lEBrY0;
		} else { /* right eye */
			eyebrowCoordinates[4]=this.rEBrX0;
			eyebrowCoordinates[5]=this.rEBrY0;
		}
		return eyebrowCoordinates;
	}
	
	/**
	 * This method detects the mouth's y coordinate using y-Profile 
	 * @param pix - the mouth's ROI 
	 * @param y0  - start y 
	 * @param width - the mouth's ROI width
	 * @param height - the mouth's ROI height
	 * @return
	 */
	public int getMouthY(int pix[],int y0, int width ,int height) {
		int minSum = Integer.MAX_VALUE;
		int currentSum=0;
		int minY=0;
		
		for (int y=0; y < height; y++) {
			for (int x=0; x<width; x++) {
				currentSum+=pix[y*width+x];
			}	
			if (currentSum < minSum) {
				minSum = currentSum;
				minY = y;
			}
			currentSum=0;
		}
		return minY + y0;
	}
	
}
