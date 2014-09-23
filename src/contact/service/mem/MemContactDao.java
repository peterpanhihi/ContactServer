package contact.service.mem;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import contact.entity.Contact;
import contact.service.ContactDao;

/**
 * Data access object for saving and retrieving contacts.
 * This DAO uses an in-memory list of contacts, which may
 * be lost when the application exits.
 * Use DaoFactory to get an instance of this class, such as:
 * <p><tt>
 * dao = DaoFactory.getInstance().getContactDao()
 * </tt>
 * @author jim
 */
public class MemContactDao implements ContactDao {
	private List<Contact> contacts;
	private AtomicLong nextId;
	
	public MemContactDao() {
		contacts = new ArrayList<Contact>();
		nextId = new AtomicLong(1000L);
		createTestContact( );
	}
	
	/** add contacts for testing.  id is the starting id to use. */
	private void createTestContact( ) {
		long id = 101;
		if (find(id) == null) {
			Contact test = new Contact("Test contact", "Joe Experimental", "none@testing.com");
			test.setId(id);
			contacts.add(test);
		}
		id++;
		if (find(id) == null) {
			Contact test2 = new Contact("Another Test contact", "Testosterone", "testee@foo.com");
			test2.setId(id);
			contacts.add(test2);
		}
		if (nextId.longValue() <= id) nextId.set(id+1);
	}

	/**
	 * @see contact.service.ContactDao#find(long)
	 */
	@Override
	public Contact find(long id) {
		for(Contact c : contacts) 
			if (c.getId() == id) return c;
		return null;
	}

	/**
	 * @see contact.service.ContactDao#findAll()
	 */
	@Override
	public List<Contact> findAll() {
		return java.util.Collections.unmodifiableList(contacts);
	}

	/**
	 * Find contacts whose title contains string
	 * @see contact.service.ContactDao#findByTitle(java.lang.String)
	 */
	@Override
	public List<Contact> findByTitle(String match) {
		assert match != null : "Read the Javadoc for ContactDao";
		
		List<Contact> matchlist = new ArrayList<Contact>();
		// a regular expression to match part of the title. Use \b to match word boundary.
		Pattern pattern = Pattern.compile(".*"+match+".*", Pattern.CASE_INSENSITIVE);
		int size = contacts.size();
		for(int k=0; k<size; k++) {
			Contact contact = contacts.get(k);
			if ( pattern.matcher( contact.getTitle() ).matches() ) matchlist.add(contact);
		}
		return matchlist;
	}

	/**
	 * @see contact.service.ContactDao#delete(long)
	 */
	@Override
	public boolean delete(long id) {
		for(int k=0; k<contacts.size(); k++) {
			if (contacts.get(k).getId() == id) {
				contacts.remove(k);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see contact.service.ContactDao#save(contact.entity.Contact)
	 */
	@Override
	public boolean save(Contact contact) {
		if (contact.getId() == 0) {
			contact.setId( getUniqueId() );
			return contacts.add(contact);
		}
		// check if this contact is already in persistent storage
		Contact other  = find(contact.getId());
		if (other == contact) return true;
		if ( other != null ) contacts.remove(other);
		return contacts.add(contact);
	}

	/**
	 * @see contact.service.ContactDao#update(contact.entity.Contact)
	 */
	@Override
	public boolean update(Contact update) {
		Contact contact = find(update.getId());
		if (contact == null) return false;
		contact.applyUpdate(update);
		save(contact);
		return true;
	}
	
	/**
	 * Get a unique contact ID.
	 * @return unique id not in persistent storage
	 */
	private synchronized long getUniqueId() {
		long id = nextId.getAndAdd(1L);
		while( id < Long.MAX_VALUE ) {	
			if (find(id) == null) return id;
			id = nextId.getAndAdd(1L);
		}
		return id; // this should never happen
	}
	
}
