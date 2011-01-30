/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.actions;

import java.awt.event.*;
import main.MainWindow;

public class EnableMouseAction implements ActionListener {
	MainWindow mWnd = null;
	
	public EnableMouseAction(MainWindow mWnd) {
		this.mWnd = mWnd;
	}
	
	public void actionPerformed(ActionEvent e) {
		mWnd.enableMouseAction(e);
	}
}
