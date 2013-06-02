package ejava.examples.orm.core.products;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ejava.examples.orm.core.annotated.Car;

import junit.framework.TestCase;

/**
 * This test case provides a demo of using a class that has been mapped
 * to the database with with specific table and column annotations. 
 * 
 * @author jcstaff
 * $Id:$
 */
public class TableColumnAnnotationDemo extends TestCase {
    private static Log log = LogFactory.getLog(TableColumnAnnotationDemo.class);
    private static final String PERSISTENCE_UNIT = "ormCore";
    private EntityManagerFactory emf;
    private EntityManager em = null;

    protected void setUp() throws Exception {        
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);   
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    protected void tearDown() throws Exception {
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            if (tx.getRollbackOnly() == true) { tx.rollback(); }
            else                              { tx.commit(); }
        }
        em.close();
    }
    
    /**
     * This test demonstrates the ability to map a simple object to the 
     * database using custom table and column annotations.
     * 
     * By looking at the Car class, we would expect to see a ORBCORE_CAR table
     * with columns CAR_ID, CAR_MAKE, CAR_MODEL, CAR_YEAR, and CAR_COST. 
     */
    public void testTableColumnMapping() {
        log.info("testTableColumnMapping");

        try {           
            ejava.examples.orm.core.annotated.Car car = new Car(1);
            car.setMake("chevy");
            car.setModel("tahoe");
            car.setYear(2002);
            car.setCost(new BigDecimal(10000.00));
            
            //insert a row in the database
            em.persist(car);
            log.info("created car:" + car);
            
            //find the inserted object
            Car car2 = em.find(Car.class, 1L); 
    
            log.info("found car:" + car2);
            assertNotNull(car2);
            assertEquals(car.getYear(), car2.getYear());
            
            //lets update the year
            car.setYear(2000);
            em.flush();
            //since the persistence context is still active, cars
            //are actually the same object 
            assertEquals(car.getYear(), car2.getYear());
            log.info("updated car:" + car2);
            
            //lets delete the object
            em.remove(car);
            em.flush();
            log.info("removed car:" + car);
            
            //lets put a car back in at end of test so we can see it in database 
            em.persist(car2);
            log.info("created leftover car:" + car2);
            
        } catch (PersistenceException ex) {
            StringBuilder text = new StringBuilder(ex.getMessage());
            Throwable cause = ex.getCause();
            while (cause != null) {
                text.append("\nCaused By:" + cause);
                cause = cause.getCause();
            }
            log.error("error in testTableColumnMapping:" + text, ex);
            fail("error in testTableColumnMapping:" + text);
        }
    }
    
    /**
     * Demonstrates the use of precision and scale
     */
    public void testPrecision() {
        ejava.examples.orm.core.annotated.Car car = new Car(1);
        car.setMake("chevy");
        car.setModel("tahoe");
        car.setYear(2002);
        //precision defined in ORM as precision=7, scale=2 
        car.setCost(new BigDecimal("12345.66"));
        
        //persist with current values  
    	em.persist(car);
    	em.flush();
    	em.clear();
    	
    	//get a fresh copy from the DB
    	Car car2 = em.find(Car.class, car.getId());
    	log.info("car.cost=" + car.getCost());
    	log.info("car2.cost=" + car2.getCost());
    	assertTrue("unexpectected value", car.getCost().equals(car2.getCost()));
    	
    	
    	//update beyond the scale values -- too many digits to right of decimal
        car2.setCost(new BigDecimal("1234.666"));
    	em.flush();
    	em.clear();
    	Car car3 = em.find(Car.class, car.getId());
    	log.info("car2.cost=" + car2.getCost());
    	log.info("car3.cost=" + car3.getCost());
    	assertFalse("unexpected scale", car2.getCost().equals(car3.getCost()));
    	
    	//update beyond the precision values -- too many digits overall
    	car2 = car3;
        car2.setCost(new BigDecimal("123456.66"));
        try {
	    	em.flush();
	    	fail("database accepted too many digits");
	    	em.clear();
	    	car3 = em.find(Car.class, car.getId());
	    	log.info("car2.cost=" + car2.getCost());
	    	log.info("car3.cost=" + car3.getCost());
	    	assertFalse("unexpected precision", car2.getCost().equals(car3.getCost()));
        } catch (PersistenceException ex) {
        	log.info("caught expected exception:" + ex);
        }
    }
}
