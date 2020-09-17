package com.ilsian.tomcat;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UserSecurity - An interface to provide basic user permissions and access
 * security.
 * 
 * The security module is provided to the core AppServlet to manage routing
 * of web services with different user permission levels.  The details of
 * how users are stored, managed, and verified should be implemented by
 * the specific application according to its needs.
 * 
 * NOTE: This class does not generally directly implement any look and feel
 * or rendering, but generally uses redirects or provides templates back to
 * the application or core AppServlet can route to rendering components.
 * 
 * Implementations should:
 * - Allow their own login FTL resources to return a user with permission kLoginInvalid
 * - Return null users and redirect to Login pages for all other queries when no user is logged in
 * - Implement a POST action handler to receive login attempts
 * - Provide a security redirect action when resources exceed user permission levels
 * 
 * @author justin
 *
 */
public interface UserSecurity extends ActionHandler {
	
	/**
	 * Get the current USER for a request via session info.  If null
	 * is returned, caller should do no further rendering as a redirect
	 * has occurred.  A UserInfo with permission kLoginInvalid is
	 * returned when the target resource relates to Logging in.
	 * 
	 * @param req Servlet request
	 * @param resp Servlet response
	 * @return A user record or null if no user is logged in
	 */
	public UserInfo getUserInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException;
	
	/**
	 * Application specific redirection to a security error page due to permission failure
	 * @param response Servlet response
	 * @param reqLevel Required security level
	 * @param currLevel User's security level
	 * @throws IOException
	 */
	public void loginSecurityRedirect(HttpServletResponse response, int reqLevel, int currLevel) throws IOException;
	
}
