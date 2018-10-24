/* $Header: idc/bundles/java/sapume/src/test/java/org/identityconnectors/sapume/test/SAPUMEConnectorTests.java /main/47 2015/04/01 23:53:39 smelgiri Exp $ */

/* Copyright (c) 2011, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YYYY)
    jagadeeshkumar.r         10/15/2014 - Creation
 */

/**
 *  @author  jagadeeshkumar.r     
 */
package org.identityconnectors.sapume.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.sapume.SAPUMEConfiguration;
import org.identityconnectors.sapume.SAPUMEConstants;
import org.junit.Assert;
//import org.junit.FixMethodOrder;
import org.junit.Test;
//import org.junit.runners.MethodSorters;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SAPUMEConnectorTests {

	
	String user ="testuser";
	private SAPUMETestUtils _utils = new SAPUMETestUtils();
	SAPUMEConfiguration cfg = new SAPUMEConfiguration();
	OperationOptionsBuilder builder = new OperationOptionsBuilder();
	private static final Log LOGGER = Log.getLog(SAPUMEConnectorTests.class);
	// Create a test user 
	@Test
	public void test0_create() {
		//get a free available user
		_utils.handleExistingUser(user); 

		Set<Attribute> attrSet = new HashSet<Attribute>();
		attrSet.add(SAPUMETestUtils.newAttr(Name.NAME, user));
		attrSet.add(SAPUMETestUtils.newAttr(
				OperationalAttributes.PASSWORD_NAME, new GuardedString(
						"Welcome123".toCharArray())));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LASTNAME, "TESTLNAME"));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.FIRSTNAME, "TESTFNAME"));    
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LOGONNAME, user));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_FROM, 1412781268081L));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_TO, 16749397800000L));
		Uid _uid = ICFTestHelper.getInstance().getFacade().create(ObjectClass.ACCOUNT, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),_uid.getUidValue());
	}
	@Test
	public void test0_createWithChildData() {
		final List<String> groupList = new ArrayList<String>();
		final List<String> roleList = new ArrayList<String>();
		//get a free available user
		_utils.handleExistingUser(user); 

		Set<Attribute> attrSet = new HashSet<Attribute>();
		attrSet.add(SAPUMETestUtils.newAttr(Name.NAME, user));
		attrSet.add(SAPUMETestUtils.newAttr(
				OperationalAttributes.PASSWORD_NAME, new GuardedString(
						"Welcome123".toCharArray())));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LASTNAME, "TESTLNAME"));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.FIRSTNAME, "TESTFNAME"));    
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LOGONNAME, user));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_FROM, 1412781268081L));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_TO, 16749397800000L));
		roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:com.sap.caf.eu.gp.roles.superuser");
		roleList.add("ROLE.PCD_ROLE_PERSISTENCE.dmXk1qFK4czAIHJ5W+sstDBRzBs=");
		groupList.add("GRUP.SUPER_GROUPS_DATASOURCE.EVERYONE");
		groupList.add("GRUP.PRIVATE_DATASOURCE.un:Administrators");
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_ROLES, roleList));
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_GROUPS, groupList));	
		Uid _uid = ICFTestHelper.getInstance().getFacade().create(ObjectClass.ACCOUNT, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),_uid.getUidValue());
	}
	
	@Test(expected=ConnectorException.class)
	public void test0_createUserWithInvalidPassword() {
		String user1 ="testuser1";
		//get a free available user
		_utils.handleExistingUser(user1); 

		Set<Attribute> attrSet = new HashSet<Attribute>();
		attrSet.add(SAPUMETestUtils.newAttr(Name.NAME, user1));
		attrSet.add(SAPUMETestUtils.newAttr(
				OperationalAttributes.PASSWORD_NAME, new GuardedString(
						"Password12%%%%%%!!!@".toCharArray())));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LASTNAME, "TESTLNAME"));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.FIRSTNAME, "TESTFNAME"));    
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LOGONNAME, user1));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_FROM, 1412781268081L));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_TO, 16749397800000L));
		try {
			ICFTestHelper.getInstance().getFacade()
					.create(ObjectClass.ACCOUNT, attrSet, null);
		} catch (ConnectorException e) {
			throw ConnectorException.wrap(e);
		}
	}

	@Test(expected=ConnectorException.class)
	public void test0_createUserWithInvalidChildData() {
		String user1 ="testuser1";
		final List<String> groupList = new ArrayList<String>();
		final List<String> roleList = new ArrayList<String>();
		//get a free available user
		_utils.handleExistingUser(user1); 

		Set<Attribute> attrSet = new HashSet<Attribute>();
		attrSet.add(SAPUMETestUtils.newAttr(Name.NAME, user1));
		attrSet.add(SAPUMETestUtils.newAttr(
				OperationalAttributes.PASSWORD_NAME, new GuardedString(
						"Welcome123".toCharArray())));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LASTNAME, "TESTLNAME"));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.FIRSTNAME, "TESTFNAME"));    
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.LOGONNAME, user1));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_FROM, 1412781268081L));
		attrSet.add(SAPUMETestUtils.newAttr(
				SAPUMEConstants.VALID_TO, 16749397800000L));
		roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:AEADMIN_INVALID");
		roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:AEApprover");
		groupList.add("GRUP.PRIVATE_DATASOURCE.un:Group1_INVALID");
		groupList.add("GRUP.PRIVATE_DATASOURCE.un:Group2");
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_ROLES, roleList));
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_GROUPS, groupList));	
		try {
			ICFTestHelper.getInstance().getFacade().create(ObjectClass.ACCOUNT, attrSet, null);
		} catch (RuntimeException e) {
			throw ConnectorException.wrap(e);
		}
	}

	@Test
	public void test1_addSingleRole() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();
		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> roleList = new ArrayList<String>();		
		
		roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:com.sap.caf.eu.gp.roles.superuser");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_ROLES, roleList));
		ICFTestHelper.getInstance().getFacade().addAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}

	@Test
	public void test2_addMultipleRoles() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();

		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> roleList = new ArrayList<String>();
		
		
		roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:com.sap.caf.eu.gp.roles.superuser");
		roleList.add("ROLE.PCD_ROLE_PERSISTENCE.dmXk1qFK4czAIHJ5W+sstDBRzBs=");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_ROLES, roleList));
		ICFTestHelper.getInstance().getFacade().addAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}
	
	@Test
	public void test3_addSingleGroup() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();
		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> groupList = new ArrayList<String>();
		
		groupList.add("GRUP.SUPER_GROUPS_DATASOURCE.EVERYONE");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_GROUPS, groupList));	
		ICFTestHelper.getInstance().getFacade().addAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}

	@Test
	public void test4_addMultipleGroups() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();
		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> groupList = new ArrayList<String>();
		
		groupList.add("GRUP.SUPER_GROUPS_DATASOURCE.EVERYONE");
		groupList.add("GRUP.PRIVATE_DATASOURCE.un:Administrators");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_GROUPS, groupList));	
		ICFTestHelper.getInstance().getFacade().addAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}
	
	@Test
	public void test5_removeSingleRole() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();
		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> roleList = new ArrayList<String>();		
		
		roleList.add("ROLE.PCD_ROLE_PERSISTENCE.Qgdg8VS4qfpTbJcmeugBvd1DjY0=");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_ROLES, roleList));
		ICFTestHelper.getInstance().getFacade().removeAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}

	@Test
	public void test6_removeMultipleRoles() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();
		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> roleList = new ArrayList<String>();
		
		roleList.add("ROLE.UME_ROLE_PERSISTENCE.un:com.sap.caf.eu.gp.roles.superuser");
		roleList.add("ROLE.PCD_ROLE_PERSISTENCE.Qgdg8VS4qfpTbJcmeugBvd1DjY0=");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_ROLES, roleList));
		ICFTestHelper.getInstance().getFacade().removeAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}
	
	@Test
	public void test7_removeSingleGroup() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();
		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> groupList = new ArrayList<String>();
		
		groupList.add("GRUP.SUPER_GROUPS_DATASOURCE.EVERYONE");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_GROUPS, groupList));	
		ICFTestHelper.getInstance().getFacade().removeAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}

	@Test
	public void test8_removeMultipleGroups() {
		Uid uid = _utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid();
		Set<Attribute> attrSet = new HashSet<Attribute>();		
		final List<String> groupList = new ArrayList<String>();
		
		groupList.add("GRUP.SUPER_GROUPS_DATASOURCE.EVERYONE");
		groupList.add("GRUP.PRIVATE_DATASOURCE.un:Administrators");
		
		attrSet.add(AttributeBuilder.build(SAPUMEConstants.ASSIGNED_GROUPS, groupList));	
		ICFTestHelper.getInstance().getFacade().removeAttributeValues(ObjectClass.ACCOUNT, uid, attrSet, null);
		Assert.assertEquals(_utils.searchByValue(ObjectClass.ACCOUNT,"__NAME__", user).getUid().getUidValue(),uid.getUidValue());
	}
}
