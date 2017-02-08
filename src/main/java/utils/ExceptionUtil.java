package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by CJ on 2/8/2017.
 */
public class ExceptionUtil {

    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
