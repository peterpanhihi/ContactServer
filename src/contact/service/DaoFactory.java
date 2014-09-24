package contact.service;

import javax.xml.bind.JAXBException;

/**
 * DaoFactory defines methods for obtaining instance of data access objects.
 * To create DAO you first get an instance of a concrete factory by invoking
 * <p>
 * <tt>DaoFactory factory = DaoFactory.getInstance(); </tt>
 * <p>
 * Then use the <tt>factory</tt> object to get instances of actual DAO.
 * This factory is an abstract class.  There are concrete subclasses for
 * each persistence mechanism.  You can add your own factory by subclassing
 * this factory.
 * 
 * @author jim
 */
public abstract class DaoFactory {
	// singleton instance of this factory
	private static DaoFactory factory;
	
	/** this class shouldn't be instantiated, but constructor must be visible to subclasses. */
	protected DaoFactory() {
		// nothing to do
	}
	
	/**
	 * Get a singleton instance of the DaoFactory.
	 * @return instance of a concrete DaoFactory
	 */
	public static DaoFactory getInstance() {
		if (factory == null) factory = new contact.service.mem.MemDaoFactory();
		return factory;
	}
	
	/**
	 * Get an instance of a data access object for Contact objects.
	 * Subclasses of the base DaoFactory class must provide a concrete
	 * instance of this method that returns a ContactDao suitable
	 * for their persistence framework.
	 * @return instance of Contact's DAO
	 */
	public abstract ContactDao getContactDao();
	
	/**
	 * Shutdown all persistence services.
	 * This method gives the persistence framework a chance to
	 * gracefully save data and close databases before the
	 * application terminates.
	 */
	public abstract void shutdown();
	
	/**
	 * Load a saved data from a file by URL
	 * @param url Locator of file for loading
	 * @throws JAXBException
	 */
	public abstract void loadFile(String url) throws JAXBException;
}
