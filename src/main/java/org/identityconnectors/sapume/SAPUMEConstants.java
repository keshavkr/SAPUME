package org.identityconnectors.sapume;

public interface SAPUMEConstants {
	public static final String VALID_TO = "validto";
	public static final String VALID_FROM = "validfrom";
	public static final String PASSWORD = "password";
	public static final String OLDPASSWORD = "oldpassword";
	public static final String YES = "yes";
	public static final String DATE_FORMAT = "yyyyMMddHHmmss'Z'";
	public static final String OW_DATE_FORMAT = "E MMM d HH:mm:ss Z yyyy";
	public static final String CONFIG_DATE_FORMAT = "yyyy-MM-dd";
	public static final String SAPUSER = "sapuser";
	public static final String SAPROLE = "saprole";
	public static final String SAPGROUP = "sapgroup";
	public static final String LOGONNAME = "logonname";
	public static final String DATASOURCE = "datasource";
	public static final String ASSIGNED_ROLES = "assignedroles";
	public static final String ASSIGNED_GROUPS = "assignedgroups";
	public static final String ALLASSIGNED_ROLES = "allassignedroles";
	public static final String ALLASSIGNED_GROUPS = "allassignedgroups";
	public static final String RECON_DATE_FORMAT = "yyyyMMddHHmmss'Z'";
	public static final String ID = "id";
	public static final String OBJCLSROLE = "Role";
	public static final String OBJCLSGROUP = "Group";
	public static final String OBJCLSGROUPNAME = "__Group__";
	public static final String OBJCLSUSERNAME = "__User__";
	public static final String PASSWORDNAME = "__PASSWORD__";
	public static final String ACCOUNTNAME = "__ACCOUNT__";
	public static final String ROLENAME = "__ROLE__";
	public static final String ENABLENAME = "__ENABLE__";
	public static final String NAME = "__NAME__";
	public static final String UID = "__UID__";
	public static final String ADD = "add";
	public static final String DELETE = "delete";
	public static final String UNIQUENAME = "uniquename";
	public static final String MEMBER = "member";
	public static final String SEARCHATTRTOGET = "searchAttrToGet";
	public static final String DSLIST = "DSLIST";	

	// OW Test connection
	public static final String USERNAME = "sapume random user";
	public static final String FIRSTNAME = "firstname";
	public static final String LASTNAME = "lastname";
	public static final String DISPLAYNAME = "displayname";

	// START :: Bug 18895142
	public static final String STATE = "state";
	public static final String SALUTATION = "salutation";
	public static final String PASSWORDCHANGEREQUIRED = "passwordchangerequired";
	public static final String TIMEZONE = "timezone";
	public static final String ZIP = "zip";
	public static final String EMAIL = "email";
	public static final String SECURITYPOLICY = "securitypolicy";
	public static final String DEPARTMENT = "department";
	public static final String FAX = "fax";
	public static final String ACCESSIBILITYLEVEL = "accessibilitylevel";
	public static final String ISSERVICEUSER = "isserviceuser";
	public static final String COMPANY = "company";
	public static final String LASTMODIFYDATE = "lastmodifydate";
	public static final String CERTIFICATE = "certificate";
	public static final String DESCRIPTION = "description";
	public static final String COUNTRY = "country";
	public static final String POBOX = "pobox";
	public static final String ORGUNIT = "orgunit";
	public static final String TELEPHONE = "telephone";
	public static final String ISLOCKED = "islocked";
	public static final String STREETADDRESS = "streetaddress";
	public static final String LOCALE = "locale";
	public static final String MOBILE = "mobile";
	public static final String ISPASSWORDDISABLED = "ispassworddisabled";
	public static final String JOBTITLE = "jobtitle";
	public static final String CITY = "city";
	public static final String TITLE = "title";
	// END :: Bug 18895142

	//Parsing exception values
	public static final String FAILURE = "failure";
	public static final String DOESNT_EXIST = "doesn't exist";
	public static final String ALREADY_EXIST = "already exists";
	public static final String SUCCESS = "success";
}
