/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main;

import javax.media.*;
import javax.media.format.*;
import javax.media.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import libsvm.*;

public class VideoProcessing implements Effect {
	/* codec attributes */
	Format inFormat;
	Format outFormat;
	Format formats[];
	byte outData[];

	/* main window */
	MainWindow wnd;

	/* detect face's features */
	FaceFeaturesFinder faceFeaturesFinder ;
	
	/* traking manager */
	FaceFeaturesTrailer faceFeaturesTrailer ;

	/* left eye coordinates  */
	int leX,leY ;
	
	/* right eye coordinates */
	int reX,reY;
	
	/* nose peak coordinates */
	int noseX,noseY;
	
	/* "between the eyes" coordinates is in the middle of distance between
	 * eyes
	 */
	int bteX,bteY;

	final int eyebrowTreshold = 140;

	/* search area width and height */
	final int searchWidth = 25;
	final int searchHeight = 21;

	/* features template width and height */
	final int templateWidth = 15;
	final int templateHeight = 11;

	/* convert current frame to image */
	BufferToImage bti;
	Image image,previousImage;

	/* nose shift x and y */
	int noseShiftX,noseShiftY;
	/* bte shift x and y */
	int bteShiftX,bteShiftY;

	/* nose and bte zone */
	int noseTemplate[][];
	int bteTemplate[][];
	int leftEyeTemplate[][];
	int rightEyeTemplate[][];

	/* color and gray scale pixels from nose top and
	 * between the eyes zone
	 */
	int colorNosePix[],colorBtePix[];
	int grayNosePix[],grayBtePix[];

	/* left and right eyebrow coordinates */
	int lEBr[],rEbr[];

	/* distance between left/right eye and BTE */
	int xDistBetweenLEyeAndBTE,yDistBetweenLEyeAndBTE;
	int xDistBetweenREyeAndBTE,yDistBetweenREyeAndBTE;

	/* pixels from eyes regions */
	int prevLEyePix[], currentLEyePix[];
	int prevREyePix[], currentREyePix[];

	/* integral eye's image regions */
	int lEyeII[][], lEyeSum[][];
	int rEyeII[][], rEyeSum[][];

	/* control mouse pointer */
	Robot mouse;
	long leftEyeBlinkTime=0;
	long rightEyeBlinkTime=0;

	public VideoProcessing(CaptureDeviceInfo cdi, svm_model model, MainWindow wnd) {
		formats = cdi.getFormats();
		outData = new byte[240*320*3];
		this.wnd = wnd;

		noseTemplate     = new int [templateWidth][templateHeight];
		bteTemplate      = new int [templateWidth][templateHeight];
		leftEyeTemplate  = new int [templateWidth][templateHeight];
		rightEyeTemplate = new int [templateWidth][templateHeight];
		colorNosePix     = new int [searchWidth*searchHeight];
		colorBtePix      = new int [searchWidth*searchHeight];
		grayNosePix      = new int [searchWidth*searchHeight];
		grayBtePix       = new int [searchWidth*searchHeight];
		prevLEyePix      = new int [searchWidth*searchHeight];
		currentLEyePix   = new int [searchWidth*searchHeight];
		prevREyePix      = new int [searchWidth*searchHeight];
		currentREyePix   = new int [searchWidth*searchHeight];
		lEyeII           = new int [searchWidth][searchHeight];
		rEyeII           = new int [searchWidth][searchHeight];
		lEyeSum          = new int [searchWidth][searchHeight];
		rEyeSum          = new int [searchWidth][searchHeight];

		faceFeaturesFinder = new FaceFeaturesFinder(model);
		faceFeaturesTrailer = new FaceFeaturesTrailer();
		try {
			mouse = new Robot();
		} catch(Exception e){}
	}

