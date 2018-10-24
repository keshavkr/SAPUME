/**
* Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * Description :  Extends the {@link AbstractConfiguration} class to provide all the necessary
 * parameters to initialize the SAP UME Connector.
 * Source code : SAPUMEConfiguration.java
 * Author :jagadeeshkumar.r
 * @version : 
 * @created on : 
 * Modification History:
 * S.No. Date Bug fix no:
 */

package org.identityconnectors.sapume;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

/**
 * Extends the {@link AbstractConfiguration} class to provide all the necessary
 * parameters to initialize the SAP UME Connector.
 * 
 * @author jagadeeshkumar.r
 *
 */
public class SAPUMEConfiguration extends AbstractConfiguration {

	private String umeUserId;
	private String umeUrl;
	private GuardedString umePassword;
	private GuardedString dummyPassword;
	private Boolean changePwdFlag;
	private Boolean pwdHandlingSupport;
	private Boolean logSPMLRequest;
	private String enableDate;
	private String[] groupDatasource;
	private String[] roleDatasource;
	private String logonNameInitialSubstring;

	/**
	 * Constructor
	 */
	public SAPUMEConfiguration() {
	}

	/**
	 * Gets umeUrl to connect to SAPUME target
	 * 
	 * @return umeUrl as string
	 */
	@ConfigurationProperty(order = 1, displayMessageKey = "SAPUMEURL_DISPLAY", helpMessageKey = "SAPUMEURL_HELP", required = true)
	public String getUmeUrl() {
		return umeUrl;
	}

	/**
	 * Sets UmeURL
	 * 
	 * @param String
	 *            umeUrl
	 */
	public void setUmeUrl(String umeUrl) {
		this.umeUrl = umeUrl;
	}

	/**
	 * Gets UmeUserId to connect to SAPUME target
	 * 
	 * @return UmeUserId as string
	 */
	@ConfigurationProperty(order = 2, displayMessageKey = "SAPUMEUID_DISPLAY", helpMessageKey = "SAPUMEUID_HELP", required = true)
	public String getUmeUserId() {
		return umeUserId;
	}

	/**
	 * Sets UmeUserId
	 * 
	 * @param String
	 *            UmeUserId
	 */
	public void setUmeUserId(String umeUserId) {
		this.umeUserId = umeUserId;
	}

	/**
	 * Gets umePassword to connect to SAPUME target
	 * 
	 * @return umePassword as GuardedString
	 */
	@ConfigurationProperty(order = 3, displayMessageKey = "SAPUMEPWD_DISPLAY", helpMessageKey = "SAPUMEPWD_HELP", required = true, confidential= true)
	public GuardedString getUmePassword() {
		return umePassword;
	}

	/**
	 * Sets UmePassword
	 * 
	 * @param String
	 *            UmePassword
	 */
	public void setUmePassword(GuardedString umePassword) {
		this.umePassword = umePassword;
	}

	/**
	 * Gets dummyPassword
	 * 
	 * @return String dummyPassword
	 */
	@ConfigurationProperty(order = 4, displayMessageKey = "SAPUMEDUMMYPWD_DISPLAY", helpMessageKey = "SAPUMEDUMMYPWD_HELP", required = true, confidential=true)
	public GuardedString getDummyPassword() {
		return dummyPassword;
	}

	/**
	 * Sets DummyPassword
	 * 
	 * @param String
	 *            DummyPassword
	 */
	public void setDummyPassword(GuardedString dummyPassword) {
		this.dummyPassword = dummyPassword;
	}

	/**
	 * Gets ChangePwdFlag
	 * 
	 * @return String yes/no
	 */
	@ConfigurationProperty(order = 5, displayMessageKey = "SAPUMECHGPWD_DISPLAY", helpMessageKey = "SAPUMECHGPWD_HELP", required = true)
	public Boolean getChangePwdFlag() {
		return changePwdFlag;
	}

