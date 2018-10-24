/*
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description :SAPUME Connector Schema placeholder
 * Source code : SAPUMESchema.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfoUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;

/**
 * Class to build schema
 * 
 * @author jagadeeshkumar.r
 * 
 */
public final class SAPUMESchema implements SAPUMEConstants {
	private static final Log LOG = Log.getLog(SAPUMESchema.class);
	private  Map<String, AttributeInfo> _accountAttributeMap;
	private  Map<String, AttributeInfo> _groupAttributeMap;
	private  Map<String, AttributeInfo> _roleAttributeMap;
	private  Set<String> _accountAttributeNames;
	private  Set<String> _groupAttributeNames;
	private  Set<String> _roleAttributeNames;
	private  Schema _schema;
	//private SAPUMEConnection connection = null;
	// START :: Bug 18895142
	private SAPUMEConnection connection = null;
	private SAPUMEConfiguration configuration = null;
	// END :: Bug 18895142
	
	public SAPUMESchema() {
		
	}
	

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            {@link SAPUMEConfiguration} to use
	 * @param connection
	 *            {@link SAPUMEConnection} to use
	
	 */
	public SAPUMESchema(final SAPUMEConfiguration configuration,final SAPUMEConnection connection ) {
		this.configuration = configuration;
		this.connection = connection;
	}
	public  Map<String, AttributeInfo> getAccountAttributeMap() {
		ensureSchema();
		return _accountAttributeMap;
	}

	public  Map<String, AttributeInfo> getGroupAttributeMap() {
		ensureSchema();
		return _groupAttributeMap;
	}

	public  Map<String, AttributeInfo> getRoleAttributeMap() {
		ensureSchema();
		return _roleAttributeMap;
	}

	public  Set<String> getAccountAttributeNames() {
		ensureSchema();
		return _accountAttributeNames;
	}

	public  Set<String> getGroupAttributeNames() {
		ensureSchema();
		return _groupAttributeNames;
	}

	public  Set<String> getRoleAttributeNames() {
		ensureSchema();
		return _roleAttributeNames;
	}

	public  Schema getSchema() {
		ensureSchema();
		return _schema;
	}

	private  void ensureSchema() {
		if (_schema == null) {
			_schema = initSchema();
		}
	}

	private  Schema initSchema() {

		if (_schema != null) {
			return _schema;
		}

		final SchemaBuilder schemaBuilder = new SchemaBuilder(
				SAPUMEConnector.class);
		LinkedHashMap<String, ArrayList<String>> linkedSet = null;
		SAPUMEUtil sapumeUtil = new SAPUMEUtil(connection,configuration);
		linkedSet = sapumeUtil.getAtrributesFomTarget();
		ObjectClassInfo oci = null;
		ObjectClassInfo groupInfo = null;
		ObjectClassInfo roleInfo = null;

		{
			LOG.info("BEGIN");

			Set<AttributeInfo> attributes = getAttributesforObjectClass(
					SAPUSER, linkedSet);
			attributes.add(OperationalAttributeInfos.ENABLE);
			_accountAttributeMap = AttributeInfoUtil.toMap(attributes);
			_accountAttributeNames = _accountAttributeMap.keySet();
			// Start:Unique ID Issue in AC - BUG 17910026
			attributes.add(AttributeInfoBuilder
					.buildCurrentAttributes(ObjectClass.ACCOUNT_NAME));

			ObjectClassInfoBuilder objcBuilder = new ObjectClassInfoBuilder();
			objcBuilder.setType(ObjectClass.ACCOUNT_NAME);
			objcBuilder.addAllAttributeInfo(attributes);
			oci = objcBuilder.build();
			schemaBuilder.defineObjectClass(oci);
		}
		// End - BUG 17910026

		{
			Set<AttributeInfo> attributes = getAttributesforObjectClass(
					SAPROLE, linkedSet);
			_roleAttributeMap = AttributeInfoUtil.toMap(attributes);
			_roleAttributeNames = _roleAttributeMap.keySet();

			ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
			ociBuilder.addAllAttributeInfo(attributes);
			ociBuilder.setType(OBJCLSROLE);
			roleInfo = ociBuilder.build();
			schemaBuilder.defineObjectClass(roleInfo);
		}
		{
			Set<AttributeInfo> attributes = getAttributesforObjectClass(
					SAPGROUP, linkedSet);
			_groupAttributeMap = AttributeInfoUtil.toMap(attributes);
			_groupAttributeNames = _groupAttributeMap.keySet();

			ObjectClassInfoBuilder ociBuilder = new ObjectClassInfoBuilder();
			ociBuilder.addAllAttributeInfo(attributes);
			ociBuilder.setType(OBJCLSGROUP);
			groupInfo = ociBuilder.build();
			schemaBuilder.defineObjectClass(groupInfo);
		}
		schemaBuilder.clearSupportedObjectClassesByOperation();
		schemaBuilder.addSupportedObjectClass(CreateOp.class, oci);
		schemaBuilder.addSupportedObjectClass(SchemaOp.class, oci);
		schemaBuilder.addSupportedObjectClass(TestOp.class, oci);
		schemaBuilder.addSupportedObjectClass(DeleteOp.class, oci);
		schemaBuilder.addSupportedObjectClass(UpdateAttributeValuesOp.class,
				oci);
		schemaBuilder.addSupportedObjectClass(SearchOp.class, oci);
		schemaBuilder.addSupportedObjectClass(SearchOp.class, roleInfo);
		schemaBuilder.addSupportedObjectClass(SearchOp.class, groupInfo);	
		schemaBuilder.addSupportedObjectClass(TestOp.class, roleInfo);
		schemaBuilder.addSupportedObjectClass(TestOp.class, groupInfo);

		_schema = schemaBuilder.build();

		LOG.info("RETURN");
		return _schema;
	}

