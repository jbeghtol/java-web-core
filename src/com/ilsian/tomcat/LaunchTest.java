package com.ilsian.tomcat;

import java.util.HashMap;

import org.apache.catalina.LifecycleException;

import com.ilsian.commonweb.res.Loader;

/**
 * Test launcher to start the embedded tomcat WebServer.
 * Starts a web server with a couple test servlets, sleeps for a time, then cleanly
 * stops and exits.
 * @author justin
 *
 */
public class LaunchTest {

	public static void main(String [] args) throws LifecycleException, InterruptedException {
		
		WebServer ws = new WebServer(9181);
		System.err.println("Created WebServer on port 9181!");
		ws.registerServlet(new StaticResourceServlet(Loader.class), "SRS", new String [] { "/favicon.ico", "/res/*" } );
		System.err.println("Registered SRS servlet!");
		ws.registerServlet(new FTLServlet(Loader.createTemplateLoader(), new FTLDataMapFactory() {

			@Override
			public HashMap<?,?> getFTLDataMap(String tmplName) {
				HashMap<String,String> h = new HashMap<String,String>();
				h.put("testdata", "This is dynamic data!");
				return h;
			}
			
		}), "FTL", new String [] { "/ftl/*" } );
		System.err.println("Registered FTL servlet!");
		ws.startUp();
		System.err.println("Started tomcat! Sleeping 30s.");
		Thread.sleep(120000);
		ws.shutDown();
		System.err.println("Shutdown tomcat!");
	}

}
