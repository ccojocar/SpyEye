/* Open a video capture device and start grabbing the video frames 
 * 
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main;

import javax.media.*;
import javax.media.protocol.*;
import javax.media.control.FormatControl;
import javax.media.format.VideoFormat;
import javax.swing.JOptionPane;
import java.awt.*;
import java.util.Vector;

public class DevicesScanner implements BufferTransferHandler {
    
	public DevicesScanner() {}
    
    /**
     * transfer data empty handler 
     */
    public void transferData(PushBufferStream pbs) {}
    
    /**
     * this method scans for active video devices
     * 
     * @param frame
     * @return
     */
    public PushBufferDataSource scanDevices( MainWindow mWnd) {
    	PushBufferDataSource pbds = null;
    	
    	/* the video capture format */
    	String encoding = "RGB";
    	float fps = 30.0f;
    	Dimension size = new Dimension(320,240);
    	
    	VideoFormat videoFormat = new VideoFormat(encoding, size,
    			                   Format.NOT_SPECIFIED, null, fps);
    	
    	Vector devices = CaptureDeviceManager.getDeviceList(videoFormat);
    	
    	if (devices.size() < 1) {
    		JOptionPane.showMessageDialog( mWnd, 
    		                               "No capture devices found\n" ,
    		                               videoFormat.toString() ,
    		                               JOptionPane.ERROR_MESSAGE );
    		System.exit(-1);
    	}
    	
    	CaptureDeviceInfo cdi =(CaptureDeviceInfo)devices.elementAt(0);
    	mWnd.cdi = cdi;
    	
    	try {
    		DataSource ds = Manager.createDataSource(cdi.getLocator());
    		if (ds instanceof CaptureDevice) {
    			FormatControl[] vFc = ((CaptureDevice) ds).getFormatControls();
    			for (int i=0; i < cdi.getFormats().length; i++) {
    				VideoFormat vf = (VideoFormat)cdi.getFormats()[i];
    				if (vf.matches(videoFormat)) {
    					videoFormat = (VideoFormat)vf.intersects(videoFormat);
    					vFc[0].setFormat(videoFormat);
    					break;
    				}
    			}
    		}
    		
    		ds.connect();
    		
    		pbds =(PushBufferDataSource)ds;
    		pbds.getStreams()[0].setTransferHandler(this);
    		pbds.start();
    		return pbds;
    	} catch (Exception e) {
    		JOptionPane.showMessageDialog(mWnd ,
    			  						"Could not connect to video capture device. Please check " +
    			  						"your web camera ",
    			  						null,
    			  						JOptionPane.ERROR_MESSAGE);
    		System.exit(-1);
    		return null;
    	}
    }
}