	/**
	 * Get attributes as per the Object class
	 * 
	 * @param objClass
	 *            -String
	 * @param linkedSet -
	 *            LinkedHashMap<String,ArrayList<String>>
	 * @return - Set<AttributeInfo>
	 */

	private  Set<AttributeInfo> getAttributesforObjectClass(
			String objClass, LinkedHashMap<String, ArrayList<String>> linkedSet) {
		Set<AttributeInfo> attributes = null;

		if (linkedSet != null && !linkedSet.isEmpty()) {
			attributes = new HashSet<AttributeInfo>();
			ArrayList<String> attrs = linkedSet.get(objClass);
			if (attrs != null) {
				String[] arrayOfString = attrs.toString().split(",");
				for (String str : arrayOfString) {
					if (str.contains("[") || str.contains("]")) {
						str = str.replace("[", "");
						str = str.replace("]", "");
					}
					LOG.info("attribute name :: {0}" + str);
					if (LOGONNAME.equalsIgnoreCase(str)) {
						attributes.add(AttributeInfoBuilder.build(str,
								String.class, EnumSet.of(Flags.REQUIRED,
										Flags.NOT_UPDATEABLE)));
					} else if (LASTNAME.equalsIgnoreCase(str)
							|| UNIQUENAME.equals(str)) {
						attributes.add(AttributeInfoBuilder.build(str,
								String.class, EnumSet.of(Flags.REQUIRED)));
					} else if (PASSWORD.equalsIgnoreCase(str)) {
						attributes.add(AttributeInfoBuilder.build(
								OperationalAttributes.PASSWORD_NAME,
								GuardedString.class, EnumSet.of(
										Flags.NOT_READABLE,
										Flags.NOT_RETURNED_BY_DEFAULT, Flags.REQUIRED)));
					}else if (OLDPASSWORD.equalsIgnoreCase(str)) {
						attributes.add(AttributeInfoBuilder.build(str,
								GuardedString.class, EnumSet.of(
										Flags.NOT_READABLE,
										Flags.NOT_RETURNED_BY_DEFAULT)));
					} else if (ASSIGNED_ROLES.equalsIgnoreCase(str)
							|| ASSIGNED_GROUPS.equalsIgnoreCase(str)) {
						attributes.add(AttributeInfoBuilder.build(str,
								String.class, EnumSet.of(Flags.MULTIVALUED)));
					} else if (DATASOURCE.equals(str)
							|| ALLASSIGNED_ROLES.equals(str)
							|| ALLASSIGNED_GROUPS.equals(str)) {
						attributes.add(AttributeInfoBuilder.build(str,
								String.class, EnumSet.of(Flags.NOT_CREATABLE,
										Flags.NOT_UPDATEABLE)));
					}else if (ISLOCKED.equals(str)
							|| ISPASSWORDDISABLED.equals(str)) {
						attributes.add(AttributeInfoBuilder.build(str,
								boolean.class)); 
					}else {
						attributes.add(AttributeInfoBuilder.build(str
								.toString(), String.class));
					}
				}
			}
		}
		return attributes;
	}
}
