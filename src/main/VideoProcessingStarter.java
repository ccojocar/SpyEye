/* 
 * This class starts the video processing 
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
import javax.media.control.TrackControl;
import javax.media.protocol.*;
import javax.swing.*;

public class VideoProcessingStarter implements ControllerListener {
	
	Processor processor;
	boolean stateChanged=true;
	Object stateMutex = new Object();
	
	public VideoProcessingStarter() {}
	
	/**
	 * this method starts video player and starts the processing 
	 * @param ds   - data source
	 * @param cdi  - video device informations
	 * @param mWnd - parent window
	 * @return processor instance
	 */
	public Processor start(MainWindow wnd , DataSource ds , CaptureDeviceInfo cdi) {
		try {
			processor = Manager.createProcessor(ds);
		} catch(Exception e) {
		    JOptionPane.showMessageDialog(wnd ,
		    							  e.getMessage(),
		    							  null ,
		    							  JOptionPane.ERROR_MESSAGE);
		    System.exit(-1);
		}
		
		processor.addControllerListener(this);
		
		processor.configure();
	    if(!wait(Processor.Configured)) {
	    	JOptionPane.showMessageDialog(wnd ,
	    								  "Configuration error",
	    								  null ,
	    								  JOptionPane.ERROR_MESSAGE);
	    	System.exit(-1);
	    }
	    
	    /* use it as a player */
	    processor.setContentDescriptor(null);
	    
	    TrackControl vTrackcontrols[] = processor.getTrackControls();
	    
	    if (vTrackcontrols == null) {
	    	JOptionPane.showMessageDialog(wnd ,
	    			                      "Can't get the processor control",
	    			                      null ,
	    			                      JOptionPane.ERROR_MESSAGE);
	    	System.exit(-1);
	    }
	    
	    /* gets only the video trak from all tracks */
	    TrackControl videoTracker = null;
	    for (int i=0; i < vTrackcontrols.length; i++) {
	    	if (vTrackcontrols[i].getFormat() instanceof VideoFormat) {
	    		videoTracker =vTrackcontrols[i];
	    		break;
	    	}
	    }
	    
	    if (videoTracker == null) {
	    	JOptionPane.showMessageDialog( wnd ,
	    			                      "Can't get the processor control" ,
	    			                      null ,
	    			                      JOptionPane.ERROR_MESSAGE);
	    	System.exit(-1);
	    }
	    
	    try
	    {
	    	Codec codec[]={ new VideoProcessing(cdi,wnd.svmModel,wnd) };
	        videoTracker.setCodecChain(codec);  
	    } catch (UnsupportedPlugInException e) {
	    	JOptionPane.showMessageDialog(wnd,
	    								  e.getMessage(),
	    								  null,
	    								  JOptionPane.ERROR_MESSAGE);
	    	System.exit(-1);
	    }
	    
	    /* build the processor */
	    processor.prefetch();
	    if (!wait(Controller.Prefetched)) {
	    	JOptionPane.showMessageDialog(wnd ,
	    								  "An error occurred during the video processor cretion",
	    			                      null ,
	    			                      JOptionPane.ERROR_MESSAGE);
	    	System.exit(-1);
	    }
	    
	    processor.start();
	    
	    return processor;
	}
	
	/**
	 * this method blocks the thread until the processor will reach the 
	 * specified state
	 * @param state  - private state 
	 * @return true for successful transition or false 
	 * for failed transition
	 */
	boolean wait( int state) {
		synchronized(stateMutex) {
			try {
				while(processor.getState()!= state && stateChanged) {
					stateMutex.wait();
				}
			} catch(Exception e) {}
		}
		return stateChanged;
	}
	
	/**
	 *  this method checks the states of the processor and notifies
	 *  the waiting thread
	 */
	public void controllerUpdate(ControllerEvent e) {
		if (e instanceof ConfigureCompleteEvent ||
		   e instanceof RealizeCompleteEvent   ||
		   e instanceof PrefetchCompleteEvent) {
			synchronized(stateMutex) {
				stateChanged = true;
				stateMutex.notifyAll();
			}
		} else {
			if (e instanceof ResourceUnavailableEvent) {
				synchronized(stateMutex) {
					stateChanged=false;
					stateMutex.notifyAll();
				}
			} else {
				if (e instanceof EndOfMediaEvent) {
				   processor.close();
				   System.exit(0);
				}
			}
		}
	}
}
