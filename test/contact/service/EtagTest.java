package contact.service;

import static org.junit.Assert.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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
 * JUnit test to test the use of ETag.
 * GET, PUT, DELETE recognize If-Match and If-None-Match
 * POST recognize ETag.
 * @author Juthamas
 *
 */
public class EtagTest {
	private static String serviceUrl;
	private static HttpClient client;
	private ContentResponse response;
	private Request request;
	private StringContentProvider provider;
	private String etag;
	private int id = 666;
	
	@BeforeClass
	public static void doFirst() throws Exception{
		//Start the Jetty server.
		//Suppose this method returns the URL (with port) of the server
		serviceUrl = JettyMain.startServer( 8080 )+"contacts/";
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
	public void testETagPost(){
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
			id = 101;
			
			response = client.GET(serviceUrl+id);
			assertEquals(Status.OK.getStatusCode(),response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
			id++;
			
			response = client.GET(serviceUrl+id);
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
			id = 101;
			
			response = client.GET(serviceUrl+id);
			assertEquals(Status.OK.getStatusCode(),response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
			//Test GET If-None-Match (ETag is same)
			request = client.newRequest(serviceUrl+id).header("If-None-Match", etag).method(HttpMethod.GET);
			response = request.send();
			assertEquals(Status.NOT_MODIFIED.getStatusCode(), response.getStatus());
			
			//Test GET If-None-Match = different Etag
			etag = client.GET(serviceUrl+102).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+id).header("If-None-Match", etag).method(HttpMethod.GET);
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
			id = 101;
			
			response = client.GET(serviceUrl+id);
			assertEquals(Status.OK.getStatusCode(),response.getStatus());
			etag = response.getHeaders().get("Etag");
			assertTrue(etag != null);
			
			
			//Test GET If-Match = same ETag 
			request = client.newRequest(serviceUrl+id).header("If-Match", etag).method(HttpMethod.GET);
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
		id = 101;
		try{
			
			String etag = client.GET(serviceUrl+id).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+id).header("If-Match",etag).header("If-None-Match", etag).method(HttpMethod.GET);
			response = request.send();
			assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
			
			//FAIL : Test GET If-Match (different ETag).
			etag = client.GET(serviceUrl+102).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+id).header("If-Match", etag).method(HttpMethod.GET);
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
			response = client.GET(serviceUrl+102);
			etag = response.getHeaders().get("Etag");
			
			//Test If-Match should same as current ETag.
			etag = response.getHeaders().get("Etag");
			provider = new StringContentProvider("<contact id=\"102\"><title>Test put contact with If-Match</title><name>Put Match Experimental</name><email>match@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+"102").header("If-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			
			assertEquals(Status.OK.getStatusCode(), response.getStatus());
			
			//Test If-None-Match shouldn't has same ETag.
			//Unless the same ETag.
			provider = new StringContentProvider("<contact id=\"101\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+"101").header("If-None-Match", etag);
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
			response = client.GET(serviceUrl+101);
			etag = response.getHeaders().get("Etag");
			
			//FAIL : Test send both of If-None-Match and If-Match.
			provider = new StringContentProvider("<contact id=\"101\"><title>Test put contact</title><name>Put Experimental</name><email>put@testing.com</email><photoUrl/></contact>");
			request = client.newRequest(serviceUrl+"101").header("If-None-Match", etag).header("If-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			
			assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
			
			//FAIL: Test If-None-Match match with current ETag.
			request = client.newRequest(serviceUrl+"101").header("If-None-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			
			assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
			
			//FAIL : Test If-Match does not match with current ETag.
			etag = client.GET(serviceUrl+102).getHeaders().get("Etag");
			request = client.newRequest(serviceUrl+"101").header("If-Match", etag);
			request = request.content(provider, "application/xml");
			request = request.method(HttpMethod.PUT);
			response = request.send();
			assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
			
		}catch(Exception e){}
	}
	
	@Test
	public void testDelete(){
		try{
			id = 666;
			
			//Get Etag. (id = 666)
			response = client.GET(serviceUrl+id);
			etag = response.getHeaders().get("Etag");
			
			//SUCCESS : Test DELETE If-Match (ETag is same)
			request = client.newRequest(serviceUrl+id).header("If-Match", etag);
			request = request.method(HttpMethod.DELETE);
			response = request.send();
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
			
			//Add new contact which id is 555.
			id = 555;
			testETagPost();
			
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
			id = 102;
			
			String wrongETag = client.GET(serviceUrl+101).getHeaders().get("Etag");
			
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
