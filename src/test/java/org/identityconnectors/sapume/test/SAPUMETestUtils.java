package org.identityconnectors.sapume.test;

import java.util.List;
import java.util.Properties;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.sapume.SAPUMEConfiguration;
import org.identityconnectors.sapume.SAPUMEConnector;
import org.identityconnectors.test.common.PropertyBag;
import org.identityconnectors.test.common.TestHelpers;

/**
 *  Utility methods for Unit tests
 *
 * @author jagadeeshkumar.r
 */
public class SAPUMETestUtils {

	private static final Log LOGGER = Log.getLog(SAPUMETestUtils.class);
	/**
	 * Set values for all the config parameters
	 * @return SAPUMEConfiguration
	 */

	public static SAPUMEConfiguration newConfiguration() {
		SAPUMEConfiguration config = new SAPUMEConfiguration();
		boolean changePwdFlag= false;
		boolean logSPMLRequest= false;
		boolean pwdHandlingSupport= false;
		try {
			PropertyBag properties = TestHelpers.getProperties(SAPUMEConnector.class);
			config.setUmeUserId(properties.getStringProperty("connector.umeUserId"));
			config.setUmeUrl(properties.getStringProperty("connector.umeUrl"));
			GuardedString umePassword = properties.getProperty("connector.umePassword",GuardedString.class);
			config.setUmePassword(umePassword);
			config.setDummyPassword(properties.getProperty("connector.dummyPassword",GuardedString.class));
			boolean changePaawordFlag = 	properties.getProperty("connector.changePwdFlag",Boolean.class);
			if (changePaawordFlag==true){
				changePwdFlag= true;
				config.setChangePwdFlag(changePwdFlag);
			}else if(changePaawordFlag==false){
				config.setChangePwdFlag(changePwdFlag);
			}
			boolean passwordhanSupport = 	properties.getProperty("connector.pwdHandlingSupport",Boolean.class);
			if (passwordhanSupport==true){
				pwdHandlingSupport= true;
				config.setPwdHandlingSupport(pwdHandlingSupport);
			}else if(passwordhanSupport==false){
				config.setPwdHandlingSupport(pwdHandlingSupport);
			}
			boolean lofSpmlRequest = 	properties.getProperty("connector.logSPMLRequest",Boolean.class);
			if (lofSpmlRequest==true){
				logSPMLRequest= true;
				config.setLogSPMLRequest(logSPMLRequest);
			}else if(lofSpmlRequest==false){
				config.setLogSPMLRequest(logSPMLRequest);
			}
			config.setEnableDate(properties
					.getStringProperty("connector.enableDate"));
			String groupDatasource[]=properties.getProperty("connector.groupDatasource",String[].class);
			config.setGroupDatasource(groupDatasource);
			String roleDatasource[]=properties.getProperty("connector.roleDatasource",String[].class);
			config.setRoleDatasource(roleDatasource);
			config.setLogonNameInitialSubstring(properties.getStringProperty("connector.logonNameInitialSubstring"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return config;
	}
	
	public static Attribute newAttr(String name, Object obj) {
		AttributeBuilder attrBldr = new AttributeBuilder();
		attrBldr.setName(name);
		attrBldr.addValue(obj);
		return attrBldr.build();
	}
	
	public void handleExistingUser(String user) {
		
		AttributeBuilder userattb = new AttributeBuilder();
		userattb.setName("__NAME__");
		userattb.addValue(user);
        Filter filter = FilterBuilder.equalTo(userattb.build());
        OperationOptionsBuilder builder = new OperationOptionsBuilder();
        List<ConnectorObject> objects = TestHelpers.searchToList(ICFTestHelper.getInstance().getFacade(), ObjectClass.ACCOUNT, filter , builder.build());
        if (!objects.isEmpty()) {
        	LOGGER.info("User already Exists {0}", user);
        	ICFTestHelper.getInstance().getFacade().delete(ObjectClass.ACCOUNT, objects.get(0).getUid() , null);
        }
}

	/*returns connector objects*/
	public ConnectorObject searchByValue(ObjectClass ObjectClass,String ObjectName,Object ObjectValue){
		
		AttributeBuilder guidattr = new AttributeBuilder();
		guidattr.setName(ObjectName);
		guidattr.addValue(ObjectValue);
		return searchByAttribute(ObjectClass , guidattr.build());       
	}
	
	public ConnectorObject searchByAttribute(ObjectClass oClass) {		
		return searchByAttribute(oClass, null , (OperationOptions) null);		
	}
	
	public ConnectorObject searchByAttribute(ObjectClass oClass, Attribute attr ) {		
		return searchByAttribute(oClass, FilterBuilder.equalTo(attr) , (OperationOptions) null);		
	}
	
	public ConnectorObject searchByAttribute(ObjectClass oClass, Filter filter , OperationOptions options) {		
		 List<ConnectorObject> objects = TestHelpers.searchToList(ICFTestHelper.getInstance().getFacade(), oClass, filter , options);
	        return objects.size() > 0 ? objects.get(0) : null;
	}
}
