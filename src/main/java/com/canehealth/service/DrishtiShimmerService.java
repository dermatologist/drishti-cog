package com.canehealth.service;

import com.canehealth.repository.DrishtiShimmerDataRepository;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.gtri.hdap.mdata.jpa.entity.ApplicationUser;
import org.gtri.hdap.mdata.jpa.entity.ShimmerData;
import org.gtri.hdap.mdata.service.ShimmerResponse;
import org.gtri.hdap.mdata.service.ShimmerService;
import org.gtri.hdap.mdata.service.UnsupportedFhirDatePrefixException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DrishtiShimmerService extends ShimmerService {

    private final Logger logger = LoggerFactory.getLogger(DrishtiShimmerService.class);

    @Autowired
    private DrishtiShimmerDataRepository drishtiShimmerDataRepository;

    @Value("${app.drishti.omhcallbackuri}")
    private String omhCallbackUri;

    @Value("${app.drishti.shimmerbase}")
    private String shimmerBase;
    @Value("${app.drishti.shimmerredirect}")
    private String shimmerRedirect;

    @Override
    public ShimmerResponse requestShimmerAuthUrl(String shimmerId, String shimkey){
        String shimmerAuthUrl = shimmerBase + SHIMMER_AUTH_URL;
        String shimmerRedirectUrl =shimmerRedirect;
        shimmerAuthUrl = shimmerAuthUrl.replace("{shim-key}", shimkey);
        shimmerAuthUrl = shimmerAuthUrl.replace("{username}", shimmerId);
        shimmerAuthUrl = shimmerAuthUrl.replace("{redirect-url}", shimmerRedirectUrl);
        logger.debug("Sending authorization request to " + shimmerAuthUrl);
        HttpGet httpGet = new HttpGet(shimmerAuthUrl);
        ShimmerResponse shimmerResponse = processShimmerAuthRequest(httpGet);
        return shimmerResponse;
    }

    @Override
    public void completeShimmerAuth(String shimkey, String code, String state) throws Exception{
        String shimmerAuthCallbackUrl = shimmerBase + SHIMMER_AUTH_CALLBACK;
        shimmerAuthCallbackUrl = shimmerAuthCallbackUrl.replace("{shim-key}", shimkey);
        shimmerAuthCallbackUrl = shimmerAuthCallbackUrl.replace("{code}", code);
        shimmerAuthCallbackUrl = shimmerAuthCallbackUrl.replace("{state}", state);

        logger.debug("Completing Shimmer Auth: " + shimmerAuthCallbackUrl);

        HttpGet httpGet = new HttpGet(shimmerAuthCallbackUrl);
        CloseableHttpClient httpClient = createHttpClient();
        HttpClientContext httpClientContext = HttpClientContext.create();
        CloseableHttpResponse shimmerAuthResponse = httpClient.execute(httpGet, httpClientContext);
        int statusCode;
        try {
            statusCode = shimmerAuthResponse.getStatusLine().getStatusCode();
        }
        finally {
            shimmerAuthResponse.close();
        }
        if (statusCode != 200) {
            logger.debug("Auth Callback Resulted in Response Code " + statusCode);
            throw new Exception("Authorization did not complete: " +  EntityUtils.toString(shimmerAuthResponse.getEntity()));
        }

        logger.debug("Completed Shimmer Auth");
    }

    /**
     * Calls the Shimmer API to retrieve data for a user
     * @param applicationUser the application user for the query
     * @param dateQueries A list of Strings of the format yyyy-MM-dd with start and end date parameters
     * @return ShimmerResponse object with response details.
     */
    @Override
    public ShimmerResponse retrieveShimmerData(String shimmerDataUrlFragment, ApplicationUser applicationUser, List<String> dateQueries){
        logger.debug("Querying Shimmer");
        String shimmerDataUrl = shimmerBase + shimmerDataUrlFragment;
        shimmerDataUrl = shimmerDataUrl.replace("{shim-key}", applicationUser.getApplicationUserId().getShimKey());
        shimmerDataUrl = shimmerDataUrl.replace("{username}", applicationUser.getShimmerId());
        shimmerDataUrl = shimmerDataUrl.replace("{normalize}", "true");

        LocalDate startDate = null;
        LocalDate endDate = null;
        if( dateQueries != null ) {
            try {
                Map<String, LocalDate> dates = parseDateQueries(dateQueries);
                startDate = dates.get(START_DATE_KEY);
                endDate = dates.get(END_DATE_KEY);
            }
            catch(UnsupportedFhirDatePrefixException ufdpe){
                String errorResponse = "{\"status\":" + HttpStatus.SC_BAD_REQUEST + ",\"exception\":\"" + ufdpe.getMessage() + "\"}";
                return new ShimmerResponse(HttpStatus.SC_BAD_REQUEST, errorResponse);
            }
        }
        if(startDate != null) {
            shimmerDataUrl += SHIMMER_START_DATE_URL_PARAM;
            shimmerDataUrl = shimmerDataUrl.replace("{start-date}", startDate.toString());
        }
        if(endDate != null){
            shimmerDataUrl += SHIMMER_END_DATE_URL_PARAM;
            shimmerDataUrl = shimmerDataUrl.replace("{end-date}", endDate.toString());
        }

        logger.debug("Sending data request to " + shimmerDataUrl);
        HttpGet httpGet = new HttpGet(shimmerDataUrl);
        ShimmerResponse shimmerResponse = processShimmerDataRequest(httpGet);
        logger.debug("Completed query to Shimmer");
        return shimmerResponse;
    }

    /**
     * Writes Shimmer JSON data to the database
     * @param applicationUser the user who owns the data
     * @param jsonResponse the data for the user
     * @return the documentID for the stored shimmer data;
     */
    @Override
    public String storePatientJson(ApplicationUser applicationUser, String jsonResponse){
        logger.debug("Storing patient data");
        ShimmerData shimmerData = new ShimmerData(applicationUser, jsonResponse);
        drishtiShimmerDataRepository.save(shimmerData);
        return shimmerData.getDocumentId();
    }

    /*========================================================================*/
    /* Private Methods */
    /*========================================================================*/

    private ShimmerResponse processShimmerAuthRequest(HttpUriRequest request){
        CloseableHttpClient httpClient = createHttpClient();
        HttpClientContext httpClientContext = HttpClientContext.create();
        ShimmerResponse shimmerResponse;
        try {
            CloseableHttpResponse shimmerAuthResponse = httpClient.execute(request, httpClientContext);
            //checkShimmerAuthResponse handles closing shimmerAuthResponse
            try {
                shimmerResponse = checkShimmerAuthResponse(shimmerAuthResponse);
            }
            catch(IOException ioe){
                logger.error("Error processing Shimmer response", ioe);
                String errorResponse = "{\"status\":" + org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value() + ",\"exception\":\"Could not complete Shimmer request\"}";
                shimmerResponse = new ShimmerResponse(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse);
            }
        }
        catch(IOException ioe){
            String errorResponse = "{\"status\":" + org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value() + ",\"exception\":\"Could not process Shimmer response\"}";
            shimmerResponse = new ShimmerResponse(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse);
        }
        return shimmerResponse;
    }

    /**
     * Creates a {@link CloseableHttpClient} to use in the application
     * @return
     */
    private CloseableHttpClient createHttpClient(){
        //TODO: Fix to use non-deprecated code
        logger.debug("Creating HTTP Client");
        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(globalConfig)
//                .setSSLSocketFactory(getSslsf())
//                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .build();
        logger.debug("Returning created HTTP Client");
        return httpClient;
    }

    private ShimmerResponse checkShimmerAuthResponse(CloseableHttpResponse shimmerAuthResponse) throws IOException {
        String shimmerResponseData = null;
        ShimmerResponse shimmerResponse;
        try {
            int statusCode = shimmerAuthResponse.getStatusLine().getStatusCode();
            //All we need is the cookie, which is managed as part of the HTTPContext
            //for now ignore the content of the response. At a later date process
            //the JSON. It contains permission and user metadata.
            HttpEntity responseEntity = shimmerAuthResponse.getEntity();
            //get the json from the response and get the auth URL to redirect the user
            String responseStr = EntityUtils.toString(responseEntity);

            shimmerResponse = new ShimmerResponse(statusCode, responseStr);

            logger.debug("Response Code " + statusCode);
            if(statusCode == 200) {
                //All we need is the cookie, which is managed as part of the HTTPContext
                //for now ignore the content of the response. At a later date process
                //the JSON. It contains permission and user metadata.
//                HttpEntity responseEntity = shimmerAuthResponse.getEntity();
                //get the json from the response and get the auth URL to redirect the user
//                String responseStr = EntityUtils.toString(responseEntity);
                logger.debug("Shimmer Auth Response: " + responseStr);

                //response JSON {"id":"5b6852e7345e53000bbf6894","stateKey":null,"username":"93c542ab-2705-4526-bf1b-1212d2185087","redirectUri":null,"requestParams":null,"authorizationUrl":null,"clientRedirectUrl":null,"isAuthorized":true,"serializedRequest":null}

                JSONObject responseJson = new JSONObject(responseStr);
                //check if we are already authenticated
                if( !responseJson.getBoolean("isAuthorized") ){
                    logger.debug("User is not authorized");
                    shimmerResponseData = responseJson.getString("authorizationUrl");
                }
                else{
                    logger.debug("User is authorized");
                    shimmerResponseData = System.getenv(omhCallbackUri);
                }

                //set the URL as
                shimmerResponse.setResponseData(shimmerResponseData);

                logger.debug("Authorization URL " + shimmerResponseData);
            }
        } finally {
            shimmerAuthResponse.close();
        }
        return shimmerResponse;
    }

    private ShimmerResponse processShimmerDataRequest(HttpUriRequest request){
        ShimmerResponse shimmerResponse;
        try {
            logger.debug("Sending Shimmer Data Request");
            CloseableHttpClient httpClient = createHttpClient();
            HttpClientContext httpClientContext = HttpClientContext.create();
            CloseableHttpResponse shimmerDataResponse = httpClient.execute(request, httpClientContext);
            logger.debug("Received shimmer data");
            //checkShimmerDataResponse closes the shimmerDataResponse
            shimmerResponse = checkShimmerDataResponse(shimmerDataResponse);
        }
        catch(IOException ioe){
            logger.error("Error processing Shimmer response", ioe);
            String errorResponse = "{\"status\":" + org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value() + ",\"exception\":\"Could not process Shimmer response\"}";
            shimmerResponse = new ShimmerResponse(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse);
        }
        return shimmerResponse;
    }

    private ShimmerResponse checkShimmerDataResponse(CloseableHttpResponse shimmerAuthResponse) throws IOException {
        logger.debug("Looking for Data");
        String jsonResp = "";
        ShimmerResponse shimmerResponse;
        try {
            int statusCode = shimmerAuthResponse.getStatusLine().getStatusCode();
            HttpEntity responseEntity = shimmerAuthResponse.getEntity();
            jsonResp = EntityUtils.toString(responseEntity);

            logger.debug("Response Code " + statusCode);
            logger.debug("Data Response: " + jsonResp);
            shimmerResponse = new ShimmerResponse(statusCode, jsonResp);
//            if(statusCode == 200) {
//                //All we need is the cookie, which is managed as part of the HTTPContext
//                //for now ignore the content of the response. At a later date process
//                //the JSON. It contains permission and user metadata.
//
//                //get the json from the response and get the auth URL to redirect the user
//                jsonResp = EntityUtils.toString(responseEntity);
//                logger.debug("Data Response: " + jsonResp);
//            }
//            else{
//                logger.debug("Different Response Code " + statusCode);
//                jsonResp = EntityUtils.toString(responseEntity);
//                logger.debug(EntityUtils.toString(responseEntity));
//            }

        }
        catch(Exception ioe){
            logger.error("Error parsing response Entity", ioe);
            String errorResponse = "{\"status\":500,\"exception\":\"Could not parse response entity of Shimmer response\"}";
            shimmerResponse = new ShimmerResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, errorResponse);
        }
        finally {
            shimmerAuthResponse.close();
        }

        return shimmerResponse;
    }
}
