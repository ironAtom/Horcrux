/**
 * The MultiThreadedServer Class, listen to the port
 *  and wait to establish socket connection
 * 
 * Author:	Jian Wang	<jianwan3@andrew.cmu.edu>
 * 		  	Qinyu Tong	<qtong@andrew.cmu.edu>
 * 
 * Date:	Tue Jan 27 21:02:09 EST 2015
 * */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import messagePasser.CommUtil;

public class MultiThreadedServer implements Runnable{

    protected int serverPort;
    protected ServerSocket serverSocket;
    protected boolean isStopped    = false;
    protected Thread runningThread= null;
    protected CommUtil commUtil;

    public MultiThreadedServer(int portNumber, CommUtil commUtil){
        this.serverPort = portNumber;
        this.commUtil = commUtil;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            new Thread(
                new WorkerRunnable(
                    clientSocket, commUtil)
            ).start();
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

}