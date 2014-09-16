package contact;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;

public class JettyMain {
	
	static  final int PORT = 1111;
	
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
