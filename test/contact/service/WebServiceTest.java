package contact.service;

import static org.junit.Assert.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import contact.JettyMain;
import contact.entity.Contact;
/**
 * A JUnit test to test the Contact web service.
 * Test GET one contact, POST a new contact, PUT an update, DELETE contact.
 * Include a request that succeed and not succeed
 * test the memory-based DAO.
 * 
 * @author Juthamas
 * @version 2014.10.6
 *
 */
public class WebServiceTest {
	private static String serviceUrl;
	private static HttpClient client;
	private Contact tester1;
	private Contact tester2;
	private ContactDao dao = DaoFactory.getInstance().getContactDao();
	private ContentResponse response;
	private Request request;
	private StringContentProvider provider;
	
	@BeforeClass
	public static void doFirst() throws Exception{
		//Start the Jetty server.
		//Suppose this method returns the URL (with port) of the server
		serviceUrl = JettyMain.startServer( 8080 )+"contacts";
	}
	
	@AfterClass
	public static void doLast() throws Exception{
		//stop the Jetty server after the last test
		JettyMain.stopServer();
	}
	
	@Before
	public void setUp() throws Exception{
		long id = 101;
		tester1 = new Contact("Test contact", "Joe Experimental", "none@testing.com");
		tester1.setId(id);
		id++;
		tester2 = new Contact("Another Test contact", "Testosterone", "testee@foo.com");
		tester2.setId(id);
		dao.save(tester1);
		dao.save(tester2);
		
		client = new HttpClient();
		client.start();
	}
	
	@After
	public void clean() throws Exception{
		dao.delete(tester1.getId());
		dao.delete(tester2.getId());
		
		client.stop();
	}
	
	@Test
	public void testGet() throws InterruptedException, ExecutionException, TimeoutException{
		response = client.GET(serviceUrl+"/"+ tester1.getId());
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());

		response = client.GET(serviceUrl+"/"+ tester2.getId());
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
	}
	
	@Test
	public void failGet() throws InterruptedException, ExecutionException, TimeoutException{
		long wrongID = 123123123;
		response = client.GET(serviceUrl+"/"+wrongID);
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
		
		wrongID = 103;
		response = client.GET(serviceUrl+"/"+wrongID);
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testPost() throws InterruptedException, TimeoutException, ExecutionException{
		provider = new StringContentProvider("<contact id=\"222\"><title>Test zZz</title><name>test Experimental</name><email>test@testing.com</email><photoUrl/></contact>");
		request = client.newRequest(serviceUrl);
		request = request.content(provider, "application/xml");
		request = request.method(HttpMethod.POST);
			
		response = request.send();
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
		
		//test Location header.
		String[] location = response.getHeaders().get("Location").split("/");
		long id = Long.parseLong(location[location.length - 1]);
		
		response = client.GET(serviceUrl+"?title=z");
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
	}
	
	@Test
	public void failPost() throws InterruptedException, TimeoutException, ExecutionException{
		provider = new StringContentProvider("<contact id=\"222\"><title>Test contact</title><name>test Experimental</name><email>test@testing.com</email><photoUrl/></contact>");
		request = client.newRequest(serviceUrl);
		request = request.content(provider, "application/xml");
		request = request.method(HttpMethod.POST);
		
		response = request.send();
		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testPut() throws InterruptedException, TimeoutException, ExecutionException{
		long id = tester1.getId();
		provider = new StringContentProvider("<contact id=\""+ id +"\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
		request = client.newRequest(serviceUrl+"/"+id);
		request = request.content(provider, "application/xml");
		request = request.method(HttpMethod.PUT);
			
		response = request.send();
		System.out.println(response.getStatus());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void failPut() throws InterruptedException, TimeoutException, ExecutionException{
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
	}
	
	@Test
	public void testDelete() throws InterruptedException, TimeoutException, ExecutionException{
		request = client.newRequest(serviceUrl+"/222");
		request = request.method(HttpMethod.DELETE);
		
		response = request.send();
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void failDelete() throws InterruptedException, TimeoutException, ExecutionException{
		request = client.newRequest(serviceUrl+"/555");
		request = request.method(HttpMethod.DELETE);
		
		response = request.send();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
}