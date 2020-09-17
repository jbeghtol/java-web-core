package com.ilsian.tomcat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * The FTLServlet uses a freemarker template manager to render the last part of a URL
 * as an FTL template.  Templates are located using the provided Configuration, which
 * resolves actual template locations, and a factory to create the dynamnic datamap
 * to facilitate rendering the template.  It is presumed that all templates in this
 * context generate HTML.
 * 
 * @author justin
 *
 */
public class FTLServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Logger logger = java.util.logging.Logger.getLogger(StaticResourceServlet.class.getCanonicalName());
	
	Configuration _FTLLoader;		// FTL template loader
	FTLDataMapFactory _dataFactory;	// Factory to locate data maps for template rendering

	/**
	 * Constructor.  Create a Freemarker rendering servlet.
	 * @param ftlConfig The freemarker template loader
	 * @param dataFactory A factory to get datamaps by template
	 */
	public FTLServlet(Configuration ftlConfig, FTLDataMapFactory dataFactory) {
		_FTLLoader = ftlConfig;
		// assign a data factory, or our own 'empty' version if none is provided
		_dataFactory = dataFactory != null ? dataFactory : new FTLDataMapFactory() {

			@Override
			public HashMap<?,?> getFTLDataMap(String tmplName) {
				// this is silly, but you can't create an empty map with ? types
				return new HashMap<String, String>();
			}

		};
	}

	/**
	 * Determine the FTL template name from a URL request
	 * @param request The HTTP request
	 * @return The last element of the URL
	 */
	protected static String resourceFromRequest(HttpServletRequest request) {
		final String uri = request.getRequestURI();
		return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
	}

	/**
	 * Handle HTTP Get requests for templates
	 * @param request - The HTTP request
	 * @param response - The HTTP response
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		final String templateName = resourceFromRequest(request);
		logger.finest(String.format("FTL:goGet[%s]=%s", request.getRequestURI(), templateName));

		response.setContentType("text/html");
		ServletOutputStream p = response.getOutputStream();

		final Template t = _FTLLoader.getTemplate(templateName);
		final HashMap<?,?> dmap = _dataFactory.getFTLDataMap(templateName);

		OutputStreamWriter osw = new OutputStreamWriter(p);
		try {
			t.process(dmap, osw);
		} catch (TemplateException e) {
			throw new IOException("Template error: " + e.getMessage());
		}
		osw.flush();
		p.flush();
		p.close();
	}
}
