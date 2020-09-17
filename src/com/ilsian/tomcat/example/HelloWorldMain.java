package com.ilsian.tomcat.example;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.LifecycleException;

import com.ilsian.commonweb.res.Loader;
import com.ilsian.tomcat.StaticResourceServlet;
import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.UserSecurity;
import com.ilsian.tomcat.WebServer;

/**		
 * HelloWorldMain
 * 
 * Test application to run a simple Hello World web application.  This application 'main'
 * creates a local instance of a very simple AppServlet running in a web container.
 * It uses a dummy security model that always has a user logged in and servers only the
 * HelloWorld.ftl template and handles POST operations from that FTL to shut down the server.
 * 
 * To test:
 * 1) run this classes Main
 * 2) Goto http://localhost:9988/hello
 * 3) Click the 'STOP SERVER' button to kill the demo
 * 
 * @author justin
 *
 */
public class HelloWorldMain {

	public static void main(String[] args) {
		final WebServer server = new WebServer(9988, 
					null,
					null,
					false);
		// our 'default' servlet, serve static resources and the root favicon
		server.registerServlet(new StaticResourceServlet(Loader.class), "SRS", new String [] { "/favicon.ico", "/res/*" } );

		UserSecurity noSecurity = new UserSecurity() {

			@Override
			public void handleAction(String action, UserInfo user,
					HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
				// Hijacked this to handle the POST button press to quit the demo
				response.setContentType("text/html");
				ServletOutputStream p = response.getOutputStream();
				p.println("<html><body><h1>Shutting down in 2s!</h1></body>");
				p.flush();
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(2000);
							server.shutDown();
						} catch (Exception ignore) {
							
						}
					}
				}).start();
			}

			@Override
			public UserInfo getUserInfo(HttpServletRequest req,
					HttpServletResponse resp) throws IOException {
				return new UserInfo("user", UserInfo.kLoginUser);
			}

			@Override
			public void loginSecurityRedirect(HttpServletResponse response,
					int reqLevel, int currLevel) throws IOException {
				// not used, for handling redirect when no user
			}
		};
		server.registerServlet(new HelloWorldAppServlet(noSecurity, "action"), "HELLO", new String [] { "/hello" });
		try {
			server.startUp();
		} catch (LifecycleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
