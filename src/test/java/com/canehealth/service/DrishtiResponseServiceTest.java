package com.canehealth.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DrishtiResponseServiceTest {

    private final Logger logger = LoggerFactory.getLogger(DrishtiResponseServiceTest.class);

    @Value("${app.drishti.patientresourceid}")
    private String patientResourceId;

    @Value("${app.drishti.patientidsystem}")
    private String patienIdSystem;

    @Value("${app.drishti.obscatsystem}")
    private String obsCatSystem;
    @Value("${app.drishti.obscatcode}")
    private String obsCatCode;
    @Value("${app.drishti.obscatdisplay}")
    private String obsCatDisplay;
    @Value("${app.drishti.obscodesystem}")
    private String obsCodeSystem;
    @Value("${app.drishti.obscodecode}")
    private String obsCodeCode;
    @Value("${app.drishti.obscodedisplay}")
    private String obsCodeDisplay;
    @Value("${app.drishti.obscompcodesystem}")
    private String obsCompCodeSystem;
    @Value("${app.drishti.obscompcodecode}")
    private String obsCompCodeCode;
    @Value("${app.drishti.obscompcodedisplay}")
    private String obsCompCodeDisplay;
    @Value("${app.drishti.obscompcodetext}")
    private String obsCompCodeText;
    @Value("${app.drishti.obscompvaluecodeunit}")
    private String obsCompValueCodeUnit;
    @Value("${app.drishti.obscompvaluecodesystem}")
    private String obsCompValueCodeSystem;
    @Value("${app.drishti.obscompvaluecodecode}")
    private String obsCompValueCodeCode;


    @Autowired
    private DrishtiResponseService drishtiResponseService;


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getShimmerId() {
        String userShimmerId = drishtiResponseService.getShimmerId("client1", "googlefit");
        assertNotNull(userShimmerId);
        logger.info("userShimmerId: " + userShimmerId);
    }

    @Test
    public void generatePatient() {
    }

    @Test
    public void createIdentifier() {
    }

    @Test
    public void createCategory() {
    }

    @Test
    public void createObservationComponent() {
    }

    @Test
    public void makeByteArrayForDocument() {
    }

    @Test
    public void generateObservation() {
    }
}