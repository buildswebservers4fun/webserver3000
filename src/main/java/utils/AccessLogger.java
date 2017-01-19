package utils;

import org.apache.logging.log4j.LogManager;

public class AccessLogger {

private static org.apache.logging.log4j.Logger logger;
	
	public static org.apache.logging.log4j.Logger getInstance(){
		if(logger == null){
			logger = LogManager.getLogger("access");
		}
		return logger;
	}
	
}
