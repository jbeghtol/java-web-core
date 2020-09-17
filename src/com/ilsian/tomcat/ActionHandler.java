package com.ilsian.tomcat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ActionHandler - An interface that handles HTTP GET or POST requests
 * 
 * @author justin
 *
 */
public interface ActionHandler
{
	public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}