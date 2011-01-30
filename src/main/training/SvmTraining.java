/*
 * Copyright 2007   Cosmin Cojocar <cosmin.cojocar@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */

package main.training;

import libsvm.*;
import java.io.*;

public class SvmTraining {
	private svm_parameter param;
	private svm_problem prob;
	private svm_model model;
	
	/**
	 * class constructor
	 */
	public SvmTraining() {
		param = new svm_parameter();
		
		/* default values */
		param.svm_type=svm_parameter.C_SVC;
		param.kernel_type=svm_parameter.RBF;
		param.gamma=1.0/(35*21);
		param.cache_size=200;
		param.C = 1;
		param.eps=1e-3;
		param.shrinking=1;
		param.probability=0;
	}
	
	/**
	 * set training data extracted from every image
	 * @param label
	 * @param nodes
	 */
	private void setTrainingData(double label[], svm_node nodes[][]) {
		prob = new svm_problem();
		prob.l = label.length;
		prob.x = new svm_node[prob.l][];
		prob.y = new double[prob.l];
		for (int i = 0 ; i < prob.l; i++) {	
			prob.x[i]= nodes[i];
			prob.y[i]= label[i];
		}
	}
	
	/**
	 * start SVM training
	 * @param modelFileName - SVM model file path
	 * @param nodes  - all data used for training (in SVM format)
	 * @param label  - data labels
	 * @throws IOException
	 */
	public void training(String modelFileName, svm_node nodes[][], double label[])throws IOException {
		String errorMsg;
		setTrainingData(label,nodes);
		errorMsg = svm.svm_check_parameter(prob, param);
		if(errorMsg!=null) {
			System.err.println("Error:"+errorMsg);
			System.exit(1);
		}
		model = svm.svm_train(prob, param);
		svm.svm_save_model(modelFileName, model);
	}
}
