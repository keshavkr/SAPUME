import org.identityconnectors.contract.exceptions.ObjectNotFoundException
import org.identityconnectors.contract.data.groovy.Lazy
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.EmbeddedObject;

connector.umeUserId="sapuser"
connector.umeUrl="http://172.26.144.52:50000/spml/spmlservice"
connector.umePassword=new GuardedString("Oracle123".toCharArray())
connector.dummyPassword=new GuardedString("DPassword12".toCharArray())
connector.changePwdFlag=false
connector.pwdHandlingSupport=true
connector.logSPMLRequest=true
connector.enableDate="9999-12-31"
connector.groupDatasource=(String[])["PRIVATE_DATASOURCE","R3_ROLE_DS"]
connector.roleDatasource=(String[])["UME_ROLE_PERSISTENCE","PCD_ROLE_PERSISTENCE"]
connector.logonNameInitialSubstring="abcdefghijklmnopqrstuvwxyz1234567890"

// TODO fill in the following test configurations

// Connector WRONG configuration for ValidateApiOpTests
testsuite.Validate.invalidConfig = [
  [ umeUrl : "" ]//,
//  [ login : "" ],
//  [ password : "" ]
  ]

// Connector WRONG configuration for TestApiOpTests
testsuite.Test.invalidConfig = [
  [ umePassword: "NonExistingPassword_foo_bar_boo" ]
]
testsuite.Search.disable.caseinsensitive = true
testsuite {
    bundleJar = System.getProperty("bundleJar")
    bundleName = System.getProperty("bundleName")
    bundleVersion=System.getProperty("bundleVersion")
    connectorName="org.identityconnectors.sapume.SAPUMEConnector"

Schema {
        oclasses = ['__ACCOUNT__','Role','Group']
        attributes {
            __ACCOUNT__.oclasses = [
                '__NAME__','__ENABLE__','__CURRENT_ATTRIBUTES__','__PASSWORD__','logonname','isserviceuser',
			'firstname','lastname','salutation','title','jobtitle','mobile','displayname',
			'description','oldpassword','email','fax','locale','timezone','validfrom',
			'validto','certificate','lastmodifydate','islocked','ispassworddisabled','telephone',
			'department','id','securitypolicy','datasource','assignedroles','allassignedroles',
			'assignedgroups','allassignedgroups','company','streetaddress','city','zip','pobox',
			'country','state','orgunit','accessibilitylevel','passwordchangerequired'
            ] // __ACCOUNT__.oclasses
	    Role.oclasses = [
		'lastmodifydate','displayname','datasource','member','description','__NAME__','id','uniquename'
            ] // Role.oclasses 
	    Group.oclasses = [
               'lastmodifydate','assignedroles','allassignedroles','displayname','datasource','member','description',
		'__NAME__','id','distinguishedname','uniquename'
            ] // Group.oclasses

        } // attributes

        attrTemplate = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]// attrTemplate

	readOnlyAttrTemplate = [
            type: String.class,
            readable: true,
            createable: false,
            updateable: false,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]// attrTemplate

        attrTemplateLong = [
            type: long.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]// attrTemplateLong
       
        operations = [
          //  UpdateAttributeValuesOp: ['__ACCOUNT__','Role','Group'],  
            SearchApiOp: ['__ACCOUNT__','Role','Group'],
            ScriptOnConnectorApiOp: [],
            ValidateApiOp: [],
            AuthenticationApiOp: [],
            GetApiOp: ['__ACCOUNT__','Role','Group'],
            SchemaApiOp: ['__ACCOUNT__'],
            TestApiOp: ['__ACCOUNT__','Role','Group'],
          //  ScriptOnResourceApiOp: ['__ACCOUNT__'],
            CreateApiOp: ['__ACCOUNT__'],
            DeleteApiOp: ['__ACCOUNT__'],
	     UpdateApiOp: ['__ACCOUNT__'],
	     //SyncApiOp: ['__ACCOUNT__']
        //    ResolveUsernameApiOp: ['__ACCOUNT__','Role','Group']	
        ]//operations
    } // Schema

    Schema."__NAME__".attribute.__ACCOUNT__.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ]// __NAME__
		

    Schema."__CURRENT_ATTRIBUTES__".attribute.__ACCOUNT__.oclasses = [
	     type: EmbeddedObject.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true           
        ]// __CURRENT_ATTRIBUTES__

    Schema."__ENABLE__".attribute.__ACCOUNT__.oclasses = [
            type: boolean.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ]// __ENABLE__

    Schema."__PASSWORD__".attribute.__ACCOUNT__.oclasses = [
            type: GuardedString.class,
            readable: false,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: false
        ]// __PASSWORD__

   Schema."oldpassword".attribute.__ACCOUNT__.oclasses = [
            type: GuardedString.class,
            readable: false,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: false
        ]// __PASSWORD__

    Schema.firstname.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.isserviceuser.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.title.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.jobtitle.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.salutation.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.mobile.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.displayname.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.description.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.email.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.fax.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.locale.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.timezone.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.lastmodifydate.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.telephone.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.department.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.id.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.securitypolicy.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.datasource.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.readOnlyAttrTemplate")
    Schema.company.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.streetaddress.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.company.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.city.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.zip.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.pobox.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.country.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.state.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.orgunit.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.certificate.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.accessibilitylevel.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.passwordchangerequired.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.allassignedroles.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.readOnlyAttrTemplate")
    Schema.allassignedgroups.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.readOnlyAttrTemplate")
    Schema.validfrom.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.validto.attribute.__ACCOUNT__.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.logonname.attribute.__ACCOUNT__.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: false,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ] // logonname

    Schema.lastname.attribute.__ACCOUNT__.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ] // lastname
    Schema.islocked.attribute.__ACCOUNT__.oclasses = [
            type: boolean.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ] // islocked
    Schema.ispassworddisabled.attribute.__ACCOUNT__.oclasses = [
            type: boolean.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: false,
            returnedByDefault: true
        ] // ispassworddisabled
    Schema.assignedroles.attribute.__ACCOUNT__.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: true,
            returnedByDefault: true
        ] // assignedroles
    Schema.assignedgroups.attribute.__ACCOUNT__.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: true,
            returnedByDefault: true
        ] // assignedgroups

    Schema.lastmodifydate.attribute.Group.oclasses = Lazy.get("testsuite.Schema.attrTemplate")    
    Schema.displayname.attribute.Group.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.datasource.attribute.Group.oclasses = Lazy.get("testsuite.Schema.readOnlyAttrTemplate")
    Schema.member.attribute.Group.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.description.attribute.Group.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.id.attribute.Group.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.distinguishedname.attribute.Group.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.allassignedroles.attribute.Group.oclasses = Lazy.get("testsuite.Schema.readOnlyAttrTemplate")
   
    Schema.uniquename.attribute.Group.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ] // uniquename

    Schema."__NAME__".attribute.Group.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ]// __NAME__

    Schema.assignedroles.attribute.Group.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: false,
            multiValue: true,
            returnedByDefault: true
        ] // assignedroles

    Schema.lastmodifydate.attribute.Role.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.displayname.attribute.Role.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.datasource.attribute.Role.oclasses = Lazy.get("testsuite.Schema.readOnlyAttrTemplate")
    Schema.member.attribute.Role.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.description.attribute.Role.oclasses = Lazy.get("testsuite.Schema.attrTemplate")
    Schema.id.attribute.Role.oclasses = Lazy.get("testsuite.Schema.attrTemplate")

    Schema.uniquename.attribute.Role.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ] // uniquename

    Schema."__NAME__".attribute.Role.oclasses = [
            type: String.class,
            readable: true,
            createable: true,
            updateable: true,
            required: true,
            multiValue: false,
            returnedByDefault: true
        ]// __NAME__

}

