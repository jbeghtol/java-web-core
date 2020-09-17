package com.ilsian.tomcat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ilsian.commonweb.res.Loader;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * AppServlet - Base class for an application level servlet with support for custom
 * GET and POST handlers as well as template handlers to render data from FTL templates.
 * 
 * The general approach uses a simple routing structure, where the target application
 * can specify the routing parameters (typically 'action', examples below use this
 * with a 'gui' servlet).  The general way this works is described here:
 * 
 * - All URL requests with gui?action=XXXX have their get handlers looked up from
 *   a get handlers map.  Any failure to lookup causes the default handler to be used
 *   instead (registered with the DEFAULT_HANDLER action).
 *   
 * - All URL requests with gui?ftl=XXXX are requests for pages to be rendered directly
 *   from freemarker templates.  For these requests, handlers are looked up from an
 *   ftl handlers map.  If no entry exists, they create one with the default data bindings
 *   and user permissions set to require kLoginUser permissions. All other URL parameters
 *   are placed in the datamap 'urlparams' field.  This allows FTL templates to dynamically
 *   handle custom params without any custom Java code.  If given template name doesn't
 *   exist, a default FTL handler is used instead (registered with the DEFAULT_HANDLER action).
 *   
 * - All POST requests with gui?action=XXXX have their post handlers looked up from
 *   a post handlers map.  Any failure to lookup causes an error.
 *   
 * - All POST requests with gui?ftl=XXXX use any registered TemplateResourceHandlers
 *   that include a post handler to serve back data.  This allows bidirectional communication
 *   for FTL resources.  User permissions are enforced prior to the post handler invocation
 *   to simplify security. Any failure to lookup for a GET causes the default handler to be used
 *   instead (registered with the DEFAULT_HANDLER action).  Any failure to lookpu for a POST
 *   causes a 
 *   
 * FTL CONTENT TYPE
 * - By default all FTL templates are served with content type 'text/html'.  Special file
 *   suffixes can be used to select alternate mime-types.
 * - Files ending in JSON.ftl -> 'application/json'
 * - Files ending in JS.ftl -> 'text/javascript'
 * - Files ending in CSS.ftl -> 'text/css'
 *
 * SUBCLASS RESPONSIBILITIES
 * - AppServlet subclasses should register all handlers in their own init methods using
 * the addGetHandler, addPostHandler, addFTLHander methods.
 * - Subclasses should provide an object implementing UserSecurity and the name of the
 * routing parameter (typically 'action') during their constructor
 * - Subclasses should implement the createDataMap method to create common data bindings 
 * for their FTL resources.
 * 
 * @author justin
 *
 */
public abstract class AppServlet extends HttpServlet {

	// name for all default handlers (used in all three handlers)
	public static final String DEFAULT_HANDLER = "::default";

	protected UserSecurity _userModel;		///< User security interface
	private String _routingParam;	///< The name of our routing param, normally 'action'
	Configuration _templateCfg=null;///< FTL Template loading
	
	// Handler maps for GET, POST, and FTL templates
	Hashtable<String, ActionHandler> _getHandlers = new Hashtable<String, ActionHandler>();
	Hashtable<String, ActionHandler> _postHandlers = new Hashtable<String, ActionHandler>();
	Hashtable<String, TemplateResourceHandler> _ftlHandlers = new Hashtable<String, TemplateResourceHandler>();
	
	/**
	 * Constructor
	 * @param userModel The user security implementation
	 * @param routeParam The query parameter used to route services
	 */
	public AppServlet(UserSecurity userModel, String routeParam) {
		_userModel = userModel;
		_routingParam = routeParam;
	}
	
	/**
	 * Servlet initialization
	 */
	public void init() throws ServletException {
		// creates a template resource loader for core and local ftl resources
		// based on the final subclass classpath
		_templateCfg = Loader.createTemplateLoader(this.getClass(), "ftl");
	}
	
	/**
	 * Add a new handler for GET operations
	 */
	public void addGetHandler(String act, ActionHandler hnd) {
		_getHandlers.put(act, hnd);
	}
	
	/**
	 * Add a new handler for POST operations
	 */
	public void addPostHandler(String act, ActionHandler hnd) {
		_postHandlers.put(act, hnd);
	}
	
	/**
	 * Add a new handler for FTL Get/Post operations
	 */
	public void addFtlHandler(String ftlname, TemplateResourceHandler hnd) {
		_ftlHandlers.put(ftlname, hnd);
	}
	
