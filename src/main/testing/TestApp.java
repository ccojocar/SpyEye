/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.testing;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.*;

import libsvm.*;
import main.training.*;
import main.*;

public class TestApp {

	public static void main(String[] args) {
		try {	
			svm_model svmModel = svm.svm_load_model("SvmModel.eye");
			FaceFeaturesFinder faceFeaturesFinder = new FaceFeaturesFinder(svmModel);
				
			if (args.length != 1) {
				throw new IOException("invalid images path");
			}
		
			String imagesPath=args[0] ;
			File file = new File(imagesPath);
			File directory;
			if (file.isDirectory()) {
				directory = new File(file.getCanonicalPath());
			} else {
				throw new IOException(file.toString( ) + " is not a directory");
			}	
			
			String filesList[] = directory.list(new PgmFileFilter());
			FileWriter fw = new FileWriter("test.cvs");
			
			for (int i=0; i<filesList.length; i++) {
				System.out.print(".");
				String absImageFilePath=imagesPath+filesList[i];
				String absEyesFilePath=imagesPath+filesList[i].replaceAll(".pgm", ".eye");
				ReadCoordinatesBioID rc = new ReadCoordinatesBioID(absEyesFilePath); 
				
				BytePixmap p = new BytePixmap(absImageFilePath);
				
				int[] pixels = new int[p.size];
				for (int j = 0; j < pixels.length; j++)
					pixels[j] = 0xFF000000 + Pixmap.intValue(p.data[j]) * 0x010101; 
				
				MemoryImageSource source = new MemoryImageSource(p.width,p.height,pixels, 0, p.width);
				Image img = Toolkit.getDefaultToolkit().createImage(source);
				
				int coordinates[] = faceFeaturesFinder.findFaceFeatures(img);
				
				if (coordinates != null) {	
					int c[]=rc.getCoord();
					if ((c != null)) {
						fw.write(coordinates[0]+" , "+coordinates[1]+" , "
							+coordinates[2]+" , "+coordinates[3]+" , "
							+c[0]+" , "+c[1]+" , "
							+c[2]+" , "+c[3]+"\n");
						
						fw.flush();
					}		
				}
			}
			fw.close(); 
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		System.out.println("End.");	
	}
}
