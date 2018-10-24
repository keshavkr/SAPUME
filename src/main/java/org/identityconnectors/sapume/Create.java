/**
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description : SAPUME Connector Create Operation implementation
 * Source code : Create.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.Uid;
import org.openspml.message.AddRequest;
import org.openspml.message.SpmlResponse;

public class Create implements SAPUMEConstants {

	private SAPUMEConnection connection = null;
	private SAPUMEConfiguration configuration = null;
	// START :: Bug 19594162
	private Delete _delete = null;
	// END :: Bug 19594162
	private static final Log LOG = Log.getLog(Create.class);

	/**
	 * comment for Check in test Constructor
	 * 
	 * @param configuration
	 *            {@link org.identityconnectors.sapume.SAPUMEConfiguration} to
	 *            be used
	 * @param connection
	 *            {@link org.identityconnectors.sapume.SAPUMEConnection} to be
	 *            used
	 */
	Create(final SAPUMEConfiguration configuration,
			final SAPUMEConnection connection) {
		LOG.info("BEGIN");
		this.configuration = configuration;
		this.connection = connection;
		// START :: Bug 19594162
		_delete = new Delete(configuration, connection);
		// END :: Bug 19594162
		LOG.info("RETURN");
	}

	/**
	 * Main entry point for creating objects in SAPUME Resource
	 * 
	 * @param objClass
	 *            Object class
	 * @param attrs
	 *            Object Attributes
	 * @param options
	 *            Operation Options
	 * @return {@link org.identityconnectors.framework.common.objects.Uid} of
	 *         the created object
	 */
	Uid create(final String targetObjCls, final Set<Attribute> attrs) {
		LOG.info("BEGIN");
		String sUID = null;
		List<org.openspml.message.Attribute> lstSpmlAttrs = new ArrayList<org.openspml.message.Attribute>();
		try {
			Iterator<Attribute> iterator = attrs.iterator();
			AddRequest addReq = new AddRequest();

			while (iterator.hasNext()) {
				Attribute attr = iterator.next();
				org.openspml.message.Attribute spmlAttr = new org.openspml.message.Attribute();

				// Converts OW/OIM attributes name to SAP UME target system
				// attributes which is understandable by the target
				String sAttributeName = SAPUMEUtil.mapConnAttrToTargetAttr(
						targetObjCls, attr.getName().toString());

				// if attribute is password/__PASSWORD__ and Password handling
				// support is true in configuration then create user with dummy
				// password given in configuration.
				if (configuration.getPwdHandlingSupport()
						&& (sAttributeName.equalsIgnoreCase(PASSWORD) || sAttributeName
								.equalsIgnoreCase(PASSWORDNAME))) {
					String sPassword = new Update(configuration, connection)
							.getPasswordElement(attr, targetObjCls);
					spmlAttr.setName(PASSWORD);
					spmlAttr.setValue(sPassword);
				}
				// Handling validto and validfrom attribute
				else if (sAttributeName.equalsIgnoreCase(VALID_TO)
						|| sAttributeName.equalsIgnoreCase(VALID_FROM)) {
					// Handling validto and validfrom date fields
					Long lDate = null;
					String sValue = null;
					String sSourceDate = attr.getValue().get(0).toString();
					if (!sSourceDate.equals("0")) {
						try {
							lDate = new Long(sSourceDate);
						} catch (Exception e) {
							// Date from OW is coming in the format of
							// 'E MMM d HH:mm:ss Z yyyy'.
							// This has to be formatted and that to be converted
							// as
							// long value
							try {
								SimpleDateFormat sdf = new SimpleDateFormat(
										OW_DATE_FORMAT);
								Date dFormatedDate = sdf.parse(sSourceDate);
								lDate = dFormatedDate.getTime();
							} catch (Exception ex) {
								LOG.error(ex, configuration
										.getMessage("SAPUME_ERR_INV_DATE"), ex
										.getMessage());
								throw new ConnectorException(configuration
										.getMessage("SAPUME_ERR_INV_DATE")
										+ " " + ex.getMessage(), ex);
							}
						}
						Date d = new Date(lDate);
						DateFormat dateFormatter = new SimpleDateFormat(
								DATE_FORMAT);
						// Converting above date format to "yyyyMMddHHmmss'Z'"
						// which
						// is a target date format
						sValue = dateFormatter.format(d);
						spmlAttr.setName(sAttributeName);
						spmlAttr.setValue(sValue);
					}

				}else if(ENABLENAME.equalsIgnoreCase(sAttributeName) && 
						attr.getValue().get(0).toString().equalsIgnoreCase("false")){
						String date = null;
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
						spmlAttr.setName(VALID_TO);
						spmlAttr.setValue(date);
					}
				else {

					spmlAttr.setName(sAttributeName);
					if (attr.getValue().size() == 1) {
						spmlAttr.setValue(attr.getValue().get(0).toString());
					} else {
						// Holds multivalues (list) of roles/group
						LOG.info(configuration
								.getMessage("SAPUME_INFO_ATTR_NAME"),
								sAttributeName);
						spmlAttr.setValue(attr.getValue());
					}
				}
				// Setting all the spml attributes one by one in
				// List<org.openspml.message.Attribute> lstSpmlAttrs
				LOG.info(configuration.getMessage("SAPUME_INFO_ATTR_NAME"),
						sAttributeName);
				// if Valid From and Valid to are empty then skip those
				// attributes from request
				if (spmlAttr.getName() != null)
					lstSpmlAttrs.add(spmlAttr);
			}
			addReq.setAttributes(lstSpmlAttrs);
			addReq.setObjectClass(targetObjCls);

			// Logs SPML request if user requires to print request in LOG
			// 'password' and 'oldpassword' values is set to 'XXXXXXX'
			if (configuration.getLogSPMLRequest()) {
				AddRequest logAddReq = new AddRequest();
				List<org.openspml.message.Attribute> lstLogAttr = addReq
						.getAttributes();

				for (org.openspml.message.Attribute attr : lstLogAttr) {
					String attrName = attr.getName();
					if (configuration.getPwdHandlingSupport()
							&& attrName.equalsIgnoreCase(PASSWORD)) {
						logAddReq.setAttribute(attrName, "XXXXXXX");
					} else {
						logAddReq.setAttribute(attrName, attr.getValue());
					}
				}
				LOG.info(configuration.getMessage("SAPUME_INFO_SPML_REQUEST"),
						logAddReq.toXml());
			}
			LOG.error("Perf: create request started for user {o}"+ AttributeUtil.getNameFromAttributes(attrs).getNameValue());
			SpmlResponse spmlResponse = connection.getResponse(addReq.toXml());
			LOG.error("Perf: create response completed for user {o}"+ sUID);
			sUID = new SAPUMEUtil().getResponse(spmlResponse, null);
		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_CREATE_USER"), e
					.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_CREATE_USER")
					+ " " + e.getMessage(), e);
		}

		// If Change password is yes in configuration then set
		// the process form password as new password
		// START :: Bug 19594162 Added Try Catch block for deleting user in case
		// of exception
		try {
			if (configuration.getPwdHandlingSupport()
					&& configuration.getChangePwdFlag()
					&& targetObjCls.equalsIgnoreCase(SAPUSER) && sUID != null) {
				Iterator itr = attrs.iterator();
				Attribute attr = null;

				// Iterating through all the attributes to get password.
				while (itr.hasNext()) {
					attr = (Attribute) itr.next();
					if (attr.getName().toString().equalsIgnoreCase(PASSWORD)
							|| attr.getName().toString().equalsIgnoreCase(
									PASSWORDNAME)) {
						sUID = new Update(configuration, connection)
								.modifyPassword(sUID, attr);
						break;
					}
				}
			}
		} catch (Exception e) {
			_delete.delete(sUID);
			LOG.error(e,
					configuration.getMessage("SAPUME_ERR_PASSWORD_FAILED"), e
							.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_PASSWORD_FAILED")
					+ " " + e.getMessage(), e);
		}
		// END :: Bug 19594162
		LOG.info("Return {0}", sUID);
		return new Uid(sUID);
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
}
