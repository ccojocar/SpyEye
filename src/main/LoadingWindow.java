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

public class LoadingWindow extends JFrame {
	
	private static final long serialVersionUID = 4111536248601708079L;
	protected JPanel panel = new JPanel();
	protected JLabel lbText= new JLabel();
	protected JProgressBar progress = new JProgressBar();
	
	public LoadingWindow() {
		this.setSize(290,75);
		this.setResizable(false);
		this.setUndecorated(true);
		
		panel = (JPanel)this.getContentPane();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createEtchedBorder());
		
		lbText.setBounds(20,10,250,25);
		panel.add(lbText);
	
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setBounds(20,40,250,20);
		progress.setStringPainted(true);
		panel.add(progress);
		
		Dimension size = this.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width  - size.width ) / 2, 
		        		 (screenSize.height - size.height) / 2 );
	}
	
	/**
	 * set info text
	 * @param text - given text
	 */
	public void setText(String text) {
		lbText.setText(text);
	}
	
	/**
	 * set progress bar value
	 * @param value - progress value
	 */
	public void setProgress(int value) {
		progress.setValue(value);
	}
}
