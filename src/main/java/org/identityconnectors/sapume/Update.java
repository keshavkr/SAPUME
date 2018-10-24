/*
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description :
 * Source code : Update.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.openspml.message.FilterTerm;
import org.openspml.message.Modification;
import org.openspml.message.ModifyRequest;
import org.openspml.message.SearchRequest;
import org.openspml.message.SearchResponse;
import org.openspml.message.SearchResult;
import org.openspml.message.SpmlResponse;

public class Update implements SAPUMEConstants {
	private SAPUMEConnection connection = null;
	private SAPUMEConfiguration configuration = null;
	private SAPUMEUtil util = new SAPUMEUtil();
	private static final Log LOG = Log.getLog(Update.class);
	
	/**
	 * Constructor
	 * 
	 * @param connection
	 *            {@link SAPUMEConnection} to use
	 * @param configuration
	 *            {@link SAPUMEConfiguration} to use
	 * @param query
	 *            {@link Query} instance to use
	 */
	public Update(final SAPUMEConfiguration configuration,
			final SAPUMEConnection connection) {
		this.configuration = configuration;
		this.connection = connection;
	}

	/**
	 * Update implementation
	 * 
	 * @see org.identityconnectors.sapume.SAPUMEConnector#update(org.identityconnectors.framework.common.objects.Uid,
	 *      java.util.Set, String)
	 */
	Uid update(Uid uid, Set<Attribute> attrs, String targetObjCls,
			ObjectClass objClass,SAPUMESchema sapumeSchema ) {
		LOG.info("BEGIN");
		final String sMethodName="update";
		String retUid = null;
		String sUid = null;
		ModifyRequest modifyRequest = new ModifyRequest();
		// Logs SPML request if user requires to print request in LOG
		ModifyRequest logModifyReq = new ModifyRequest();

		SAPUMEUtil objUMEUtil = new SAPUMEUtil(connection, configuration);
		ArrayList<String> defaultAttibutes = new ArrayList<String>();
		try {
			Set<Attribute> replaceAttributes = null;
			if (AttributeUtil.getCurrentAttributes(attrs) != null) {
				// Start::Unique ID Issue in AC - BUG 17910026
				sUid = setUniqueIdentifier(uid.getUidValue(), attrs);
				modifyRequest.setIdentifier(sUid);
				replaceAttributes = removeCurrentAttrFromSet(attrs, objClass);
				// END - BUG 17910026
			} else {
				sUid = uid.getUidValue();
				modifyRequest.setIdentifier(sUid);
				replaceAttributes = attrs;
			}
			Iterator<Attribute> attrsIter = replaceAttributes.iterator();
			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				logModifyReq.setIdentifier(sUid); // BUG 17910026

			// In case of OIM update is called for each attribute modification
			// individually but in case of OWS
			// all the modified attributes are sent in single set
			String modified_sUid = objUMEUtil.getLogonnameFromUID(sUid);
			
			defaultAttibutes.addAll(sapumeSchema.getAccountAttributeNames());
			defaultAttibutes.add(NAME);
			while (attrsIter.hasNext()) {
				Attribute attr = attrsIter.next();
				boolean isValidAttribute = defaultAttibutes.contains(attr.getName());
				if(!isValidAttribute){
					throw new ConnectorException(configuration
							.getMessage("SAPUME_ERR_INVALID_ATTRIBUTE")
							+ " " + new ConnectorException().getMessage(),
							new ConnectorException());
				}
				// Converts OW/OIM attributes name to SAP UME target system
				// attributes which is understandable by the target
				String attrName = SAPUMEUtil.mapConnAttrToTargetAttr(
						targetObjCls, attr.getName().toString());

				// Modification for multi value attribute such as
				// assignroles, assigngroups or members
				if (attrName.equalsIgnoreCase(ASSIGNED_ROLES)
						|| attrName.equalsIgnoreCase(ASSIGNED_GROUPS)
						|| attrName.equalsIgnoreCase(MEMBER)) {
					getMultiValueElement(modifyRequest, attr, sUid,
							targetObjCls, objUMEUtil, logModifyReq);
				}
				// Handling password (user form/ OIM) or __PASSWORD__
				// (accountlevel) and
				else if (configuration.getPwdHandlingSupport()
						&& (attrName.equalsIgnoreCase(PASSWORD) || attrName
								.equalsIgnoreCase(PASSWORDNAME))) {
					// START :: Bug 19133071
					LOG.info("sUid  : {0} " + sUid);
					LOG.info("modified_sUid : {0} " + modified_sUid);
					LOG.info("configuration: UmeUserId : {0} ", configuration.getUmeUserId());
					String sPassword="";					
					if(modified_sUid.equalsIgnoreCase(configuration.getUmeUserId()) 
							|| objUMEUtil.isTechnicaluser(modified_sUid)){
						Object attrVal = attr.getValue().get(0);
						if (attrVal instanceof GuardedString) {
							GuardedString gsPassword = (GuardedString) attrVal;
							sPassword = connection.decode(gsPassword);
						} else if (attrVal instanceof String) {
							sPassword = (String) attrVal;
						}
					}else{
						sPassword = getPasswordElement(attr, targetObjCls);
					}
					// END :: Bug 19133071
						modifyRequest.addModification(PASSWORD, sPassword);
						// Logs SPML request if user requires to print request in
						// LOG
						// Here, Password attribute will be set as "XXXXXXX' instead
						// of
						// displaying original password entered by user
							if (configuration.getLogSPMLRequest()){
								logModifyReq.addModification(PASSWORD, "XXXXXXXX");
							}
				}
				// validto, validfrom and enable
				else if (attrName.equalsIgnoreCase(VALID_TO)
						|| attrName.equalsIgnoreCase(VALID_FROM)
						|| attrName.equalsIgnoreCase(ENABLENAME)) {
					getDateElement(modifyRequest, attr, logModifyReq);
				}

				// //////////////////////////////////////////
				// Other attributes such as firstname, etc.,
				// //////////////////////////////////////////
				else {
					// We need to ignore 'logonname' attribute as if user
					// updates 'logonname' attribute in OW/OIM then, SAP UME
					// target accepts this and updates in target accordingly.
					// But in GUI browser, SAP doesn't allow to update logonname
					// (It is read only field during update).

					if (!attrName.equalsIgnoreCase(LOGONNAME)) {
						// if any text field is updated to empty value
						Object sAttributeValue = attr.getValue() == null ? ""
								: attr.getValue().get(0);
						modifyRequest.addModification(attrName, sAttributeValue
								.toString());

						// Logs SPML request if user requires to print request
						// in LOG
						if (configuration.getLogSPMLRequest())
							logModifyReq.addModification(attrName,
									sAttributeValue.toString());
					} else {
						LOG.info(configuration
								.getMessage("SAPUME_INFO_MODIFY_LOGONNAME"));
					}
				}
			}

			// If modifyRequest attribute is null or size is equal to zero then
			// throws connector expections
			if (modifyRequest.getModifications() == null
					|| modifyRequest.getModifications().size() == 0) {
				LOG.info(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"),
						logModifyReq.toXml());
				LOG.error(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"),logModifyReq.toXml());
				throw new ConnectorException(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"));
			}
			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				LOG.info(configuration.getMessage("SAPUME_INFO_SPML_REQUEST"),
						logModifyReq.toXml());
			LOG.error("Perf: "+sMethodName+" Updating {1} started for user {0} ", util.getLogonnameFromUID(uid.getUidValue()), replaceAttributes);
			SpmlResponse spmlResponse = connection.getResponse(modifyRequest
					.toXml());
			LOG.error("Perf: "+sMethodName+" Updating {1} completed for user {0} ", util.getLogonnameFromUID(uid.getUidValue()),replaceAttributes);
			// getting info from spmlResponse
			retUid = objUMEUtil.getResponse(spmlResponse, sUid);

			// Bug 19133071 - Added Below if condition for fix.
			if(!(modified_sUid.equalsIgnoreCase(configuration.getUmeUserId()))) {
				if(!(objUMEUtil.isTechnicaluser(modified_sUid))){
					if (configuration.getPwdHandlingSupport()
							&& configuration.getChangePwdFlag()
							&& targetObjCls.equalsIgnoreCase(SAPUSER)) {
						Attribute attr = null;
						// Iterating through all the attributes to get password.
						Iterator<Attribute> passwordAttr = replaceAttributes.iterator();
						while (passwordAttr.hasNext()) {	
							attr = (Attribute) passwordAttr.next();
							if (attr.getName().toString().equalsIgnoreCase(PASSWORD)
									|| attr.getName().toString().equalsIgnoreCase(
											PASSWORDNAME)) {
								// Modify old password(Dummy password in configuration)
								// with new password(set in processform)
								Attribute passAttr = AttributeBuilder.build(PASSWORD,
										attr.getValue().get(0));
								retUid = modifyPassword(sUid, passAttr)
										.toString(); //BUG 17910026
								break;
							}
						}
					}
				}
			}
		}catch(ConnectorException connectorException){
			LOG.error(connectorException, configuration.getMessage("SAPUME_ERR_INVALID_ATTRIBUTE"),connectorException.getMessage());
			throw  new ConnectorException(configuration
					.getMessage("SAPUME_ERR_INVALID_ATTRIBUTE")
					+ " " + connectorException.getMessage(), connectorException);
		}
		catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_UME_UPDATE_USER"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_UME_UPDATE_USER")
					+ " " + e.getMessage(), e);
		}
		LOG.info("END");
		return new Uid(retUid);
	}

	/**
	 * Handling multi value attribute such as assigned roles, assigned groups
	 * and members.
	 * 
	 * @param modifyRequest
	 *            ModifyRequest
	 * @param multiValueAttr
	 *            Attribute which is modified
	 * @param sUid
	 *            Uid value as string
	 * @param targetObjCls
	 *            it could be either sapuser, sapgroup, or saprole
	 * @param objUMEUtil
	 *            SAPUMEUtil object
	 * @param logModifyReq
	 *            ModifyRequest used to print request in log
	 */
	public void getMultiValueElement(ModifyRequest modifyRequest,
			Attribute multiValueAttr, String sUid, String targetObjCls,
			SAPUMEUtil objUMEUtil, ModifyRequest logModifyReq) {
		LOG.info("BEGIN");
		List<String> lstAddRecords = new ArrayList<String>();
		List<String> lstRmRecords = new ArrayList<String>();

		String attrName = multiValueAttr.getName();

		// Newlst :List of newly added ROLES or GROUPS
		List Newlst = multiValueAttr.getValue();

		// Existlst: Will get the ROLES/GROUPS already assigned to the User.
		List Existlst = objUMEUtil.getAssignedMVA(sUid, attrName, targetObjCls);

		// Compare the new list with the existing list.
		HashMap hmMultiValueAttrs = objUMEUtil.compare(Existlst, Newlst);

		// lstAddRecords :records to be added to user
		lstAddRecords = (List) hmMultiValueAttrs.get(ADD);

		// lstRmRecords :records to be deleted from user
		lstRmRecords = (List) hmMultiValueAttrs.get(DELETE);

		if (lstAddRecords != null && lstAddRecords.size() > 0) {
			for (Object addRecord : lstAddRecords) {
				Modification mod = new Modification(attrName, addRecord);
				mod.setOperation(ADD);
				modifyRequest.addModification(mod);

				// Logs SPML request if user requires to print request in LOG
				if (configuration.getLogSPMLRequest())
					logModifyReq.addModification(mod);

			}
		}

		if (lstRmRecords != null && lstRmRecords.size() > 0) {
			for (Object rmRecord : lstRmRecords) {
				Modification mod = new Modification(attrName, rmRecord);
				mod.setOperation(DELETE);
				modifyRequest.addModification(mod);

				// Logs SPML request if user requires to print request in LOG
				if (configuration.getLogSPMLRequest())
					logModifyReq.addModification(mod);
			}
		}
		LOG.info("RETURN");
	}

	/**
	 * Handling attributes (validto, validfrom and __ENABLE__)
	 * 
	 * @param modifyRequest
	 *            ModifyRequest
	 * @param attr
	 *            <Attribute> Attribute which is modified
	 * @param logModifyReq
	 *            ModifyRequest used to print request in log
	 */
	public void getDateElement(ModifyRequest modifyRequest, Attribute attr,
			ModifyRequest logModifyReq) {
		LOG.info("BEGIN");

		String sAttrName = attr.getName().toString();

		// ///////////////////////////////////////
		// Handling validto and validfrom date
		// ///////////////////////////////////////

		if (sAttrName.equalsIgnoreCase(VALID_TO)
				|| sAttrName.equalsIgnoreCase(VALID_FROM)) {
			Long lDate = null;
			String sSourceDate = attr.getValue().get(0).toString();
			try {
				lDate = new Long(sSourceDate);
			} catch (Exception e) {
				// Date from OW is coming in the format
				// 'E MMM d HH:mm:ss Z yyyy'.
				// This has to be formatted as long value
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(OW_DATE_FORMAT);
					Date dFormatedDate = sdf.parse(sSourceDate);
					lDate = dFormatedDate.getTime();
				} catch (Exception ex) {
					LOG.error(ex, configuration
							.getMessage("SAPUME_ERR_INV_DATE"),ex.getMessage());
					throw new ConnectorException(configuration
							.getMessage("SAPUME_ERR_INV_DATE")
							+ " " + ex.getMessage(), ex);
				}
			}
			// Converting Long to date format ex:june 04, 1984,sun
			Date d = new Date(lDate);
			DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
			// Converting above date format to "yyyyMMddHHmmss'Z'" which
			// is a target date format
			modifyRequest.addModification(sAttrName, dateFormatter.format(d));

			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				logModifyReq
						.addModification(sAttrName, dateFormatter.format(d));
		}

		// ///////////////////////////////
		// Enables/Disable user accounts
		// ///////////////////////////////

		else if (sAttrName.equalsIgnoreCase(ENABLENAME)) {
			String date = null;
			// __ENABLE__ = "true" to enable user
			// __ENABLE__ = "false" to disable user
			if (attr.getValue().get(0).toString().equalsIgnoreCase("true")) {
				String dateValue = configuration.getEnableDate();
				DateFormat dateFormat = new SimpleDateFormat(CONFIG_DATE_FORMAT);
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						DATE_FORMAT);
				try {
					if (dateValue != null) {
						date = dateFormatter.format((dateFormat
								.parse(dateValue)));
					}
				} catch (ParseException e) {
					LOG.error(e, configuration.getMessage("SAPUME_ERR_PARSE"),e.getMessage());
					throw new ConnectorException(configuration
							.getMessage("SAPUME_ERR_PARSE")
							+ " " + e.getMessage(), e);
				}
			} else {
				// Get the currect date from OW/OIM server and set it as
				// end for user account in target

				// Get the currect date from OW/OIM server
				Date currentDate = new Date();
				// Convert to milli seconds
				long millisecCurrentDate = currentDate.getTime();
				// Users end date of account validity is current date minus 1
				// day i.e., yesterday
				millisecCurrentDate = millisecCurrentDate - 24 * 60 * 60 * 1000;
				Date yesterday = new Date(millisecCurrentDate);

				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						DATE_FORMAT);
				date = dateFormatter.format(yesterday).toString();
			}

			modifyRequest.addModification(VALID_TO, date);

			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				logModifyReq.addModification(VALID_TO, date);
		}

		LOG.info("END");
	}

	/**
	 * Handling password or __PASSWORD__ attribute.\
	 * 
	 * @param modifyRequest
	 *            Modify request
	 * @param attr
	 *            <Attribute> Attribute which is modified
	 * @param targetObjCls
	 *            password reset can be applicable for only sapuser
	 * @param logModifyReq
	 *            ModifyRequest used to print request in log
	 */
	public String getPasswordElement(Attribute attr, String targetObjCls) {
		String sAttrName = attr.getName().toString();
		String sPassword = "";
		// If the ChangePassword is YES,then reset the password with
		// dummy password first
		if (configuration.getChangePwdFlag()
				&& targetObjCls.equalsIgnoreCase(SAPUSER)) {
			// Throws exception in case if dummy password is null
			if (configuration.getDummyPassword() != null) {
				GuardedString dummyPassword = (GuardedString) configuration
						.getDummyPassword();
				sPassword = connection.decode(dummyPassword);
			} else {
				LOG
						.error(configuration
								.getMessage("SAPUME_ERR_DUMMY_PASSWORD"));
				throw new ConnectorException(configuration
						.getMessage("SAPUME_ERR_DUMMY_PASSWORD")
						+ " " + new IllegalArgumentException().getMessage(),
						new IllegalArgumentException());
			}
		} else {
			Object attrVal = attr.getValue().get(0);
			// If user updates his/her account level password in OW,
			// then it comes as guard string.
			if (attrVal instanceof GuardedString) {
				GuardedString newPassword = (GuardedString) attrVal;
				sPassword = connection.decode(newPassword);
			}
			// If user set password in user form
			else if (attrVal instanceof String) {
				sPassword = (String) attrVal;
			}
		}
		return sPassword;
	}

	public String modifyPassword(final String uid, Attribute passwordAttr) {

		LOG.info("BEGIN");
		final String sMethodName="modifyPassword";
		try {
			// setIdentifier is required
			ModifyRequest req = new ModifyRequest();
			req.setIdentifier(uid);

			GuardedString dummyPassword = (GuardedString) configuration
					.getDummyPassword();
			String sDummyPassword = connection.decode(dummyPassword);

			String newPassword = "";
			Object attrVal = passwordAttr.getValue().get(0);
			// If user updates his/her account level password in OW, then
			// it comes as gurard string.
			if (attrVal instanceof GuardedString) {
				GuardedString gsPassword = (GuardedString) attrVal;
				newPassword = connection.decode(gsPassword);
			} else if (attrVal instanceof String) {
				newPassword = (String) attrVal;
			}
			req.addModification(OLDPASSWORD, sDummyPassword);
			req.addModification(PASSWORD, newPassword);
			LOG.error("Perf: "+sMethodName+" Updating password started for user {0} ", util.getLogonnameFromUID(uid));		
			SpmlResponse spmlResponse = connection.getResponse(req.toXml());
			LOG.error("Perf: "+sMethodName+" Updating password completed for user {0} ", util.getLogonnameFromUID(uid));
			util.getResponse(spmlResponse, uid);
		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_PASSWORD_FAILED"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_PASSWORD_FAILED")
					+ " " + e.getMessage(), e);
		}
		LOG.info("END");
		return uid;
	}

	/**
	 * Dispose resources
	 */
	void dispose() {
		LOG.info("BEGIN");
		// Since we are getting connection every time
		// before processing SPML request, this method will do only
		// clean up of configuration and connection object.
		this.configuration = null;
		this.connection = null;
		LOG.info("END");
	}

	/**
	 * Add Child table data(Roles and Groups)
	 * 
	 * @param uid
	 * @param valuesToAdd
	 * @param targetObjCls
	 * @return
	 */
	public Uid addAttributeValues(Uid uid, Set<Attribute> attrs,
			String targetObjCls, ObjectClass objClass) {
		LOG.info("BEGIN");
		final String sMethodName="addAttributeValues";
		String retUid = null;
		ModifyRequest modifyRequest = new ModifyRequest();
		// Logs SPML request if user requires to print request in LOG
		ModifyRequest logModifyReq = new ModifyRequest();
		List<Object> lstAddRecords = new ArrayList();
		String attrName = null;
		SAPUMEUtil objUMEUtil = new SAPUMEUtil(connection, configuration);
		try {
			// Start:Unique ID Issue in AC - BUG 17910026
			// String sUid = setUniqueIdentifier(uid.getUidValue(), attrs);
			// Set<Attribute> valuesToAdd =
			// removeCurrentAttrFromSet(attrs,objClass);
			String sUid = null;
			Set<Attribute> valuesToAdd;
			if (AttributeUtil.getCurrentAttributes(attrs) != null) {
				sUid = setUniqueIdentifier(uid.getUidValue(), attrs);
				modifyRequest.setIdentifier(sUid);
				valuesToAdd = removeCurrentAttrFromSet(attrs, objClass);
			} else {
				sUid = uid.getUidValue();
				modifyRequest.setIdentifier(sUid);
				valuesToAdd = attrs;
			}
			// END - BUG 17910026
			Iterator<Attribute> attrsIter = valuesToAdd.iterator();
			// modifyRequest.setIdentifier(sUid);

			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				logModifyReq.setIdentifier(sUid); // BUG 17910026

			// In case of OIM update is called for each attribute modification
			// individually but in case of OWS
			// all the modified attributes are sent in single set
			while (attrsIter.hasNext()) {
				Attribute attr = attrsIter.next();

				// Converts OW/OIM attributes name to SAP UME target system
				// attributes which is understandable by the target
				attrName = SAPUMEUtil.mapConnAttrToTargetAttr(targetObjCls,
						attr.getName().toString());

				// Modification for multi value attribute such as
				// assignroles, assigngroups or members
				if (attrName.equalsIgnoreCase(ASSIGNED_ROLES)
						|| attrName.equalsIgnoreCase(ASSIGNED_GROUPS)
						|| attrName.equalsIgnoreCase(MEMBER)) {
					// Newlst :List of newly added ROLES or GROUPS
					lstAddRecords = attr.getValue();
					if (lstAddRecords != null && lstAddRecords.size() > 0) {
						for (Object addRecord : lstAddRecords) {
							Modification mod = new Modification(attrName,
									addRecord);
							mod.setOperation(ADD);
							modifyRequest.addModification(mod);

							// Logs SPML request if user requires to print
							// request in LOG
							if (configuration.getLogSPMLRequest())
								logModifyReq.addModification(mod);

						}
					}
				}
			}
			// If modifyRequest attribute is null or size is equal to zero then
			// throws connector expections
			if (modifyRequest.getModifications() == null
					|| modifyRequest.getModifications().size() == 0) {
				LOG.info(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"),
						logModifyReq.toXml());
				LOG.error(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"),logModifyReq.toXml());
				throw new ConnectorException(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"));
			}
			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				LOG.info(configuration.getMessage("SAPUME_INFO_SPML_REQUEST"),
						logModifyReq.toXml());
			LOG.error("Perf: "+sMethodName+" Updating {1} started for user {0} ", util.getLogonnameFromUID(sUid), valuesToAdd);		
			SpmlResponse spmlResponse = connection.getResponse(modifyRequest
					.toXml());
			LOG.error("Perf: "+sMethodName+" Updating {1} completed for user {0} ", util.getLogonnameFromUID(sUid), valuesToAdd);		
			// getting info from spmlResponse
			retUid = objUMEUtil.getResponse(spmlResponse, sUid);
		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_ADD_ATTR_VALUES"));
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_ADD_ATTR_VALUES")
					+ " " + e.getMessage(), e);
		}
		LOG.info("END");
		return new Uid(retUid);
	}

	/**
	 * Remove Child table data(Roles and Groups)
	 * 
	 * @param uid
	 * @param valuesToRemove
	 * @param targetObjCls
	 * @return
	 */
	public Uid removeAttributeValues(Uid uid, Set<Attribute> attrs,
			String targetObjCls, ObjectClass objClass) {
		LOG.info("BEGIN");
		final String sMethodName="removeAttributeValues";
		String retUid = null;
		ModifyRequest modifyRequest = new ModifyRequest();
		// Logs SPML request if user requires to print request in LOG
		ModifyRequest logModifyReq = new ModifyRequest();
		List<Object> lstRmRecords = new ArrayList();
		String attrName = null;
		SAPUMEUtil objUMEUtil = new SAPUMEUtil(connection, configuration);
		try {
			// Start:Unique ID Issue in AC - BUG 17910026
			// String sUid = setUniqueIdentifier(uid.getUidValue(), attrs);
			// Set<Attribute> valuesToRemove =
			// removeCurrentAttrFromSet(attrs,objClass );
			String sUid = null;
			Set<Attribute> valuesToRemove;
			if (AttributeUtil.getCurrentAttributes(attrs) != null) {
				sUid = setUniqueIdentifier(uid.getUidValue(), attrs);
				modifyRequest.setIdentifier(sUid);
				valuesToRemove = removeCurrentAttrFromSet(attrs, objClass);
			} else {
				sUid = uid.getUidValue();
				modifyRequest.setIdentifier(sUid);
				valuesToRemove = attrs;
			}
			// END - BUG 17910026
			Iterator<Attribute> attrsIter = valuesToRemove.iterator();
			// modifyRequest.setIdentifier(sUid);

			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				logModifyReq.setIdentifier(sUid); // BUG 17910026

			// In case of OIM update is called for each attribute modification
			// individually but in case of OWS
			// all the modified attributes are sent in single set
			while (attrsIter.hasNext()) {
				Attribute attr = attrsIter.next();

				// Converts OW/OIM attributes name to SAP UME target system
				// attributes which is understandable by the target
				attrName = SAPUMEUtil.mapConnAttrToTargetAttr(targetObjCls,
						attr.getName().toString());

				// Modification for multi value attribute such as
				// assignroles, assigngroups or members
				if (attrName.equalsIgnoreCase(ASSIGNED_ROLES)
						|| attrName.equalsIgnoreCase(ASSIGNED_GROUPS)
						|| attrName.equalsIgnoreCase(MEMBER)) {
					// Newlst :List of newly added ROLES or GROUPS
					lstRmRecords = attr.getValue();
					if (lstRmRecords != null && lstRmRecords.size() > 0) {
						for (Object addRecord : lstRmRecords) {
							Modification mod = new Modification(attrName,
									addRecord);
							mod.setOperation(DELETE);
							modifyRequest.addModification(mod);

							// Logs SPML request if user requires to print
							// request in LOG
							if (configuration.getLogSPMLRequest())
								logModifyReq.addModification(mod);

						}
					}
				}
			}
			// If modifyRequest attribute is null or size is equal to zero then
			// throws connector expections
			if (modifyRequest.getModifications() == null
					|| modifyRequest.getModifications().size() == 0) {
				LOG.info(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"),
						logModifyReq.toXml());
				LOG.error(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"));
				throw new ConnectorException(configuration
						.getMessage("SAPUME_ERR_MODIFY_REQUEST_ATTR"));
			}
			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest())
				LOG.info(configuration.getMessage("SAPUME_INFO_SPML_REQUEST"),
						logModifyReq.toXml());
			LOG.error("Perf: "+sMethodName+" Updating {1} started for user {0} ", util.getLogonnameFromUID(sUid),valuesToRemove);		
			SpmlResponse spmlResponse = connection.getResponse(modifyRequest
					.toXml());
			LOG.error("Perf: "+sMethodName+" Updating {1} completed for user {0} ", util.getLogonnameFromUID(sUid),valuesToRemove);		
			// getting info from spmlResponse
			retUid = objUMEUtil.getResponse(spmlResponse, sUid);
		} catch (Exception e) {
			LOG.error(e, configuration
					.getMessage("SAPUME_ERR_REMOVE_ATTR_VALUES")
					+ " " + e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_REMOVE_ATTR_VALUES")
					+ " " + e.getMessage(), e);
		}
		LOG.info("END");
		return new Uid(retUid);
	}

	// Added for BUG 17910026
	/**
	 * Get unique identifier using Search request. Added this method to fix the
	 * Unique ID issue
	 * 
	 * @param logonname
	 * @return retUid
	 */

	public  String getUniqueIdentifier(String logonname) {
		String retUid = null;
		try {
			SearchRequest searchReq = new SearchRequest();
			searchReq.setSearchBase(SAPUSER);
			// specify the attributes to return
			searchReq.addAttribute("id");
			// specify the filter
			FilterTerm ft = new FilterTerm();
			ft.setOperation(FilterTerm.OP_EQUAL);
			ft.setName(LOGONNAME);
			ft.setValue(logonname);
			searchReq.addFilterTerm(ft);
			LOG.error("Perf: Search request to get UniqueIdentifier started for user {0} ", logonname);		
			SpmlResponse spmlResponse = connection.getResponse(searchReq
					.toXml());
			LOG.error("Perf: Search request to get UniqueIdentifier completed for user {0} ", logonname);		
			SearchResponse resp = (SearchResponse) spmlResponse;
			List results = resp.getResults();
			if (results != null) {
				SearchResult sr = (SearchResult) results.get(0);
				retUid = sr.getIdentifierString();
			}
		} catch (Exception e) {
			LOG.error(e, configuration
					.getMessage("SAPUME_ERR_GET_UNIQUEIDENTIFIER")
					+ " " + e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_GET_UNIQUEIDENTIFIER")
					+ " " + e.getMessage(), e);
		}
		return retUid;
	}

	/**
	 * To set the appropriate uid for users created in AC Flow -BUG 17910026
	 * 
	 * @param sUid
	 * @param attrs
	 * @return sUid
	 */
	private String setUniqueIdentifier(String sUid, Set<Attribute> attrs) {
		LOG.info("BEGIN");
		Set<Attribute> currentAttrSet = new HashSet<Attribute>(AttributeUtil
				.getCurrentAttributes(attrs));
		String cUid = AttributeUtil.getNameFromAttributes(currentAttrSet)
				.getNameValue();
		if (cUid.equals(sUid)) {
			sUid = getUniqueIdentifier(cUid);
		}
		LOG.info("END :: " + sUid);
		return sUid;
	}

	/**
	 * Removing CurrentAttributes from Set- BUG 17910026
	 * 
	 * @param attrs
	 * @param objClass
	 * @return attributes
	 */
	private Set<Attribute> removeCurrentAttrFromSet(Set<Attribute> attrs,
			ObjectClass objClass) {
		Set<Attribute> attributes = new HashSet<Attribute>(attrs);
		attributes.remove(AttributeBuilder.buildCurrentAttributes(objClass,
				AttributeUtil.getCurrentAttributes(attrs)));
		return attributes;
	}

}
