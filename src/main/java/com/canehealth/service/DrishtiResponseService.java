package com.canehealth.service;

import com.canehealth.repository.DrishtiApplicationUserRepository;
import com.canehealth.repository.DrishtiShimmerDataRepository;
import org.gtri.hdap.mdata.jpa.entity.ApplicationUser;
import org.gtri.hdap.mdata.jpa.entity.ApplicationUserId;
import org.gtri.hdap.mdata.service.ResponseService;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DrishtiResponseService extends ResponseService {


    @Autowired
    private DrishtiApplicationUserRepository drishtiApplicationUserRepository;

    @Autowired
    private DrishtiShimmerDataRepository shimmerDataRepository;

    private final Logger logger = LoggerFactory.getLogger(ResponseService.class);

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

    @Override
    public String getShimmerId(String ehrId, String shimkey){
        logger.debug("Checking User EHR ID: [" + ehrId + "] ShimKey: [" + shimkey + "]");
        ApplicationUserId applicationUserId = new ApplicationUserId(ehrId, shimkey);
        //debug info
        logger.debug("Find by ID " + drishtiApplicationUserRepository.findByApplicationUserId(applicationUserId).isPresent());
        ApplicationUser user;
        Optional<ApplicationUser> applicationUserOptional = drishtiApplicationUserRepository.findByApplicationUserId(applicationUserId);
        if(applicationUserOptional.isPresent()){
            logger.debug("Found the user");
            user = applicationUserOptional.get();
        }
        else{
            logger.debug("Did not find user. Creating new one");
            user = createNewApplicationUser(applicationUserId);
        }
        String shimmerId = user.getShimmerId();
        logger.debug("Returning shimmer id: " + shimmerId);
        return shimmerId;
    }

    @Override
    public Patient generatePatient(String shimKey){
        Patient patient = new Patient();
        //set id
        patient.setId(patientResourceId);
        //set identifier
        List<Identifier> idList = createSingleIdentifier(shimKey);
        patient.setIdentifier(idList);
        return patient;
    }

    @Override
    public Identifier createIdentifier(String id){
        Identifier identifier = new Identifier();
        identifier.setSystem(patienIdSystem);
        identifier.setValue(id);
        return identifier;
    }
}
