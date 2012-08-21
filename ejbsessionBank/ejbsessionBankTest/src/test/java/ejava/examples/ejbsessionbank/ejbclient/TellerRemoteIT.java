package ejava.examples.ejbsessionbank.ejbclient;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;


import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import ejava.examples.ejbsessionbank.bl.BankException;
import ejava.examples.ejbsessionbank.bl.Teller;
import ejava.examples.ejbsessionbank.bo.Account;
import ejava.examples.ejbsessionbank.bo.Ledger;
import ejava.examples.ejbsessionbank.bo.Owner;
import ejava.examples.ejbsessionbank.ejb.TellerEJB;
import ejava.examples.ejbsessionbank.ejb.TellerRemote;

public class TellerRemoteIT {
    Log log = LogFactory.getLog(TellerRemoteIT.class);
    InitialContext jndi;
    String jndiName = System.getProperty("jndi.name",getLookupName());
    
    @Before
    public void setUp() throws Exception {
        log.debug("getting jndi initial context");
        jndi = new InitialContext();    
        log.debug("jndi=" + jndi.getEnvironment());
        
        cleanup();
    }
    
    public static String getLookupName() {
    	/*
    	The app name is the EAR name of the deployed EJB without .ear suffix.
    	Since we haven't deployed the application as a .ear,
    	the app name for us will be an empty string
    	*/
    	String appName = "ejbsessionBankEAR-3.0.2012.2-SNAPSHOT";
    	
    	/* The module name is the JAR name of the deployed EJB
    	   without the .jar suffix.
    	   */
    	String moduleName = "ejbsessionBankEJB-3.0.2012.2-SNAPSHOT";
    	
    	/*AS7 allows each deployment to have an (optional) distinct name.
    	This can be an empty string if distinct name is not specified.
    	*/
    	String distinctName = "";

    	// The EJB bean implementation class name
    	String beanName = TellerEJB.class.getSimpleName();

    	// Fully qualified remote interface name
    	final String interfaceName = TellerRemote.class.getName();

    	// Create a look up string name
    	String name = "ejb:" + appName + "/" + moduleName + "/" +
    			distinctName + "/" + beanName + "!" + interfaceName;
    	return name;
    }
    
