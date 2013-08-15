package net.canadensys.dataportal.occurrence;

import org.openqa.selenium.WebDriver;

/**
 * Abstract integration testing class.
 * 
 * @author canadensys
 *
 */
public abstract class AbstractIntegrationTest {
	
	//TODO : read from a config file or set with DI
	protected static final String TESTING_SERVER_URL = "http://localhost:9966/explorer/";
	protected WebDriver browser;

}