__ACCOUNT__.__ENABLE__=true
__ACCOUNT__.modified.__ENABLE__=true

strictCheck=true
username = "testJunit" + Lazy.random("User")
__ACCOUNT__."__NAME__"= username

__ACCOUNT__."logonname"=username

i1.Search.__ACCOUNT__."__NAME__"="000132251680"
i1.Search.__ACCOUNT__."logonname"="000132251680"

i2.Search.__ACCOUNT__."__NAME__"="000132251681"
i2.Search.__ACCOUNT__."logonname"="000132251681"

i3.Search.__ACCOUNT__."__NAME__"="000132251682"
i3.Search.__ACCOUNT__."logonname"="000132251682"

i4.Search.__ACCOUNT__."__NAME__"="000132251683"
i4.Search.__ACCOUNT__."logonname"="000132251683"

i5.Search.__ACCOUNT__."__NAME__"="000132251684"
i5.Search.__ACCOUNT__."logonname"="000132251684"

i6.Search.__ACCOUNT__."__NAME__"="000132251685"
i6.Search.__ACCOUNT__."logonname"="000132251685"

i7.Search.__ACCOUNT__."__NAME__"="000132251686"
i7.Search.__ACCOUNT__."logonname"="000132251686"

i8.Search.__ACCOUNT__."__NAME__"="000132251687"
i8.Search.__ACCOUNT__."logonname"="000132251687"

