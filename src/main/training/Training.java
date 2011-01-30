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
import java.io.*;
import java.util.*;

public class Training {
	private Vector<svm_node[]> vNodes  = new Vector<svm_node[]>();
	private Vector<Double> vLabels = new Vector<Double>();
	private int offsetDirection = 0;
	
	/**
	 * extract data from a private pgm image file and eye coordinates file
	 * @param pgmImgFilePath  - pgm image file path 
	 * @param eyeFilePath     - eyes coordinate file path
	 * @param width           - image width
	 * @param height          - image height  
	 * @param label           - image label (true face or false face)
	 * @param falseFaceOffset - offset used for training with false faces
	 * @throws IOException
	 */
	public void update(String pgmImgFilePath, String eyeFilePath,
					   int width, int height, double label, 
					   int falseFaceOffset) throws IOException {
		svm_node nodes[];
		int xOffset=0;
		int yOffset=0;
		
		/* get eyes coordinates */
		EyesCoordinatesReader eyesReader= new EyesCoordinatesReader(eyeFilePath);
		int coordinates[]=eyesReader.getCoordinates();
		
		/* extract face template*/
		BytePixmap p= new BytePixmap(pgmImgFilePath);
		int[] pixels = new int[p.size];
		for (int i = 0; i < pixels.length; i++)
	    	pixels[i] = Pixmap.intValue(p.data[i]); 
	    
		
		TrainingDataManager mng= new TrainingDataManager(width,height,pixels);
		
		/* set offset for false faces on 4 directions */ 
		if (label == -1) {
			switch (offsetDirection) {
				case 0: {
					xOffset=-falseFaceOffset;
					yOffset=0;
					offsetDirection=1;
					break;
				}
				case 1: {
					xOffset=0;
					yOffset=falseFaceOffset;
					offsetDirection=2;
					break;
				}
				case 2: {
					xOffset=falseFaceOffset;
					yOffset=0;
					offsetDirection=3;
					break;
				}
				case 3: {
					xOffset=0;
					yOffset=-falseFaceOffset;
					offsetDirection=0;
					break;
				}
				default : {
					xOffset=0;
					yOffset=0;
					offsetDirection=0;
				}
			}
		}
	    
	    /* extract training data in SVM format */
	    nodes = mng.getTemplatePixelsInSVMFormat(coordinates[0] + xOffset,coordinates[1] + yOffset,
	    			coordinates[2] + xOffset,coordinates[3] + yOffset);
	    vNodes.add(nodes);
	    vLabels.add(label);
	}
	
	/**
	 * this method prepares data for training and starts 
	 * the svm training 
	 * @param modelFileName - svm model file path
	 * @throws IOException
	 */
	public void run(String modelFileName) throws IOException {
		svm_node nodes[][] = new svm_node[vNodes.size()][];
		double labels[] = new double[vLabels.size()];
		
		for (int i = 0; i < vNodes.size(); i++) {
			nodes[i]=(svm_node[])vNodes.elementAt(i);
		}
		
		for (int i=0; i<vLabels.size(); i++) {
			labels[i]=(double)vLabels.elementAt(i);
		}
		
		/* send data to SVM for training */
		SvmTraining trainer = new SvmTraining();
		trainer.training(modelFileName, nodes, labels);
	}
}
