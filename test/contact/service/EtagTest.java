package contact.service;

import static org.junit.Assert.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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
 * JUnit test to test the use of ETag.
 * GET, PUT, DELETE recognize If-Match and If-None-Match
 * POST recognize ETag.
 * @author Juthamas
 *
 */
public class EtagTest {
	private static String serviceUrl;
	private static HttpClient client;
	private Contact tester1;
	private Contact tester2;
	private ContactDao dao = DaoFactory.getInstance().getContactDao();
	private ContentResponse response;
	private Request request;
	private StringContentProvider provider;
	private String etag;
	
	@BeforeClass
	public static void doFirst() throws Exception{
		//Start the Jetty server.
		//Suppose this method returns the URL (with port) of the server
		serviceUrl = JettyMain.startServer( 8080 )+"contacts/";
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
		dao.removeAll();
		client.stop();
	}
	
	public void testETagPost(long id){
		try{
			provider = new StringContentProvider("<contact id=\""+id+"\"><title>Test contact</title><name>test Experimental</name><email>test@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.POST);
			response = request.send();
			
			assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
			
			String etag = response.getHeaders().get("etag");
			String expectedEtag = client.GET(serviceUrl+id).getHeaders().get("etag");
			assertEquals(expectedEtag, etag);
			
		} catch(Exception e){}
	}
	
	@Test
	public void TestGetEtag(){
		try{
			response = client.GET(serviceUrl+tester1.getId());
			assertEquals(Status.OK.getStatusCode(),response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
			response = client.GET(serviceUrl+tester2.getId());
			assertEquals(Status.OK.getStatusCode(),response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestGETIfNonMatch(){
		try{
		
			response = client.GET(serviceUrl+tester1.getId());
			assertEquals(Status.OK.getStatusCode(),response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
			//Test GET If-None-Match (ETag is same)
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-None-Match", etag).method(HttpMethod.GET);
			response = request.send();
			assertEquals(Status.NOT_MODIFIED.getStatusCode(), response.getStatus());
			
			//Test GET If-None-Match = different ETag
			etag = client.GET(serviceUrl+tester2.getId()).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-None-Match", etag).method(HttpMethod.GET);
			response = request.send();
			assertEquals(Status.OK.getStatusCode(), response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestGETIfMatch(){
		try{
			response = client.GET(serviceUrl+tester1.getId());
			assertEquals(Status.OK.getStatusCode(),response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
			
			//Test GET If-Match = same ETag 
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-Match", etag).method(HttpMethod.GET);
			response = request.send();
			assertEquals(Status.OK.getStatusCode(), response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void failGet(){
		try{
			
			String etag = client.GET(serviceUrl+tester1.getId()).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-Match",etag).header("If-None-Match", etag).method(HttpMethod.GET);
			response = request.send();
			assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
			
			//FAIL : Test GET If-Match (different ETag).
			etag = client.GET(serviceUrl+tester2.getId()).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-Match", etag).method(HttpMethod.GET);
			response = request.send();
			assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void passPut(){
		try{
			//Get Etag. 
			response = client.GET(serviceUrl+tester2.getId());
			etag = response.getHeaders().get("Etag");
			
			//Test If-Match should same as current ETag.
			etag = response.getHeaders().get("Etag");
			provider = new StringContentProvider("<contact id=\""+tester2.getId()+"\"><title>Test put contact with If-Match</title><name>Put Match Experimental</name><email>match@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+tester2.getId()).header("If-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			
			assertEquals(Status.OK.getStatusCode(), response.getStatus());
			
			//Test If-None-Match shouldn't has same ETag.
			//Unless the same ETag.
			provider = new StringContentProvider("<contact id=\""+tester1.getId()+"\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-None-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			
			assertEquals(Status.OK.getStatusCode(), response.getStatus());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void failPut(){
		try{
			//Get Etag. 
			response = client.GET(serviceUrl+tester1.getId());
			etag = response.getHeaders().get("Etag");
			
			//FAIL : Test send both of If-None-Match and If-Match.
			provider = new StringContentProvider("<contact id=\""+tester1.getId()+"\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-None-Match", etag).header("If-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			
			assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
			
			//FAIL: Test If-None-Match match with current ETag.
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-None-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			
			assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
			
			//FAIL : Test If-Match does not match with current ETag.
			etag = client.GET(serviceUrl+tester2.getId()).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+tester1.getId()).header("If-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
			
		}catch(Exception e){}
	}
	
	@Test
	public void testDelete(){
		try{
			long id = 666;
			testETagPost(id);
			//Get Etag. (id = 666)
			response = client.GET(serviceUrl+id);
			etag = response.getHeaders().get("Etag");
			
			//SUCCESS : Test DELETE If-Match (ETag is same)
			request = client.newRequest(serviceUrl+id).header("If-Match", etag);
			request = request.method(HttpMethod.DELETE);
			response = request.send();
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
			
			id = 555;
			testETagPost(id);
			//SUCCESS : Test DELETE If-None-Match. (ETag is different)
			request = client.newRequest(serviceUrl+id).header("If-None-Match", etag);
			request = request.method(HttpMethod.DELETE);
			response = request.send();
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
			
			//Test DELETE the same contact.
			request = client.newRequest(serviceUrl+id).header("If-None-Match", etag);
			request = request.method(HttpMethod.DELETE);
			response = request.send();
			assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void failDelete(){
		try{
			long id = 1111;
			testETagPost(id);
			
			String wrongETag = client.GET(serviceUrl+tester1.getId()).getHeaders().get("Etag");
			
			//FAIL : Test DELETE If-Match (ETag is different)
			request = client.newRequest(serviceUrl+id).header("If-Match", wrongETag);
			request = request.method(HttpMethod.DELETE);
			response = request.send();
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
			
			//FAIL : Test DELETE If-None-Match (ETag is same)
			wrongETag = client.GET(serviceUrl+id).getHeaders().get("Etag");
			
			request = client.newRequest(serviceUrl+id).header("If-None-Match", wrongETag);
			request = request.method(HttpMethod.DELETE);
			response = request.send();
			assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
