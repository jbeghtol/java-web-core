package com.ilsian.tomcat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple servlet that redirects all requests elsewhere, useful to 
 * prevent servlet context errors if a bad suffix path is given.
 * @author justin
 *
 */
public class RedirectServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private String _redirectUrl;	// The URL we should redirect everything to
	
	/**
	 * Constructor.  Create a redirect servlet.
	 * @param redirectUrl URL to redirect all requests to
	 */
	public RedirectServlet(String redirectUrl) {
		_redirectUrl = redirectUrl;
	}
	
	/**
	 * Handle HTTP Get requests and redirect them
	 * @param request - The HTTP request
	 * @param response - The HTTP response
	 */
	@Override
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect(_redirectUrl);
	}
}
