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
import java.awt.event.*;
import javax.swing.*;
import javax.media.*;
import javax.media.protocol.*;

import libsvm.*;
import main.actions.*;

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	public boolean startDetection = false;
	public boolean startTracking = false;
	public boolean mouseEnable = false;
	public svm_model svmModel;
	
	Processor processor;
    PushBufferDataSource pbds;
    CaptureDeviceInfo cdi;
    Component controlComponent,visualComponent;
    ScreenWindow videoScreen;
    BorderLayout borderLayout = new BorderLayout();
    JPanel  mainPanel;
    JButton faceRecog = new JButton();
    JButton enableMouse = new JButton();
    JButton refresh = new JButton();
    JButton settings = new JButton();
    JLabel  playerInfo = new JLabel();
    
    public MainWindow(LoadingWindow wnd) {
    	enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    	try {
    		wnd.setText("Loading svm model ...");
    		wnd.setProgress(0);
    		initControls();
    		svmModel = svm.svm_load_model("data/SvmModel.eye");
    		wnd.setText("Looking for video camera ...");
    		wnd.setProgress(50);
    		findVideoCamera();
    		wnd.setProgress(100);
    		wnd.setVisible(false);
    		this.getContentPane().setCursor(Cursor.getDefaultCursor());
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * this method initiates the main window visuals elements  
     * @throws Exception
     */
    private void initControls() throws Exception {
    	mainPanel = (JPanel)this.getContentPane();
    	mainPanel.setLayout(null);
    	this.setResizable(false);
    	this.setSize(new Dimension(495,275));
    	this.setTitle("Real time face's features detection and tracking");
    	this.addWindowFocusListener(new FocusController(this));
    	faceRecog.setBounds(335,15,150,25);
    	faceRecog.setText("find face's features");
    	faceRecog.addActionListener(new FindFeaturesAction(this));
    	mainPanel.add(faceRecog);
    	
    	enableMouse.setBounds(335,45,150,25);
    	enableMouse.setText("enable mouse");
    	enableMouse.setEnabled(false);
    	enableMouse.setForeground(Color.darkGray);
    	enableMouse.addActionListener(new EnableMouseAction(this));
    	mainPanel.add(enableMouse);
    	
    	refresh.setBounds(335,75,150,25);
    	refresh.setText("refresh screen");
    	refresh.addActionListener(new RefreshAction(this));
    	mainPanel.add(refresh);
    	
    	playerInfo.setText("video player controls");
    	playerInfo.setBounds(360,150,150,25);
    	mainPanel.add(playerInfo);
    }
    
    /**
     * looking for video device and makes all the preparations for it  
     * after that  starts the video capture 
     */
    private void findVideoCamera() {
    	DevicesScanner ds = new DevicesScanner();
    	pbds = ds.scanDevices(this);
    	if (pbds != null) {
    		VideoProcessingStarter vps = new VideoProcessingStarter();
    	    processor=vps.start(this,pbds, cdi);
    	    visualComponent = processor.getVisualComponent();
    	    visualComponent.setLocation(5,5);
    	    visualComponent.setSize(new Dimension(320,240));
    	    this.getContentPane().add(visualComponent);
    	    controlComponent = processor.getControlPanelComponent();
    	    controlComponent.setLocation(393,180);
    	    controlComponent.setSize(35,20);
    	    this.getContentPane().add(controlComponent);
    	 }
    }
    
    /**
     *  process main window events
     */
    protected void processWindowEvent(WindowEvent e) {
    	super.processWindowEvent(e);
    	if(e.getID() == WindowEvent.WINDOW_CLOSING) {
    		exitPreparation();
    	}
    }
    
    /**
     * 	Closes video camera, and removes video device controls
     */
    private void exitPreparation() {
    	if (processor != null) {
    		processor.stop();
    		processor.close();
    		this.remove(visualComponent);
    		this.remove(controlComponent);
    	}
    	System.exit(0);
    }
    
    /**
     * start face recognition
     * @param e
     */
    public void faceFeaturesDetectionAction(ActionEvent e) {
    	startDetection=!startDetection;
    	enableMouse.setEnabled(true);
    }
    
    /**
     * this method enable mouse control with eyes and nose
     * @param e
     */
    public void enableMouseAction(ActionEvent e) {
    	mouseEnable=!mouseEnable;
    	if (mouseEnable) {
    		try {
    			videoScreen = new ScreenWindow(visualComponent);
    			videoScreen.setLocation(0 , 0);
    			videoScreen.setVisible(true);
    			this.setState(Frame.ICONIFIED);
    		} catch (Exception ex) {}
    	}
    }
    
    /**
     * refresh the web camera connection 
     * @param e - action event
     */
    public void refreshAction(ActionEvent e) {
    	this.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	enableMouse.setEnabled(false);
    	this.startTracking=false;
    	this.startDetection=false;
    	this.mouseEnable=false;
    	if (processor != null) {
    		processor.stop();
    		processor.close();
    		this.remove(controlComponent);
    		this.remove(visualComponent);
    		findVideoCamera();
    	}
    	this.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void gotFocus(WindowEvent e) {
    	this.mouseEnable=false;
    	this.startTracking=false;
    	this.startDetection=false;
    	enableMouse.setEnabled(false);
    	if (videoScreen != null) {
    		videoScreen.setVisible(false);
    		this.getContentPane().add(visualComponent);
    	}
    }
    
    public void lostFocus(WindowEvent e) {}
}
