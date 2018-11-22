package com.canehealth.service;

import com.canehealth.repository.DrishtiShimmerDataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DrishtiShimmerServiceTest {

    @Autowired
    private DrishtiShimmerService shimmerService;

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