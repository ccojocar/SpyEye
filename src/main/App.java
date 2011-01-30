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
import javax.swing.UIManager;

public class App {
	public App() {
		LoadingWindow loadingWnd = new LoadingWindow();
		loadingWnd.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		loadingWnd.setVisible(true);
		
		MainWindow wnd = new MainWindow(loadingWnd);
		wnd.validate();
		
		/* center the window */
		Dimension wndSize = wnd.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if(wndSize.height > screenSize.height) {
			wndSize.height = screenSize.height;
		} if(wndSize.width > screenSize.width) {
			wndSize.width = screenSize.width;
		}
		wnd.setLocation((screenSize.width  - wndSize.width ) / 2, 
				        (screenSize.height - wndSize.height) / 2);
		wnd.setVisible(true);
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch( Exception e) {
			e.printStackTrace();
		}
		new App();
	}
}