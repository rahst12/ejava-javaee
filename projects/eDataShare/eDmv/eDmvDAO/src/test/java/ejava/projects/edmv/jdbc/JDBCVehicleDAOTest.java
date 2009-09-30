package ejava.projects.edmv.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ejava.projects.edmv.bo.Person;
import ejava.projects.edmv.bo.VehicleRegistration;
import ejava.projects.edmv.dao.PersonDAO;
import ejava.projects.edmv.dao.VehicleDAO;
import ejava.projects.edmv.jdbc.JDBCPersonDAO;
import ejava.projects.edmv.jdbc.JDBCVehicleDAO;

import junit.framework.TestCase;

/**
 * This test case provides an example of one might test the JDBC DAO. It 
 * provides most of the basics, knowing the DAO only implemented one method.
 * As more tests are added, the implementation show here would benefit from
 * re-usable techniques to check the values within the database.
 * 
 * @author jcstaff
 *
 */
public class JDBCVehicleDAOTest extends TestCase {
	private static Log log = LogFactory.getLog(JDBCVehicleDAOTest.class);
	private static String jdbcDriver = System.getProperty("jdbc.driver");
	private static String jdbcURL = System.getProperty("jdbc.url");
	private static String jdbcUser = System.getProperty("jdbc.user");
	private static String jdbcPassword = System.getProperty("jdbc.password");
	
	private Connection connection;
	private VehicleDAO vehicleDAO;
	private PersonDAO personDAO;
	
	public void setUp() throws Exception {
		assertNotNull("jdbc.driver not supplied", jdbcDriver);
		assertNotNull("jdbc.url not supplied", jdbcURL);
		assertNotNull("jdbc.user not supplied", jdbcUser);
		assertNotNull("jdbc.password not supplied", jdbcPassword);
		
		log.debug("loading JDBC driver:" + jdbcDriver);
		Thread.currentThread()
		      .getContextClassLoader()
		      .loadClass(jdbcDriver)
		      .newInstance();
		
		log.debug("getting connection(" + jdbcURL +
				", user=" + jdbcUser + ", password=" + jdbcPassword + ")");
		connection = 
			DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPassword);
		
	    vehicleDAO = new JDBCVehicleDAO();
	    ((JDBCVehicleDAO)vehicleDAO).setConnection(connection);
	    personDAO = new JDBCPersonDAO();
	    ((JDBCPersonDAO)personDAO).setConnection(connection);
		
		connection.setAutoCommit(false);
		cleanup();
	}

	protected void tearDown() throws Exception {
		if (connection != null) {
			connection.commit();
		    ((JDBCVehicleDAO)vehicleDAO).setConnection(null);
			connection.close();
		}
	}
	
	private void cleanup() throws Exception {
		Statement statement=null;
		try {
			statement = connection.createStatement();
            statement.execute("DELETE FROM EDMV_VREG_OWNER_LINK");
            statement.execute("DELETE FROM EDMV_PERSON");
            statement.execute("DELETE FROM EDMV_VREG");
		}
		finally {
            if (statement != null) { statement.close(); }			
		}
	}

	/**
	 * This method tests a single create into the database using the DAO. 
	 * This tests some core functionality, but clearly more types of inserts
	 * should also be tested. For example, what happens when the same userId 
	 * is added a second time.
	 * 
	 * @throws Exception
	 */
	public void testJDBCCreate() throws Exception {
		log.info("*** testJDBCCreate ***");

		Person owner1 = new Person();
		owner1.setLastName("Smith");
		personDAO.createPerson(owner1);
		log.debug("created owner1:" + owner1);
		
        Person owner2 = new Person();
        owner2.setLastName("Jones");
        personDAO.createPerson(owner2);
        log.debug("created owner2:" + owner2);

        VehicleRegistration registration = new VehicleRegistration();
        registration.setVin("123");
        registration.getOwners().add(owner1);
        registration.getOwners().add(owner2);
        vehicleDAO.createRegistration(registration);
        log.debug("created registration:" + registration);
        
    	//up to here the client was ignorant of the technology used, the 
    	//rest of this is used to test what should have happened above 
    	//and must leverage the JDBC resources.
    	
    	connection.commit();
    	Statement statement = null;
    	ResultSet rs = null;
    	try {
    	    statement = connection.createStatement();
    	    
        	log.debug("getting core vehicle info...");
    	    rs = statement.executeQuery(
    	    		"SELECT VIN FROM EDMV_VREG WHERE ID='" 
    	    		+ registration.getId() + "'");
    	    if (rs.next()) {
    	    	String vin = rs.getString("VIN");
    	    	assertEquals("unexpected vin", registration.getVin(), vin);
    	    }
    	    else {
    	    	fail("no registration found");
    	    }
            log.debug("JDBC registration looks okay");
    	        	    
    	    rs = statement.executeQuery(
    	    	"SELECT LAST_NAME " +
    	    	"FROM EDMV_PERSON, EDMV_VREG, EDMV_VREG_OWNER_LINK " +
    	    	"WHERE EDMV_PERSON.ID = EDMV_VREG_OWNER_LINK.OWNER_ID " +
    	    	    "AND EDMV_VREG_OWNER_LINK.VEHICLE_ID = " + 
    	    	    registration.getId());
    	    while (rs.next()) {
    	    	String name = rs.getString("LAST_NAME");
    	    	assertTrue("unexpected last name", 
    	    	        owner1.getLastName().equals(name) ||
    	    	        owner2.getLastName().equals(name));
    	    }
    	    
        	log.debug("JDBC owners looks good");
    	}
    	finally {
    		if (rs != null) { rs.close(); }
    		if (statement != null) { statement.close(); }
    	}
	}
}