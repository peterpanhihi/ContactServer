package contact.service;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import contact.JettyMain;
/**
 * A JUnit test to test the Contact web service.
 * Test GET one contact, POST a new contact, PUT an update, DELETE contact.
 * Include a request that succeed and not succeed
 * test the memory-based DAO.
 * 
 * @author Juthamas
 * @version 2014.8.23
 *
 */
public class WebServiceTest {
	private static String serviceUrl;
	private static HttpClient client;
	private ContentResponse response;
	private Request request;
	private StringContentProvider provider;
	
	@BeforeClass
	public static void doFirst() throws Exception{
		//Start the Jetty server.
		//Suppose this method returns the URL (with port) of the server
		serviceUrl = JettyMain.startServer( 8080 )+"contacts";
		client = new HttpClient();
		client.start();
	}
	
	@AfterClass
	public static void doLast() throws Exception{
		//stop the Jetty server after the last test
		JettyMain.stopServer();
		client.stop();
	}
	
	@Test
	public void testGet(){
		try {
			int id = 101;
			response = client.GET(serviceUrl+"/"+id);
			assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
			
			id++;
			response = client.GET(serviceUrl+"/"+id);
			assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
		} catch (Exception e) {}
	}
	
	@Test
	public void failGet(){
		try{
			response = client.GET(serviceUrl+"/123123123");
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
			
			response = client.GET(serviceUrl+"/103");
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
		} catch(Exception e){}
	}
	
	@Test
	public void testPost(){
		try{
			provider = new StringContentProvider("<contact id=\"222\"><title>Test contact</title><name>test Experimental</name><email>test@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.POST);
			
			response = request.send();
			assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
		} catch(Exception e){}
	}
	
	@Test
	public void failPost(){
		try{
			provider = new StringContentProvider("<contact id=\"222\"><title>Test contact</title><name>test Experimental</name><email>test@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.POST);
			
			response = request.send();
			assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
		} catch(Exception e){}
	}
	
	@Test
	public void testPut(){
		try{
			provider = new StringContentProvider("<contact id=\"222\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+"/222");
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			
			response = request.send();
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		}catch(Exception e){}
	}
	
	@Test
	public void failPut(){
		try{
			provider = new StringContentProvider("<contact id=\"222\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+"/333");
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			
			response = request.send();
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
			
			provider = new StringContentProvider("<contact id=\"444\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+"/444");
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			
			response = request.send();
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
			
		}catch(Exception e){}
	}
	
	@Test
	public void testDelete(){
		try{
			request = client.newRequest(serviceUrl+"/222");
			request = request.method(HttpMethod.DELETE);
			
			response = request.send();
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		}catch(Exception e){}
	}
	
	@Test
	public void failDelete(){
		try{
			request = client.newRequest(serviceUrl+"/555");
			request = request.method(HttpMethod.DELETE);
			
			response = request.send();
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
		}catch(Exception e){}
	}
}
