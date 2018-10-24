/*
* Copyright (c) 2011, 2018, Oracle and/or its affiliates. All rights reserved.
 * Description :Utility class of the SAPUME Connector
 * Source code : SAPUMEUtil.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidPasswordException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.openspml.message.AddResponse;
import org.openspml.message.Filter;
import org.openspml.message.FilterTerm;
import org.openspml.message.SchemaRequest;
import org.openspml.message.SearchRequest;
import org.openspml.message.SearchResponse;
import org.openspml.message.SearchResult;
import org.openspml.message.SpmlResponse;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAPUMEUtil implements SAPUMEConstants {

	private SAPUMEConnection connection = null;
	private SAPUMEConfiguration configuration = null;
	private static final Log LOG = Log.getLog(SAPUMEUtil.class);
	private LinkedHashMap<String, ArrayList<String>> accList = new LinkedHashMap<String, ArrayList<String>>();

	/**
	 * Default Constructor
	 */
	public SAPUMEUtil() {

	}

	/**
	 * Constructor
	 * 
	 * @param connection
	 *            {@link org.identityconnectors.sapume.SAPUMEConnection} to be
	 *            used
	 * @param configuration
	 *            {@link org.identityconnectors.sapume.SAPUMEConfiguration} to
	 *            be used
	 */
	public SAPUMEUtil(SAPUMEConnection connection,
			SAPUMEConfiguration configuration) {
		this.configuration = configuration;
		this.connection = connection;
	}

	/**
	 * Determines spml response status either success, failure,
	 * 
	 * @param SpmlResponse
	 *            spmlResponse
	 * @param String
	 *            uid
	 * 
	 * @return String user id
	 */
	public String getResponse(SpmlResponse spmlResponse, String uid) {
		LOG.info("BEGIN");
		String sUID = null;

		// ///////////////////////////////
		// Handling Failure situations
		// ///////////////////////////////
		if (spmlResponse.getResult().contains(FAILURE)) {
			// User doesnt exist in target
			if (spmlResponse.getErrorMessage().contains(DOESNT_EXIST)) {
				LOG.error(new UnknownUidException(), configuration
						.getMessage("SAPUME_ERR_USER_NOT_EXISTS"),uid);
				throw new UnknownUidException(configuration
						.getMessage("SAPUME_ERR_USER_NOT_EXISTS")
						+ " " + new UnknownUidException().getMessage(),
						new UnknownUidException());
			}
			// If password doesnt meet target system standards
			else if (spmlResponse.getErrorMessage().contains(
					"ALPHANUM_REQUIRED_FOR_PSWD")
					|| spmlResponse.getErrorMessage().contains(
							"PASSWORD_TOO_SHORT")
					|| spmlResponse.getErrorMessage().contains(
							"PASSWORD_TOO_LONG")) {
				LOG.error(new InvalidPasswordException(), configuration
						.getMessage("SAPUME_ERR_INVALID_PASSWORD"),new InvalidPasswordException().getMessage());
				throw new ConnectorException(configuration
						.getMessage("SAPUME_ERR_INVALID_PASSWORD")
						+ " "
						+ new InvalidPasswordException(spmlResponse
								.getErrorMessage()),
						new InvalidPasswordException());
			}
			// If user already exists in target
			else if (spmlResponse.getErrorMessage().contains(ALREADY_EXIST)) {
				LOG.error(new AlreadyExistsException(), configuration
						.getMessage("SAPUME_ERR_USER_EXISTS"));
				throw new ConnectorException(configuration
						.getMessage("SAPUME_ERR_USER_EXISTS")
						+ " "
						+ new AlreadyExistsException(spmlResponse
								.getErrorMessage()),
						new AlreadyExistsException());
			}
			// Other exceptions
			else {
				throw new ConnectorException(
						configuration.getMessage("SAPUME_ERR_OTHERS")
								+ " "
								+ new ConnectorException(spmlResponse
										.getErrorMessage()),
						new ConnectorException());
			}
		}

		// ///////////////////////////////
		// Handling Success situations
		// ///////////////////////////////
		else if (spmlResponse.getResult().contains(SUCCESS)) {
			if (spmlResponse instanceof AddResponse) {
				LOG.info(SUCCESS);
				sUID = ((AddResponse) spmlResponse).getIdentifierString();
			} else {
				sUID = uid;
			}
		}
		LOG.info("RETURN");
		return sUID;
	}

	/**
	 * Compares two list and finds the added and deleted values.
	 * 
	 * @param List
	 *            <String> lstExist
	 * @param List
	 *            <String> lstNew
	 * 
	 * @return HashMap contains key as add/delete or both and value as list of
	 *         roles/groups
	 */
	public HashMap compare(List<String> lstExist, List<String> lstNew) {
		LOG.info("BEGIN");
		List<String> lstAdd = new ArrayList<String>();
		List<String> lstRemove = new ArrayList<String>();
		HashMap hmMultiValueAttr = new HashMap();
		if (lstNew == null) {
			lstNew = new ArrayList();
		}

		// Existing list is not empty
		if (lstExist != null && !lstExist.isEmpty()) {

			for (String lstElem : lstExist) {
				// if the new list doesn't contain already added ROLE/GROUP in
				// existing list then remove the ROLE/GROUP from User
				if (!lstNew.contains(lstElem))
					lstRemove.add(lstElem);
			}
			// if the Existing list doesn't contain newly added ROLE/GROUP
			// then Add the ROLE/GROUP to the User
			for (String lstElem : lstNew) {
				if (!lstExist.contains(lstElem))
					lstAdd.add(lstElem);
			}
			// Add the list of ROLES/GROUPS to HashMap
			if (lstAdd.size() > 0)
				hmMultiValueAttr.put(ADD, lstAdd);

			// Add the list of ROLES/GROUPS to be Removed to HashMap
			if (lstRemove.size() > 0)
				hmMultiValueAttr.put(DELETE, lstRemove);

			// If user contains single ROLE/GROUP and even for the Last role of
			// a user while removing
			// lstNew will be empty.
			if (lstNew == null || lstNew.isEmpty())
				hmMultiValueAttr.put(DELETE, lstRemove);
		}
		// First assignment of ROLE/GROUP to a user.Existing List will be empty
		// and NewList contains New Record to be added
		else if (lstExist.isEmpty()) {
			if (lstNew != null && !lstNew.isEmpty())
				hmMultiValueAttr.put(ADD, lstNew);
		}
		LOG.info("RETURN");
		return hmMultiValueAttr;
	}

	/**
	 * Fetches list of assigned multi value attributes such as, assignedrole,
	 * assignedgroup etc., for a user/group from target.
	 * 
	 * @param Uid
	 *            uid
	 * @param String
	 *            attrName
	 * 
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getAssignedMVA(String sUid, String attrName,
			String targetObjCls) {
		LOG.info("BEGIN");
		final String sMethodName="getAssignedMVA";
		ArrayList<String> lstAssignedMVA = new ArrayList<String>();
		SearchRequest searchReq = new SearchRequest();
		Filter oFilter = new Filter();
		try {
			// Searching user details from SAP system using uid
			FilterTerm oSub2FilterTerm = new FilterTerm();
			oSub2FilterTerm.setOperation(FilterTerm.OP_EQUAL);
			oSub2FilterTerm.setName(ID);
			oSub2FilterTerm.setValue(sUid);

			oFilter.addTerm(oSub2FilterTerm);
			searchReq.setSearchBase(targetObjCls);
			if (oFilter != null) {
				searchReq.setFilter(oFilter);
			}
			searchReq.addAttribute(attrName);

			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				LOG.info(configuration.getMessage("SAPUME_INFO_SPML_REQUEST"),
						searchReq.toXml());
			LOG.error("Perf: "+sMethodName+" started for user {0} ",getLogonnameFromUID(sUid));
			SpmlResponse spmlResponse = connection.getResponse(searchReq
					.toXml());
			LOG.error("Perf: "+sMethodName+" completed for user {0} ",getLogonnameFromUID(sUid));
			SearchResponse resp = (SearchResponse) spmlResponse;
			List results = resp.getResults();
			SearchResult searchResult = (SearchResult) results.get(0);

			// Log the record count
			/*
			 * LOG.info("Multivalued attribute records fetched from target :
			 * {0}", results != null ? results.size() : 0);
			 */
			LOG.info(configuration
					.getMessage("SAPUME_INFO_FETCH_MULTIVALUE_ATTRIBUTE"),
					results != null ? results.size() : 0);
			if (searchResult.getAttribute(attrName) != null) {
				// for single value output of
				// searchResult.getAttributeValue(attrName) is a string and for
				// multiple values for attribute its ArrayList
				if (searchResult.getAttributeValue(attrName) instanceof ArrayList) {
					lstAssignedMVA = (ArrayList) searchResult
							.getAttributeValue(attrName);
				} else {
					String s = searchResult.getAttributeValue(attrName)
							.toString();
					lstAssignedMVA.add(s);
				}

			}
		} catch (Exception e) {
			LOG.error(e, configuration
					.getMessage("SAPUME_ERR_GET_MULTIVALUE_ATTRIBUTES"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_GET_MULTIVALUE_ATTRIBUTES")
					+ " " + e.getMessage(), e);
		}
		LOG.info("RETURN");
		return lstAssignedMVA;

	}

	/**
	 * It converts OW/OIM attributes name to SAP UME target system attributes
	 * which is understandable by the target
	 * 
	 * For eg, __UID__ will be convert into "id". Converts __NAME_ attribute to
	 * "uniquename" if object class either Role/Group otherwise, converts to
	 * "logonname" if object class is for User.
	 * 
	 * @param objCls
	 * @param connAttrName
	 * @return
	 */
	public static String mapConnAttrToTargetAttr(String objCls,
			String connAttrName) {

		String attrName = "";
		// Attribute is __UID__
		if (Uid.NAME.equals(connAttrName)) {
			attrName = "id";
		}
		// Object class is Role/Group and __NAME__
		else if (objCls.equalsIgnoreCase(OBJCLSROLE)
				|| objCls.equalsIgnoreCase(OBJCLSGROUP)
				|| objCls.equalsIgnoreCase(OBJCLSGROUPNAME)
				|| objCls.equalsIgnoreCase(SAPGROUP)
				|| objCls.equalsIgnoreCase(SAPROLE)) {
			if (Name.NAME.equals(connAttrName))
				attrName = "uniquename";
			else
				attrName = connAttrName;
		}
		// Object class is Account/User and __NAME__
		else if (objCls.equalsIgnoreCase(OBJCLSUSERNAME)
				|| objCls.equalsIgnoreCase(ACCOUNTNAME)
				|| objCls.equalsIgnoreCase(SAPUSER)) {
			if (Name.NAME.equals(connAttrName))
				attrName = "logonname";
			else
				attrName = connAttrName;
		}
		// Otherwise, return the attribute as it is.
		else {
			attrName = connAttrName;
		}
		return attrName;
	}

	/**
	 * Get all the attributes supported from the target dynamically Adding this
	 * method as part of this BUG 18895142
	 * 
	 * @param spmlResponse
	 * @return
	 */
	public LinkedHashMap<String, ArrayList<String>> getAtrributesFomTarget() {
		LOG.info("BEGIN");
		SpmlResponse spmlResponse = null;
		try {
			SchemaRequest schmaReq = new SchemaRequest();
			schmaReq.setSchemaIdentifier("SAPprincipals");
			schmaReq.setProviderIdentifier("SAP");
			spmlResponse = connection.getResponse(schmaReq.toXml());
			// obtain and configure a SAX based parser
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			// obtain object for SAX parser
			javax.xml.parsers.SAXParser saxParser = saxParserFactory
					.newSAXParser();
			// default handler for SAX handler class
			// all three methods are written in handler's body
			DefaultHandler defaultHandler = new DefaultHandler() {
				StringBuffer stringBuffer = null;
				ArrayList<String> attributeNames = null;
				String key = null;

				// this method is called every time the parser gets an open tag
				// '<'
				// identifies which tag is being open at time by assigning an
				// open flag
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					if (stringBuffer == null) {
						stringBuffer = new StringBuffer();
					}
					if (qName.equalsIgnoreCase("spml:objectClassDefinition")) {
						key = attributes.getValue("name");

					} else if (qName
							.equalsIgnoreCase("spml:attributeDefinitionReference")) {
						stringBuffer.append(attributes.getValue("name"));
						stringBuffer.append(",");
					}
				}

				/*
				 * When the parser encounters the end of an element, it calls
				 * this method
				 */
				public void endElement(String uri, String localName,
						String qName) throws SAXException {

					if (qName.equalsIgnoreCase("spml:objectClassDefinition")) {
						// add it to the list
						StringBuilder b = new StringBuilder(stringBuffer
								.toString());
						attributeNames = new ArrayList<String>();
						b.replace(stringBuffer.lastIndexOf(","), stringBuffer
								.lastIndexOf(",") + 1, "");
						attributeNames.add(b.toString());
						accList.put(key, attributeNames);
						stringBuffer = null;
						key = null;
					}
				}
			};
			if (spmlResponse != null) {
				saxParser.parse(new InputSource(new StringReader(spmlResponse
						.toXml())), defaultHandler);
			}
		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_PARSE_ATTR"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_PARSE_ATTR")
					+ " " + e.getMessage(), e);
		}
		return accList;
	}	
	
	/**
	 * Check whether user is technical or default
	 * Bug : 19954765 - OPAM:USER CREATED AS TECHNICAL USER CANT ABLE TO UPDATE PASSWORD
	 * @param logonName
	 * @return  
	 */
	public boolean isTechnicaluser(String logonName) {
		LOG.info("BEGIN");
		boolean isTechnicaluser = false;
		String sSecurityPolicy = null;
		SearchRequest sReq = new SearchRequest();
		sReq.addAttribute(SECURITYPOLICY);
		sReq.setSearchBase(SAPUSER);
		// specify the filter 
		FilterTerm ft = new FilterTerm();
		ft.setOperation(FilterTerm.OP_EQUAL);
		ft.setName(LOGONNAME);
		ft.setValue(logonName);
		sReq.addFilterTerm(ft);
		try {
			SpmlResponse spmlResponse = connection.getResponse(sReq.toXml());
			SearchResponse resp = (SearchResponse) spmlResponse;
			List results = resp.getResults();
			if(results != null){
				SearchResult searchResult = (SearchResult) results.get(0);
				sSecurityPolicy = searchResult.getAttributeValue(SECURITYPOLICY)
						.toString();
				if (sSecurityPolicy.equalsIgnoreCase("technical")) {
					isTechnicaluser = true;
				}
			}
		} catch (Exception e) {
			LOG.error("Exception while checking SecurityPolicy of user : {0}",
					e.getMessage());
			throw ConnectorException.wrap(e);
		}
		LOG.info("RETURN");
		return isTechnicaluser;
	}
	
	/**
	 * Get logonname from uid
	 * @param uid
	 * @return logonname
	 */
	public String getLogonnameFromUID(String uid){
		String logonname=null;
		if(uid.lastIndexOf(":") > -1){
			logonname= uid.substring(uid.lastIndexOf(":")+1);
		}else{
			logonname= uid.substring(uid.lastIndexOf(".")+1);
		}		
		return logonname;		
	}
	
	// Added for Bug 28284654
	/**
	 * Get unique name from Role/group UniqueId using Search request. 	 * 
	 * @param uniqueId
	 * @param attrName
	 * @return retUniqueName
	 */

	public  String getUniqueNamefromUniqueId(String attrName,String uniqueId) {
		String retUniqueName = null;
		try {
			SearchRequest searchReq = new SearchRequest();
			if(attrName.contains("umerole")){
				searchReq.setSearchBase(SAPROLE);
			}else{
				searchReq.setSearchBase(SAPGROUP);
			}			
			// specify the attributes to return
			searchReq.addAttribute(UNIQUENAME);
			// specify the filter
			FilterTerm ft = new FilterTerm();
			ft.setOperation(FilterTerm.OP_EQUAL);
			ft.setName(ID);
			ft.setValue(uniqueId);
			searchReq.addFilterTerm(ft);
			LOG.error("Perf: Search request to get UniqueName for role {0} ", uniqueId);		
			SpmlResponse spmlResponse = connection.getResponse(searchReq
					.toXml());		
			SearchResponse resp = (SearchResponse) spmlResponse;
			List results = resp.getResults();
			if (results != null) {
				SearchResult sr = (SearchResult) results.get(0);
				retUniqueName = (String) sr.getAttributeValue(UNIQUENAME);
			}
		} catch (Exception e) {
			LOG.error(e, configuration
					.getMessage("SAPUME_ERR_GET_UNIQUEIDENTIFIER")
					+ " " + e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_GET_UNIQUEIDENTIFIER")
					+ " " + e.getMessage(), e);
		}
		return retUniqueName;
	}
}
