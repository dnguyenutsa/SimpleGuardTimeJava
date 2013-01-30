package codes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


import com.guardtime.transport.SimpleHttpStamper;
import com.guardtime.tsp.GTDataHash;
import com.guardtime.tsp.GTException;
import com.guardtime.tsp.GTHashAlgorithm;
import com.guardtime.tsp.GTTimestamp;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimpleHTTPServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
//        server.createContext("/test", new MyHandler());
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	File file = new File (System.getProperty("user.dir"), "/data/temp.txt");
        	FileWriter fw = new FileWriter(file.getAbsoluteFile());
        	BufferedWriter bw = new BufferedWriter(fw);
            InputStream dataInFile = t.getRequestBody();
            
                       
            String content = IOUtils.toString(dataInFile, "UTF-8");
            System.out.println(content);
            bw.write(content);
            
            GTDataHash dataHash = new GTDataHash(GTHashAlgorithm.DEFAULT);
    		dataHash.update(dataInFile).close();
    		dataInFile.close();

    		// Get timestamp
    		try {
				GTTimestamp ts = SimpleHttpStamper.create(dataHash, "http://stamper.guardtime.net/gt-signingservice");
	            t.sendResponseHeaders(200,0);
	            OutputStream os = t.getResponseBody();
	            os.write(ts.getEncoded());
	            os.close();
			} catch (GTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			bw.close();
        }
    }
    
    static class HttpFileHandler implements HttpHandler  {

        private final String docRoot;

        public HttpFileHandler(final String docRoot) {
            super();
            this.docRoot = docRoot;
        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            String target = request.getRequestLine().getUri();

            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                System.out.println("Incoming entity content (bytes): " + entityContent.length);
            }

            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>File" + file.getPath() +
                        " not found</h1></body></html>",
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                System.out.println("File " + file.getPath() + " not found");

            } else if (!file.canRead() || file.isDirectory()) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>Access denied</h1></body></html>",
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                System.out.println("Cannot read file " + file.getPath());

            } else {

                response.setStatusCode(HttpStatus.SC_OK);
                FileEntity body = new FileEntity(file, ContentType.create("text/html"));
                response.setEntity(body);
                System.out.println("Serving file " + file.getPath());
            }
        }

		@Override
		public void handle(HttpExchange arg0) throws IOException {
			// TODO Auto-generated method stub
			
		}

    }

}