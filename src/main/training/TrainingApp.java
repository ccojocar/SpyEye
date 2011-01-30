/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.training;

import java.io.*;

public class TrainingApp {
	   
	/**
	 * the entry point of training application 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			if (args.length != 1) {
				throw new IOException("invalid images path");
			}
			
			String imagesPath=args[0];
			
			File file = new File(imagesPath);
			File directory;
			if(file.isDirectory()) {
				directory = new File(file.getCanonicalPath());
			} else {
				throw new IOException(file.toString( ) + " is not a directory");
			}
			
			String filesList[] = directory.list(new PgmFileFilter());
			Training trainer = new Training();
			System.out.println("True faces data ...");
			
			/* update true faces data */
			for(int i = 0; i < filesList.length; i++) {
				String absImageFilePath=imagesPath+filesList[i];
				String absEyesFilePath=imagesPath+filesList[i].replaceAll(".pgm", ".eye");
				System.out.println(absImageFilePath);
				System.out.println(absEyesFilePath);
				trainer.update(absImageFilePath, absEyesFilePath,384,286,1,0);
			}
			
			System.out.println("False faces data ...");
			
			/* update false faces data */
			for(int i = 0; i < filesList.length; i++) {
				String absImageFilePath=imagesPath+filesList[i];
				String absEyesFilePath=imagesPath+filesList[i].replaceAll(".pgm", ".eye");
				
				System.out.println(absImageFilePath);
				System.out.println(absEyesFilePath);
				
				/* for 4 directions */
				trainer.update(absImageFilePath, absEyesFilePath, 384, 286, -1,50);
				trainer.update(absImageFilePath, absEyesFilePath, 384, 286, -1,50);
				trainer.update(absImageFilePath, absEyesFilePath, 384, 286, -1,50);
				trainer.update(absImageFilePath, absEyesFilePath, 384, 286, -1,50);
			}
			
			System.out.println("Start training ...");
			
			/* start svm training */
			trainer.run("SvmModel.eye");
			
			System.out.println("End training.");
		} catch (IOException e) {
			System.err.println(e);
		}
	}	 
}
