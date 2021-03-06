package ejava.examples.orm.core.products;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ejava.examples.orm.core.ColorType;
import ejava.examples.orm.core.mapped.Vase;

import junit.framework.TestCase;

/**
 * This test case provides an example of providing special mappings for
 * certain types, like dates and enums.
 *  
 * @author jcstaff
 * $Id:$
 */
public class TypesMappingDemo extends TestCase {
    private static Log log = LogFactory.getLog(TypesMappingDemo.class);
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
    public void testValues() {
        log.info("testValues");
        ejava.examples.orm.core.mapped.Vase vase = new Vase(2);
        Date date = new Date();
        vase.setADate(date);
        vase.setATime(date);
        vase.setATimestamp(date);
        vase.setColorId(ColorType.RED);
        vase.setColorName(ColorType.RED);
        
        //insert a row in the database
        em.persist(vase);
        log.info("created case:" + vase);
        
        //find the inserted object
        em.flush();
        em.clear();
        Vase vase2 = em.find(Vase.class, 2L); 

        log.info("found vase:" + vase2);
        assertNotNull(vase2);
        assertEquals(vase2.getADate(), vase2.getADate());
        assertEquals(vase2.getATime(), vase2.getATime());
        assertEquals(vase2.getATimestamp(), vase2.getATimestamp());
        assertEquals(vase2.getColorId(), vase2.getColorId());
        assertEquals(vase2.getColorName(), vase2.getColorName());        
    }        
}