i9.Search.__ACCOUNT__."__NAME__"="000132251688"
i9.Search.__ACCOUNT__."logonname"="000132251688"

i10.Search.__ACCOUNT__."__NAME__"="000132251689"
i10.Search.__ACCOUNT__."logonname"="000132251689"




__ACCOUNT__."firstname"="SURESH"
__ACCOUNT__."lastname"="KADIYALA"
__ACCOUNT__."password"= new GuardedString("Mphasis12".toCharArray())
__ACCOUNT__."__PASSWORD__"=new GuardedString("Mphasis12".toCharArray())
// Role."uniquename"="JK1NOV20"
__ACCOUNT__."salutation"="Mr"
__ACCOUNT__."title"="Mr"
__ACCOUNT__.jobtitle="ML"
__ACCOUNT__.mobile="9886614388"
__ACCOUNT__.displayname="JK1DEC15"
__ACCOUNT__.description="Contrzact Test"
__ACCOUNT__.oldpassword=new ObjectNotFoundException()
__ACCOUNT__.email="suresh.kadiyala@mphasis.com"
__ACCOUNT__.fax="1234567890"
__ACCOUNT__.locale="en"
__ACCOUNT__."timezone"="IST"
__ACCOUNT__."validfrom"=new ObjectNotFoundException()
__ACCOUNT__."validto"=new ObjectNotFoundException()
__ACCOUNT__."certificate"=new ObjectNotFoundException()
__ACCOUNT__."lastmodifydate"=new ObjectNotFoundException()
__ACCOUNT__."islocked"="false"
__ACCOUNT__."ispassworddisabled"="false"
__ACCOUNT__."telephone"="9886614388"
__ACCOUNT__."department"="apps"
__ACCOUNT__."id"=new ObjectNotFoundException()
__ACCOUNT__."securitypolicy"="default"
//__ACCOUNT__."datasource"=
__ACCOUNT__."assignedroles"="ROLE.UME_ROLE_PERSISTENCE.un:CAFAdmin"
//__ACCOUNT__."allassignedroles"=
__ACCOUNT__."assignedgroups"="GRUP.PRIVATE_DATASOURCE.un:ADSCallers"
__ACCOUNT__."allassignedgroups"=
__ACCOUNT__."company"="Mphasis"
__ACCOUNT__."streetaddress"="GTP"
__ACCOUNT__."city"="Bangalore"
__ACCOUNT__."zip"="560079"
__ACCOUNT__."pobox"="12345678"
__ACCOUNT__."country"="India"
__ACCOUNT__."state"="Karnataka"
__ACCOUNT__."orgunit"=new ObjectNotFoundException()
__ACCOUNT__."accessibilitylevel"="1"
__ACCOUNT__."passwordchangerequired"="true"
//__ACCOUNT__."__NAME__"="JK1DEC15"
__ACCOUNT__."isserviceuser"=new ObjectNotFoundException()





 testsuite.Update.updateToNullValue.skippedAttributes = [
                '__NAME__','__ENABLE__','__CURRENT_ATTRIBUTES__','__PASSWORD__','logonname','isserviceuser',
			'firstname','lastname','salutation','title','jobtitle','mobile','displayname',
			'description','oldpassword','email','fax','locale','timezone','validfrom',
			'validto','certificate','lastmodifydate','islocked','ispassworddisabled','telephone',
			'department','id','securitypolicy','datasource','assignedroles','allassignedroles',
			'assignedgroups','allassignedgroups','company','streetaddress','city','zip','pobox',
			'country','state','orgunit','accessibilitylevel','passwordchangerequired'
           ]
		   

