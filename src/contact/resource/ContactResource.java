package contact.resource;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * ContactResource provides RESTful Contact server.
 * ContactResourcr can create new contacts, get contacts by id or title, 
 * get all contacts, update a contact, delete a contact.
 * 
 * @author Juthamas Utamaphethai
 * @version 2014.9.30
 *
 */
@Path("/contacts")
@Singleton
public class ContactResource {
	private ContactDao dao;
	private ContactList contactList;
	private final Response NOT_FOUND = Response.status(Response.Status.NOT_FOUND).build();
	private final Response NOT_MODIFIED = Response.status(Response.Status.NOT_MODIFIED).build();
	private final Response CONFLICT = Response.status(Response.Status.CONFLICT).build();
	private final Response BAD_REQUEST = Response.status(Response.Status.BAD_REQUEST).build();
	private final Response PRECONDITION_FAILED = Response.status(Response.Status.PRECONDITION_FAILED).build();
	
	/**
	 * Get ContactDao from DaoFactory.
	 */
	public ContactResource() {
		dao = DaoFactory.getInstance().getContactDao();
		contactList = new ContactList();
		System.out.println("ContactResource : Initializing.");
	}
	
	/**
	 * GET a list of contacts or
	 * get contact(s) whose title contains the query string.
	 * Convert List of contact to XML by marshal method.
	 * @param title, the query string that we want to find in contact's title.
	 * @return 200 OK with contact, 404 NOT_FOUND if don't have a contact, 304 NOT_MODIFIED if contact isn't changed.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getContacts(@HeaderParam("If-Match") String ifMatch,@HeaderParam("If-None-Match") String ifNoneMatch, @Context Request request, @QueryParam("title") String title){
		List<Contact> contact = null;
		if(title != null){
			Pattern pattern = Pattern.compile(".*"+title+".*",Pattern.CASE_INSENSITIVE); 
			Matcher matcher = pattern.matcher(title);
			if(matcher.matches()){
				contact = dao.findByTitle(title);
			}
		}
		else{
			contact = dao.findAll();
		}
		
		if(contact.isEmpty()){
			return NOT_FOUND;
		}
		
		if(ifMatch != null && ifNoneMatch != null)return BAD_REQUEST;
		
		contactList.setContacts(contact);
		String oldEtag = contactList.getEtag();

		if(ifMatch == null || ifNoneMatch == null || ifMatch.equals(oldEtag) || !ifNoneMatch.equals(oldEtag)){
			CacheControl cc = new CacheControl();
			// make the cookie expire after the browser is closed.
			cc.setMaxAge(-1);
			
			EntityTag etag = new EntityTag(oldEtag);
			
			//if the preconditions indicate that the client has the latest version of the resource
			//and the 304 Not Modified status code will be automatically assigned.
			ResponseBuilder builder = request.evaluatePreconditions(etag);
			if(builder == null){
				builder = Response.ok(mashal(contact));
				builder.tag(etag);
			}
			
			builder.cacheControl(cc);
			return builder.build();
		}
		return NOT_MODIFIED;
	}
	
	/**
	 * Get one contact by id.
	 * @param id of contact that user request.
	 * @return 200 OK if success, 404 NOT_FOUND if don't have a contact, 304 NOT_MODIFIED if contact isn't changed.
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@HeaderParam("If-Match") String ifMatch,@HeaderParam("If-None-Match") String ifNoneMatch, @Context Request request, @PathParam("id") long id){
		Contact contact = dao.find(id);
		if(contact == null)return NOT_FOUND;
		
		if(ifMatch != null && ifNoneMatch != null)return BAD_REQUEST;
		
		String oldEtag = contact.getTag();
		
		if(ifMatch == null || ifNoneMatch == null || ifMatch.equals(oldEtag) || !ifNoneMatch.equals(oldEtag)){
			CacheControl cc = new CacheControl();
			cc.setMaxAge(-1);
			
			EntityTag etag = new EntityTag(oldEtag);
			
			ResponseBuilder builder = request.evaluatePreconditions(etag);
			
			if(builder == null){
				builder = Response.ok(contact);
				builder.tag(etag);
			}
			
			builder.cacheControl(cc);
			return builder.build();
		}
		return NOT_MODIFIED;
	}
	
	/**
	 * Create a new contact.
	 * Use JAX-RS that automatically unmarshal data to Contact object annotaion
	 * with JAXB annotation.
	 * 
	 * @param element get values of contact's element.
	 * @param uriInfo access request header and build URI information
	 * @return the Location header if it created and response CONFLICT if the contact already exists
	 */
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response postContracts(JAXBElement<Contact> element, @Context UriInfo uriInfo, @Context Request request){
		Contact contact = element.getValue();
		if(dao.find(contact.getId()) == null)
		if(dao.save(contact)){
			EntityTag etag = new EntityTag(contact.getTag());
			ResponseBuilder builder = request.evaluatePreconditions(etag);
			
			if(builder == null){
				URI uri = uriInfo.getAbsolutePath();
				UriBuilder uriBuilder = UriBuilder.fromUri(uri).path(contact.getId()+"");			
				uri = uriBuilder.build();
				
				builder = Response.created(uri);
				builder.tag(etag);
			}	
			return builder.build();
		}
		return CONFLICT;
	}
	