    private void cleanup() throws Exception {
        if (jndi!=null) {
            TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);
            if (jndi!=null) {
                for (int index=0; ; index+=100) {
                    List<Owner> owners = teller.getOwnersLoaded(index, 100);
                    if (owners.size() == 0) { break; }
                    for (Owner owner : owners) {
                        log.debug("removing owner:" + owner);
                        for (Account a: owner.getAccounts()) {
                            zeroAccount(teller, a);
                        }
                        teller.removeOwner(owner.getId());
                    }
                }
                
                for (@SuppressWarnings("unused")
				int index=0; ; index+= 100) {
                    List<Account> accounts = teller.getAccounts(0, 100);
                    if (accounts.size() == 0) { break; }
                    for (Account a: accounts) {
                        log.debug("cleaning up account:" + a);
                        zeroAccount(teller, a);
                        teller.closeAccount(a.getAccountNumber());                        
                    }
                }
            }
        }
    }
    
    private void zeroAccount(
            Teller teller, Account account) throws BankException {
        log.debug("cleaning up account:" + account);
        if (account.getBalance() > 0) {
            account.withdraw(account.getBalance());
           teller.updateAccount(account);
        }
        else if (account.getBalance() < 0) {
            account.deposit(account.getBalance() * -1);
            teller.updateAccount(account);
        }
    }

    @Test
    public void testLookupTellerRemote() throws Exception {
        log.info("*** testLookupTellerRemote ***");
        @SuppressWarnings("unused")
        TellerRemote teller = null;
        
        log.debug("looking up remote:" + jndiName);
        try {
            Object object = jndi.lookup(jndiName);
            log.debug("found object:" + object);
            teller = (TellerRemote)object;
        }
        catch (Exception ex) {
            log.fatal("error getting teller remote:" + ex);
            fail("error getting teller remote:" + ex);
        }        
    }    

    @Test
    public void testCreateAccount() throws Exception {
        log.info("*** testCreateAccount ***");
        Account account=null;
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);
        
        log.debug("creating account, teller=" + teller);
        try {
            account = teller.createAccount("1234");
            log.debug("account created:" + account);
        }
        catch (Exception ex) {
            log.fatal("error creating account:" + ex, ex);
            fail("error creating account:" + ex);
        }        
        
        try {
            teller.createAccount(account.getAccountNumber());
            fail("created account with duplicate number");
        }
        catch (Exception expected) {
            log.info("got expected exception trying to create " +
                    "duplicate account:" + expected);
        }
    }
    
    @Test
    public void testGetAccount() throws Exception {
        log.info("*** testGetAccount ***");
        Account account = null;
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);
        
        log.debug("creating account, teller=" + teller);
        try {
            account = teller.createAccount("1234");
            log.debug("account created:" + account);
            Account account2 = teller.getAccount(account.getAccountNumber());
            log.debug("got account:" + account);
            
            assertEquals("unexpected account num:"+account2.getAccountNumber(),
                    account.getAccountNumber(), account2.getAccountNumber());
            assertEquals("unexpected account bal:"+account2.getBalance(),
                    (int)(100*account.getBalance()), 
                    (int)(100*account2.getBalance()));
        }
        catch (Exception ex) {
            log.fatal("error getting account:" + ex, ex);
            fail("error getting account:" + ex);
        }        
    }    

    @Test
    public void testUpdateAccount() throws Exception {
        log.info("*** testUpdateAccount ***");
        Account account = null;
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);
        
        log.debug("creating account, teller=" + teller);
        try {
            account = teller.createAccount("1234");
            log.debug("account created:" + account);
            
            account.deposit(5.00);
            assertEquals("unexpected balance:" + account.getBalance(),
                    (int)(100*5.00), 
                    (int)(100*account.getBalance()));
            teller.updateAccount(account);
            log.debug("updated account:" + account);
            
            Account account2 = teller.getAccount(account.getAccountNumber());
            log.debug("retrieved updated account:" + account2);
                        
            assertEquals("unexpected account bal:"+account2.getBalance(),
                    (int)(100*account.getBalance()), 
                    (int)(100*account2.getBalance()));
            
            account.withdraw(10.00);
            assertEquals("unexpected balance:" + account.getBalance(),
		            (int)(100*-5.00), 
		            (int)(100*account.getBalance()));
            teller.updateAccount(account);
            log.debug("updated account:" + account);
            
            account2 = teller.getAccount(account.getAccountNumber());
            log.debug("retrieved updated account:" + account2);
                        
            assertEquals("unexpected account bal:"+account2.getBalance(),
                    (int)(100*account.getBalance()), 
                    (int)(100*account2.getBalance()));
        }
        catch (Exception ex) {
            log.fatal("error updating account:" + ex, ex);
            fail("error updating account:" + ex);
        }        
    }    
    
    @Test
    public void testCloseAccount() throws Exception {
        log.info("*** testCloseAccount ***");
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);

        try {
            Account account = teller.createAccount("1234");
            account.deposit(10.0);
            teller.updateAccount(account);
            try {
                log.debug("trying to close account with + balance:" + account);
                teller.closeAccount(account.getAccountNumber());
                fail("account was reported closed");
            }
            catch (BankException expected) {}            
            
            account.withdraw(20.0);
            teller.updateAccount(account);
            try {
                log.debug("trying to close account with - balance:" + account);
                teller.closeAccount(account.getAccountNumber());
                fail("account was reported closed");
            }
            catch (BankException expected) {}
            
            account.deposit(10.0);
            teller.updateAccount(account);
            log.debug("trying to close account with no balance:" + account);
            teller.closeAccount(account.getAccountNumber());
        }
        catch (BankException ex) {
            log.fatal("error getting overdrawn accounts:" + ex, ex);
            fail("error getting overdrawn accounts:" + ex);
        }
    }

    @Test
    public void testFindOverdrawnAccounts() throws Exception {
        log.info("*** testFindOverdrawnAccounts ***");
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);

        try {
            int num = 0;
            Account account1 = teller.createAccount("" + ++num);
            @SuppressWarnings("unused")
            Account account2 = teller.createAccount("" + ++num);
            Account account3 = teller.createAccount("" + ++num);
            log.debug("created 3 accounts");

            account1.deposit(10.0);
            teller.updateAccount(account1);                
            account3.withdraw(10.0);
            teller.updateAccount(account3);                
            log.debug("updated 2 accounts");
            
            List<Account> accounts=teller.getOverdrawnAccounts(0, 100);
            log.debug("overdrawn accounts:" + accounts);
            assertEquals("unexpected number of accounts:"+accounts, 
                    1, accounts.size());
        }
        catch (BankException ex) {
            log.fatal("error getting overdrawn accounts:" + ex, ex);
            fail("error getting overdrawn accounts:" + ex);
        }
    }

    @Test
    public void testFindAllAccounts() throws Exception {
        log.info("*** testFindAllAccounts ***");
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);
        int TOTAL = 100;

        try {
            for(int i=0; i<TOTAL; i++) {
                Account account = teller.createAccount("" + i);
                account.deposit(i);
                teller.updateAccount(account);                
            }
            
            int index=0;
            for(List<Account> accounts=teller.getAccounts(index, TOTAL/10);
                accounts.size() > 0;
                accounts = teller.getAccounts(index, TOTAL/10)) {
                log.debug("got " + accounts.size() + " accounts");
                for(Account a: accounts) {
                    assertEquals("unexpected balance",
	                    (int)(100*index++), 
	                    (int)(100*a.getBalance()));
                }
            }            
            assertEquals("unexpected number of accounts:"+index, TOTAL, index);
        }
        catch (BankException ex) {
            log.fatal("error getting accounts:" + ex, ex);
            fail("error getting accounts:" + ex);
        }
    }
    
    @Test
    public void testGetLedger() throws Exception {
        log.info("*** testGetLedger ***");
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);
        int TOTAL = 100;

        try {
            for(int i=0; i<TOTAL; i++) {
                Account account = teller.createAccount("" + i);
                account.deposit(i);
                teller.updateAccount(account);                
            }

            Ledger ledger = teller.getLedger();
            assertNotNull("ledger is null", ledger);
            log.debug("got ledger:" + ledger);
            
            assertEquals("unexpected number of accounts:"+
                    ledger.getNumberOfAccounts(), 
                    TOTAL, ledger.getNumberOfAccounts());
        }
        catch (BankException ex) {
            log.fatal("error getting ledger:" + ex, ex);
            fail("error getting ledger:" + ex);
        }
    }

    @Test
    public void testGetLedger2() throws Exception {
        log.info("*** testGetLedger2 ***");
        TellerRemote teller = (TellerRemote)jndi.lookup(jndiName);
        int TOTAL = 100;

        try {
            for(int i=0; i<TOTAL; i++) {
                Account account = teller.createAccount("" + i);
                account.deposit(i);
                teller.updateAccount(account);                
            }

            Ledger ledger = teller.getLedger2();
            assertNotNull("ledger is null", ledger);
            log.debug("got ledger:" + ledger);
            
            assertEquals("unexpected number of accounts:"+
                    ledger.getNumberOfAccounts(), 
                    TOTAL, ledger.getNumberOfAccounts());
        }
        catch (BankException ex) {
            log.fatal("error getting ledger2:" + ex, ex);
            fail("error getting ledger2:" + ex);
        }
    }
}