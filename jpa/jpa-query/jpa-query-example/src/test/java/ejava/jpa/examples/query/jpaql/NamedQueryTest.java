package ejava.jpa.examples.query.jpaql;

import static org.junit.Assert.*;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import ejava.jpa.examples.query.Customer;
import ejava.jpa.examples.query.QueryBase;
import ejava.jpa.examples.query.Sale;

public class NamedQueryTest extends QueryBase {
	private static final Log log = LogFactory.getLog(NamedQueryTest.class);

    @Test
    public void testNamedQuery() {
        log.info("*** testNamedQuery() ***");
        
        Customer customer = 
            em.createNamedQuery("Customer.getCustomersByName", Customer.class)
              .setParameter("first", "cat")
              .setParameter("last", "inhat")
              .getResultList()
              .get(0);
        assertNotNull("no customer found", customer);
        log.info("found customer:" + customer);
        
        List<Sale> sales = em.createNamedQuery(
        	"Customer.getCustomerPurchases", Sale.class)
			.setParameter("custId", customer.getId())
			.getResultList();
        assertTrue("no sales found", sales.size() > 0);
        for (Sale s: sales) {
            log.info("found sale:" + s);
        }
    }
}