__ACCOUNT__.modified."validfrom"=new ObjectNotFoundException()
__ACCOUNT__.modified."validto"=new ObjectNotFoundException()
__ACCOUNT__.modified."assignedroles"="ROLE.UME_ROLE_PERSISTENCE.un:CAFAdmin"
__ACCOUNT__.modified."assignedgroups"="GRUP.PRIVATE_DATASOURCE.un:ADSCallers"
__ACCOUNT__.modified."securitypolicy"="default"
__ACCOUNT__.modified."id"=new ObjectNotFoundException()
__ACCOUNT__.modified."password"=new ObjectNotFoundException()
__ACCOUNT__.modified."oldpassword"=new ObjectNotFoundException()
__ACCOUNT__.modified."certificate"=new ObjectNotFoundException()
__ACCOUNT__.modified."passwordchangerequired"="true"
__ACCOUNT__.modified."timezone"="IST"
__ACCOUNT__.modified."salutation"="Mr"
__ACCOUNT__.modified."zip"="560079"
__ACCOUNT__.modified."email"="suresh.kadiyala@mphasis.com"
__ACCOUNT__.modified."department"="apps"
__ACCOUNT__.modified."fax"="1234567890"
__ACCOUNT__.modified."accessibilitylevel"="1"
__ACCOUNT__.modified."isserviceuser"=new ObjectNotFoundException()
__ACCOUNT__.modified."company"="Mphasis"
__ACCOUNT__.modified."lastmodifydate"=new ObjectNotFoundException()
__ACCOUNT__.modified."description"="updated"
__ACCOUNT__.modified."country"="India"

__ACCOUNT__.modified."pobox"="12345678"
__ACCOUNT__.modified."orgunit"=new ObjectNotFoundException()
__ACCOUNT__.modified."telephone"="9986659812"
__ACCOUNT__.modified."islocked"="false"
__ACCOUNT__.modified."streetaddress"="GTP"
__ACCOUNT__.modified."displayname"="JK1DEC15"

__ACCOUNT__.modified."locale"="en"
__ACCOUNT__.modified."mobile"="9986659812"
__ACCOUNT__.modified."jobtitle"="SSE"
__ACCOUNT__.modified."ispassworddisabled"="false"
__ACCOUNT__.modified."city"="BangaloreUpdate"
__ACCOUNT__.modified."lastname"="Kadi"
__ACCOUNT__.modified."title"="MR"
__ACCOUNT__.modified."__NAME__"=new ObjectNotFoundException()
//__ACCOUNT__.modified."logonname"=new ObjectNotFoundException()

i1.modified.__ACCOUNT__."__NAME__"="000132251780"
i1.modified.__ACCOUNT__."logonname"="000132251780"

i2.Update.__ACCOUNT__."__NAME__"="000132251781"
i2.Update.__ACCOUNT__."logonname"="000132251781"

__ACCOUNT__.added."assignedroles"=new ObjectNotFoundException()
__ACCOUNT__.added."assignedgroups"=new ObjectNotFoundException()
Search.compareExistingObjectsByUidOnly = true



i1.Multi.__ACCOUNT__."__NAME__"="000132251780"
i1.Multi.__ACCOUNT__."logonname"="000132251780"

i2.Multi.__ACCOUNT__."__NAME__"="000132251781"
i2.Multi.__ACCOUNT__."logonname"="000132251781"

i3.Multi.__ACCOUNT__."__NAME__"="000132251782"
i3.Multi.__ACCOUNT__."logonname"="000132251782"

i4.Multi.__ACCOUNT__."__NAME__"="000132251783"
i4.Multi.__ACCOUNT__."logonname"="000132251783"

i5.Multi.__ACCOUNT__."__NAME__"="000132251784"
i5.Multi.__ACCOUNT__."logonname"="000132251784"

i6.Multi.__ACCOUNT__."__NAME__"="000132251785"
i6.Multi.__ACCOUNT__."logonname"="000132251785"

i7.Multi.__ACCOUNT__."__NAME__"="000132251786"
i7.Multi.__ACCOUNT__."logonname"="000132251786"

i8.Multi.__ACCOUNT__."__NAME__"="000132251787"
i8.Multi.__ACCOUNT__."logonname"="000132251787"

i9.Multi.__ACCOUNT__."__NAME__"="000132251788"
i9.Multi.__ACCOUNT__."logonname"="000132251788"

i10.Multi.__ACCOUNT__."__NAME__"="000132251789"
i10.Multi.__ACCOUNT__."logonname"="000132251789"

//i0.Multi.__ACCOUNT__."validto"="1451458003"


//i0.Multi.__ACCOUNT__.validto












