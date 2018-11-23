package com.canehealth.service;

import org.gtri.hdap.mdata.service.ShimmerResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DrishtiShimmerServiceTest {

    private final Logger logger = LoggerFactory.getLogger(DrishtiShimmerServiceTest.class);

    @Autowired
    private DrishtiShimmerService drishtiShimmerService;

    @Autowired
    private DrishtiResponseService drishtiResponseService;


    @Value("${app.drishti.omhcallbackuri}")
    private String omhCallbackUri;

    @Value("${app.drishti.shimmerbase}")
    private String shimmerBase;
    @Value("${app.drishti.shimmerredirect}")
    private String shimmerRedirect;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void requestShimmerAuthUrl() {

        ShimmerResponse shimmerResponse = drishtiShimmerService.requestShimmerAuthUrl(drishtiResponseService.getShimmerId("client1", "googlefit"), "googlefit");
        assertNotNull(shimmerResponse);
        logger.info("shimmerResponse: " + shimmerResponse);
        logger.info("shimmerResponseCode: " + shimmerResponse.getResponseCode());
        logger.info("AuthURL: " + shimmerResponse.getResponseData());
        assertEquals(HttpStatus.OK.value(), shimmerResponse.getResponseCode());
    }

    @Test
    public void completeShimmerAuth() {
    }

    @Test
    public void retrieveShimmerData() {
    }

    @Test
    public void storePatientJson() {
    }
}