	/**
	 * Create a data model for rendering an FTL template
	 * @param user - User requesting the template page, or null if user hasn't logged in
	 * @param request - The associated HTTP request (useful for adding Session data)
	 * @return A data model for FTL rendering
	 */
	public abstract HashMap createDataMap(UserInfo user, HttpServletRequest request);
	
	/**
	 * doGet - Handle Web GET requests
	 * Prioritized routing - 
	 * (1) - No user logged in? render the login prompt via provided subclass template
	 * (2) - Query contains 'ftl'? Render given FTL template (or the DEFAULT ftl handler if
	 *       it is not located)
	 * (3) - Render using getHandler matching the provided action (or the DEFAULT get handler
	 *       if the action is not found)
	 */
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		
		UserInfo user = _userModel.getUserInfo(request, response);
		if (user == null)
		{
			// no user?  model has redirected to a login page, serve nothing
			return;
		}
		
		// special handling for the 'ftl' query
		final String ftlQ = ftlFromParam(request);
		if (ftlQ != null)
		{
			// generic FTL requests
			handleActionFTLGet(ftlQ, user, request, response);
			return;
		}
		
		if (user.mLevel == UserInfo.kLoginInvalid)
		{
			// FTL handlers have security checking built-in, but GETs do not, so
			// we only handle GET actions when we have a valid user.  This should
			// not generally happen, as the only time kLoginInvalid is returned is
			// for rendering Login pages, which are required to be in FTL.
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		// finally, dispatch to one of our action handlers
		final String action = request.getParameter(_routingParam);
		handleActionGet(action, user, request, response);
	}

	/*
	 * doPost - Handle Web POST requests
	 * Prioritized routing - 
	 * (1) - Check any action parameter to see if its a POST message for logging in.  If it
	 *       returns true, it has redirected and no further handling is needed.
	 * (2) - No user logged in? render the login prompt via provided subclass template
	 * (3) - Query contains 'ftl'? Render given FTL template (or the DEFAULT ftl handler if
	 *       it is not located, for GET only - missing templates for POST responsed with an
	 *       error: SC_NOT_FOUND)
	 * (4) - Render using postHandler matching the provided action (or the DEFAULT get handler
	 *       if the action is not found - missing post handlers use the DEFAULT post handler)
	 */
	@Override
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		
		final String action = request.getParameter(_routingParam);
		
		// get user login info
		UserInfo userInfo = _userModel.getUserInfo(request, response);
		if (userInfo == null)
		{
			// no user?  model has redirected to a login page, serve nothing
			return;
		}
		
		// special handling for the 'ftl' query on POST
		final String ftlQ = ftlFromParam(request);
		if (ftlQ != null)
		{
			// generic FTL requests
			handleActionFTLPost(ftlQ, userInfo, request, response);
			return;
		}
		
		if (userInfo.mLevel == UserInfo.kLoginInvalid)
		{
			// FTL handlers have security checking built-in, but GETs do not, so
			// we only handle GET actions when we have a valid user.  This should
			// not generally happen, as the only time kLoginInvalid is returned is
			// for rendering Login pages, which are required to be in FTL.
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		handleActionPost(action, userInfo, request, response);
	}
	 
	protected void handleActionGet(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ActionHandler h = null;
		if (action != null)
			h = _getHandlers.get(action);
		if (h == null)
			h = _getHandlers.get(DEFAULT_HANDLER);
		if (h != null)
		{
			h.handleAction(action, user, request, response);
			return;
		}
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No default handlers found for App Server.");
	}
	
	protected void handleActionFTLGet(String ftl, UserInfo user, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final ActionHandler h = _ftlHandlers.get(ftl);
		if (h != null)
		{
			h.handleAction(ftl, user, request, response);
			return;
		}
		else
		{
			// no registered handlers, but we allow any FTL request, with default security and no data model
			new TemplateResourceHandler(ftl).handleAction(ftl, user, request, response);
			return;
		}
	}

	protected void handleActionPost(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ActionHandler h = null;
		if (action != null)
			h = _postHandlers.get(action);
		if (h == null)
			h = _postHandlers.get(DEFAULT_HANDLER);
		if (h != null)
		{
			h.handleAction(action, user, request, response);
			return;
		}
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No default handlers found for App Server.");
	}
	
