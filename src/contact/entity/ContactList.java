package contact.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="contacts")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContactList {
	private List<Contact> contact;
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
