package com.canehealth.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.canehealth.config.DataConfig;
import com.canehealth.config.DatasetInitializer;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {
    private final Logger logger = LoggerFactory.getLogger(DataController.class);
    private DatasetInitializer dataInitializer;
    private DataConfig dataConfig;
    private IGenericClient fhirClient;
    //private static FhirContext fhirContext;
    private final FhirContext ctx;

    @Value("${app.drishti.shimmerbase}")
    private String serverBase;

    public DataController(DatasetInitializer dataInitializer, IGenericClient fhirClient, DataConfig dataConfig) {
        this.dataConfig = dataConfig;
        this.fhirClient = fhirClient;
        this.dataInitializer = dataInitializer;
        //fhirContext = FhirContext.forDstu3();
        ctx = FhirContext.forDstu3();
    }

    /**
     * Handles Bundle as posted. The first element is the patient.
     *
     */
    //GET https://apps.hdap.gatech.edu/hapiR4/baseR4/DocumentReference?subject=EXxcda
    @PostMapping("/ProcessBundle")
    public ResponseEntity processBundle(@RequestBody String bundleString) {
        final IParser iParser = ctx.newJsonParser();

        Bundle bundle = iParser.parseResource(Bundle.class, bundleString);

        String uuid = bundle.getId();
        logger.info("Processing bundle: ", uuid);

        // First resource in the bundle is the patient
        Patient patient = (Patient) bundle.getEntry().get(0).getResource();

        // Creating client
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        // Conditional Create this patient
        MethodOutcome outcome = client.create()
                .resource(patient)
                .conditional()
                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("urn:system", uuid))
                .execute();

        // This will return Boolean.TRUE if the server responded with an HTTP 201 created,
        // otherwise it will return null.
        Boolean created = outcome.getCreated();

        // The ID of the created, or the pre-existing resource
        IdDt id = (IdDt) outcome.getId();
        patient.setId(id);
        logger.info("Patient ID: " + id.getValue());
        MethodOutcome outcome2;
        IdDt id2;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();
            if (resource instanceof Observation) {
                Observation obs = (Observation) resource;
                obs.setSubject(new Reference(patient));
                outcome2 = client.create()
                        .resource(obs)
                        .prettyPrint()
                        .encodedJson()
                        .execute();
                id2 = (IdDt) outcome2.getId();
                logger.info("Set Observation with ID: " + id2.getValue());
            }else if (resource instanceof CarePlan){
                CarePlan carePlan = (CarePlan) resource;
                carePlan.setSubject(new Reference(patient));
                outcome2 = client.create()
                        .resource(carePlan)
                        .prettyPrint()
                        .encodedJson()
                        .execute();
                id2 = (IdDt) outcome2.getId();
                logger.info("Set Careplan with ID: " + id2.getValue());
            }
        }
        return ResponseEntity.ok(patient);
    }

//    @RequestMapping(value = "/fhir/baseDstu3/metadata", method = RequestMethod.POST)
//    public void initData(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
//        log.debug("Feeding data...");
//        //dataInitializer.feedData(dataInitializer.parseDatasets(dataConfig), fhirClient);
//        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//        log.debug("Done");
//    }
}