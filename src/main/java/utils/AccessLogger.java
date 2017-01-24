package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccessLogger {

private static Logger logger;
	
	public static Logger getInstance(){
		if(logger == null){
			logger = LogManager.getLogger("access");
		}
		return logger;
	}
	
}
