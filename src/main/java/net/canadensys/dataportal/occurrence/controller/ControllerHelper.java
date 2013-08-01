package net.canadensys.dataportal.occurrence.controller;

import java.util.HashMap;
import java.util.Locale;

import net.canadensys.dataportal.occurrence.config.OccurrencePortalConfig;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for common Controller operations.
 * @author canadensys
 *
 */
public class ControllerHelper {
	
	/**
	 * Add common header variables to modelRoot.
	 * Including locale, rootURL, currentVersion and useMinified
	 * @param request
	 * @param locale
	 * @param appConfig
	 * @param modelRoot
	 */
	public static void setPageHeaderVariables(Locale locale, OccurrencePortalConfig appConfig, HashMap<String,Object> modelRoot){

		modelRoot.put("locale",appConfig.getResourceBundle(locale));
		
		//Are we using versioning?
		if(!StringUtils.isBlank(appConfig.getCurrentVersion())){
			modelRoot.put("currentVersion", appConfig.getCurrentVersion());
		}
		//Are we using minified files?
		modelRoot.put("useMinified", BooleanUtils.toBoolean(appConfig.getUseMinified()));
		
		//Root URL of the web page
		if(!StringUtils.isBlank(appConfig.getRootURL())){
			modelRoot.put("rootURL", appConfig.getRootURL());
		}
	}

}
