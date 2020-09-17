package com.ilsian.tomcat;

import java.util.HashMap;

/**
 * FTLDataMapFactory defines a simple interface by which an embedded
 * web application can provide template-specific or global data maps
 * to Freemarker templates rendered by the FTLServlet.
 * 
 * @author justin
 */
public interface FTLDataMapFactory {
	/**
	 * Get a datamap to connect templates with dynamic data.
	 * @param tmplName - Template name
	 * @return A datamap for this template
	 */
	public HashMap<?,?> getFTLDataMap(String tmplName);
}
