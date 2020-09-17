package com.ilsian.tomcat.example;

import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.ilsian.tomcat.AppServlet;
import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.UserSecurity;

/**
 * HelloWorldAppServlet
 * 
 * A simple demo App Servlet.  Sets up the default GET handler to render the HelloWorld.ftl
 * template and handles POST actions from that template to use out dummy security model's
 * action handler to shut down the server.
 * 
 * @author justin
 *
 */
public class HelloWorldAppServlet extends AppServlet {

	public HelloWorldAppServlet(UserSecurity userModel, String routeParam) {
		super(userModel, routeParam);
	}

	@Override
	public HashMap createDataMap(UserInfo user, HttpServletRequest request) {
		HashMap hmap = new HashMap();
		// this message is rendered inside the HelloWorld.ftl template
		hmap.put("message", "Hello, world!");
		return hmap;
	}
	
	public void init() throws ServletException {
		super.init();
		
		// setup HTTP-GET handlers
		addGetHandler(DEFAULT_HANDLER, new TemplateResourceHandler("HelloWorld.ftl"));
		// this routes all POST messages to the HelloWorld.ftl back to our user security handler, which exits
		addFtlHandler("HelloWorld.ftl", new TemplateResourceHandler().setPostHandler(_userModel));
	}
}
