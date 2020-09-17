package com.ilsian.tomcat;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * WebLib
 * Static wrapper for method to simplify HTTP request handling.
 * @author justin
 *
 */
public class WebLib {

	/**
	 * Extract a String param from an HTTP request, returning a default
	 * value if it does not exist.
	 */
	public static String getStringParam(HttpServletRequest request, String paramName, String paramDefault)
	{
		final String actual = request.getParameter(paramName);
		if (actual == null)
			return paramDefault;
		else
			return actual;
	}
	
	/**
	 * Extract a boolean param from an HTTP request, returning a default
	 * value if it does not exist.
	 */
	public static boolean getBoolParam(HttpServletRequest request, String paramName, boolean paramDefault)
	{
		final String actual = request.getParameter(paramName);
		if (actual == null)
			return paramDefault;
		else
			return actual.equalsIgnoreCase(Boolean.TRUE.toString()) || actual.equalsIgnoreCase("on");
	}
	
	/**
	 * Extract a int param from an HTTP request, returning a default
	 * value if it does not exist or is invalid.
	 */
	public static int getIntParam(HttpServletRequest request, String paramName, int paramDefault)
	{
		final String actual = request.getParameter(paramName);
		if (actual == null)
			return paramDefault;
		try {
			return Integer.parseInt(actual);
		} catch (NumberFormatException nfe) {
			return paramDefault;
		}
	}
	
	/**
	 * Extract an attribute from an HTTP session, returning a default
	 * value if it does not exist.
	 */
	public static Object getSesssinAttribute(HttpSession session, String aName, Object paramDefault)
	{
		final Object actual = session.getAttribute(aName);
		if (actual == null)
			return paramDefault;
		else
			return actual;
	}
	
	/**
	 * Render a simple JSON response message formatted as {result: bool, message: text}
	 */
	public static void renderStandardJSONResponse(HttpServletResponse response, boolean success, String message ) throws IOException
	{
		response.setContentType("application/json");
		ServletOutputStream p = response.getOutputStream();
		JSONObject jobj = new JSONObject();
		try {
			// put some data from the request back into the response to validate
			jobj.put("result", success);
			jobj.put("message", message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		p.print(jobj.toString());			
		p.flush();	
	}
	
	/**
	 * Render a simple JSON response message formatted as {result: bool, message: text}
	 */
	public static void renderArrayJSONResponse(HttpServletResponse response, boolean [] success, String [] message ) throws IOException
	{
		response.setContentType("application/json");
		ServletOutputStream p = response.getOutputStream();
		JSONArray json = new JSONArray();
		try {
			for (int i=0;i<success.length; i++)
			{
				JSONObject jobj = new JSONObject();
				jobj.put("result", success[i]);
				jobj.put("message", message[i]);
				json.put(jobj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		p.print(json.toString());			
		p.flush();	
	}
}
