package com.ilsian.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Load/serve static resources included in the JAR/Package from one or more class paths.
 * @author justin
 *
 */
public class StaticResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Logger logger = java.util.logging.Logger.getLogger(StaticResourceServlet.class.getCanonicalName());
	
	public static final int CACHE_DEFAULT = -1;	// default, do not specify any caching options
	public static final int CACHE_DISABLE = 0;  // disable caching for resources served
	
	private Vector<Class<?>> _hostClasses = new Vector<Class<?>>();	// classes to search for the resource
	private String _cacheControlHeader = null;					// generated cache control header
	
	/**
	 * Constructor.  Create a static resource servlet.
	 * @param baseResClass Class who shares a path with the resources
	 */
	public StaticResourceServlet(Class<?> baseResClass) {
		this(baseResClass, CACHE_DEFAULT);
	}
	
	/**
	 * Constructor.  Create a static resource servlet.
	 * @param baseResClass Class who shares a path with the resources
	 * @param cacheDays Resource cache policy
	 */
	public StaticResourceServlet(Class<?> baseResClass, int cacheDays) {
		// start off with ourself if nothing else
		addStaticResourceClass(baseResClass !=null?baseResClass:getClass());
		
		// setup any custom caching rules for static content - default leaves it null
		if (cacheDays == 0)
			_cacheControlHeader = "no-cache";
		else if (cacheDays > 0)
			_cacheControlHeader = String.format("max-age=%d", cacheDays * 24 * 60 * 60);
	}
	
	/**
	 * Add an additional classpath to resolve resources.
	 * @param c Class who shares a path with resources
	 * @return this, to facilitate adding loaders in a single line
	 */
	public StaticResourceServlet addStaticResourceClass(Class<?> c) {
		if (!_hostClasses.contains(c))
			_hostClasses.add(c);
		
		return this;
	}
	
	/**
	 * Determine the resource name from a URL request
	 * @param request The HTTP request
	 * @return The last element of the URL
	 */
	protected static String resourceFromRequest(HttpServletRequest request)
	{
		final String uri = request.getRequestURI();
		return uri.substring(uri.lastIndexOf('/')+1, uri.length());
	}
	
	/**
	 * Handle HTTP Get requests for static resources
	 * @param request - The HTTP request
	 * @param response - The HTTP response
	 */
	@Override
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {

		final String resource = resourceFromRequest(request);
		logger.finest(String.format("SRS:goGet[%s]=%s",request.getRequestURI(), resource));
		getClassResource(resource, response);
	}
	
	/**
	 * Serve a static resource by its name
	 * @param res The name of the resource
	 * @param response The HTTP response to write data
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void getClassResource(String res, HttpServletResponse response) throws ServletException, IOException
	{
		// no classes, no data
		if (_hostClasses.isEmpty())
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// determine the content type from extension
		if (res.endsWith("js"))
		{
			response.setContentType("text/javascript");
		}
		else if (res.endsWith("css"))
		{
			response.setContentType("text/css");
		}
		else if (res.endsWith("gif"))
		{
			response.setContentType("image/gif");
		}
		else if (res.endsWith("woff"))
		{
			response.setContentType("application/x-font-woff");
		}
		else if (res.endsWith("woff2"))
		{
			response.setContentType("application/font-woff2");
		}
		else if (res.endsWith("svg"))
		{
			response.setContentType("image/svg+xml");
		}
		else if (res.endsWith("ttf"))
		{
			response.setContentType("application/x-font-ttf");
		}
		else if (res.endsWith("ico"))
		{
			response.setContentType("image/x-icon");
		}
		else if (res.endsWith("html"))
		{
			response.setContentType("text/html");
		}
		else if (res.endsWith("png"))
		{
			response.setContentType("image/png");
		}
		else if (res.endsWith("jpg"))
		{
			response.setContentType("image/jpeg");
		}
		else if (res.endsWith(".css.map"))
		{
			response.setContentType("application/json");
		}
		else
		{
			// for security reasons, don't let static resources get pulled for things we aren't assigning
			// mime-types to, such as .java or .class files
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
	
		// search all class paths for this resource, in order, until located
		InputStream is = null;
		for (Class<?> c:_hostClasses)
		{
			is = c.getResourceAsStream(res);
			if (is != null)
				break;
		}
		
		if (is == null)
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		if (_cacheControlHeader != null)
			response.addHeader("cache-control", _cacheControlHeader);
		
		ServletOutputStream sos = response.getOutputStream();
		copyStream(is, sos);
		sos.flush();
		sos.close();
	}
	
	/**
	 * Copy data between in and out streams.
	 * @param in Stream to copy from
	 * @param out Stream to copy to
	 * @throws IOException
	 */
	static void copyStream( InputStream in, OutputStream out ) throws IOException
    {
	    byte[] buf = new byte[8*1024];
	    int len;
	    while ( ( len = in.read( buf ) ) != -1 )
	        out.write( buf, 0, len );
    }
	
}