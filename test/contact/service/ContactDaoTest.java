package contact.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.mem.MemContactDao;

public class ContactDaoTest {
	ContactDao dao;
	Contact contact1;
	Contact contact2;
	Contact contact3;
	
	@Before
	public void setUp() {
		// create a new DAO for each test and create some sample contacts
		dao = new MemContactDao();
		contact1 = new Contact("contact1", "Joe Contact", "joe@microsoft.com");
		contact2 = new Contact("contact2", "Sally Contract", "sally@foo.com");
		contact3 = new Contact("contact3", "Foo Bar", "foo@barclub.com");
	}
	
	private void saveAllContacts() {
		dao.save(contact1);
		dao.save(contact2);
		dao.save(contact3);
	}
	
	@Test
	public void testFind() {
		// nothing in database yet
		Contact foo = dao.find(1);
		assertNull( "Empty DAO should return null", foo);
		dao.save(contact1);
		assertTrue("DAO must assign contact id", contact1.getId() > 0);
		dao.save(contact2);
		assertTrue("DAO must assign contact id", contact2.getId() > 0);
		long id1 = contact1.getId();
		long id2 = contact2.getId();
		assertTrue("Contact IDs must be different", id1 != id2 );
		
		Contact result1 = dao.find(id1);
		assertEquals("Should find first contact", contact1, result1);
		Contact result2 = dao.find(id2);
		assertEquals("Should find second contact", contact2, result2);
		
		// find should be repeatable
		Contact x1 = dao.find(id1);
		assertSame( result1, x1 );
		
		long id = id1 + id2 + 1; // something not in the dao
		Contact result = dao.find(id);
		assertNull( "Contact not in dao", result);
	}

	@Test
	public void testFindAll() {
		List<Contact> results = dao.findAll();
		assertEquals("DAO should be empty", 0, results.size());
		saveAllContacts();
		results = dao.findAll();
		assertEquals(3, results.size());
		assertTrue( results.contains(contact1) );
		assertTrue( results.contains(contact2) );
		assertTrue( results.contains(contact3) );
	}
	

	@Test
	public void testDelete() {
		dao.save(contact1);
		long id = contact1.getId();
		assertTrue( "Delete a contact in dao", dao.delete(id));
		// shouldn't be there now
		Contact result = dao.find(id);
		assertNull( "Already deleted this contact", result);
		// second time it should fail
		assertFalse( "Already deleted this contact", dao.delete(id) );
		// now for a bunch of contacts
		saveAllContacts();
		
		List<Contact> all = dao.findAll();
		assertEquals("Added 3 contacts", 3, all.size() );
		id = contact2.getId();
		assertTrue( dao.delete(id) );
		assertEquals( 2, all.size() );
		assertFalse( dao.delete(id) );
		id = contact1.getId();
		assertTrue( dao.delete(id) );
		assertEquals( 1, all.size() );
		assertFalse( dao.delete(id) );
		id = contact3.getId();
		assertTrue( dao.delete(id) );
		assertEquals( 0, all.size() );
		assertFalse( dao.delete(id) );
	}
	
	@Test
	public void testUpdate() {
		saveAllContacts();
		
		String email = contact2.getEmail();
		String title = contact2.getTitle();
		String newname = "A shiny new name";
		// create an update for contact2
		Contact update = new Contact( contact2.getId() );
		update.setName( newname );
		assertTrue("Update an existing contact", dao.update(update) );
		// only the name should be updated!
		contact2 = dao.find(contact2.getId());
		assertEquals( newname, contact2.getName() );
		assertEquals( email, contact2.getEmail() );
		assertEquals( title, contact2.getTitle() );
		// update the title
		String newtitle = "Master of the Universe";
		update.setTitle(newtitle);
		update.setName("");  // empty string means delete the name
		dao.update(update);
		contact2 = dao.find( contact2.getId() );
		assertEquals(newtitle, contact2.getTitle() );
		assertEquals("", contact2.getName() );
		assertEquals(email, contact2.getEmail() );
	}
}