/**
* Copyright (c) 2011, 2016, Oracle and/or its affiliates. All rights reserved.
 * Description :
 * Source code : Query.java
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.openspml.message.Filter;
import org.openspml.message.FilterTerm;
import org.openspml.message.SearchRequest;
import org.openspml.message.SearchResponse;
import org.openspml.message.SearchResult;
import org.openspml.message.SpmlResponse;

class Query implements SAPUMEConstants {
	// comment for Check in test
	private static final Log LOG = Log.getLog(Query.class);
	private SAPUMEConnection connection = null;
	private SAPUMEConfiguration configuration = null;
	

	/*
	 * private final String[] sCharacters = { "a", "b", "c", "d", "e", "f", "g",
	 * "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
	 * "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
	 * "9", "!", "@", "#", "$", "%", "^", "&", "(", ")", "-", "=" };
	 */

	/**
	 * Constructor
	 * 
	 * @param connection
	 *            Initialized
	 *            {@link org.identityconnectors.SAPUMEConnection.SAPConnection}
	 * @param configuration
	 *            SAP configuration
	 */
	Query(SAPUMEConfiguration configuration, SAPUMEConnection connection) {
		this.connection = connection;
		this.configuration = configuration;
	}

	/**
	 * @param objectClass
	 *            ObjectClass
	 * @param query
	 *            Query string
	 * @param handler
	 *            Callback handler
	 * @param options
	 *            Operation Options
	 * @see org.identityconnectors.sapume.SAPUMEConnector#executeQuery(org.identityconnectors.framework.common.objects.ObjectClass,
	 *      String,
	 *      org.identityconnectors.framework.common.objects.ResultsHandler,
	 *      org.identityconnectors.framework.common.objects.OperationOptions)
	 */
	public void executeQuery(ObjectClass objCls, Filter query,
			final ResultsHandler handler, final OperationOptions options,SAPUMESchema sapumeSchema) {

		LOG.info("BEGIN");
		final String sMethodName="executeQuery";
		ArrayList<String> attrsToGet = new ArrayList<String>();
		ArrayList<String> attrsToGetReAttr = new ArrayList<String>();
		ArrayList<String> defaultAttibutes = new ArrayList<String>();
		String[] attrsToGetArray = null;
		Filter oFilter = null;
		List<String> lstDS = null;
		int characterCount = 1;
		String sSearchBase = null;
		boolean bConstructFilter = false;
		if (options != null) {
			// Account attributes names of Target
			attrsToGetArray = options.getAttributesToGet();
			if (attrsToGetArray != null) {
				for (int i = 0; i < attrsToGetArray.length; i++) {

					// Converts OW/OIM attributes name to SAP UME target system
					// attributes which is understandable by the target
					String attrName = SAPUMEUtil.mapConnAttrToTargetAttr(objCls
							.getObjectClassValue(), attrsToGetArray[i]);

					// Remove duplicate entries
					if (!attrsToGet.contains(attrName) && i<=25) {
						attrsToGet.add(attrName);
					}else if (!attrsToGet.contains(attrName) && i > 25
							&& !attrsToGetReAttr.contains(attrName)) {
						attrsToGetReAttr.add(attrName);
					}
				}
				
			}else {
				defaultAttibutes.addAll(sapumeSchema.getAccountAttributeNames());
				int count = 0;
				attrsToGet.add(LOGONNAME);
				for(String attrName : defaultAttibutes){
					if (!attrsToGet.contains(attrName) && count<=25) {
						attrsToGet.add(attrName);
					}else if (!attrsToGet.contains(attrName) &&count > 25
							&& !attrsToGetReAttr.contains(attrName)) {
						attrsToGetReAttr.add(attrName);
					}
					count++;
				}
				attrsToGet.remove(PASSWORDNAME);
				attrsToGet.remove(ENABLENAME);
				attrsToGet.remove(OLDPASSWORD);
				attrsToGetReAttr.remove(PASSWORDNAME);
				attrsToGetReAttr.remove(ENABLENAME);
				attrsToGetReAttr.remove(OLDPASSWORD);
			}
		}

		// Get the data source name from configuration page.
		// This is implemented since 'getResourceAttributeFromResource'
		// method always returns single element from configuration page
		// even though if we gave multiple values.
		if (options.getOptions().get(DSLIST) != null) {
			lstDS = getDSList(objCls);
			if (lstDS != null) {
				for (String dsName : lstDS) {
					ConnectorObjectBuilder connObj = new ConnectorObjectBuilder();
					connObj
							.addAttribute(AttributeBuilder
									.build(dsName, dsName));
					connObj.setUid(dsName);
					connObj.setName(dsName);
					connObj.setObjectClass(objCls);
					handler.handle(connObj.build());
				}
			}
		}
		// Perfoms the lookup recons for Group/Role/Users
		else {

			// /////////////////////////////////////////////////
			// Get role or group details based on object class
			// /////////////////////////////////////////////////

			if (objCls.is(OBJCLSROLE) || objCls.is(OBJCLSGROUP)
					|| objCls.is(OBJCLSGROUPNAME)) {
				LOG.info(configuration.getMessage("SAPUME_INFO_RECON"), objCls);
				if (objCls.is(OBJCLSROLE)) {
					// set the search base as 'saprole'
					sSearchBase = SAPROLE;

					// If attributes to get is empty, add role account attribute
					// names such as id and description
					if (attrsToGet.size() == 0) {
						attrsToGet.addAll(sapumeSchema.getRoleAttributeNames());
					}
				} else if (objCls.is(OBJCLSGROUP) || objCls.is(OBJCLSGROUPNAME)) {
					// set the search base as 'sapgroup'
					sSearchBase = SAPGROUP;

					// If attributes to get is empty, add group account
					// attribute names such as id and description
					if (attrsToGet.size() == 0) {
						attrsToGet
								.addAll(sapumeSchema.getGroupAttributeNames());
					}
				}

				if (query == null) {
					// Get the list of groups for a particular data source
					if (options.getOptions().get(DATASOURCE) != null) {
						// Get data source name from operation option
						// Fetches list of groups/roles from sap ume system.
						Object sDS = options.getOptions().get(DATASOURCE);
						// Construct query to perfom specific role/group recon
						query = constructQueryForOW(sDS);
					}
					// Performs full group or role lookup recon
					else {
						// Get list of data source from configuration
						lstDS = getDSList(objCls);
						// Construct query to perform full role or group recon
						query = constructQueryForOW(lstDS);
					}
				}
				// Peforms lookup recon
				getLookupValues(query, attrsToGet, sSearchBase, objCls, handler);
			}

			// ///////////////////////////////////////
			// Get users details
			// ///////////////////////////////////////

			else if (objCls.getObjectClassValue().equalsIgnoreCase(
					OBJCLSUSERNAME)
					|| objCls.getObjectClassValue().equalsIgnoreCase(
							ACCOUNTNAME)) {
				// Get Initial Substring characters from Config Lookup

				String sLogonNameInitialSub = configuration
						.getLogonNameInitialSubstring();

				// During full reconciliation in OW, attrsToGet has only the
				// attribute 'id' but we need to get all user attribute values
				// so, we've to add all account attributes to attrsToGet.
				if (attrsToGet.size() == 1) {
					attrsToGet.addAll(sapumeSchema.getAccountAttributeNames());
					// Removed __PASSWORD__ and __ENABLE, since these attributes
					// are part of OW
					attrsToGet.remove(PASSWORDNAME);
					attrsToGet.remove(ENABLENAME);
				}

				// If query is null, we will do full reconciliation
				if (query != null) {
					FilterTerm term = (FilterTerm) query.getTerms().get(0);
					// Check query contains any and expression
					if (term.getOperands() == null
							&& term.getName().equalsIgnoreCase(DATASOURCE)) {
						bConstructFilter = true;
					}
				} else {
					// Get list of users from target based on the attributes
					// which are required for customer.
					// For eg, get list of users id from target for group update
					Object oAttrGet = options.getOptions().get(SEARCHATTRTOGET);
					if (oAttrGet != null) {
						// Reset attrToGet(list) as we are getting attribute
						// names which are given by user in user form either
						// sample form, group, or role.
						attrsToGet.clear();
						if (oAttrGet instanceof String)
							attrsToGet.add(oAttrGet.toString());
						else if (oAttrGet instanceof List) {
							List lstAttrGet = (List) oAttrGet;
							attrsToGet.addAll(lstAttrGet);
						}
					}
					bConstructFilter = true;
				}

				if (bConstructFilter) {
					characterCount = sLogonNameInitialSub.length();
				}
				for (int i = 0; i < characterCount; i++) {
					SearchRequest searchReq = new SearchRequest();
					searchReq.setSearchBase(SAPUSER);
					if (bConstructFilter) {
						// Build Filter for full reconciliation
						oFilter = constructFilterForFullRecon(
								sLogonNameInitialSub.substring(i, i + 1), query);
					} else {
						oFilter = query;
					}

					searchReq.setFilter(oFilter);
					// Start: OPAM Bug 18093761
					if (attrsToGet.size() < 1) {
						attrsToGet.add(LOGONNAME);
						attrsToGet.add(ID);
					}
					// END: OPAM Bug 18093761
					 //if search request contains id or not 
                                        //if not add id attribute
							if(!attrsToGet.contains("id"))
								attrsToGet.add("id");
					for (String sAttribute : attrsToGet) {
						searchReq.addAttribute(sAttribute);
					}

					try {
						// Logs SPML request if user requires to print request
						// in LOG
						if (configuration.getLogSPMLRequest()) {
							LOG.info(configuration
									.getMessage("SAPUME_INFO_SPML_REQUEST"),
									searchReq.toXml());
						}
						LOG.error("Perf: "+sMethodName+" Search operation started");
						SpmlResponse spmlResponse = connection
								.getResponse(searchReq.toXml());
						LOG.error("Perf: "+sMethodName+" Search operation Completed");

						SearchResponse resp = (SearchResponse) spmlResponse;
						List results = resp.getResults();
						if (attrsToGetReAttr.size() > 0) {
							searchReq = new SearchRequest();
							attrsToGet.addAll(attrsToGetReAttr);
							searchReq.setSearchBase(SAPUSER);
							if (bConstructFilter) {
								// Build Filter for full reconciliation
								oFilter = constructFilterForFullRecon(
										sLogonNameInitialSub.substring(i, i + 1), query);
							} else {
								oFilter = query;
							}

							searchReq.setFilter(oFilter);
							for (String sAttribute : attrsToGetReAttr) {
								searchReq.addAttribute(sAttribute);
							}
							if (configuration.getLogSPMLRequest()) {
								LOG.info(configuration
										.getMessage("SAPUME_INFO_SPML_REQUEST"),
										searchReq.toXml());
							}

							spmlResponse = connection
									.getResponse(searchReq.toXml());

							resp = (SearchResponse) spmlResponse;
							List reAttresults = resp.getResults();

							if (reAttresults != null) {
								for(int j=0;j<results.size();j++){
									SearchResult searchResultObject = (SearchResult)results.get(j);
									//SearchResult sr = (SearchResult)reAttresults.get(0);
									for(int k=0;k<reAttresults.size();k++){
										SearchResult searchResultInnerObject = (SearchResult)reAttresults.get(k);
										if(j==k){
											List atts = searchResultInnerObject.getAttributes();
											if (atts != null) {
												Iterator it = atts.iterator();
												while (it.hasNext()) {
													org.openspml.message.Attribute att = (org.openspml.message.Attribute)it.next();
													searchResultObject.setAttribute(att);
												}
											}
										}
									}
								}
							}
						}
						if (results != null) {
							// Log the record count
							LOG.info(configuration
									.getMessage("SAPUME_INFO_RECORDS"), objCls
									.getObjectClassValue(), results.size());
							// Added to fix Bug 23336190, Variable isContinueRecon will become False when stop button in scheduler is active - Start
							boolean isContinueRecon = true;
							// Added to fix Bug 23336190 - End
							for (Object res : results) {
								// Added to fix Bug 23336190, When Stop button in scheduler is active it will throw an Exception- Start
								if (!isContinueRecon) {
	                                                            LOG.warn("Stop processing of the result set");
	                                                            return;
	                                                           }
								// Added to fix Bug 23336190 - End
								SearchResult searchResult = (SearchResult) res;
								ConnectorObject co = createConnectorObjectUser(
										searchResult, objCls, attrsToGet);
								// Added to fix Bug 23336190, Assigning value to variable isContunueRecon - Start
								isContinueRecon = handler.handle(co);
								// Added to fix Bug 23336190 - End
							}
							
						}
							if(bConstructFilter){
								attrsToGet.removeAll(attrsToGetReAttr);
							}
						
					} catch (Exception e) {
						LOG.error(e, configuration
								.getMessage("SAPUME_ERR_FETCH_USERS"),e.getMessage());
						throw new ConnectorException(configuration
								.getMessage("SAPUME_ERR_FETCH_USERS")
								+ " " + e.getMessage(), e);
					}
				}
			} else {
				LOG.ok(configuration
						.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS"), objCls
						.getObjectClassValue());
			}
			LOG.info(configuration.getMessage("SAPUME_INFO_RETURN"));
		}
	}

	/**
	 * This constructs the spml Filter to fetch list of data source from target.
	 * 
	 * This is only applicable for OW.
	 * 
	 * @param lstDataSource
	 *            list of data source name
	 * @return Filter
	 */
	private Filter constructQueryForOW(Object oDS) {
		LOG.info("BEGIN");
		Filter oFilter = new Filter();
		FilterTerm oMainFilterTerm = new FilterTerm();

		// If datasource as type of String
		// This value will passed from user form if customer pass single DS
		if (oDS instanceof String) {
			oMainFilterTerm.setOperation(FilterTerm.OP_EQUAL);
			oMainFilterTerm.setName(DATASOURCE);
			oMainFilterTerm.setValue(oDS);
			oFilter.addTerm(oMainFilterTerm);
		}
		// If datasource as type of List
		// This value will fetched from configuration page
		else {
			oMainFilterTerm.setOperation(FilterTerm.OP_OR);
			oFilter.addTerm(oMainFilterTerm);

			List<String> lstDataSource = (List<String>) oDS;

			for (String sDS : lstDataSource) {
				FilterTerm oSubFilterTerm = new FilterTerm();
				oSubFilterTerm.setOperation(FilterTerm.OP_EQUAL);
				oSubFilterTerm.setName(DATASOURCE);
				oSubFilterTerm.setValue(sDS);
				oMainFilterTerm.addOperand(oSubFilterTerm);
			}
		}
		LOG.info("RETURN");
		return oFilter;
	}

	/**
	 * Get role and group values from target and updates in IdM.
	 * 
	 * @param oFilter
	 *            Filter name
	 * @param attrsToGet
	 *            account attribute names which values need to fetch from
	 *            target.
	 * @param sSearchBase
	 *            It could be either any one of saprole or sapgroup
	 * @param oclass
	 *            Object class
	 */
	private void getLookupValues(Filter oFilter, ArrayList<String> attrsToGet,
			String sSearchBase, ObjectClass oclass, ResultsHandler handler) {
		LOG.info("BEGIN");
		final String sMethodName="getLookupValues";
		SearchRequest searchReq = new SearchRequest();
		SpmlResponse spmlResponse = null;
		searchReq.setSearchBase(sSearchBase);

		// Setting the filter if it is not null
		if (oFilter != null) {
			searchReq.setFilter(oFilter);
		}

		// Add the attribute which values are going to be fetched from target.
		for (String attrs : attrsToGet) {
			searchReq.addAttribute(attrs);
		}

		try {
			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest()) {
				LOG.info(configuration.getMessage("SAPUME_INFO_SPML_REQUEST"),
						searchReq.toXml());
			}
			LOG.error("Perf: "+sMethodName+" recon operation started for {0}", sSearchBase);
			spmlResponse = connection.getResponse(searchReq.toXml());
			LOG.error("Perf: "+sMethodName+" recon operation completed for {0}", sSearchBase);

		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_SEARCH_ROLES_GROUPS"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_SEARCH_ROLES_GROUPS")
					+ " " + e.getMessage(), e);
		}

		// Build connector object for Roles and Groups
		try {
			String spmlResult = spmlResponse.getResult();
			SearchResponse resp = (SearchResponse) spmlResponse;
			List<SearchResult> results = resp.getResults();

			if (results != null) {
				// Log the record count
				LOG.info(configuration.getMessage("SAPUME_INFO_RECORDS"),
						oclass.getObjectClassValue(), results.size());
				// Added to fix Bug 23336190, Variable isContinueRecon will become False when stop button in scheduler is active - Start
				boolean isContinueRecon = true;
				// Added to fix Bug 23336190 - End
				for (SearchResult searchResult : results) {
					try {
						// Added to fix Bug 23336190, When Stop button in scheduler is active it will throw an Exception- Start
						if (!isContinueRecon) {
                                                    LOG.ok("Stop processing of the result set");
                                                    return;
                                                   }
						// Added to fix Bug 23336190 - End
						ConnectorObject co = createConnectorObjectGroupandRole(
								searchResult, oclass, attrsToGet);
						// Added to fix Bug 23336190, Assigning value to variable isContunueRecon - Start
						isContinueRecon = handler.handle(co);
						// Added to fix Bug 23336190 - End
					} catch (Exception e) {
						LOG.error(e,configuration.getMessage("SAPUME_ERR_SEARCH_ROLES_GROUPS_HANDLER"),
										e.getMessage());
						throw new ConnectorException(
								configuration.getMessage("SAPUME_ERR_SEARCH_ROLES_GROUPS_HANDLER")
										+ " " + e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_SEARCH_ROLES_GROUPS"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_SEARCH_ROLES_GROUPS")
					+ " " + e.getMessage(), e);
		}
		LOG.info("RETURN");
	}

	/**
	 * Filter is constructed to fetch all user accounts from target.
	 * 
	 * @param startsubString
	 *            It is initial substring value in filter
	 * @param query
	 *            Filter
	 * @return Filter
	 */

	private Filter constructFilterForFullRecon(String startsubString,
			Filter query) {
		LOG.info("BEGIN");
		Filter oFilter = new Filter();
		FilterTerm oMainFilterTerm = new FilterTerm();

		FilterTerm oSubFilterTerm = new FilterTerm();
		oSubFilterTerm.setOperation(FilterTerm.OP_SUBSTRINGS);
		oSubFilterTerm.setName(LOGONNAME);
		oSubFilterTerm.setInitialSubstring(startsubString);

		if (query != null) {
			oMainFilterTerm.setOperation(FilterTerm.OP_AND);
			oMainFilterTerm.addOperand((FilterTerm) query.getTerms().get(0));
			oMainFilterTerm.addOperand(oSubFilterTerm);
			oFilter.addTerm(oMainFilterTerm);
		} else {
			oFilter.addTerm(oSubFilterTerm);
		}
		LOG.info("RETURN");
		return oFilter;
	}

	/**
	 * Create ConnectorObject for user based on the SAP UME SearchResult
	 * 
	 * @param searchResult
	 *            result holding the attribute values
	 * @param objectClass
	 *            ObjectClass of object to create
	 * @param attrsToGet
	 *            Attributes to get
	 * @return ConnectorObject
	 */
	ConnectorObject createConnectorObjectUser(SearchResult searchResult,
			final ObjectClass objectClass, final Collection<String> attrsToGet) {
		LOG.info("BEGIN");
		ConnectorObjectBuilder objectBuilder = new ConnectorObjectBuilder();
		String sLogonName = null, sId = null;
		Object value = null;
		try {
			Iterator<String> itr = attrsToGet.iterator();
			while (itr.hasNext()) {
				value = null;
				String sAttributeName = itr.next();

				// Handling for multi valued attributes
				if (sAttributeName.equalsIgnoreCase(ASSIGNED_ROLES)
						|| sAttributeName.equalsIgnoreCase(ASSIGNED_GROUPS)
						|| sAttributeName.equalsIgnoreCase(ALLASSIGNED_ROLES)
						|| sAttributeName.equalsIgnoreCase(ALLASSIGNED_GROUPS)) {
					AttributeBuilder abuilder = new AttributeBuilder();
					abuilder.setName(sAttributeName);
					if (searchResult.getAttributeValue(sAttributeName) != null) {

						value = searchResult.getAttributeValue(sAttributeName);
						LOG.info(configuration
								.getMessage("SAPUME_INFO_ATTRIBUTE_VALUE"),
								value);

						if (value instanceof ArrayList) {
							ArrayList multiList = (ArrayList) value;
							for (Object values : multiList) {
								String attrValue = (String) values;
								abuilder.addValue(attrValue);
							}
						} else {
							String attrValue = (String) value;
							abuilder.addValue(attrValue);
						}
					} else {
						ArrayList alMultiValues = new ArrayList();
						abuilder.addValue(alMultiValues);
					}
					Attribute multiAttrs = abuilder.build();
					objectBuilder.addAttribute(multiAttrs);
				} // Handling of Date fields
				else if (sAttributeName.equalsIgnoreCase(VALID_TO)
						|| sAttributeName.equalsIgnoreCase(VALID_FROM)) {
					DateFormat formatter = new SimpleDateFormat(
							RECON_DATE_FORMAT);
					Date date = null;
					if (searchResult.getAttributeValue(sAttributeName) != null) {
						value = searchResult.getAttributeValue(sAttributeName);
						date = formatter.parse((String) value);
						value = date.getTime();

					}
					objectBuilder.addAttribute(AttributeBuilder.build(
							sAttributeName, value));
					// Handling for Enable/Disable resource
					if (sAttributeName.equalsIgnoreCase(VALID_TO)) {
						Date dtToday = new Date();
						// Disable the resource
						if ((date != null) && date.before(dtToday)) {
							objectBuilder.addAttribute(AttributeBuilder
									.buildEnabled(false));
						} else {
							objectBuilder.addAttribute(AttributeBuilder
									.buildEnabled(true));
						}
					}
				} else {
					// Handling other attributes such as logoname, firstname,
					// etc.,
					if (searchResult.getAttributeValue(sAttributeName) != null) {
						value = searchResult.getAttributeValue(sAttributeName);
						objectBuilder.addAttribute(AttributeBuilder.build(
								sAttributeName, value));
					}
					if (sAttributeName.equalsIgnoreCase(LOGONNAME)) {
						sLogonName = (String) value;
					}
					if (sAttributeName.equalsIgnoreCase(ID)) {
						sId = (String) value;
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e, configuration
					.getMessage("SAPUME_ERR_CREATE_CONNECTOR_OBJECT"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_CREATE_CONNECTOR_OBJECT")
					+ " " + e.getMessage(), e);
		}

		if (sId != null)
			objectBuilder.setUid(sId);

		// Set the value of id as logon name in case of logon name is null
		// This implementation is to get user list for group update
		if (sLogonName != null)
			objectBuilder.setName(sLogonName);
		else
			objectBuilder.setName(sId);

		objectBuilder.setObjectClass(objectClass);
		LOG.info("RETURN");
		return objectBuilder.build();
	}

	/**
	 * Create ConnectorObject is created for Role and Group
	 * 
	 * @param oClass
	 *            ObjectClass of object to create.
	 * @param searchResult
	 *            SearchResult holds the SPML response
	 * @param attrsToGet
	 *            contains attribute which values are fetched from target.
	 * @return ConnectorObject
	 */
	ConnectorObject createConnectorObjectGroupandRole(
			SearchResult searchResult, ObjectClass oClass,
			ArrayList<String> attrsToGet) {
		LOG.info("BEGIN");
		ConnectorObjectBuilder objectBuilder = new ConnectorObjectBuilder();
		String sIdentifier = null;
		try {
			Iterator<String> itrAttrToGet = attrsToGet.iterator();

			if (searchResult.getIdentifier().getId() != null) {
				sIdentifier = searchResult.getIdentifier().getId();
				objectBuilder.addAttribute(AttributeBuilder.build(ID,
						sIdentifier));
			}
			while (itrAttrToGet.hasNext()) {
				Object value = null;
				String sAttributeName = itrAttrToGet.next();
				// Handling for multi valued attributes (assigned roles &
				// members)
				if (sAttributeName.equalsIgnoreCase(ASSIGNED_ROLES)
						|| sAttributeName.equalsIgnoreCase(MEMBER)) {
					AttributeBuilder abuilder = new AttributeBuilder();
					abuilder.setName(sAttributeName);
					if (searchResult.getAttributeValue(sAttributeName) != null) {
						value = searchResult.getAttributeValue(sAttributeName);
						if (value instanceof ArrayList) {
							ArrayList multiList = (ArrayList) value;
							for (Object values : multiList) {
								String attrValue = (String) values;
								abuilder.addValue(attrValue);
							}
						} else {
							String attrValue = (String) value;
							abuilder.addValue(attrValue);
						}
					} else {
						abuilder.addValue(value);
					}
					Attribute multiAttrs = abuilder.build();
					objectBuilder.addAttribute(multiAttrs);
				} else if (sAttributeName.equals(DATASOURCE)) {
					objectBuilder.addAttribute(AttributeBuilder.build(
							sAttributeName, sIdentifier.split("\\.")[1]));
				}
				// Process single value atribute such as description in case if
				// it is not null
				else if (searchResult.getAttribute(sAttributeName) != null
						&& searchResult.getAttributeValue(sAttributeName) != null) {
					objectBuilder.addAttribute(AttributeBuilder.build(
							sAttributeName, searchResult.getAttributeValue(
									sAttributeName).toString()));
				}
				// If any target system attribute value is null then set
				// that attribute value as id
				else {
					objectBuilder.addAttribute(AttributeBuilder.build(
							sAttributeName, sIdentifier));
				}
			}

		} catch (Exception e) {
			LOG.error(e, configuration
					.getMessage("SAPUME_ERR_GET_RLS_GRPS_CONNECTOROBJECT"),e.getMessage());
			throw new ConnectorException(configuration
					.getMessage("SAPUME_ERR_GET_RLS_GRPS_CONNECTOROBJECT")
					+ " " + e.getMessage(), e);
		}
		objectBuilder.setUid(sIdentifier);
		objectBuilder.setName(sIdentifier);
		objectBuilder.setObjectClass(oClass);
		LOG.info("RETURN");
		return objectBuilder.build();

	}

	/**
	 * This will returns datasource names which are present in configuration
	 * page of resource.
	 * 
	 * @param objCls
	 * @return list of datasource name
	 */
	private List<String> getDSList(ObjectClass objCls) {
		List<String> lstDS = new ArrayList<String>();
		if (objCls.is(OBJCLSROLE)) {
			lstDS = Arrays.asList(configuration.getRoleDatasource());
		} else if (objCls.is(OBJCLSGROUP) || objCls.is(OBJCLSGROUPNAME)) {
			lstDS = Arrays.asList(configuration.getGroupDatasource());
		}
		return lstDS;
	}

	/**
	 * Dispose resources
	 */
	void dispose() {
		LOG.info("BEGIN");
		// Since we are getting connection every time
		// before processing SPML request, this method will do
		// only clean up of configuration and connection object.
		configuration = null;
		connection = null;
		LOG.info("END");

	}
}

