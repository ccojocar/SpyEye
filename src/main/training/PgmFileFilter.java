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

public class PgmFileFilter implements FilenameFilter {
	
	/** 
	 * accept only images in pgm format
	 */
	public boolean accept(File directory , String name) {
		if(name.endsWith(".pgm"))
			return true;
		else
			return false;
	}
}
