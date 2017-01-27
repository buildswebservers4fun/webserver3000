package protocol;

/**
 * Created by CJ on 1/27/2017.
 */
public class ServerException extends Exception  {

    public ServerException() {
        super();
    }

    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable e) {
        super(message,e);
    }

    public ServerException(Throwable e) {
        super(e);
    }
}