	/**
	 * this method processes the frames one by one to find the face's features 
	 */
	public int process(Buffer inBuffer , Buffer outBuffer) {
		outData = (byte[])inBuffer.getData();

		if (wnd.startTracking) {
			/* keep previous frame */
			previousImage = image;
			image =  bti.createImage(inBuffer);

			/* keep old coordinates from previous frame */
			int prevNoseX   = noseX;
			int prevNoseY   = noseY;
			int prevBteX    = bteX;
			int prevBteY    = bteY;

			/* stay inside of frame boundaries */
			noseShiftX = faceFeaturesTrailer.checkHorizontalFrontier(noseX, noseShiftX, searchWidth, 319);
			noseShiftY = faceFeaturesTrailer.checkVerticalFrontier(noseY, noseShiftY, searchHeight, 239);
			bteShiftX  = faceFeaturesTrailer.checkHorizontalFrontier(bteX, bteShiftX, searchWidth, 319);
			bteShiftY  = faceFeaturesTrailer.checkVerticalFrontier(bteY, bteShiftY, searchHeight, 239);

			/* extract nose ROI */
			colorNosePix = ImageProcessing.getPixels(image, noseX+noseShiftX-searchWidth/2,
						   noseY+noseShiftY-searchHeight/2, searchWidth, searchHeight,colorNosePix);
			grayNosePix  = ImageProcessing.toGrayscale(colorNosePix, grayNosePix);

			/* extract BTE ROI */
			colorBtePix = ImageProcessing.getPixels(image, bteX+bteShiftX-searchWidth/2,
						   bteY+bteShiftY-searchHeight/2, searchWidth, searchHeight,colorNosePix);
			grayBtePix  = ImageProcessing.toGrayscale(colorBtePix, grayBtePix);

			/* find new nose and BTE position */
			Point NosePoint,BtePoint;
			NosePoint = faceFeaturesTrailer.templateMatching(noseTemplate, grayNosePix, templateWidth, templateHeight,
						searchWidth, searchHeight);
			BtePoint  = faceFeaturesTrailer.templateMatching(bteTemplate, grayBtePix, templateWidth, templateHeight,
						searchWidth, searchHeight);

			/* update nose coordinates */
			noseX   = noseX + noseShiftX - searchWidth/2 + (int)NosePoint.getX();
			noseY   = noseY + noseShiftY - searchHeight/2 + (int)NosePoint.getY();

			/* update between the eyes coordinates */
			bteX = bteX + bteShiftX - searchWidth/2 + (int)BtePoint.getX();
		    bteY = bteY + bteShiftY - searchHeight/2 + (int)BtePoint.getY();

		    /* stay inside of frame boundaries */
		    xDistBetweenLEyeAndBTE = faceFeaturesTrailer.checkHorizontalFrontier(bteX, xDistBetweenLEyeAndBTE , searchWidth, 319);
		    yDistBetweenLEyeAndBTE = faceFeaturesTrailer.checkVerticalFrontier(bteY, yDistBetweenLEyeAndBTE, searchHeight, 239);
		    xDistBetweenREyeAndBTE = faceFeaturesTrailer.checkHorizontalFrontier(bteX, xDistBetweenREyeAndBTE,searchWidth, 319);
		    yDistBetweenREyeAndBTE = faceFeaturesTrailer.checkVerticalFrontier(bteY, yDistBetweenREyeAndBTE, searchHeight, 239);

		    int shiftedleX = bteX + xDistBetweenLEyeAndBTE;
		    int shiftedleY = bteY + yDistBetweenLEyeAndBTE;
		    int shiftedreX = bteX + xDistBetweenREyeAndBTE;
		    int shiftedreY = bteY + yDistBetweenREyeAndBTE;

		    double eyesDistance = Math.sqrt(Math.pow(shiftedreX-shiftedleX, 2)+
		    							    Math.pow(shiftedreY-shiftedleY, 2));

		    double slope = ((shiftedreX - shiftedleX)!= 0 ? (double)(shiftedreY-shiftedleY)/
														  (double)(shiftedreX-shiftedleX) : 0);

			/* detect left and right eyebrow */
			lEBr=faceFeaturesFinder.getEyebrow(0, shiftedleX, shiftedleY, shiftedreX, shiftedreY,
					eyesDistance, image, slope, eyebrowTreshold);
			rEbr=faceFeaturesFinder.getEyebrow(1, shiftedleX, shiftedleY, shiftedreX, shiftedreY,
					eyesDistance, image, slope, eyebrowTreshold);

			/*draw left eyebrow*/
			if(lEBr != null) {
				Drawer.drawLine(lEBr[0]+lEBr[4] ,lEBr[1]+lEBr[5],
								lEBr[2]+lEBr[4] ,lEBr[3]+lEBr[5],outData );

				/* left eye must be under eyebrow */
				int maxEbY = Math.max(lEBr[1], lEBr[3]);
				if (( bteY + yDistBetweenLEyeAndBTE - searchHeight/2 ) < (lEBr[5] + maxEbY - 5 )) {
					yDistBetweenLEyeAndBTE = (lEBr[5] + maxEbY -5) - (bteY - searchHeight/2);
				}
			}

			/* draw right eyebrow */
			if (rEbr != null) {
				Drawer.drawLine(rEbr[0]+rEbr[4] ,rEbr[1]+rEbr[5],
							    rEbr[2]+rEbr[4] ,rEbr[3]+rEbr[5],outData);

				/* right eye must be under eyebrow */
				int maxEbY = Math.max(rEbr[1], rEbr[3]);
				if (( bteY + yDistBetweenREyeAndBTE - searchHeight/2 ) < (rEbr[5] + maxEbY - 5)) {
					yDistBetweenREyeAndBTE = (rEbr[5] + maxEbY -5) - ( bteY - searchHeight/2);
				}
			}

			/* stay inside frame */
			yDistBetweenLEyeAndBTE = faceFeaturesTrailer.checkVerticalFrontier(bteY, yDistBetweenLEyeAndBTE, searchHeight, 239);
			yDistBetweenREyeAndBTE = faceFeaturesTrailer.checkVerticalFrontier(bteY, yDistBetweenREyeAndBTE, searchHeight, 239);

			int auxPix[] = new int [searchWidth*searchHeight];
		    /* extracts pixels for left eye */
			auxPix   = ImageProcessing.getPixels(previousImage,bteX+xDistBetweenLEyeAndBTE-searchWidth/2,bteY+yDistBetweenLEyeAndBTE-searchHeight/2,
												 searchWidth, searchHeight, auxPix);
		    prevLEyePix = ImageProcessing.toGrayscale(auxPix, prevLEyePix);
		    auxPix   = ImageProcessing.getPixels(image,bteX+xDistBetweenLEyeAndBTE-searchWidth/2,bteY+yDistBetweenLEyeAndBTE-searchHeight/2,
		    									 searchWidth, searchHeight, auxPix);
		    currentLEyePix = ImageProcessing.toGrayscale(auxPix, currentLEyePix);

		    /* extract pixels for right eye */
		    auxPix   = ImageProcessing.getPixels(previousImage,bteX+xDistBetweenREyeAndBTE-searchWidth/2,bteY+yDistBetweenREyeAndBTE-searchHeight/2,
		    									 searchWidth, searchHeight, auxPix);
		    prevREyePix = ImageProcessing.toGrayscale(auxPix, prevREyePix);
		    auxPix   = ImageProcessing.getPixels(image,bteX+xDistBetweenREyeAndBTE-searchWidth/2,bteY+yDistBetweenREyeAndBTE-searchHeight/2,
		    									 searchWidth, searchHeight, auxPix);
		    currentREyePix = ImageProcessing.toGrayscale(auxPix, currentREyePix);

		    /* temporareY offsets values of nose and BTE */
			noseShiftX= noseX - prevNoseX;
		    noseShiftY= noseY - prevNoseY;
		    bteShiftX = bteX - prevBteX;
		    bteShiftY = bteY - prevBteY;

		    /* check whether all offsets are less than 1 for discovering eyes motion*/
		    boolean isMotion =((Math.abs(noseShiftX)<1) && (Math.abs(noseShiftY)<1) &&
		    				    (Math.abs(bteShiftX)<1)  && (Math.abs(bteShiftY)<1)) ? true : false;

		    boolean bLeftMotion  = false;
		    boolean bRightMotion = false;

		    /* detect motions */
		    bLeftMotion  = faceFeaturesTrailer.checkMotions(currentLEyePix ,prevLEyePix ,searchWidth ,searchHeight,
		     		bteX+xDistBetweenLEyeAndBTE-searchWidth/2 ,bteY+yDistBetweenLEyeAndBTE-searchHeight/2 ,outData ,isMotion);
		    bRightMotion = faceFeaturesTrailer.checkMotions(currentREyePix ,prevREyePix ,searchWidth ,searchHeight,
		    		bteX+xDistBetweenREyeAndBTE-searchWidth/2 ,bteY+yDistBetweenREyeAndBTE-searchHeight/2 ,outData ,isMotion);

		    /* update coordinates only for still eyes */
		    boolean bDetectLeftEye  = true;
		    boolean bDetectRightEye = true;

		    if ((bLeftMotion)&&(bRightMotion)) {
		    	bDetectLeftEye =false;
		    	bDetectRightEye=false;
		    } else {
		    	if(bLeftMotion) {
		    		bDetectLeftEye=false;
		    		/* more than 500ms */
		    		if ((System.currentTimeMillis() - leftEyeBlinkTime) >= 500) {
		    			leftEyeBlinkTime = System.currentTimeMillis();
		    			if(wnd.mouseEnable) {
		    				mouse.mousePress(InputEvent.BUTTON1_MASK);
		    				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
		    			}
		    		}
		    	}

		    	if (bRightMotion) {
		    		bDetectRightEye=false;
		    		/* more than 500ms */
		    		if ((System.currentTimeMillis() - rightEyeBlinkTime) >= 500) {
		    			rightEyeBlinkTime = System.currentTimeMillis();
		    			if (wnd.mouseEnable) {
		    				mouse.mousePress(InputEvent.BUTTON1_MASK);
		    				mouse.mouseRelease(InputEvent.BUTTON1_MASK);
		    			}
		    		}
		    	}
		    }

		    /* detect left eye */
		    if (bDetectLeftEye) {
		    	lEyeII = ImageProcessing.calculateIntegralImage(searchWidth, searchHeight, currentLEyePix, lEyeSum,lEyeII);
		    	Point LEPoint = faceFeaturesTrailer.findEye( currentLEyePix,leftEyeTemplate, lEyeII,
		    			searchWidth,searchHeight,templateWidth,templateHeight);

		    	for (int y=0 ; y<templateHeight ; y++) {
		    		for(int x=0 ; x<templateWidth ; x++) {
		    			leftEyeTemplate[x][y]=currentLEyePix[((int)LEPoint.getY() - templateHeight/2 + y)*searchWidth +
		    			                                   ((int)LEPoint.getX() - templateWidth/2 + x)];
		    		}
		    	}

		    	leX = bteX + xDistBetweenLEyeAndBTE - searchWidth/2 + (int)LEPoint.getX();
		    	leY = bteY + yDistBetweenLEyeAndBTE - searchHeight/2 + (int)LEPoint.getY();
		    }

		    /* detect right eye */
		    if (bDetectRightEye) {
		    	rEyeII = ImageProcessing.calculateIntegralImage(searchWidth, searchHeight, currentREyePix, rEyeSum,rEyeII);
		    	Point REPoint = faceFeaturesTrailer.findEye( currentREyePix,rightEyeTemplate, rEyeII,
		    			searchWidth,searchHeight,templateWidth,templateHeight);

		    	for (int y = 0; y < templateHeight; y++) {
		    		for(int x=0 ; x<templateWidth ; x++) {
		    			rightEyeTemplate[x][y]=currentREyePix[((int)REPoint.getY() - templateHeight/2 + y)*searchWidth +
		    			                                    ((int)REPoint.getX() - templateWidth/2 + x)];
		    		}
		    	}

		    	reX = bteX + xDistBetweenREyeAndBTE - searchWidth/2 + (int)REPoint.getX();
		    	reY = bteY + yDistBetweenREyeAndBTE - searchHeight/2 + (int)REPoint.getY();
		    }

			svm_node[] svmTemplate;
			Vector<svm_node[]> vTemplate = new Vector<svm_node[]>();

			svmTemplate=faceFeaturesFinder.extractEyesTemplate(leX, leY, reX, reY);
			vTemplate.add(svmTemplate);
			double[] svmResult = faceFeaturesFinder.classifyTemplates(vTemplate); // check eyes position

			if (svmResult[0] >= 0) {
			    /* update the bte position */
			    eyesDistance = Math.sqrt(Math.pow(reX-leX, 2)+ Math.pow(reY-leY,2));
				slope =( (reX-leX)!= 0 ? (double)(reY-leY)/(double)(reX-leX) : 0 );
			    int tempBteX = leX + (int)((eyesDistance/2)*Math.cos(Math.atan(slope)));
			    int tempBteY = leY + (int)((eyesDistance/2)*Math.sin(Math.atan(slope)));

			    /* extract new bte from frame */
			    colorBtePix = ImageProcessing.getPixels(image, tempBteX-searchWidth/2, tempBteY-searchHeight/2,searchWidth, searchHeight,colorNosePix);
			    grayBtePix  = ImageProcessing.toGrayscale(colorBtePix, grayBtePix);

				BtePoint  = faceFeaturesTrailer.templateMatching(bteTemplate, grayBtePix, templateWidth, templateHeight, searchWidth, searchHeight);
				bteX  = tempBteX - searchWidth/2 + (int)BtePoint.getX();
				bteY  = tempBteY - searchHeight/2 + (int)BtePoint.getY();

				double intersPoint[] = new double[3];

				if (!Geometry.getLineLineIntersection(leX,leY,reX,reY,bteX,bteY,noseX,noseY,intersPoint)) {
					intersPoint[0] = bteX;
					intersPoint[1] = bteY;
				}

				/* correct nose peack position */
				double angle = Math.abs(Math.atan(slope));

				/* the distance between the BTE and nose peak sould be almost half
				 * of the distance between eyes
				 */
				int noseX0,noseY0;
				noseX0 = (int)intersPoint[0] + (int)( Math.sin(angle) *  eyesDistance/2d );
				noseY0 = (int)intersPoint[1] + (int)( Math.cos(angle) *  eyesDistance/2d );

				/* find the real postion */
				colorNosePix = ImageProcessing.getPixels(image, noseX0-searchWidth/2, noseY0-searchHeight/2, searchWidth, searchHeight,colorNosePix);
				grayNosePix  = ImageProcessing.toGrayscale(colorNosePix, grayNosePix);
				NosePoint = faceFeaturesTrailer.templateMatching(noseTemplate, grayNosePix, templateWidth, templateHeight, searchWidth, searchHeight);

				noseX  = noseX0 - searchWidth /2 + (int)NosePoint.getX();
				noseY  = noseY0 - searchHeight/2 + (int)NosePoint.getY();

				/* shift values are used to predict face's movements */
				noseShiftX= noseX - prevNoseX;
			    noseShiftY= noseY - prevNoseY;
			    bteShiftX = bteX - prevBteX;
			    bteShiftY = bteY - prevBteY;

			    /* distance between eyes and BTE */
			    xDistBetweenLEyeAndBTE = leX - bteX;
				yDistBetweenLEyeAndBTE = leY - bteY;
				xDistBetweenREyeAndBTE = reX - bteX;
				yDistBetweenREyeAndBTE = reY - bteY;

			    /* extract nose peak template */
				for (int y=0; y<templateHeight; y++) {
					for (int x=0; x<templateWidth; x++) {
						noseTemplate[x][y] =  grayNosePix[(y + (int)NosePoint.getY() - templateHeight/2)*searchWidth +
						                                  (x + (int)NosePoint.getX() - templateWidth/2)];
					}
				}

				/* extract BTE template */
				for (int y=0; y<templateHeight; y++) {
					for (int x=0 ; x<templateWidth ; x++) {
						bteTemplate[x][y] =  grayBtePix[(y + (int)BtePoint.getY() - templateHeight/2)*searchWidth +
						                                 (x + (int)BtePoint.getX() - templateWidth/2)];
					}
				}

				/* control mouse pointer with face features */
				if (wnd.mouseEnable) {
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

					int screenCentereX = screenSize.width/2;
					int screenCentereY = screenSize.height/2;

					/* relative to video screen window */
					int relativCentereX= 160 - noseX ;
					int relativCentereY= 120 - noseY ;

					int xScaleFactor = 20;
					int yScaleFactor = 20;

					mouse.mouseMove(screenCentereX - (relativCentereX*xScaleFactor),
									screenCentereY - (relativCentereY*yScaleFactor));
				}

				/* draw nose */
				Drawer.drawCircle(noseX ,noseY , 8 , outData);
	 		    Drawer.drawCenter(noseX ,noseY , 1 , outData);

				/* draw eyes */
				Drawer.drawCircle(leX ,leY , 8 , outData);
				Drawer.drawCircle(reX ,reY , 8 , outData);
		  	    Drawer.drawCenter(leX ,leY , 1 , outData);
				Drawer.drawCenter(reX ,reY , 1 , outData);

				/* extract mouth's pixels */
				int colorMouthPix[] = new int [25*(int)eyesDistance];
				int mouthPix[] = new int[25*(int)eyesDistance];

				colorMouthPix = ImageProcessing.getPixels(image,(int)(bteX-eyesDistance/2),(int)(bteY+eyesDistance-5),
				    										  (int)eyesDistance, 25 , colorMouthPix);
				mouthPix = ImageProcessing.toGrayscale(colorMouthPix,mouthPix);

				/* detect mouth's y coordinate using Y-Profile */
				int mouthY=faceFeaturesFinder.getMouthY(mouthPix, (int)(bteY+eyesDistance-5),
				    										(int)eyesDistance,25);
				/* draw mouth line */
				Drawer.drawLine((int)(bteX-eyesDistance/2),mouthY,(int)(bteX+eyesDistance/2),mouthY, outData);

			} else {
				wnd.startDetection = true;
				wnd.startTracking = false;
			}
		} else
		/* detection operation */
		if(wnd.startDetection) {
			/* convert frame to image */
			if (bti == null) {
				bti = new BufferToImage((VideoFormat)inBuffer.getFormat());
			}
			image = bti.createImage(inBuffer);

			/* find eyes and nose coordinate */
			int coordinates[] = faceFeaturesFinder.findFaceFeatures(image);

			/* if eyes and nose were found */
			if (coordinates != null) {
				wnd.startTracking=true;
				wnd.startDetection = false;

				/* extract eyes and nose coordinates */
				leX = coordinates[0];
				leY = coordinates[1];
				reX = coordinates[2];
				reY = coordinates[3];
				noseX = coordinates[4];
				noseY = coordinates[5];

				/* BTE coordinates is in the middle of distance between
				 * left and right eye*/
				int xDistance = reX - leX;
				int yDistance = reY - leY;
				double step = (yDistance == 0) ? 0 : ((double)xDistance / (double)yDistance) ;
				bteX = leX + xDistance/2;
				bteY = (step != 0) ? (leY + (int)((xDistance/2) / step )): leY ;

				noseShiftX = 0;
				noseShiftY = 0;
				bteShiftX  = 0;
				bteShiftY  = 0;

				/* relative distances */
				xDistBetweenLEyeAndBTE = leX - bteX;
				yDistBetweenLEyeAndBTE = leY - bteY;
				xDistBetweenREyeAndBTE = reX - bteX;
				yDistBetweenREyeAndBTE = reY - bteY;

				int grayPix[] = faceFeaturesFinder.grayPixels;
				int tempX,tempY;
				for (int y=0 ; y<templateHeight ; y++) {
					for(int x=0 ; x<templateWidth ; x++) {
						/* extract nose grayscale pixels */
						tempX = noseShiftX + noseX - templateWidth/2 + x;
						tempY = noseShiftY + noseY - templateHeight/2 + y;
						if ((tempX >= 0) && (tempX < 320) && (tempY >=0 ) && (tempY < 240)) {
							noseTemplate[x][y] = grayPix[tempY*320 + tempX];
						} else {
							noseTemplate[x][y] = 0;
						}

						/* extract BTE grayscale pixels */
						tempX = bteShiftX + bteX - templateWidth/2 + x;
						tempY = bteShiftY + bteY - templateHeight/2 + y;
						if ((tempX >= 0) && (tempX < 320) && (tempY >=0 ) && (tempY < 240)) {
							bteTemplate[x][y] = grayPix[tempY*320 + tempX];
						} else {
							bteTemplate[x][y] = 0;
						}

						/* extract left eye grayscale pixels */
						tempX = xDistBetweenLEyeAndBTE + leX - templateWidth/2 + x;
						tempY = xDistBetweenLEyeAndBTE + leY - templateHeight/2 + y;
						if ((tempX >= 0) && (tempX < 320) && (tempY >=0 ) && (tempY < 240)) {
							leftEyeTemplate[x][y] = grayPix[tempY*320 + tempX];
						} else {
							leftEyeTemplate[x][y] = 0;
						}

						/* extract right eye grayscale pixels */
						tempX = xDistBetweenREyeAndBTE + reX - templateWidth/2 + x;
						tempY = xDistBetweenREyeAndBTE + reY - templateHeight/2 + y;
						if ((tempX >= 0) && (tempX < 320) && (tempY >=0 ) && (tempY < 240)) {
							rightEyeTemplate[x][y] = grayPix[tempY*320 + tempX];
						} else {
							rightEyeTemplate[x][y] = 0;
						}
					}
				}
			}
		}

		outBuffer.setData(outData);

		/* copy input atributes to output */
		outBuffer.setFormat(inBuffer.getFormat());
		outBuffer.setLength(inBuffer.getLength());
		outBuffer.setOffset(inBuffer.getOffset());

		return BUFFER_PROCESSED_OK;
	}
	
