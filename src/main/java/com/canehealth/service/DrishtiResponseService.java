package com.canehealth.service;

import com.canehealth.repository.DrishtiApplicationUserRepository;
import com.canehealth.repository.DrishtiShimmerDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.gtri.hdap.mdata.jpa.entity.ApplicationUser;
import org.gtri.hdap.mdata.jpa.entity.ApplicationUserId;
import org.gtri.hdap.mdata.jpa.entity.ShimmerData;
import org.gtri.hdap.mdata.service.ResponseService;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

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

    @Override
    public List<CodeableConcept> createCategory() {
        CodeableConcept codeableConcept = createCodeableConcept(obsCatSystem, obsCatCode, obsCatDisplay);
        List<CodeableConcept> codeableConceptList = new ArrayList<>();
        codeableConceptList.add(codeableConcept);
        return codeableConceptList;
    }

    @Override
    public Observation.ObservationComponentComponent createObservationComponent(int stepCount) {
        Observation.ObservationComponentComponent occ = new Observation.ObservationComponentComponent();
        //set code
        CodeableConcept codeableConcept = createCodeableConcept(obsCompCodeSystem, obsCompCodeCode, obsCompCodeDisplay, obsCompCodeText);
        occ.setCode(codeableConcept);

        //set valueQuantity
        Quantity quantity = new Quantity();
        quantity.setValue(stepCount);
        quantity.setUnit(obsCompValueCodeUnit);
        quantity.setSystem(obsCompValueCodeSystem);
        quantity.setCode(obsCompValueCodeCode);
        occ.setValue(quantity);

        return occ;
    }

    @Override
    public byte[] makeByteArrayForDocument(String documentId) {
        logger.debug("Making Bundle for Document" + documentId);
        //get fitbit data for binary resource
        ShimmerData shimmerData = shimmerDataRepository.findByDocumentId(documentId);
        //get shimmer data
        String jsonData = "";
        if (shimmerData != null) {
            jsonData = shimmerData.getJsonData();
            logger.debug("Got JSON data " + jsonData);
            //Delete the stored user data because it has been retrieved. We do not want to hold on to data longer than needed.
            shimmerDataRepository.delete(shimmerData);
        }
        return jsonData.getBytes();
    }


    @Override
    public Observation generateObservation(String shimKey, long timestamp, JsonNode omhStepCount) {
        logger.debug("Generating Observation");
        JsonNode omhStepCountBody = omhStepCount.get("body");
        logger.debug("got body");

        JsonNode omhStepCountHeader = omhStepCount.get("header");
        logger.debug("got header");

        String identifier = omhStepCountHeader.get("id").asText();
        logger.debug("identifier: " + identifier);

        String startDateStr = omhStepCountBody.get("effective_time_frame").get("time_interval").get("start_date_time").asText();
        logger.debug("startDateStr: [" + startDateStr + "]");

        String endDateStr = omhStepCountBody.get("effective_time_frame").get("time_interval").get("end_date_time").asText();
        logger.debug("endDateStr: [" + endDateStr + "]");

        int stepCount = omhStepCountBody.get("step_count").asInt();
        logger.debug("stepCount: " + stepCount);

        String deviceSource = omhStepCountHeader.get("acquisition_provenance").get("source_name").asText();
        logger.debug("deviceSource: " + deviceSource);

        String deviceOrigin = omhStepCountHeader.get("acquisition_provenance").get("source_origin_id").asText();
        logger.debug("deviceOrigin: " + deviceOrigin);

        List<String> deviceInfoList = new ArrayList<String>();
        deviceInfoList.add(deviceSource);
        deviceInfoList.add(deviceOrigin);
        deviceInfoList.add(Long.toString(timestamp));
        String deviceInfo = String.join(",", deviceInfoList);

        Observation observation = new Observation();
        //set Id
        observation.setId(UUID.randomUUID().toString());

        //set patient
        Patient patient = generatePatient(shimKey);
        List<Resource> containedResources = new ArrayList<Resource>();
        containedResources.add(patient);
        observation.setContained(containedResources);

        //set identifier
        List<Identifier> idList = createSingleIdentifier(identifier);
        observation.setIdentifier(idList);

        //set status
        observation.setStatus(Observation.ObservationStatus.UNKNOWN);

        //set category
        List<CodeableConcept> categories = createCategory();
        observation.setCategory(categories);

        //set code
        CodeableConcept code = createCodeableConcept(obsCodeSystem, obsCodeCode, obsCodeDisplay);
        observation.setCode(code);

        //set subject
        Reference subjectRef = createSubjectReference(patient.getId());
        observation.setSubject(subjectRef);

        //set effective period
        try {
            Period effectivePeriod = createEffectiveDateTime(startDateStr, endDateStr);
            observation.setEffective(effectivePeriod);
        } catch (ParseException pe) {
            logger.error("Could not parse Shimmer dates");
            pe.printStackTrace();
        }

        //set Issued
        observation.setIssued(new Date(timestamp));

        //set device
        Reference deviceRef = createDeviceReference(deviceInfo);
        observation.setDevice(deviceRef);

        //set steps
        List<Observation.ObservationComponentComponent> componentList = new ArrayList<Observation.ObservationComponentComponent>();
        Observation.ObservationComponentComponent occ = createObservationComponent(stepCount);
        componentList.add(occ);
        observation.setComponent(componentList);
        return observation;
    }

}
