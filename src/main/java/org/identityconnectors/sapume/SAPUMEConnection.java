/**
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description : Class to represent a SAP UME Connection
 * Source code : SAPUMEConnection.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.openspml.message.Filter;
import org.openspml.message.FilterTerm;
import org.openspml.message.SearchRequest;
import org.openspml.message.SpmlResponse;

public class SAPUMEConnection implements SAPUMEConstants {
	/**
	 * Setup logging for the {@link SAPUMEConnector}.
	 */
	private static final Log LOG = Log.getLog(SAPUMEConnection.class);
	private HttpURLConnection objHttpURLConnection;
	private SAPUMEConfiguration configuration = null;

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            {@link org.identityconnectors.sapume.SAPUMEConfiguration} to
	 *            be used
	 */
	public SAPUMEConnection(final SAPUMEConfiguration configuration) {
		LOG.info("BEGIN");
		this.configuration = configuration;
	}

	/**
	 * Release internal resources
	 */
	public void dispose() {
		LOG.info("BEGIN");
		configuration = null;
		// Since we are getting connection every time before processing SPML
		// request,this method will be empty always.
		LOG.info("END");
	}

	/*
	 * Gets the connection
	 * 
	 * @return {@link java.net.HttpURLConnection} of the created object
	 */
	public HttpURLConnection getConnection() {

		String sUrl = null;
		final String sUserId;
		final String sPassword;
		URLConnection httpConnection = null;

		LOG.info("BEGIN");
		final String sMethodName="getConnection";
		try {
			sUrl = this.configuration.getUmeUrl();
			sUserId = this.configuration.getUmeUserId();
			sPassword = decode(this.configuration.getUmePassword());
			if (sUserId == null || sPassword == null) {
				throw new ConnectorException(configuration
						.getMessage("SAPUME_ERR_INVALID_CREDENTIAL")
						+ " " + new ConnectorException().getMessage(),
						new ConnectorException());
			}
			URL url = new URL(sUrl);
			LOG.error("Perf: "+sMethodName+" started for user {0} ",sUserId);
			URLConnection connection = url.openConnection();
			LOG.error("Perf: "+sMethodName+" completed for user {0} "+sUserId);
			httpConnection = connection;

			String connectStr = sUserId + ":" + sPassword;
			String encoding = new sun.misc.BASE64Encoder().encode(connectStr
					.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + encoding);

			if (sUserId != null && sPassword != null) {
				Authenticator.setDefault(new Authenticator() {

					@Override
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(sUserId, sPassword
								.toCharArray());
					}
				});

			}
		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_GET_CONNECTION"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_GET_CONNECTION")
					+ " " + e.getMessage(), e);
		}
		LOG.info("RETURN");
		return ((HttpURLConnection) httpConnection);
	}

	/*
	 * Gets the Response
	 * 
	 * @return String sResponse stating whether connection is a success or
	 * failure
	 */
	public SpmlResponse getResponse(String request) {
		String sResponse = "";
		SpmlResponse spmlResponse = null;
		final String sMethodName="getResponse";
		
		String sSOAPRequest = constructRequest(request);
		LOG.info("BEGIN");
		objHttpURLConnection = getConnection();
		LOG.info(configuration.getMessage("SAPUME_INFO_CONNECTION"),
				objHttpURLConnection);
		byte[] xmlBytes = sSOAPRequest.getBytes();
		objHttpURLConnection.setRequestProperty("Content-Length", String
				.valueOf(xmlBytes.length));
		objHttpURLConnection.setRequestProperty("Content-Type",
				"text/xml; charset=UTF-8");
		objHttpURLConnection.setRequestProperty("SOAPAction", "POST");
		try {
			objHttpURLConnection.setRequestMethod("POST");
			objHttpURLConnection.setDoOutput(true);
			objHttpURLConnection.setDoInput(true);
			
			LOG.error("Perf: "+sMethodName+" Sending request started");
			OutputStream out = objHttpURLConnection.getOutputStream();
			LOG.error("Perf: "+sMethodName+" Sending request completed");
			out.write(xmlBytes);
			out.close();

			InputStreamReader isr = new InputStreamReader(objHttpURLConnection
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);

			String inputLine;
			StringBuffer result = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
				result.append(inputLine);
			in.close();
			sResponse = result.length() == 0 ? null : result.toString();
			spmlResponse = SpmlResponse.parseResponse(sResponse);
			LOG.info("RETURN");

		}
		// wrong user name and password
		catch (ProtocolException pE) {
			LOG.error(pE, configuration
					.getMessage("SAPUME_ERR_INVALID_CREDENTIAL"),pE.getMessage());
			throw new InvalidCredentialException(configuration
					.getMessage("SAPUME_ERR_INVALID_CREDENTIAL")
					+ " " + pE.getMessage(), pE);
		} catch (FileNotFoundException fE) {
			// when user name and password is wrong
			if (fE.getMessage().contains("401: Unauthorized")) {
				LOG.error(fE, configuration
						.getMessage("SAPUME_ERR_INVALID_CREDENTIAL"),fE.getMessage());
				throw new InvalidCredentialException(configuration
						.getMessage("SAPUME_ERR_INVALID_CREDENTIAL")
						+ " " + fE.getMessage(), fE);
				// when filename in the url is wrong
			} else {
				LOG.error(fE, configuration.getMessage("SAPUME_ERR_CONNECTION"),fE.getMessage());
				throw new ConnectionFailedException(configuration
						.getMessage("SAPUME_ERR_CONNECTION")
						+ " " + fE.getMessage(), fE);
			}
		}
		// when given wrong port number
		catch (IllegalArgumentException iE) {
			LOG.error(iE, configuration
					.getMessage("SAPUME_ERR_ILLEGAL_ARGUMENT"),iE.getMessage());
			throw new ConnectionFailedException(configuration
					.getMessage("SAPUME_ERR_ILLEGAL_ARGUMENT")
					+ " " + iE.getMessage(), iE);
		}
		// when host name is wrong
		catch (UnknownHostException e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_UNKNOWN_HOST"),e.getMessage());
			throw new ConnectionFailedException(configuration
					.getMessage("SAPUME_ERR_UNKNOWN_HOST")
					+ " " + e.getMessage(), e);
		}
		// if IP address is wrong
		catch (ConnectException e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_UNKNOWN_HOST"),e.getMessage());
			throw new ConnectionFailedException(configuration
					.getMessage("SAPUME_ERR_UNKNOWN_HOST")
					+ " " + e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_IO"),e.getMessage());
			throw new ConnectionFailedException(configuration
					.getMessage("SAPUME_ERR_IO")
					+ " " + e.getMessage(), e);
		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_CONNECTION"),e.getMessage());
			throw new ConnectionFailedException(configuration
					.getMessage("SAPUME_ERR_CONNECTION")
					+ " " + e.getMessage(), e);
		}
		return spmlResponse;
	}

	/*
	 * Constructs the spml request.
	 * 
	 * @return String spml request
	 */
	public String constructRequest(String request) {
		LOG.info("BEGIN");
		StringBuffer sbRequest = new StringBuffer();
		sbRequest
				.append("<?xml version='1.0' encoding='UTF-8'?> \n")
				.append(
						"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"> \n")
				.append("<SOAP-ENV:Header/> \n").append("<SOAP-ENV:Body> \n")
				.append(request).append("</SOAP-ENV:Body> \n").append(
						"</SOAP-ENV:Envelope> \n");
		LOG.info("RETURN");
		return sbRequest.toString();
	}

	/*
	 * Utility to convert GuardedString to String
	 * 
	 * @return String
	 */
	public String decode(final GuardedString string) {
		LOG.info("BEGIN");
		if (string == null) {
			return null;
		}
		GuardedStringAccessor accessor = new GuardedStringAccessor();
		string.access(accessor);
		String decoded = new String(accessor.getArray());
		accessor.clear();
		LOG.info("RETURN");
		return decoded;
	}

	public void test() {
		LOG.info("BEGIN");
		final String sMethodName="test";

		SearchRequest searchReq = new SearchRequest();
		searchReq.setSearchBase(SAPUSER);
		Filter oFilter = new Filter();

		FilterTerm oSub2FilterTerm = new FilterTerm();
		oSub2FilterTerm.setOperation(FilterTerm.OP_EQUAL);
		oSub2FilterTerm.setName(LOGONNAME);
		oSub2FilterTerm.setValue(USERNAME);
		oFilter.addTerm(oSub2FilterTerm);

		searchReq.setFilter(oFilter);
		searchReq.addAttribute(FIRSTNAME);
		try {
			LOG.error("Perf : "+sMethodName+" test connection started for user {0} ",USERNAME);			
			getResponse(searchReq.toXml());
			LOG.error("Perf : "+sMethodName+" test connection completedfor user {0} ",USERNAME);
		} catch (Exception e) {
			LOG.info(e, configuration.getMessage("SAPUME_ERR_TEST_CONNECTION"),e.getMessage());
			throw new ConnectorException(configuration.getMessage(
					"SAPUME_ERR_TEST_CONNECTION")
					+ " " + e.getMessage(), e);
		}
		LOG.info("RETURN");
	}
}
