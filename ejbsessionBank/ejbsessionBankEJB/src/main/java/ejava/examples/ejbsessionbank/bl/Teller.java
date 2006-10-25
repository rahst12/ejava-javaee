package ejava.examples.ejbsessionbank.bl;

import java.util.List;

import ejava.examples.ejbsessionbank.bo.Account;
import ejava.examples.ejbsessionbank.bo.LedgerDTO;

public interface Teller {
    Account createAccount(String accNum) throws BankException;    
    Account getAccount(String acctNum) throws BankException;    
    Account closeAccount(String acctNum) throws BankException;    
    void updateAccount(Account account) throws BankException;
    List<Account> getOverdrawnAccounts(int index, int count) 
        throws BankException;
    List<Account> getAccounts(int index, int count) 
        throws BankException;
    LedgerDTO getLedger() throws BankException;
}