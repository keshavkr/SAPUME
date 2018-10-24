/**
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description :Main implementation of the SAPUME Connector
 * Source code : SAPUMEConnector.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.AttributeNormalizer;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.openspml.message.Filter;

/**
 * Main implementation of the SAPUME Connector
 */
@ConnectorClass(displayNameKey = "SAPUME", configurationClass = SAPUMEConfiguration.class)
public class SAPUMEConnector implements Connector, CreateOp, SchemaOp, TestOp,
		AuthenticateOp, DeleteOp, UpdateAttributeValuesOp, SearchOp<Object>,
		AttributeNormalizer, SAPUMEConstants {

	/**
	 * Setup logging for the {@link SAPUMEConnector}.
	 */
	private static final Log LOG = Log.getLog(SAPUMEConnector.class);
	/**
	 * Place holder for the Connection created in the init method
	 */
	private SAPUMEConnection _connection;

	/**
	 * Place holder for the {@link Configuration} passed into the init() method
	 * {@link SAPUMEConnector#init}.
	 */
	private SAPUMEConfiguration _config;
	private Create _create = null;
	private Update _update = null;
	private Delete _delete = null;
	private Query _query = null;
	SAPUMESchema sapumeSchema = null;

	/**
	 * Initializes connection if not already initialized plus other necessary
	 * classes
	 */
	private void doInit() {
		LOG.info("BEGIN");
		if (_connection == null) {
			_connection = new SAPUMEConnection(_config);
			_create = new Create(_config, _connection);
			_update = new Update(_config, _connection);
			_delete = new Delete(_config, _connection);
			_query = new Query(_config, _connection);
			sapumeSchema = new SAPUMESchema(_config,_connection);

		}
		LOG.info("RETURN");
	}

	/**
	 * Disposes of the {@link SAPUMECOnnector}'s resources.
	 */
	public void dispose() {
		LOG.info("BEGIN");
		if (_query != null) {
			_query.dispose();
			_query = null;
		}
		if (_create != null) {
			_create.dispose();
			_create = null;
		}
		if (_update != null) {
			_update.dispose();
			_update = null;
		}
		if (_delete != null) {
			_delete.dispose();
			_delete = null;
		}
		if (_connection != null) {
			_connection.dispose();
			_connection = null;
		}
		LOG.info("RETURN");
	}

	/**
	 * Gets the Configuration context for this connector.
	 */
	public Configuration getConfiguration() {
		return _config;
	}

	/**
	 * Callback method to receive the {@link Configuration}.
	 * 
	 * @see Connector#init
	 */
	public void init(Configuration cfg) {
		_config = (SAPUMEConfiguration) cfg;
		doInit();		
	}

	/**
	 * {@inheritDoc}
	 */
	public Uid create(ObjectClass objClass, Set<Attribute> attrs,
			OperationOptions options) {
		Uid uid = null;
		String targetObjCls = null;
		LOG.info("BEGIN");
		LOG.error("Perf: create() Entered for user {0}", AttributeUtil.getNameFromAttributes(attrs).getNameValue());
		doInit();

		try {
			if (objClass.is(ObjectClass.ACCOUNT_NAME)) {
				// Object class is set to 'sapuser'
				targetObjCls = SAPUSER;
			} else if (objClass.is(ObjectClass.GROUP_NAME)
					|| objClass.is(OBJCLSGROUP)) {
				// Object class is set to 'sapgroup'
				targetObjCls = SAPGROUP;
			} else {
				LOG.error(_config.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS"),objClass);
				throw new ConnectorException(_config
						.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS")
						+ " " + new IllegalArgumentException().getMessage(),
						new IllegalArgumentException());
			}
		} catch (Exception e) {
			LOG.error(e, _config.getMessage("SAPUME_ERR_UME_CREATE"),e.getMessage());
			throw new ConnectorException(_config
					.getMessage("SAPUME_ERR_UME_CREATE")
					+ " " + e.getMessage(), e);

		}

		uid = _create.create(targetObjCls, attrs);
		LOG.error("Perf: create() Exiting for user {0}", uid);
		LOG.info("RETURN");		
		return uid;
	}

	/**
	 * {@inheritDoc}
	 */
	public Schema schema() {
		LOG.error("Perf: schema() Entered");
		doInit();
		LOG.error("Perf: schema() Exiting");
		return sapumeSchema.getSchema();
	}

	/**
	 * {@inheritDoc}
	 */
	public void test() {
		LOG.error("Perf: test() Entered");
		doInit();
		_connection.test();
		LOG.error("Perf: test() Exiting");
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(ObjectClass objClass, Uid uid, OperationOptions options) {
		LOG.info("BEGIN");
		LOG.error("Perf: delete() Entered for user {0}", uid.getUidValue().toString());
		doInit();
		_delete.delete(uid.getUidValue().toString());
		LOG.error("Perf: delete() Exiting for user {0}", uid.getUidValue().toString());
		LOG.ok("RETURN");
	}

	/**
	 * {@inheritDoc}
	 */
	public Uid addAttributeValues(ObjectClass objClass, Uid uid,
			Set<Attribute> valuesToAdd, OperationOptions options) {
		LOG.info("BEGIN");
		LOG.error("Perf: addAttributeValues() Entered for user {0}", uid.getUidValue().toString());
		String targetObjCls = null;
		doInit();

		try {
			if (objClass.is(ObjectClass.ACCOUNT_NAME)) {
				// Object class is set to 'sapuser'
				targetObjCls = SAPUSER;
			} else if (objClass.is(ObjectClass.GROUP_NAME)
					|| objClass.is(OBJCLSGROUP)) {
				// Object class is set to 'sapgroup'
				targetObjCls = SAPGROUP;
			} else if (objClass.is(OBJCLSROLE)) {
				// Object class is set to 'saprole'
				targetObjCls = SAPROLE;
			} else {
				LOG.error(_config.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS"),objClass);
				throw new ConnectorException(_config
						.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS")
						+ " " + new IllegalArgumentException().getMessage(),
						new IllegalArgumentException());
			}
		} catch (Exception e) {
			LOG.error(e, _config.getMessage("SAPUME_ERR_UME_ADDATTR"),e.getMessage());
			throw new ConnectorException(_config
					.getMessage("SAPUME_ERR_UME_ADDATTR")
					+ " " + e.getMessage(), e);
		}
		// Bug 17910026 - added objClass argument in the method
		uid = _update.addAttributeValues(uid, valuesToAdd, targetObjCls,
				objClass);
		LOG.error("Perf: addAttributeValues() Exiting for user {0}", uid.getUidValue().toString());
		LOG.info("RETURN");
		return uid;
	}

	/**
	 * {@inheritDoc}
	 */
	public Uid removeAttributeValues(ObjectClass objClass, Uid uid,
			Set<Attribute> valuesToRemove, OperationOptions options) {
		LOG.info("BEGIN");
		LOG.error("Perf: removeAttributeValues() Entered for user {0}", uid.getUidValue().toString());
		String targetObjCls = null;
		doInit();

		try {
			if (objClass.is(ObjectClass.ACCOUNT_NAME)) {
				// Object class is set to 'sapuser'
				targetObjCls = SAPUSER;
			} else if (objClass.is(ObjectClass.GROUP_NAME)
					|| objClass.is(OBJCLSGROUP)) {
				// Object class is set to 'sapgroup'
				targetObjCls = SAPGROUP;
			} else if (objClass.is(OBJCLSROLE)) {
				// Object class is set to 'saprole'
				targetObjCls = SAPROLE;
			} else {
				LOG.error(_config.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS"),objClass);
				throw new ConnectorException(_config
						.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS")
						+ " " + new IllegalArgumentException().getMessage(),
						new IllegalArgumentException());
			}
		} catch (Exception e) {
			LOG.error(e, _config.getMessage("SAPUME_ERR_UME_REMOVEATTR"),e.getMessage());
			throw new ConnectorException(_config
					.getMessage("SAPUME_ERR_UME_REMOVEATTR")
					+ " " + e.getMessage(), e);

		}
		// Bug 17910026 - added objClass argument in the method
		uid = _update.removeAttributeValues(uid, valuesToRemove, targetObjCls,
				objClass);
		LOG.error("Perf: removeAttributeValues() Exiting for user {0}", uid.getUidValue().toString());
		LOG.info("RETURN");
		return uid;
	}

	/**
	 * {@inheritDoc}
	 */
	public Uid update(ObjectClass objClass, Uid uid,
			Set<Attribute> replaceAttributes, OperationOptions options) {
		LOG.info("BEGIN");
		LOG.error("Perf: update() Entered for user {0}", uid.getUidValue().toString());
		String targetObjCls = null;
		doInit();

		try {
			if (objClass.is(ObjectClass.ACCOUNT_NAME)) {
				// Object class is set to 'sapuser'
				targetObjCls = SAPUSER;
			} else if (objClass.is(ObjectClass.GROUP_NAME)
					|| objClass.is(OBJCLSGROUP)) {
				// Object class is set to 'sapgroup'
				targetObjCls = SAPGROUP;
			} else if (objClass.is(OBJCLSROLE)) {
				// Object class is set to 'saprole'
				targetObjCls = SAPROLE;
			} else {
				LOG.error(_config.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS"),objClass);
				throw new ConnectorException(_config
						.getMessage("SAPUME_ERR_INVALID_OBJECTCLASS")
						+ " " + new IllegalArgumentException().getMessage(),
						new IllegalArgumentException());
			}
		} catch (Exception e) {
			LOG.error(e, _config.getMessage("SAPUME_ERR_UME_UPDATE"),e.getMessage());
			throw new ConnectorException(_config
					.getMessage("SAPUME_ERR_UME_UPDATE")
					+ " " + e.getMessage(), e);
		}
		// Bug 17910026 - added objClass argument in the method
		uid = _update.update(uid, replaceAttributes, targetObjCls, objClass,sapumeSchema);
		LOG.error("Perf: update() Exiting for user {0}", uid.getUidValue().toString());
		LOG.info("RETURN");
		return uid;
	}

	/**
	 * {@inheritDoc}
	 */
	public FilterTranslator<Object> createFilterTranslator(ObjectClass oclass,
			OperationOptions options) {
		return new SAPUMEFilterTranslator(oclass, this._connection,
				this._config);
	}

	/**
	 * {@inheritDoc}
	 */
	public void executeQuery(ObjectClass oclass, Object query,
			ResultsHandler handler, OperationOptions options) {
		LOG.info("BEGIN");
		LOG.error("Perf: executeQuery() Entered");
		doInit();
		_query.executeQuery(oclass, (Filter)query, handler, options,sapumeSchema);
		LOG.error("Perf: executeQuery() Exiting");
		LOG.info("RETURN");

	}

	/**
	 * {@inheritDoc}
	 */
	public Attribute normalizeAttribute(ObjectClass oclass, Attribute attribute) {
		LOG.info("BEGIN");
		if (oclass.is(ObjectClass.ACCOUNT_NAME) && attribute.is(Name.NAME)) {
			String value = (String) attribute.getValue().get(0);
			// Since search does case sensitive comparision, should not do the
			// upper case conversion
			return new Name(value.trim());
		} else if (attribute.is(Uid.NAME)) {
			String value = (String) attribute.getValue().get(0);
			// Since search does case sensitive comparision, should not do the
			// upper case conversion
			return new Uid(value.trim());
		}
		LOG.info("RETURN");
		return attribute;
	}

	/**
	 * {@inheritDoc}
	 */
	public Uid authenticate(ObjectClass objectClass, String username,
			GuardedString password, OperationOptions options) {
		LOG.info(String.format("username = %s", username));
		return null;
	}


}
