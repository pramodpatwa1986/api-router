package com.worldpay.api.router;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

@WebServlet("/apiRouter/*")
public class ApiRouter extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(getRequestUrl(request));
		prepareRequest(request, getRequest);
		HttpResponse sourceResp = httpClient.execute(getRequest);
		sendResponse(response, sourceResp);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(getRequestUrl(request));
		prepareRequest(request, postRequest);
		HttpResponse sourceResp = httpClient.execute(postRequest);
		sendResponse(response, sourceResp);

	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut putRequest = new HttpPut(getRequestUrl(request));
		prepareRequest(request, putRequest);
		HttpResponse sourceResp = httpClient.execute(putRequest);
		sendResponse(response, sourceResp);

	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpOptions optionRequest = new HttpOptions(getRequestUrl(request));
		prepareRequest(request, optionRequest);
		HttpResponse sourceResp = httpClient.execute(optionRequest);
		sendResponse(response, sourceResp);

	}

	/**
	 * Method picks url from web.xml and prepare target url based on incoming http
	 * request
	 * 
	 * @param request http servlet request
	 * @return target url
	 */
	private String getRequestUrl(HttpServletRequest request) {

		String requestUrl = getServletContext().getInitParameter("apiBaseUrl");

		requestUrl = requestUrl
				+ ((request.getPathInfo() != null && request.getPathInfo().length() > 0) ? request.getPathInfo() : "");

		requestUrl = requestUrl + ((request.getQueryString() != null && request.getQueryString().length() > 0)
				? "?" + request.getQueryString()
				: "");

		return requestUrl;

	}

	/**
	 * Prepare target request
	 * 
	 * @param request     request received from client
	 * @param postRequest target api request
	 */
	private void prepareRequest(HttpServletRequest request, HttpRequestBase postRequest) {
		try {
			copyRequestHeaders(request, postRequest);

			if (postRequest.getMethod().equalsIgnoreCase("POST") || postRequest.getMethod().equalsIgnoreCase("PUT")) {

				byte[] requestBody = new byte[1024];
				request.getInputStream().read(requestBody);
				HttpEntity reqBody = new StringEntity(new String(requestBody));

				if (postRequest.getMethod().equalsIgnoreCase("POST")) {
					((HttpPost) postRequest).setEntity(reqBody);
				} else if (postRequest.getMethod().equalsIgnoreCase("PUT")) {
					((HttpPut) postRequest).setEntity(reqBody);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * return response to caller client
	 * 
	 * @param response   Http servlet response
	 * @param sourceResp response received from calling API
	 */
	private void sendResponse(HttpServletResponse response, HttpResponse sourceResp) {
		try {
			copyResponseHeaders(response, sourceResp);
			response.setStatus(sourceResp.getStatusLine().getStatusCode());
			if (sourceResp != null && sourceResp.getEntity() != null) {
				response.getOutputStream().write((EntityUtils.toString(sourceResp.getEntity())).getBytes());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Copy all request header except content length and host as they are dynamically
	 * prepared by HTTP client
	 * 
	 * @param sourceReq client request
	 * @param targetReq API request
	 */
	private void copyRequestHeaders(HttpServletRequest sourceReq, HttpRequestBase targetReq) {
		Enumeration<String> headerNames = sourceReq.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if (!headerName.equalsIgnoreCase("content-length") && !headerName.equalsIgnoreCase("host")) {
				targetReq.setHeader(headerName, sourceReq.getHeader(headerName));
			}
		}
	}

	/**
	 * Copy response headers from API header to target response
	 * 
	 * @param targetResp response needs to be return to client
	 * @param sourceResp response received from API
	 */
	private void copyResponseHeaders(HttpServletResponse targetResp, HttpResponse sourceResp) {
		for (Header header : sourceResp.getAllHeaders()) {
			targetResp.setHeader(header.getName(), header.getValue());
		}
	}
}