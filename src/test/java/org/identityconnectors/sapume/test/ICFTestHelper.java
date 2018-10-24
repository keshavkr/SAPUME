/* $Header: idc/bundles/java/sapume/src/test/java/org/identityconnectors/sapume/test/ICFTestHelper.java /main/2 2015/07/15 02:57:02 smelgiri Exp $ */

/* Copyright (c) 2013, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
   jagadeeshkumar.r     10/15/2014 - Creation
 */

/**
 *  @author  jagadeeshkumar.r 
 */

package org.identityconnectors.sapume.test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.sapume.SAPUMEConfiguration;
import org.identityconnectors.sapume.SAPUMEConnector;
import org.identityconnectors.test.common.TestHelpers;

public final class ICFTestHelper {

	private final static String _bundleLoc ="D:\\PS3\\test\\org.identityconnectors.sapume-1.0.1.jar";
    private boolean connectionFailed = false;
    private ConnectorFacade _facade = null;
    private static SAPUMEConfiguration config=null;
    
    private Log _log = Log.getLog(getClass());
    
    public static ICFTestHelper getInstance() {
        return new ICFTestHelper();
    }

    private ICFTestHelper() {
    }
    
    static ConnectorFacade connectorFacadeBundle = null;
    public static  ConnectorFacade getConnectorFacadeHelperImp() {
    if( null != connectorFacadeBundle)
    return connectorFacadeBundle;
    SAPUMEConfiguration config = new SAPUMEConfiguration();
    setSapConfig(config, getConfig());
    ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
    APIConfiguration impl = TestHelpers.createTestConfiguration(SAPUMEConnector.class, config);
    connectorFacadeBundle =factory.newInstance(impl);
    return connectorFacadeBundle;
    }
    
    private static void setSapConfig(SAPUMEConfiguration acConfig,
            SAPUMEConfiguration groovyConfig) {

        //acConfig.setAliasUser(groovyConfig.getAliasUser());                  
        Object noparams[] = {};
        Method[] groovyConfigMethods = groovyConfig.getClass().getMethods();
        Method[] acConfigMethods = acConfig.getClass().getMethods();

        for (Method method: groovyConfigMethods) {
            if(method.getName().startsWith("get")) {                 
                String methodName=new String(method.getName().substring(3));
                for(Method acMethod: acConfigMethods){
                    if(acMethod.getName().startsWith("set")) {                 
                        String acMethodName=new String(acMethod.getName().substring(3));
                        if(methodName.equals(acMethodName)){
                            try {
                                System.out.println("Value from "+methodName+" getter will be passed to "+acMethodName+" setter");    
                                acMethod.invoke(acConfig, method.invoke(groovyConfig, noparams));
                            } catch (IllegalArgumentException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
      
     
    
    public ConnectorFacade getFacade() {

        if (connectionFailed)
            throw new IllegalStateException("Connection to target failed");

        if (_facade == null) {
            initializeFacade();
        }
        return _facade;
    }
    
    private void initializeFacade() {
        _log.info("Loading the bundle " + _bundleLoc);
        File bundleJarFile = new File(_bundleLoc);
        if (!bundleJarFile.exists()) {
            throw new IllegalArgumentException("Bundle jar doesn't exist at "
                    + _bundleLoc);
        }

        try {
            URL connectorBundle = bundleJarFile.toURI().toURL();
            _log.info("Bundle location: " + _bundleLoc);

            ConnectorInfoManagerFactory infoManagerFactory = ConnectorInfoManagerFactory
                    .getInstance();
            ConnectorInfoManager infoManager = infoManagerFactory
                    .getLocalManager(connectorBundle);

            List<ConnectorInfo> connectorInfos = infoManager
                    .getConnectorInfos();
            if (connectorInfos.size() != 1) {
                _log.error(
                        "Connector cannot be loaded. Only one bundle should be present."
                                + " {0} bundles found", connectorInfos.size());
                throw new IllegalStateException("Improper bundle found");
            }

            // Get configuration props
            APIConfiguration apiConfig = connectorInfos.get(0)
                    .createDefaultAPIConfiguration();
            
            // Set configuration props
            ConfigurationProperties configProps = apiConfig
                    .getConfigurationProperties();
            
            setsapumeConfigProps(configProps);
            
            _facade = ConnectorFacadeFactory.getInstance().newInstance(
                    apiConfig);
            _facade.schema();
            _log.info("Connector validated");
//            _facade.test();

        } catch (Exception e) {
            _log.error("Unable to initialize the bundle at {0}", _bundleLoc);
            connectionFailed = true;
           // throw new IllegalStateException(e);
            throw new ConnectorException(e);
        }
    }
    
	public ConnectorFacade newFacade() {
		SAPUMEConfiguration cfg = SAPUMETestUtils.newConfiguration();
	return newFacade(cfg);	
	}

	public ConnectorFacade newFacade(SAPUMEConfiguration cfg) {
		ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
		APIConfiguration impl = TestHelpers.createTestConfiguration(SAPUMEConnector.class, cfg);
	return factory.newInstance(impl);
	}	
    
	private void setsapumeConfigProps(ConfigurationProperties sapumeConfigProps) {
		
		SAPUMEConfiguration config = SAPUMETestUtils.newConfiguration();
		sapumeConfigProps.setPropertyValue("umeUrl", config.getUmeUrl());
		sapumeConfigProps.setPropertyValue("umeUserId", config.getUmeUserId());
		sapumeConfigProps.setPropertyValue("umePassword", config.getUmePassword());
		sapumeConfigProps.setPropertyValue("dummyPassword", config.getDummyPassword());
		sapumeConfigProps.setPropertyValue("changePwdFlag", config.getChangePwdFlag());
		sapumeConfigProps.setPropertyValue("pwdHandlingSupport", config.getPwdHandlingSupport());
		sapumeConfigProps.setPropertyValue("logSPMLRequest", config.getLogSPMLRequest());
		sapumeConfigProps.setPropertyValue("enableDate", config.getEnableDate());
		sapumeConfigProps.setPropertyValue("logonNameInitialSubstring",config.getLogonNameInitialSubstring());
		sapumeConfigProps.setPropertyValue("roleDatasource", config.getRoleDatasource());
		sapumeConfigProps.setPropertyValue("groupDatasource", config.getGroupDatasource());
	}
	 
    private static SAPUMEConfiguration getConfig() {
    	
    	if(config==null){
    	config=new SAPUMETestUtils().newConfiguration();
    	}
		return config;
	}

	private void setConfig(SAPUMEConfiguration config) {
		ICFTestHelper.config = config;
	}
}
