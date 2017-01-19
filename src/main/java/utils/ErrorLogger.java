package utils;

import org.apache.logging.log4j.LogManager;

import app.SimpleWebServer;

public class ErrorLogger {

	private static org.apache.logging.log4j.Logger logger;
	
	
	public static org.apache.logging.log4j.Logger getInstance(){
		if(logger == null){
			logger = LogManager.getLogger("error");
		}
		return logger;
	}
	
}