	/**
	 * Update a contact.
	 * @param element contains value that wants to update
	 * @param uriInfo access request header
	 * @param id for updating the contact
	 * @return the Location header in the response of OK and NOT FOUND if can't update id
	 */
	@PUT
	@Path("{id}")
	public Response putContact(@HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch, @Context Request request, JAXBElement<Contact> element, @Context UriInfo uriInfo, @PathParam("id") long id){
		Contact contact = element.getValue();
		
		if(contact.getId() != id || (ifMatch != null && ifNoneMatch != null)){
			return BAD_REQUEST;
		}
		
		String oldEtag = contact.getTag();
		
		if(ifMatch == null || ifNoneMatch == null || ifMatch.equals(oldEtag) || !ifNoneMatch.equals(oldEtag)){
			Contact old = dao.find(id);
			if(old == null)return NOT_FOUND;
			
			EntityTag etag = new EntityTag(old.getTag());
			ResponseBuilder builder = request.evaluatePreconditions(etag);
			
			if(builder == null){
				dao.update(contact);
				contactList.setEtag(this.hashCode());
				builder = Response.ok();
				builder.tag(new EntityTag( contact.getTag() ));
			}	
			return builder.build();
		}
		return PRECONDITION_FAILED;
	}
	
	/**
	 * Delete a contact with matching id.
	 * @param id for deleting contact
	 * @return response OK or NOT FOUND if not found contact to delete
	 */
	@DELETE
	@Path("{id}")
	public Response deleteContact(@HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch, @Context Request request,@PathParam("id") long id){
		Contact oldContact = dao.find(id);
		if(oldContact == null)return NOT_FOUND;
		
		if(ifMatch != null && ifNoneMatch != null)return BAD_REQUEST;
		
		String oldEtag = oldContact.getTag();
		if(ifMatch == null || ifNoneMatch == null || ifMatch.equals(oldEtag) || !ifNoneMatch.equals(oldEtag)){
			
			EntityTag etag = new EntityTag(oldContact.getTag());
			ResponseBuilder builder = request.evaluatePreconditions(etag);
			
			if(builder == null){
				dao.delete(id);
				contactList.setEtag(this.hashCode());
				builder = Response.ok();
			}
			return builder.build();
		}
		return PRECONDITION_FAILED;
	}
	
	/**
	 * Marshal List of contact to XML
	 * @param contacts to parse
	 * @return generic type information
	 */
	public GenericEntity mashal(List<Contact> contacts){
		return new GenericEntity<List<Contact>>(contacts){};
	}
}
