package ejava.projects.eleague.jdbc;

import static org.junit.Assert.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ejava.projects.eleague.bo.Address;
import ejava.projects.eleague.bo.Venue;
import ejava.projects.eleague.dao.ClubDAO;
import ejava.projects.eleague.jdbc.JDBCClubDAO;

/**
 * This test case provides an example of one might test the JDBC DAO. It 
 * provides most of the basics, knowing the DAO only implemented one method.
 * As more tests are added, the implementation show here would benefit from
 * re-usable techniques to check the values within the database.
 * 
 * @author jcstaff
 *
 */
public class JDBCClubDAOTest {
	private static Log log = LogFactory.getLog(JDBCClubDAO.class);
	private static String jdbcDriver = System.getProperty("jdbc.driver", "org.hsqldb.jdbcDriver");
	private static String jdbcURL = System.getProperty("jdbc.url", "jdbc:hsqldb:hsql://localhost:9001");
	private static String jdbcUser = System.getProperty("jdbc.user","sa");
	private static String jdbcPassword = System.getProperty("jdbc.password","");
	
	private Connection connection;
	private ClubDAO dao;
	
	@Before
	public void setUp() throws Exception {
		log.debug("loading JDBC driver:" + jdbcDriver);
		Thread.currentThread()
		      .getContextClassLoader()
		      .loadClass(jdbcDriver)
		      .newInstance();
		
		log.debug(String.format("getting connection(%s, %s, %s)", 
				jdbcURL, jdbcUser, jdbcPassword));
		connection = 
			DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPassword);
		
	    dao = new JDBCClubDAO();
	    ((JDBCClubDAO)dao).setConnection(connection);
		
		connection.setAutoCommit(false);
		cleanup();
	}

	@After
	public void tearDown() throws Exception {
		if (connection != null) {
			connection.commit();
		    ((JDBCClubDAO)dao).setConnection(null);
			connection.close();
		}
	}
	
	private void cleanup() throws Exception {
		Statement statement=null;
		try {
			statement = connection.createStatement();
			statement.execute("DELETE FROM ELEAGUE_VEN");
			statement.execute("DELETE FROM ELEAGUE_ADDR");
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
	@Test
	public void testJDBCCreate() throws Exception {
		log.info("*** testJDBCCreate ***");
		
    	@SuppressWarnings("unused")
		String userId = "foo";
    	@SuppressWarnings("unused")
		String firstName = "bar";
    	
    	Venue venue = new Venue();
    	venue.setName("myVenue");
    	venue.setAddress(new Address(0, "Laurel"));
    	
    	log.debug("instantiated Venue:" + venue);
    	
    	dao.createVenue(venue);
    	log.debug("dao created Venue:" + venue);
    	
    	//up to here the client was ignorant of the technology used, the 
    	//rest of this is used to test what should have happened above 
    	//and must leverage the JDBC resources.
    	
    	connection.commit();
    	Statement statement = null;
    	ResultSet rs = null;
    	try {
    	    statement = connection.createStatement();
    	    
        	log.debug("getting core venue info...");
    	    rs = statement.executeQuery(
    	    		"SELECT NAME FROM ELEAGUE_VEN WHERE ID='" 
    	    		+ venue.getId() + "'");
    	    if (rs.next()) {
    	    	String name = rs.getString("NAME");
    	    	assertEquals("unexpected name", venue.getName(), name);
    	    }
    	    else {
    	    	fail("no venue found");
    	    }
    	    
    	    rs = statement.executeQuery(
    	    	"SELECT CITY " +
    	    	"FROM ELEAGUE_VEN, ELEAGUE_ADDR " +
    	    	"WHERE ELEAGUE_ADDR.ID = ELEAGUE_VEN.ADDR_ID " +
    	    		"AND ELEAGUE_VEN.ID = " + venue.getId());
    	    if (rs.next()) {
    	    	String city = rs.getString("CITY");
    	    	assertEquals("unexpected city", 
    	    	        venue.getAddress().getCity(), city);
    	    }
    	    
        	log.debug("JDBC venue looks good");
    	}
    	finally {
    		if (rs != null) { rs.close(); }
    		if (statement != null) { statement.close(); }
    	}
	}
}
