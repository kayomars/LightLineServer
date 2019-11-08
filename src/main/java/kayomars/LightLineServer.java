package kayomars;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class LightLineServer {
    
    private final HttpServer server;
    private final int maxNumOfThreads = 100;
    private static final String dbName = "dbOfData.db";
    private final long numOfLines;
    
     
    /**
     * Make a new LightLightLineServer that pre-processes the txt file into a SQLite database for more efficient lookups.
     * The server listens for connections on the given port, and spins threads for new connections with the help of a 
     * cached thread pool that restricts the maximum number of threads that can spun.
     * @param port at which to host
     * @param txtFileName is the complete file name of the text file with its .txt extension
     * @throws IOException if there is a problem with I/O
     */
    public LightLineServer(int port, String txtFileName) throws IOException {
        

        // Delete preexisting db file if any. This is done with the understanding the server will
        // not be restarted often. If the file to be preprocessed doesn't change as often, this can be
        // reconfigured to instead store the original db file.
        try {
            Files.delete(Paths.get(dbName));
        } catch (Exception e) {
            // Nothing
        }
        
        // Processing the text file into a database before accepting connection requests.
        // This process may take time depending on the size of text file.
        DatabaseAbstraction myDBAbstraction = new DatabaseAbstraction(dbName);
        this.numOfLines = myDBAbstraction.prepareDatabase(txtFileName);    
        myDBAbstraction.closeConnection();
         
        
        // Use Oracle's HTTPServer to create a server
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Handle concurrent requests with multiple threads with the use of thread pooling
        server.setExecutor(Executors.newFixedThreadPool(this.maxNumOfThreads));
                  
        // Handle only requests for paths that start with /lines/
        HttpContext lines = server.createContext("/lines/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                handleLines(exchange);
            }
        });
        
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        System.out.println("Server will listen on " + this.server.getAddress());
        server.start();
    }
    
    
    /*
     * Handle a request for /lines/lineNum by returning the relevant line if it exists.
     * Function must ensure that linenum is valid. If lineNum is too large, a 413 response must
     * be made. If lineNum is found, a 200 reponse with the line should be made.
     * 
     * @param exchange HTTP request/response, modified by this method to send a
     *                 response to the client and close the exchange
     */
    private void handleLines(HttpExchange exchange) throws IOException {
        
        // Getting the lineNum from the provided request
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();        
        final String endPart = path.substring(base.length());
        
        boolean isValid = true;
        long lineNum = 0;
        
        // To check if lineNum is indeed a number
        try {
            lineNum = Long.parseLong(endPart);
        } catch (Exception e) {
            isValid = false;
        }
        
        
        String response = "";
        
        // Not included in specifications, but included for sanity
        if (!isValid || lineNum < 0) {
            exchange.sendResponseHeaders(400, 0);
            response = "<h1>400 Bad Request</h1>";
        } else if (lineNum > this.numOfLines - 1) {
            exchange.sendResponseHeaders(413, 0);
            response = "<h1>413 Payload Too Large</h1>";
        } else {
            exchange.sendResponseHeaders(200, 0);
       
            DatabaseAbstraction sepDBConnection = new DatabaseAbstraction(dbName);
            response = sepDBConnection.getLineContent(lineNum);
            sepDBConnection.closeConnection();

        }
        
        // Write the response to the output stream using ASCII character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, StandardCharsets.US_ASCII), true); 
        
        out.write(response);
        out.close();
        exchange.close();            
    }
    
   
    
    /*
     * Checks the provided arguments for the file name and runs the server if the file
     * is found.
     */ 
    public static void main(String[] args) {
        
        final Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        String txtFileName = "";
        boolean foundFile = false;
        
        
        // Check if legal arguments have been provided
        try {
            txtFileName = arguments.remove();
            foundFile = Files.exists(Paths.get(txtFileName));       
            if (!foundFile) {System.out.println("Missing Text File");}
        } catch (NoSuchElementException nse) {
            System.out.println("Missing Text File");
        } catch (SecurityException se) {
            System.out.println("Missing Text File");
        } finally {
            if (foundFile) {
                
                try {
                    // Picking a port number to listen to
                    new LightLineServer(8080, txtFileName).start();
                } catch (Exception e) {
                    System.out.println("An error occured with the server.");
                }
            }
        }
    }
}

