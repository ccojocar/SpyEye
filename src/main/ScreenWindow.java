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
import javax.swing.*;

public class ScreenWindow extends JFrame {

	private static final long serialVersionUID = -4459948438233721277L;
	
	public ScreenWindow(Component videoScreen) throws Exception {
		this.setSize(new Dimension(320,240));
		this.setUndecorated(true);
		this.getContentPane().add(videoScreen);
	}
}
