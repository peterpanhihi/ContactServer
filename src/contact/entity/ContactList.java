package contact.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * list of all contacts for Memory-base DAO
 * 
 * @author Juthamas
 * @version 2014.9.23
 *
 */
@XmlRootElement(name="contacts")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContactList {
	/** collect all contacts. */
	private List<Contact> contact;
	
	/**
	 * create a new list without data.
	 */
	public ContactList() {
		setContacts(new ArrayList<Contact>());
	}
	
	public List<Contact> getContacts() {
		return contact;
	}
	
	public void setContacts(List<Contact> contacts) {
		this.contact = contacts;
	}
}