	public void open() {}
	
	public void reset(){}
	
	public void close(){}
	
	public Format[] getSupportedInputFormats() {
		return formats;
	}

	/**
	 * this method returns supported output formats
	 */
	public Format[] getSupportedOutputFormats(Format input) {
		if (input == null) {
			System.out.print("get supported output formats for input == null");
			return formats;
		}
		if (matches(input,formats) != null) {
			return new Format[]{formats[0].intersects(input)};
		} else {
			return new Format[0];
		}
	}

	/**
	 * this method finds the output format likewise the input format
	 */
	private Format matches(Format in , Format out[]) {
		for (int i=0 ; i<out.length ; i++) {
			if (in.matches(out[i])) {
				return out[i];
			}
		}
		return null;
	}

	/**
	 * set input format
	 */
	public Format setInputFormat(Format in) {
		inFormat=in;
		return in;
	}

	/**
	 * set output format
	 */
	public Format setOutputFormat(Format out) {
		if ((out == null) || (matches(out,formats) ==null)) {
			return null;
		}

		RGBFormat incoming = (RGBFormat)out;
		Dimension size     = incoming.getSize();
		int maxDataLength  = incoming.getMaxDataLength();
		int lineStride     = incoming.getLineStride();
		float frameRate    = incoming.getFrameRate();
		int flipped        = incoming.getFlipped();

		if (size == null) {
			return null;
		}

		if (maxDataLength < (size.height * size.width *3)) {
			maxDataLength = size.height * size.width *3;
		}

		if (lineStride < (size.width *3)) {
			lineStride = size.width *3;
		}

		if (flipped != Format.FALSE) {
			flipped = Format.FALSE;
		}

		outFormat = formats[0].intersects(new RGBFormat(size,
				                                         maxDataLength,
				                                         null,
				                                         frameRate,
				                                         Format.NOT_SPECIFIED,
				                                         Format.NOT_SPECIFIED,
				                                         Format.NOT_SPECIFIED,
				                                         Format.NOT_SPECIFIED,
				                                         Format.NOT_SPECIFIED,
				                                         lineStride,
				                                         Format.NOT_SPECIFIED,
				                                         Format.NOT_SPECIFIED ));
		return outFormat;
	}

	public String getName() {
		return "Video Processing";
	}

	public Object getControl(String controlType) {
		return null;
	}

	public Object[] getControls() {
		return null;
	}
}
