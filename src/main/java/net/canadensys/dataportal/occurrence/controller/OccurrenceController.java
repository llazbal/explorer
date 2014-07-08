package net.canadensys.dataportal.occurrence.controller;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.canadensys.dataportal.occurrence.OccurrenceService;
import net.canadensys.dataportal.occurrence.config.OccurrencePortalConfig;
import net.canadensys.dataportal.occurrence.model.OccurrenceModel;
import net.canadensys.dataportal.occurrence.model.OccurrenceViewModel;
import net.canadensys.dataportal.occurrence.model.ResourceContactModel;
import net.canadensys.exception.web.ResourceNotFoundException;
import net.canadensys.web.i18n.I18nUrlBuilder;
import net.canadensys.web.i18n.annotation.I18nTranslation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller of all occurrence related features of the occurrence portal.
 * Occurrence related excludes the search functionality.
 * This controller must be stateless.
 * @author canadensys
 *
 */
@Controller
public class OccurrenceController {
	
	//get log4j handler
	private static final Logger LOGGER = Logger.getLogger(OccurrenceController.class);
	private static final ConfigurableMimeFileTypeMap MIME_TYPE_MAP = new ConfigurableMimeFileTypeMap();
	
	private static String VIEW_PARAM = "view";
	private static String DWC_VIEW_NAME = "dwc";
	
	@Autowired
	private OccurrenceService occurrenceService;
	
	@Autowired
	@Qualifier("occurrencePortalConfig")
	private OccurrencePortalConfig appConfig;
	
	@RequestMapping(value="/resources/{iptResource}/occurrences/{dwcaId:.+}", method=RequestMethod.GET)
	@I18nTranslation(resourceName="occurrence", translateFormat = "/resources/{}/occurrences/{}")
	public ModelAndView handleOccurrencePerResource(@PathVariable String iptResource,@PathVariable String dwcaId, HttpServletRequest request){
		OccurrenceModel occModel = occurrenceService.loadOccurrenceModel(iptResource,dwcaId,true);
		HashMap<String,Object> modelRoot = new HashMap<String,Object>();
		
		if(occModel != null){
			modelRoot.put("occModel", occModel);
			modelRoot.put("occRawModel",occModel.getRawModel());
			modelRoot.put("occViewModel", buildOccurrenceViewModel(occModel));
		}
		else{
			throw new ResourceNotFoundException();
		}
		//Set common stuff
		ControllerHelper.setPageHeaderVariables(request,"occurrence",new String[]{iptResource,dwcaId},appConfig, modelRoot);
		
		//handle view stuff
		String view = request.getParameter(VIEW_PARAM);
		if(DWC_VIEW_NAME.equalsIgnoreCase(view)){
			return new ModelAndView("occurrence-dwc",OccurrencePortalConfig.PAGE_ROOT_MODEL_KEY,modelRoot);
		}
		return new ModelAndView("occurrence",OccurrencePortalConfig.PAGE_ROOT_MODEL_KEY,modelRoot);
	}
	
	/**
	 * Resource contact page.
	 * @param ipt resource identifier (sourcefileid).
	 * @return
	 */
	@RequestMapping(value="/resources/{iptResource}/contact", method=RequestMethod.GET)
	@I18nTranslation(resourceName="contact", translateFormat = "/resources/{}/contact")
	public ModelAndView handleResourceContact(@PathVariable String iptResource, HttpServletRequest request){
		ResourceContactModel resourceContactModel = occurrenceService.loadResourceContactModel(iptResource);
		HashMap<String,Object> modelRoot = new HashMap<String,Object>();
		
		if(resourceContactModel != null){
			modelRoot.put("data", resourceContactModel);
		}
		else{
			throw new ResourceNotFoundException();
		}
		//Set common stuff
		ControllerHelper.setPageHeaderVariables(request,"contact",new String[]{iptResource},appConfig, modelRoot);

		return new ModelAndView("resource-contact",OccurrencePortalConfig.PAGE_ROOT_MODEL_KEY,modelRoot);
	}
	
	/**
	 * Redirect this URL to a search on an IPT resource.
	 * We support this to have a clean URL to an IPT resource.
	 * @param iptResource
	 * @return
	 */
	@RequestMapping(value="/resources/{iptResource}", method=RequestMethod.GET)
	@I18nTranslation(resourceName="resource", translateFormat = "/resources/{}")
	public ModelAndView handleIptResource(@PathVariable String iptResource, HttpServletRequest request){
		if(!occurrenceService.resourceExists(iptResource)){
			throw new ResourceNotFoundException();
		}
		Locale locale = RequestContextUtils.getLocale(request);
		String searchUrl = I18nUrlBuilder.generateI18nResourcePath(locale.getLanguage(), OccurrencePortalConfig.I18N_TRANSLATION_HANDLER.getTranslationFormat("search"), (String)null);
		RedirectView rv = new RedirectView(request.getContextPath()+searchUrl+"?iptresource="+iptResource);
		rv.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
		ModelAndView mv = new ModelAndView(rv);
		return mv;
	}
	
	/**
	 * Build and fill A OccurrenceViewModel based on a OccurrenceModel.
	 * @param occModel
	 * @return OccurrenceViewModel instance, never null
	 */
	public static OccurrenceViewModel buildOccurrenceViewModel(OccurrenceModel occModel){
		OccurrenceViewModel occViewModel = new OccurrenceViewModel();
		
		if(StringUtils.isNotEmpty(occModel.getAssociatedmedia())){
			//assumes that data are coming from harvester
			String[] media = occModel.getAssociatedmedia().split("; ");
			for(String currentMedia : media){
				if(MIME_TYPE_MAP.getContentType(currentMedia).startsWith("image")){
					occViewModel.addImage(currentMedia);
				}
				else{
					occViewModel.addOtherMedia(currentMedia);
				}
			}
		}
		return occViewModel;
	}
}
