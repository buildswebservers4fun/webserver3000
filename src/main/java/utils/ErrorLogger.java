package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorLogger {
	private static Logger logger;
	
	public static Logger getInstance(){
		if(logger == null){
			logger = LogManager.getLogger("error");
		}
		return logger;
	}
}
