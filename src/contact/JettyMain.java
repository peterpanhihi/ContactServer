package contact;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;

import contact.service.DaoFactory;
/**
 * Use a Jetty sever that is created and started via code.
 * the resource class is in the package "contact.resource" which
 * is annotated with JAX-RS @Path("/contacts").
 * 
 * @author Juthamas Utamaphethai
 * @version 2014.9.23
 *
 */
public class JettyMain {
	/** Package(s) where REST resource classes are */
	static final String RESOURCE_PACKAGE = "contact.resource";
	/** A specified port on Jetty server. */
	static  final int PORT = 8080;
	
	private static Server server;
	
	/**Start and stop the server.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		startServer(8080);
		System.out.println("Server started.  Press ENTER to stop it.");
		int ch = System.in.read();
		stopServer();
	}
	
	/**
	 * Create Jetty server and a context.
	 * Use ServletContextHandler to hold a context.
	 * ServletHolder holds Jersey ServletContainer for managing the resource class
	 * and pass HTTP request to Contact resource.
	 * @param port port for starting the server
	 * @return URI of server
	 */
	public static String startServer(int port){
		server = new Server(port);
		
		ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );
		context.setContextPath("/");
		
		ServletHolder holder = new ServletHolder(org.glassfish.jersey.servlet.ServletContainer.class);
		holder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, RESOURCE_PACKAGE);
		holder.setInitParameter(ServerProperties.JSON_PROCESSING_FEATURE_DISABLE, "false");
		
		context.addServlet(holder, "/*");
		
		server.setHandler(context);
		
		System.out.println("Starting Jetty server on port " + port);
		
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return server.getURI().toString();
	}
	
	/**
	 * Stop the server and shutdown an DaoFactory.
	 */
	public static void stopServer(){
		DaoFactory.getInstance().shutdown();
		System.out.println("Stopping server.");
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
