package ejava.projects.edmv.blimpl;

import static org.junit.Assert.*;


import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ejava.projects.edmv.blimpl.EDmvIngestor;
import ejava.projects.edmv.bo.Person;
import ejava.projects.edmv.bo.VehicleRegistration;
import ejava.projects.edmv.dao.PersonDAO;
import ejava.projects.edmv.dao.VehicleDAO;
import ejava.projects.edmv.jdbc.JDBCPersonDAO;
import ejava.projects.edmv.jdbc.JDBCVehicleDAO;

/**
 * This class provides a basic test of the ingest capability.
 * 
 * @author jcstaff
 *
 */
public class EDmvIngestorTest {
	private static Log log = LogFactory.getLog(EDmvIngestorTest.class);
	private static String jdbcDriver = 
		System.getProperty("jdbc.driver", "org.hsqldb.jdbcDriver");
	private static String jdbcURL = 
		System.getProperty("jdbc.url", "jdbc:hsqldb:hsql://localhost:9001");
	private static String jdbcUser = 
		System.getProperty("jdbc.user", "sa");
	private static String jdbcPassword = 
		System.getProperty("jdbc.password", "");
	
	private EntityManagerFactory emf;
	private EntityManager em;
	private PersonDAO personDAO;
	private VehicleDAO vehicleDAO;
	private Connection connection;
	
	@Before
	public void setUp() throws Exception {		
		log.debug("loading JDBC driver:" + jdbcDriver);
		Thread.currentThread()
		      .getContextClassLoader()
		      .loadClass(jdbcDriver)
		      .newInstance();
		
		log.debug("getting connection(" + jdbcURL +
				", user=" + jdbcUser + ", password=" + jdbcPassword + ")");
		connection = 
			DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPassword);
		
	    personDAO = new JDBCPersonDAO();
	    ((JDBCPersonDAO)personDAO).setConnection(connection);
	    vehicleDAO = new JDBCVehicleDAO();
	    ((JDBCVehicleDAO)vehicleDAO).setConnection(connection);
		
		connection.setAutoCommit(false);
		
		emf = Persistence.createEntityManagerFactory("eDmvBO-test");
		em = emf.createEntityManager();
		//we could easily switch this to the JPA version here
		//personDAO = new JPAPersonDAO();
		//((JPAPersonDAO)personDAO).setEntityManager(em);
        //vehicleDAO = new JPAVehicleDAO();
        //((JPAVehicleDAO)vehicleDAO).setEntityManager(em);
		
		cleanup();
		em.getTransaction().begin();
	}
	
	@After
	public void tearDown() throws Exception {
		if (connection != null) {
			connection.commit();
			connection.close();
		}
		
		if (em != null) {
			EntityTransaction tx = em.getTransaction();
			if (tx.isActive()) {
				if (tx.getRollbackOnly()) { tx.rollback(); }
				else                      { tx.commit(); }
			}
			em.close();
		}
		if (emf != null) {
			emf.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void cleanup() {
		List<VehicleRegistration> regs = 
		    em.createQuery("select vr from VehicleRegistration vr")
		      .getResultList();
		for (VehicleRegistration r : regs) {
		    r.getOwners().clear();
			em.remove(r);
		}
		List<Person> people = 
		    em.createQuery("select p from Person p")
		      .getResultList();
		for (Person p : people) {
		    em.remove(p);
		}
		em.getTransaction().begin();
		em.getTransaction().commit();
	}

	@Test
	public void testIngestAll() throws Exception {
		log.info("*** testIngestAll ***");
		
		String fileName = "xml/dmv-all.xml";
		InputStream is = Thread.currentThread()
		                       .getContextClassLoader()
		                       .getResourceAsStream(fileName);
		assertNotNull(fileName + " not found", is);
		
		EDmvIngestor ingestor = new EDmvIngestor();
		ingestor.setPersonDAO(personDAO);
		ingestor.setVehicleDAO(vehicleDAO);
		ingestor.setInputStream(is);
		ingestor.ingest();
		//testing goes here...
	}

}