package com.ilsian.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LDAPManager {
    
    private final String _url;
    private String _adminDN;
    public static String[] SASL_MECHANISMS = new String[] {
    		"9798-M-DSA-SHA1", "9798-M-ECDSA-SHA1", // There's a lot more...
    };
    
    /**
     * @param url - e.g. "ldap://localhost:389"
     * @param adminDN - e.g. "cn=admin,dc=testy,dc=local"
     */
    public LDAPManager(String url, String adminDN) {
        _url = url;
        _adminDN = adminDN;
    }
    
    private Hashtable<String, Object> getEnv(String userDN, String pass) {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        if (pass == null) {
            env.put(Context.SECURITY_AUTHENTICATION, "none");
        } else {
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_CREDENTIALS, pass);
        }
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, _url);
        return env;
    }
    
    public static void main(String[] args) throws NamingException {
        System.out.println("pnosrep groups:");
        LDAPManager ldap = new LDAPManager("ldap://localhost:389", "cn=admin,dc=testy,dc=local");
        System.out.println(ldap.getDN("pnosrep"));
        System.out.println("");
        System.out.println(ldap.getDN("aadmin"));
        System.out.println("");
        System.out.println("aadmin verified: " + ldap.verifyUser("aadmin", "asdf"));
        System.out.println("pnosrep verified: " + ldap.verifyUser("pnosrep", "asswrd"));
    }
    
    public String getDN(String userID) throws NamingException
    {
        Hashtable<String, Object> env = getEnv(_adminDN, null);
        LdapContext ctx = new InitialLdapContext(env, null);
        SearchResult r = findAccountByUserID(ctx, _adminDN, userID);
        return r.getNameInNamespace();
    }

    /**
     * 
     * @param userID - LDAP userID
     * @param userPW - LDAP user password
     * @param adminPW - LDAP admin password
     * @return whether the user-password combination is accessible given the admin password
     * @throws NamingException
     */
    public boolean verifyUser(String userID, String userPW)
    {
        Hashtable<String, Object> env = getEnv(_adminDN, null);
        LdapContext ctx;
        try {
            ctx = new InitialLdapContext(env, null);
            return verifyUser(ctx, _adminDN, userID, userPW);
        } catch (NamingException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean verifyUser(DirContext ctx, String ldapSearchBase, String userID, String userPW) throws NamingException
    {
        SearchResult r = findAccountByUserID(ctx, ldapSearchBase, userID);
        if (r == null) {
        	return false;
        }
        String dn = r.getNameInNamespace();
        Hashtable<String, Object> userEnv = getEnv(dn, userPW);
        try {
            new InitialDirContext(userEnv);
        } catch (NamingException e) {
            return false;
        }
        return true;
    }
    
    private SearchResult findAccountByUserID(DirContext ctx, String ldapSearchBase, String userID) throws NamingException
    {
        String searchFilter = "(&(objectClass=inetOrgPerson)(uid=" + userID + "))";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
        SearchResult searchResult = null;
        if(results.hasMoreElements()) {
             searchResult = (SearchResult) results.nextElement();
            //make sure there is not another item available, there should be only 1 match
            if(results.hasMoreElements()) {
                System.err.println("Matched multiple users for the accountName: " + userID);
                return null;
            }
        }
        return searchResult;
    }
}