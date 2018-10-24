package org.identityconnectors.sapume.test;

import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.sapume.SAPUMEConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;




public class SAPUMEConnectionTests  {
	
	private static SAPUMEConfiguration _config;
	
	@Before
	public void setup()
	{
		_config = SAPUMETestUtils.newConfiguration();
	}
	
	
	@Test
	public void test0_HealthyConnection() {
		Assert.assertNotNull(_config);		
		try {
			ICFTestHelper.getInstance().newFacade(_config).test();
		} catch (Exception e) {
			throw new NullPointerException();
		} 
	}

	//ConnectorException- Testing for Invalid/wrong Connection Parameter
	
	@Test(expected=ConnectorException.class)
	public void test1_NullPassword(){		
		_config.setUmePassword(null);
		try {
			ICFTestHelper.getInstance().newFacade(_config).test();
		} catch (ConnectorException e) {
			Assert.assertTrue(e.getMessage().contains("SAPUME_ERR_TEST_CONNECTION"));
			throw new ConnectorException(e.getMessage());
		}
	}
	
	@Test(expected=ConnectorException.class)
	public void test3_InvalidUmeUserId(){		
		_config.setUmeUserId("");
		try {
			ICFTestHelper.getInstance().newFacade(_config).test();
		} catch (ConnectorException e) {
			Assert.assertTrue(e.getMessage().contains("SAPUME_ERR_TEST_CONNECTION"));
			throw new ConnectorException(e.getMessage());
		}
	}
	
	@Test(expected=ConnectorException.class)
	public void test2_InvalidHost(){		
		_config.setUmeUrl("http://172.26.144.67:50000/spml/spmlservice");
		try {
			ICFTestHelper.getInstance().newFacade(_config).test();
		} catch (ConnectorException e) {
			Assert.assertTrue(e.getMessage().contains("SAPUME_ERR_TEST_CONNECTION"));
			throw new ConnectorException(e.getMessage());
		}
	}
}
