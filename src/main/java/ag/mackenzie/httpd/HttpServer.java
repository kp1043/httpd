package ag.mackenzie.httpd;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

 
public class HttpServer extends Thread {
	public int port = 8900;
	public String webroot = System.getProperty("user.dir") + "/htdocs";
	public static Logger logger = Logger.getLogger("ag.mackenzie.httpd");	
    private ExecutorService pool = Executors.newCachedThreadPool();
	private ServerSocket serverSocket = null;
	private boolean running;
	
	
	public HttpServer() {
		initializeWebroot(webroot);
		
	}
	
	public HttpServer(int port, String webroot) {
		this.port = port;
		this.webroot = webroot;
		initializeWebroot(webroot);
		
	}
	
	public void run() {
		serve();
	}
	
	private void initializeWebroot(String webroot) {
		File wwwdir = new File(webroot);
		if (!wwwdir.exists()) {
			if (wwwdir.mkdir()) {
				logger.info("Created web directory because it didn't exist at " + webroot);
				try {
					FileWriter writer = new FileWriter(webroot + "/index.html");
					BufferedWriter out = new BufferedWriter(writer);
					out.write("<html><head><title>Hello World</title></head><body><h1>Hello World</h1></body></html>");
					out.close();
				} catch (IOException e) {
					logger.info("Tried and failed to create default web content: " + e.getMessage());
				}
			}
		}
	}
    public void serve()  {
    	
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
        	logger.log(Level.SEVERE, "Could not bind to port " + this.port);
        	pool.shutdownNow();
            System.exit(-1);
        }
        logger.info("Now accepting connections on port " + this.port + " and serving files from " + webroot + ".");
 
        running = true;
        
        while (running) {
        	try {
				pool.execute(new HttpConnectionHandler(serverSocket.accept(), webroot, logger));
			} catch (IOException e) {
				logger.severe("Error when accepting client socket: " + e.getMessage());
			}
        }
 
        
    }
    
    public void stopServer() {
    	running = false;
    }
    
    public static void main(String[] argv) {
    	HttpServer server = new HttpServer();
    	server.start();
    }
}