	/**
	 * Sets ChangePwdFlag
	 * 
	 * @param String
	 *            ChangePassword
	 */
	public void setChangePwdFlag(Boolean changePwdFlag) {
		this.changePwdFlag = changePwdFlag;
	}

	/**
	 * Gets Password Handling supported
	 * 
	 * @return String yes/no
	 */
	@ConfigurationProperty(order = 6, displayMessageKey = "SAPUMEPWDHANDLING_DISPLAY", helpMessageKey = "SAPUMEPWDHANDLING_HELP", required = true)
	public Boolean getPwdHandlingSupport() {
		return pwdHandlingSupport;
	}

	/**
	 * Sets PwdHandlingSupport
	 * 
	 * @param String
	 *            PwdHandlingSupport
	 */
	public void setPwdHandlingSupport(Boolean pwdHandlingSupport) {
		this.pwdHandlingSupport = pwdHandlingSupport;
	}

	/**
	 * Gets Log SPML Request value(Yes to print SPML request in console) 
	 * 
	 * @return String yes/no
	 */
	@ConfigurationProperty(order = 7, displayMessageKey = "SAPUMELOGSPML_DISPLAY", helpMessageKey = "SAPUMELOGSPML_HELP")
	public Boolean getLogSPMLRequest() {
		return logSPMLRequest;
	}

	public void setLogSPMLRequest(Boolean logSPMLRequest) {
		this.logSPMLRequest = logSPMLRequest;
	}

	/**
	 * Gets EnableDate
	 * 
	 * @return String EnableDate
	 */
	@ConfigurationProperty(order = 8, displayMessageKey = "SAPENABLEDATE_DISPLAY", helpMessageKey = "SAPENABLEDATE_HELP", required = true)
	public String getEnableDate() {
		return enableDate;
	}

	/**
	 * Sets User EnableDate
	 * 
	 * @param String
	 *            EnableDate
	 */
	public void setEnableDate(String enableDate) {
		this.enableDate = enableDate;
	}

	/**
	 * Gets list of group datasource
	 * 
	 * @return List of group datasource
	 */
	@ConfigurationProperty(order = 9, displayMessageKey = "SAPUMEGRPDS_DISPLAY", helpMessageKey = "SAPUMEGRPDS_HELP", objectClasses=SAPUMEConstants.OBJCLSGROUP)
	public String[] getGroupDatasource() {
		return groupDatasource;
	}

	public void setGroupDatasource(String[] groupDatasource) {
		this.groupDatasource = groupDatasource;
	}

	/**
	 * Gets list of role datasource
	 * 
	 * @return List of role datasource
	 */
	@ConfigurationProperty(order = 10, displayMessageKey = "SAPUMERLDS_DISPLAY", helpMessageKey = "SAPUMERLDS_HELP", objectClasses=SAPUMEConstants.OBJCLSROLE)
	public String[] getRoleDatasource() {
		return roleDatasource;
	}

	public void setRoleDatasource(String[] roleDatasource) {
		this.roleDatasource = roleDatasource;
	}
	
	/**
	 * Gets logonNameInitialSubstring (default value= abcdefghijklmnopqrstuvwxyz1234567890)
	 * @return string (to use for filter)		
	 */
	@ConfigurationProperty(order = 11, displayMessageKey = "SAPUMELGNNAMEINITIAL_DISPLAY", helpMessageKey = "SAPUMELGNNAMEINITIAL_HELP", required = true)
	public String getLogonNameInitialSubstring() {
		return logonNameInitialSubstring;
	}

	public void setLogonNameInitialSubstring(String logonNameInitialSubstring) {
		this.logonNameInitialSubstring = logonNameInitialSubstring;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {

	}
	
	/**
	 * Helper method for getting the localized messages
	 * @param key
	 *   Message key
	 * @return Localized message as String
	 */
    public String getMessage(String key) {
        return getConnectorMessages().format(key, key);
    }

    public String getMessage(String key, Object... objects) {
        return getConnectorMessages().format(key, key, objects);
    }
}

