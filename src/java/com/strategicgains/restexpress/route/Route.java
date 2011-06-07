/*
    Copyright 2010, Strategic Gains, Inc.

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.strategicgains.restexpress.route;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMethod;

import com.strategicgains.restexpress.Request;
import com.strategicgains.restexpress.Response;
import com.strategicgains.restexpress.exception.ServiceException;
import com.strategicgains.restexpress.url.UrlMatch;
import com.strategicgains.restexpress.url.UrlMatcher;


/**
 * A Route is an immutable relationship between a URL pattern and a REST
 * service.
 * 
 * @author toddf
 * @since May 4, 2010
 */
public abstract class Route
{
	// SECTION: INSTANCE VARIABLES

	private UrlMatcher urlMatcher;
	private Object controller;
	private Method action;
	private HttpMethod method;
	private boolean shouldSerializeResponse = true;
	private boolean shouldUseWrappedResponse = true;
	private boolean shouldUseStreamedResponse = false;
	private String name;
	private List<String> supportedFormats = new ArrayList<String>();
	private String defaultFormat;
	private Set<String> flags = new HashSet<String>();
	private Map<String, Object> parameters = new HashMap<String, Object>();

	// SECTION: CONSTRUCTORS

	/**
	 * @param urlMatcher
	 * @param controller
	 */
	public Route(UrlMatcher urlMatcher, Object controller, Method action, HttpMethod method, boolean shouldSerializeResponse,
		boolean shouldUseWrappedResponse, boolean shouldUseStreamedResponse, String name, Set<String> flags, Map<String, Object> parameters)
	{
		super();
		this.urlMatcher = urlMatcher;
		this.controller = controller;
		this.action = action;
		this.method = method;
		this.shouldSerializeResponse = shouldSerializeResponse;
		this.shouldUseWrappedResponse = shouldUseWrappedResponse;
		this.shouldUseStreamedResponse = shouldUseStreamedResponse;
		this.name = name;
		this.flags.addAll(flags);
		this.parameters.putAll(parameters);
	}
	
	public boolean isFlagged(String flag)
	{
		return flags.contains(flag);
	}
	
	public boolean hasParameter(String name)
	{
		return (getParameter(name) != null);
	}

	public Object getParameter(String name)
	{
		return parameters.get(name);
	}
	
	public Method getAction()
	{
		return action;
	}
	
	public Object getController()
	{
		return controller;
	}
	
	public HttpMethod getMethod()
	{
		return method;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean hasName()
	{
		return (getName() != null && !getName().trim().isEmpty());
	}
	
	public String getPattern()
	{
		return urlMatcher.getPattern();
	}
	
	public boolean shouldSerializeResponse()
	{
		return shouldSerializeResponse;
	}
	
	public boolean shouldUseWrappedResponse()
	{
		return shouldUseWrappedResponse;
	}
	
	public boolean shouldUseStreamedResponse()
	{
		return shouldUseStreamedResponse;
	}
	
	public void addSupportedFormat(String format)
	{
		if (!supportsFormat(format))
		{
			supportedFormats.add(format);
		}
	}

	public boolean supportsFormat(String format)
	{
		return supportedFormats.contains(format);
	}
	
	public String getDefaultFormat()
	{
		return defaultFormat;
	}

	public UrlMatch match(String url)
	{
		return urlMatcher.match(url);
	}
	
	public List<String> getUrlParameters()
	{
		return urlMatcher.getParameterNames();
	}

	public Object invoke(Request request, Response response)
	{
		try
        {
	        return action.invoke(controller, request, response);
        }
		catch (InvocationTargetException e)
		{
			Throwable cause = e.getCause();
			
			if (RuntimeException.class.isAssignableFrom(cause.getClass()))
			{
				throw (RuntimeException) e.getCause();
			}
			else
			{
				throw new RuntimeException(cause);
			}
		}
        catch (Exception e)
        {
        	throw new ServiceException(e);
        }
	}
}
