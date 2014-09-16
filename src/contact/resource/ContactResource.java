package contact.resource;

import java.net.URI;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * ContactResource provides RESTful Contact server.
 * ContactResourcr can create new contacts, get contacts by id or title, 
 * get all contacts, update a contact, delete a contact.
 * 
 * @author Juthamas Utamaphethai
 * @version 2014.8.16
 *
 */
@Path("/contacts")
@Singleton
public class ContactResource {
	private ContactDao dao;
	
	/**
	 * Get ContactDao from DaoFactory.
	 */
	public ContactResource() {
		dao = DaoFactory.getInstance().getContactDao();
		System.out.println("ContactResource : Initializing.");
	}
	
	/**
	 * Get a list of all contacts or
	 * get contact(s) whose title contains the query string.
	 * Convert List of contact to XML by marshal method.
	 * @param q the query string that we want to find in contact's title.
	 * @return response BAD REQUEST or NOT FOUND or OK if the request succeeds.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getContacts(@QueryParam("q") String q){
		List<Contact> contact;
		if(q != null){
			contact = dao.findByTitle(q);
		}
		else{
			contact = dao.findAll();
		}
		if(contact.isEmpty()){
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(mashal(contact)).build();
	}
	
	/**
	 * Get one contact by id.
	 * @param id of contact
	 * @return response OK, BAD REQUEST, or NOT FOUND if it can't find contact.
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@PathParam("id") long id){
		Contact contact = dao.find(id);
		if(contact == null)return Response.status(Response.Status.NOT_FOUND).build();
		return Response.ok(contact).build();
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
	public Response postContracts(JAXBElement<Contact> element, @Context UriInfo uriInfo){
		Contact contact = element.getValue();
		if(dao.save(contact)){
			URI uri = uriInfo.getAbsolutePath();
			UriBuilder builder = UriBuilder.fromUri(uri).path(contact.getId()+"");			
			uri = builder.build();
			
			return Response.created(uri).build();
		}
		return Response.status(Response.Status.CONFLICT).build();
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
	public Response putContact(JAXBElement<Contact> element, @Context UriInfo uriInfo, @PathParam("id") long id){
		Contact contact = element.getValue();
		contact.setId(id);
		if(dao.update(contact)){
			URI uri = uriInfo.getAbsolutePath();
			return Response.ok("Location : "+uri+"/"+contact.getId()).build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
		
	}
	
	/**
	 * Delete a contact with matching id.
	 * @param id for deleting contact
	 * @return response OK or NOT FOUND if not found contact to delete
	 */
	@DELETE
	@Path("{id}")
	public Response deleteContact(@PathParam("id") long id){
		if(dao.delete(id))
			return Response.ok().build();
		else
			return Response.status(Response.Status.NOT_FOUND).build();
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
