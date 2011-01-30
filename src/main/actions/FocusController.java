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

public class FocusController implements WindowFocusListener {
	private MainWindow wnd;
	
	public FocusController(MainWindow wnd) {
		this.wnd = wnd;
	}
	
	public void windowGainedFocus(WindowEvent e) {
	    wnd.gotFocus(e);
	}
	
	public void windowLostFocus(WindowEvent e) {
	    wnd.lostFocus(e);
	}
}
