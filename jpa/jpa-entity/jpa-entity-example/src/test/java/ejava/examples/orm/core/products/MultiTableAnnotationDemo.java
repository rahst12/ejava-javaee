package ejava.examples.orm.core.products;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ejava.examples.orm.core.annotated.Watch;

import junit.framework.TestCase;

/**
 * This test case provides an example of mapping multiple tables into a 
 * single object. See the javadoc for the Watch class to describe the 
 * mapping details, but essentially 3 tables make-up a single Watch object.
 *  
 * @author jcstaff
 * $Id:$
 */
public class MultiTableAnnotationDemo extends TestCase {
    private static Log log = LogFactory.getLog(MultiTableAnnotationDemo.class);
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
     */
    public void testMultiTable() {
        log.info("testMultiTable");
        ejava.examples.orm.core.annotated.Watch watch = new Watch(1);
        watch.setMake("ontime");
        watch.setModel("round-and-round");
        watch.setOwner("john doe");
        watch.setCardnum("123-45-6789");
        watch.setManufacturer("getter done");
        watch.setAddress("12noon lane");
        watch.setPhone("410-555-1212");
        
        //if this works, it should store the single object in 3 tables
        em.persist(watch);
        log.info("created watch:" + watch);
        
        em.flush();
        em.clear();
        Watch watch2 = em.find(Watch.class, 1L);
        assertNotNull(watch2);
        log.info("found watch:" + watch2);
        assertEquals(watch.getMake(), watch2.getMake());
        assertEquals(watch.getModel(), watch2.getModel());
        assertEquals(watch.getOwner(), watch2.getOwner());
        assertEquals(watch.getCardnum(), watch2.getCardnum());
        assertEquals(watch.getManufacturer(), watch2.getManufacturer());
        assertEquals(watch.getAddress(), watch2.getAddress());
        assertEquals(watch.getPhone(), watch2.getPhone());
        
        em.remove(watch2);
        log.info("removed watch:" + watch);
        
        //leave a watch in DB to inspect
        Watch watch3 = new Watch(3);
        watch3.setMake("ontime3");
        watch3.setModel("round-and-round3");
        watch3.setOwner("john doe3");
        watch3.setCardnum("123-45-67893");
        watch3.setManufacturer("getter done3");
        watch3.setAddress("12noon lane3");
        watch3.setPhone("410-555-12123");       
        em.persist(watch3);
        log.info("created leftover watch:" + watch3);
    }        
}