	protected void handleActionFTLPost(String ftl, UserInfo user, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final TemplateResourceHandler h = _ftlHandlers.get(ftl);
		if (h != null)
		{
			h.handleFTLPost(ftl, user, request, response);
			return;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
	// FTL resources may be requested w/o the .ftl extention 
	private static final String ftlFromParam(HttpServletRequest request)
	{
		final String param = request.getParameter("ftl");
		if (param == null)
			return param;
		else if (param.endsWith(".ftl"))
			return param;
		return param + ".ftl";
	}

	protected static String getContentTypeForTemplate(final String templateName)
	{
		if (templateName.endsWith("JSON.ftl"))
			return "application/json";
		else if (templateName.endsWith("JS.ftl"))
			return "text/javascript";
		else if (templateName.endsWith("CSS.ftl"))
			return "text/css";
		else
			return "text/html";	
	}
	
	protected void serveTemplate(UserInfo user, HttpServletRequest request, HttpServletResponse response, String templateName, HashMap extraData) throws IOException
	{
		final String mtype = getContentTypeForTemplate(templateName);
		response.setContentType(mtype);
		ServletOutputStream p = response.getOutputStream();
		
		Template t = null;
		try {
			t = _templateCfg.getTemplate(templateName);
		} catch (FileNotFoundException fnf) {
			// in case a URL somehow (manually?) points to a non-existent FTL file, we 
			// should try to render a nicer page than a stack trace
			t = _templateCfg.getTemplate(_ftlHandlers.get(DEFAULT_HANDLER).getTemplateName());
		}
		
		final HashMap dmap = createDataMap(user, request);
		if (extraData != null)
		{
			dmap.putAll(extraData);
		}
		
		// embed also the query info, so template rendering can adjust to queries without
		// having specialized java code
		dmap.put("urlparams", request.getParameterMap());
		// also embed the name of our FTL name in case the template needs to refer to itself
		dmap.put("ftlname", templateName);
		
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
	
	public class TemplateResourceHandler implements ActionHandler
	{
		protected String _templateName;
		protected String _title;
		protected HashMap _extraData;
		protected TemplateDataFactory _extraDataFactory = null;
		boolean _factoryRefreshAlways;
		int _minPermit = UserInfo.kLoginUser;
		ActionHandler _postHandler = null;
		
		public TemplateResourceHandler() {
			this(null);
		}
		
		public TemplateResourceHandler(String resName) {
			_templateName = resName;
		}
		
		public TemplateResourceHandler setMinSecurity(int level) {
			_minPermit = level;
			return this;
		}
		
		public TemplateResourceHandler setPostHandler(ActionHandler h) {
			_postHandler = h;
			return this;
		}
		
		public TemplateResourceHandler setDataFactory(TemplateDataFactory tdf, boolean refreshAlways) {
			_extraDataFactory = tdf;
			_factoryRefreshAlways = refreshAlways;
			return this;
		}
		
		public String getTemplateName() {
			return _templateName;
		}
		
		/**
		 * An interaction is an object that supports both a data adaptor to configure the template
		 * for GUI rendering and a POST handler to receive and respond to Ajax POST requests from 
		 * the template.
		 * @param actor
		 * @param refreshAlways
		 * @return
		 */
		public TemplateResourceHandler setInteraction(TemplateInteraction actor, boolean refreshAlways) {
			_extraDataFactory = actor;
			_factoryRefreshAlways = refreshAlways;
			_postHandler = actor;
			return this;
		}
	
		@Override
		public void handleAction(String action, UserInfo user,
				HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
			if (user != null && user.mLevel < _minPermit)
			{
				_userModel.loginSecurityRedirect(response, _minPermit, user.mLevel);
				return;
			}
			
			if (_extraDataFactory != null && _extraData == null || _factoryRefreshAlways)
				_extraData = _extraDataFactory.buildTemplateData();
			
			serveTemplate(user, request, response, _templateName!=null?_templateName:action, _extraData);
			
		}
		
		/**
		 * Method called by the framework on an HTTP post to an FTL handler
		 */
		public void handleFTLPost(String ftl, UserInfo user,
				HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
			// insure user permissions are respected
			if (user.mLevel < _minPermit)
			{
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			}
			else if (_postHandler != null)
			{
				// if security passes, route the data to their post handler
				_postHandler.handleAction(ftl, user, request, response);
			}
			else
			{
				// no post handler, this was not found
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}
}
