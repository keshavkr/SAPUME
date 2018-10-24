package org.identityconnectors.sapume.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.sapume.SAPUMEConfiguration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link org.identityconnectors.sapume.SAPUMEConfiguration} class
 *
 * @author jagadeeshkumar.r
 */

public class SAPUMEConfigurationTests {
	
	@Test
	public void defaults() {
		
		SAPUMEConfiguration config = new SAPUMEConfiguration();
		Assert.assertNull(config.getUmeUserId());
		Assert.assertNull(config.getUmeUrl());
		Assert.assertNull(config.getUmePassword());
		Assert.assertNull(config.getDummyPassword());
		Assert.assertNull(config.getChangePwdFlag());
		Assert.assertNull(config.getPwdHandlingSupport());
		Assert.assertNull(config.getLogSPMLRequest());
		Assert.assertNull(config.getEnableDate());
		Assert.assertNull(config.getGroupDatasource());
		Assert.assertNull(config.getRoleDatasource());
		Assert.assertNull(config.getLogonNameInitialSubstring());
		
	}

	@Test
	public void setters() {

		// Init config parameters
		SAPUMEConfiguration config = SAPUMETestUtils.newConfiguration();

		// Assert if the config parameters are set
		Assert.assertNotNull(config.getUmeUserId());
		Assert.assertNotNull(config.getUmeUrl());
		Assert.assertNotNull(config.getUmePassword());
		Assert.assertNotNull(config.getDummyPassword());
		Assert.assertNotNull(config.getChangePwdFlag());
		Assert.assertNotNull(config.getPwdHandlingSupport());
		Assert.assertNotNull(config.getLogSPMLRequest());
		Assert.assertNotNull(config.getEnableDate());
		Assert.assertNotNull(config.getGroupDatasource());
		Assert.assertNotNull(config.getRoleDatasource());
		Assert.assertNotNull(config.getLogonNameInitialSubstring());
	}

	@Test
	public void testConfigurationalPropsAnnotations() {	
	
		SAPUMEConfiguration cfg = new SAPUMEConfiguration();	
		Method[] md = cfg.getClass().getMethods();
		int gettersCounter = 0;
		Collection<String> confidentialmethodSet = Arrays.asList("getUmePassword","getDummyPassword");
		Collection<String> requiredMethodSet =  Arrays.asList("getUmeUserId","getUmeUrl","getUmePassword",
				"getDummyPassword","getChangePwdFlag","getPwdHandlingSupport","getEnableDate","getLogonNameInitialSubstring");
		Collection<String> objclassesmethodSet = Arrays.asList("getGroupDatasource","getRoleDatasource");
		
		for (Method method : md) {
			try {
				Annotation[] annotations = method.getDeclaredAnnotations();
				for (Annotation annotation : annotations) {
					if (annotation instanceof ConfigurationProperty) {
						ConfigurationProperty myAnnotation = (ConfigurationProperty) annotation;
						gettersCounter++;
						// check for Object Classes
						if (objclassesmethodSet.contains(method.getName()))
							Assert.assertEquals(myAnnotation.objectClasses().length, 1);
						else
							Assert.assertEquals(myAnnotation.objectClasses().length, 0);
						// check for Confidential Tags
						if (confidentialmethodSet.contains(method.getName()))
							Assert.assertTrue(myAnnotation.confidential());
						else
							Assert.assertFalse(myAnnotation.confidential());

						// Check for Required Tags
						if (requiredMethodSet.contains(method.getName()))
							Assert.assertTrue(myAnnotation.required());
						else
							Assert.assertFalse(myAnnotation.required());
					}
				}
				//check for getter methods counts
				Assert.assertTrue(gettersCounter < 12);
			} catch (AssertionError ae) {
				throw new AssertionError(ae + " : Error while asserting  "+ method.getName());
			}
		}
	}	
}
