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
	/** Collect all contacts. */
	private List<Contact> contact;
	
	/** A string representing a version of a representation of a ContactList. */
	private String etag;
	
	/**
	 * create a new list without data.
	 */
	public ContactList() {
		setContacts(new ArrayList<Contact>());
		setEtag(this.hashCode());
	}
	
	public List<Contact> getContacts() {
		return contact;
	}
	
	public void setContacts(List<Contact> contacts) {
		this.contact = contacts;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(int hashCode) {
		this.etag = Integer.toString(hashCode);
	}
}
