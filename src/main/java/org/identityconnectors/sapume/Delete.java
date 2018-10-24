/**
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description: SAPUME Connector Delete Operation implementation
 * Source code : Delete.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.openspml.message.DeleteRequest;
import org.openspml.message.SpmlResponse;

public class Delete {
	  
	private SAPUMEConnection connection = null;
	private SAPUMEConfiguration configuration = null;
	private SAPUMEUtil util = null;
	private static final Log LOG = Log.getLog(Delete.class);

	/**
	 * comment for Check in test
	 * Constructor
	 * 
	 * @param connection
	 *            {@link org.identityconnectors.sapume.SAPUMEConnection} to be
	 *            used
	 */
	Delete(final SAPUMEConfiguration configuration,
			final SAPUMEConnection connection) {
		this.configuration = configuration;
		this.connection = connection;
	}

	/**
	 * This will delete accounts (User or Group) in target by creating
	 * DeleteRequest.
	 * 
	 * @param String
	 *            uid of the User/Group to be deleted
	 */
	public void delete(String uid) {
		LOG.info("BEGIN");
		DeleteRequest delReq = new DeleteRequest();
		LOG.info(configuration.getMessage("SAPUME_INFO_DELETE_USER"), uid);

		// EX: uid =SAP.PRIVATEDATASOURCE.un:sapuser1
		delReq.setIdentifier(uid);
		try {

			// Logs SPML request if user requires to print request in LOG
			if (configuration.getLogSPMLRequest()) {
				LOG.info(configuration.getMessage("SAPUME_INFO_SPML_REQUEST"),delReq.toXml());
			}
			LOG.error("Perf: delete request started for user {0}", uid);
			SpmlResponse spmlResponse = connection.getResponse(delReq.toXml());
			util = new SAPUMEUtil(connection,configuration);
			String sUid = util.getResponse(spmlResponse, uid);
			LOG.info(configuration.getMessage("SAPUME_INFO_DELETE_USER_SUCCESS"), uid);
		}catch(UnknownUidException unknownUidException){
			LOG.error(unknownUidException, configuration.getMessage("SAPUME_ERR_DELETE_USER"),unknownUidException.getMessage());
			throw new UnknownUidException(configuration.getMessage("SAPUME_ERR_DELETE_USER") + " " + unknownUidException.getMessage(), unknownUidException);
		}
		catch (Exception e) {
			LOG.error(e, configuration.getMessage("SAPUME_ERR_DELETE_USER"),e.getMessage());
			throw new ConnectorException(configuration.getMessage("SAPUME_ERR_DELETE_USER") + " " + e.getMessage(), e);          
		}
		LOG.info("END");
	}

	/**
	 * Dispose resources
	 */
	void dispose() {
		LOG.info("BEGIN");
		// Since we are getting connection every time
		// before processing SPML request, this method will do only
		// clean up of connection object.
		this.connection = null;
		LOG.info("END");
	}
}
