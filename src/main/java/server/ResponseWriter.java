package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import protocol.response.IHttpResponse;
import utils.ErrorLogger;

public class ResponseWriter implements Runnable {
    private PriorityBlockingQueue<ResponseAndSocket> responseQueue;
//    private Map<IHttpResponse, Socket> responseToSocket;
    
    public ResponseWriter() {
        super();
    }

    @Override
    public void run() {
        // requestQueue = new PriorityQueue<>(initialCapacity, comparator);
        responseQueue = new PriorityBlockingQueue<ResponseAndSocket>(500);
        ResponseAndSocket responseAndSocket;
        System.out.println("RW RUN");

        while (true) {
            // send request with highest priority
            try {
                responseAndSocket = responseQueue.poll(30, TimeUnit.MILLISECONDS);
                if (responseAndSocket != null) {
                    responseAndSocket.getResponse().write(responseAndSocket.getSocket().getOutputStream());
                    responseAndSocket.getSocket().close();
                }
            } catch (IOException | InterruptedException e) {
                ErrorLogger.getInstance().error(e);
                System.out.println("RW Exception");
            }
        }

    }
    // private static PriorityBlockingQueue<HttpRequest> requestQueue;

    public void addToQueue(IHttpResponse response, Socket socket) {
        this.responseQueue.add(new ResponseAndSocket(response, socket));
//        this.responseToSocket.put(response, socket);
        System.out.println("RW Add to Queue");
    }
}

class ResponseAndSocket {
    private IHttpResponse response;
    private Socket socket;
    
    public ResponseAndSocket(IHttpResponse response, Socket socket) {
        this.setResponse(response);
        this.setSocket(socket);
    }

    public IHttpResponse getResponse() {
        return response;
    }

    public void setResponse(IHttpResponse response) {
        this.response = response;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
