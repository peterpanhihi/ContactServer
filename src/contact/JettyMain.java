package contact;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
/**
 * Use a Jetty sever that is created and started via code.
 * the resource class is in the package "contact.resource" which
 * is annotated with JAX-RS @Path("/contacts").
 * 
 * @author Juthamas Utamaphethai
 * @version 2014.8.16
 *
 */
public class JettyMain {
	/** A specified port on Jetty server. */
	static  final int PORT = 8080;
	
	/**
	 * Create Jetty server and a context.
	 * Use ServletContextHandler to hold a context.
	 * ServletHolder holds Jersey ServletContainer for managing the resource class
	 * and pass HTTP request to Contact resource.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int port = PORT;
		Server server = new Server(port);
		
		ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );
		context.setContextPath("/");
		
		ServletHolder holder = new ServletHolder(org.glassfish.jersey.servlet.ServletContainer.class);
		holder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "contact.resource");
		context.addServlet(holder, "/*");
		
		server.setHandler(context);
		
		System.out.println("Starting Jetty server on port " + port);
		server.start();
		
		System.out.println("Server started.  Press ENTER to stop it.");
		int ch = System.in.read();
		System.out.println("Stopping server.");
		server.stop();
	}
}
