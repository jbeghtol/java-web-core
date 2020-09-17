package com.ilsian.tomcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

/**
 * A wrapper to manage running an instance of embedded tomcat to service
 * java-based dyanamic web content.
 * @author justin
 *
 */
public class WebServer {

	static final String CONTEXT_PATH = "";			// context path, none, all at root
	static final String DOC_BASE = ".";				// doc base, not really used
	static final String DEF_WORK_DIR = ".work";		// work directory, created but not populated 
	static final String DEF_CONNECTOR_PROPS = "connector.props";   // properties to configure the tomcat connector
	static final String DEF_HTTPS_CONNECTOR_PROPS = "httpsconnector.props";   // properties to configure the tomcat connector
	
	static Logger logger = java.util.logging.Logger.getLogger(WebServer.class.getCanonicalName());
	
	protected Tomcat _tomcat;		// instance of the tomcat server
	protected Context _context;		// instance of our base context
	protected int _serverPort;		// port to run on
	
	/**
	 * Constructor.  Create a new web server.
	 * @param port Network port to bind for webserver
	 */
	public WebServer(int port) {
		this(port, null, DEF_WORK_DIR, false);
	}
	
	/**
	 * Constructor.  Create a new webserver.
	 * NOTE: When using with HTTPS, information about the keystore must be 
	 * provided. This is most directly accomplished by creating connector
	 * attributes using the WebServer method
	 * getDefaultHTTPSConnectorAttributes(String alias, String storePass, String storeFile).
	 * 
	 * @param port Network port to bind for webserver
	 * @param connectorAttributes Properties to configure webserver, or null for defaults
	 * @param workDir Working directory for webserver or null to use the default
	 * @param secure True when using HTTPS
	 */
	public WebServer(int port, Properties connectorAttributes, String workDir, boolean secure) {
		_serverPort = port;
		_tomcat = new Tomcat();
		_tomcat.setPort(_serverPort);
		_tomcat.setBaseDir(workDir==null?DEF_WORK_DIR:workDir);
		_tomcat.getHost().setAppBase(".");

		// create base connector
		final Connector c = _tomcat.getConnector();
		
		// configure the connector (http or https) using custom key/value pairs
		if (connectorAttributes == null)
		{
			connectorAttributes = secure?getDefaultHTTPSConnectorAttributes():
				getDefaultConnectorAttributes();
			logger.info("Using default connector configurations.");
		}
		else
		{
			logger.info("Using custom connector configuration.");
		}
		
		for (String p : connectorAttributes.stringPropertyNames())
		{
			c.setAttribute(p, connectorAttributes.getProperty(p));
		}
		
		if (secure)
		{
			// configure https scheme
			c.setSecure(true);
			c.setScheme("https");
		}
		
		// setup the base context
		_context = _tomcat.addContext(CONTEXT_PATH, new File(DOC_BASE).getAbsolutePath());
	}

	/**
	 * Register a new servlet for the webserver.  Note: In the future we might use the Servlet Annotations to 
	 * determine the name and URL patterns.
	 * 
	 * @param srvlet Servlet object
	 * @param name Name of servlet
	 * @param patterns Url patterns to route to servlet
	 */
	public void registerServlet(HttpServlet srvlet, String name, String[] patterns) {
		_tomcat.addServlet(CONTEXT_PATH, name, srvlet);
		for (String p:patterns)
			_context.addServletMappingDecoded(p, name);
	}
	
	/**
	 * Start the webserver - nothing binds or runs until this is called
	 * @throws LifecycleException
	 */
	public void startUp() throws LifecycleException {
		_tomcat.start();
	}
	
	/**
	 * Stop the webserver - tears down and destroys everything
	 * @throws LifecycleException
	 */
	public void shutDown() throws LifecycleException {
		_tomcat.stop();
		_tomcat.getServer().await();
		// this is required to really DIE
		_tomcat.destroy();
	}
	
	/**
	 * Get the default connector attributes
	 * @return A properties file with attributes
	 */
	public static Properties getDefaultConnectorAttributes() {
		Properties p = new Properties();
		InputStream is = WebServer.class.getResourceAsStream(DEF_CONNECTOR_PROPS);
		if (is != null)
		{
			try {
				p.load(is);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		return p;
	}
	
	/**
	 * Get the default connector attributes for HTTPS
	 * @return A properties file with attributes
	 */
	public static Properties getDefaultHTTPSConnectorAttributes() {
		Properties p = new Properties();
		InputStream is = WebServer.class.getResourceAsStream(DEF_HTTPS_CONNECTOR_PROPS);
		if (is != null)
		{
			try {
				p.load(is);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		return p;
	}
	
	/**
	 * Get the default HTTPS connector parameters, appending the defined keystore
	 * fields.
	 * @param keyAlias Name of the secure key
	 * @param keystorePass Password of the keystore
	 * @param keystoreFile Location of the keystore file
	 * @return
	 */
	public static Properties getDefaultHTTPSConnectorAttributes(String keyAlias, String keystorePass, String keystoreFile) {
		final Properties p = getDefaultHTTPSConnectorAttributes();
		p.setProperty("keyAlias", keyAlias);
		p.setProperty("keystorePass", keystorePass);
		p.setProperty("keystoreFile", keystoreFile);
		return p;
	}
	
}
