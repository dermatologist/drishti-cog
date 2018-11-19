package com.canehealth.controller;

import com.canehealth.repository.DrishtiApplicationUserRepository;
import com.canehealth.repository.DrishtiShimmerDataRepository;
import com.canehealth.service.DrishtiResponseService;
import com.canehealth.service.DrishtiShimmerService;
import org.gtri.hdap.mdata.jpa.entity.ApplicationUser;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.gtri.hdap.mdata.service.ShimmerAuthenticationException;
import org.gtri.hdap.mdata.service.ShimmerResponse;
import org.gtri.hdap.mdata.util.ShimmerUtil;

import org.springframework.http.HttpStatus;

@RestController
@SessionAttributes("shimmerId")
public class ShimmerController {

    private final Logger logger = LoggerFactory.getLogger(ShimmerController.class);

    private String shimmerId = "";

    @Value("${app.drishti.omhcallbackuri}")
    private String omhCallbackUri;

    @Autowired
    private DrishtiResponseService drishtiResponseService;

    @Autowired
    private DrishtiShimmerService drishtiShimmerService;

    @Autowired
    private DrishtiApplicationUserRepository applicationUserRepository;

    @Autowired
    private DrishtiShimmerDataRepository shimmerDataRepository;


    @ModelAttribute("shimmerId")
    public String shimmerId(){
        return shimmerId;
    }

    /**
     * Make a request to http://<shimmer-host>:8083/authorize/{shimKey}?username={userId}
     * @param model
     * @param ehrId the ID of the patient in the EHR
     * @param shimkey the ID for the patient in Shimmer
     * @return
     */
    @GetMapping("/shimmerAuthentication")
    public ModelAndView authenticateWithShimmer(ModelMap model,
                                                @ModelAttribute("shimmerId") String shimmerId,
                                                RedirectAttributes attributes,
                                                @RequestParam(name="ehrId", required=true) String ehrId,
                                                @RequestParam(name="shimkey", required=true) String shimkey,
                                                BindingResult bindingResult){
        logger.debug("Trying to connect to " + shimkey + " API");
        // Make a request to http://<shimmer-host>:8083/authorize/{shimKey}?username={userId}
        // The shimKey path parameter should be one of the keys listed below, e.g. fitbit
        // for example https://<shimmer-host>/authorize/fitbit?username={userId}
        // The username query parameter can be set to any unique identifier you'd like to use to identify the user.

        String userShimmerId = drishtiResponseService.getShimmerId(ehrId, shimkey);
        //add the shimmer id to the model
        model.addAttribute("shimmerId", userShimmerId);

        ShimmerResponse shimmerResponse = drishtiShimmerService.requestShimmerAuthUrl(userShimmerId, shimkey);
        String oauthAuthUrl = null;
        if( shimmerResponse.getResponseCode() == HttpStatus.OK.value()){
            oauthAuthUrl = shimmerResponse.getResponseData();
        }
        else{
            //did not get a response URL
            throw new ShimmerAuthenticationException("Could not authorize shimmer user " + shimmerResponse.getResponseData());
        }

        logger.debug("Finished connection to " + shimkey + " API");

        //tell spring we want the attribute to survive the redirect
        attributes.addFlashAttribute("shimmerId", userShimmerId);

        //If the returned oauthAuthUrl equals the final callback URL for UI then the user has already
        //linked the EHR user to their device account via shimmer. Update the model to contain
        //loginSuccess true. The shimmerID for the model was set above so no need to set it again.
        if( oauthAuthUrl.equals(omhCallbackUri)) {
            logger.debug("User already approved. Forwarding to login callback UI page");
            model.addAttribute("loginSuccess", true);
        }

        String redirectUrl = "redirect:" + oauthAuthUrl;
        //THIS IS A HACK, check and remove  org.springframework.validation.BindingResult.shimmerId from the model
        model.remove("org.springframework.validation.BindingResult.shimmerId");

        ModelAndView mvToReturn = new ModelAndView(redirectUrl, model);

        return mvToReturn;
    }


    @GetMapping("/authorize/{shimkey}/callback")
    public ModelAndView handleShimmerOauthCallback(ModelMap model,
                                                   @ModelAttribute("shimmerId") String shimmerId,
                                                   @PathVariable String shimkey,
                                                   @RequestParam(name="code") String code,
                                                   @RequestParam(name="state") String state){
        logger.debug("Handling successful " + shimkey + " auth redirect");
        logger.debug("MODEL shimmer id " + model.get("shimmerId"));
        logger.debug("Passed in shimmer id " + shimmerId);
        logger.debug("Code " + code);
        logger.debug("State " + state);

        String omhOnFhirUi;

        //TODO: Why is this call to the shimmer API not working for Fitbit?
        try {
            drishtiShimmerService.completeShimmerAuth(shimkey, code, state);
        }
        catch(Exception e){
            e.printStackTrace();
            omhOnFhirUi = "redirect:" + omhCallbackUri;
            model.addAttribute("loginSuccess", false);
            logger.debug("Error with Authentication. Redirecting to: " + omhOnFhirUi);
            return new ModelAndView(omhOnFhirUi, model);
        }

        ApplicationUser applicationUser = applicationUserRepository.findByShimmerId(shimmerId);
        if(applicationUser != null){
            applicationUserRepository.save(applicationUser);
        }
        else{
            omhOnFhirUi = "redirect:" + omhCallbackUri;
            model.addAttribute("loginSuccess", false);
            logger.debug("Could not find Shimmer ID for user. Redirecting to: " + omhOnFhirUi);
            return new ModelAndView(omhOnFhirUi, model);
        }

        omhOnFhirUi = "redirect:" + omhCallbackUri;
        model.addAttribute("loginSuccess", true);
        model.addAttribute("shimmerId", shimmerId);
        logger.debug("Redirecting to: " + omhOnFhirUi);
        return new ModelAndView(omhOnFhirUi, model);
    }
}
