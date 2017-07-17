package com.dashboard.budget.DAO;

import java.io.File;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.Reporter;

public class PicturesStorage {

	private static Logger logger = LoggerFactory.getLogger(Reporter.class);
	public final static PicturesStorage INSTANCE = new PicturesStorage();
	private Stack<File> picStack;

	private PicturesStorage() {
		picStack = new Stack<File>();
	}
	
	public int size(){
		return picStack.size();
	}
	
	public void addPicture(File file){
		picStack.push(file);		
		logger.info("Added new screenshot: {}", file.getName());
	}
	
	public Stack<File> getAllPictures(){		
		return picStack;
	}
}
