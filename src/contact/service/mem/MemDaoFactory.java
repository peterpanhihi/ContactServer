package contact.service.mem;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.*;
import contact.service.mem.MemContactDao;

/**
 * MemDaoFactory is a factory for getting instances of entity DAO object
 * that use memory-based persistence, which isn't really persistence at all!
 * 
 * @see contact.service.DaoFactory
 * @version 2014.09.19
 * @author jim
 */
public class MemDaoFactory extends DaoFactory {
	/** instance of the entity DAO */
	private ContactDao daoInstance;
	
	public MemDaoFactory() {
		daoInstance = new MemContactDao();
		try {
			loadFile("/tmp/contact.xml");
		} catch (Exception e) {}
	}
	
	@Override
	public ContactDao getContactDao() {
		return daoInstance;
	}
	
	@Override
	public void shutdown() {
		// Use JAXB to write all your contacts to a file on disk.
		// Then recreate them the next time a MemFactoryDao and ContactDao are created.
		try {
			ContactList list = new ContactList();
			list.setContacts(daoInstance.findAll());
			JAXBContext jaxbContext = JAXBContext.newInstance(ContactList.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			jaxbMarshaller.marshal(list, new File("/tmp/contact.xml"));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void loadFile(String url) throws JAXBException {
		File file = null;
		try{
			file  = new File(url);
			
		}catch(Exception e){}

		JAXBContext jaxbContext = JAXBContext.newInstance(ContactList.class);
		
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		ContactList contactList = (ContactList)jaxbUnmarshaller.unmarshal(file);
		
		List<Contact> contacts = contactList.getContacts();
		
		for(Contact c : contacts){
			daoInstance.save(c);
		}
	}
}
