# Notes

## [Customizing overlay](http://hapifhir.io/doc_server_tester.html#Adding_the_Overlay)

## Remove all images starting with a name

```
docker rmi $(docker images |grep 'imagename')

```

## Observation request

http://tomcat.nuchange.ca/Observation?date=2018-11-01,2018-11-03&subject=517a9523-a2a9-4b38-99bd-17f952a56eb4

```
{
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 390,
  "entry": [
    {
      "resource": {
        "resourceType": "Observation",
        "id": "a25650bd-6a6e-43e4-b4fe-59057db2b633",
        "identifier": [
          {
            "system": "https://openmrs.org/shimmer/patient_ids",
            "value": "dfa44911-c7f5-4c76-9c2a-15b0fc35a38d"
          }
        ],
        "status": "unknown",
        "category": [
          {
            "coding": [
              {
                "system": "https://snomed.info.sct",
                "code": "68130003",
                "display": "Physical activity (observable entity)"
              }
            ]
          }
        ],
        "code": {
          "coding": [
            {
              "system": "http://loinc.org",
              "code": "55423-8",
              "display": "Number of steps in unspecified time Pedometer"
            }
          ]
        },
        "subject": {
          "reference": "p"
        },
        "effectivePeriod": {
          "start": "2018-11-01T09:35:51+00:00",
          "end": "2018-11-01T09:36:51+00:00"
        },
        "issued": "1970-01-18T20:42:43.352+00:00",
        "device": {
          "display": "Google Fit API,raw:com.google.step_count.cumulative:Huawei:Nexus 6P:c4ca435:BMI160 Step counter,1543363352"
        },
        "component": [
          {
            "code": {
              "coding": [
                {
                  "system": "http://hl7.org/fhir/observation-statistics",
                  "code": "maximum",
                  "display": "Maximum"
                }
              ],
              "text": "Maximum"
            },
            "valueQuantity": {
              "value": 24,
              "unit": "/{tot}",
              "system": "http://unitsofmeasure.org",
              "code": "{steps}/{tot}"
            }
          }
        ]
      }
    },
    {
      "resource": {
        "resourceType": "Observation",
        "id": "1b54f248-b59a-4e86-8276-e5a5aca0414f",
        "identifier": [
          {
            "system": "https://openmrs.org/shimmer/patient_ids",
            "value": "bcf20b10-49cc-4172-9b4c-83d01f0b6a24"
          }
        ],
        "status": "unknown",
        "category": [
          {
            "coding": [
              {
                "system": "https://snomed.info.sct",
                "code": "68130003",
                "display": "Physical activity (observable entity)"
              }
            ]
          }
        ],
        "code": {
          "coding": [
            {
              "system": "http://loinc.org",
              "code": "55423-8",
              "display": "Number of steps in unspecified time Pedometer"
            }
          ]
        },
        "subject": {
          "reference": "p"
        },
        "effectivePeriod": {
          "start": "2018-11-01T11:03:59+00:00",
          "end": "2018-11-01T11:04:59+00:00"
        },
        "issued": "1970-01-18T20:42:43.352+00:00",
        "device": {
          "display": "Google Fit API,raw:com.google.step_count.cumulative:Huawei:Nexus 6P:c4ca435:BMI160 Step counter,1543363352"
        },
        "component": [
          {
            "code": {
              "coding": [
                {
                  "system": "http://hl7.org/fhir/observation-statistics",
                  "code": "maximum",
                  "display": "Maximum"
                }
              ],
              "text": "Maximum"
            },
            "valueQuantity": {
              "value": 17,
              "unit": "/{tot}",
              "system": "http://unitsofmeasure.org",
              "code": "{steps}/{tot}"
            }
          }
        ]
      }
    },
    {
      "resource": {
        "resourceType": "Observation",
        "id": "6054b2ee-4e6b-4d60-bfae-d60631d9d206",
        "identifier": [
          {
            "system": "https://openmrs.org/shimmer/patient_ids",
            "value": "d1bd1eed-6a34-44e4-8874-302a5ed0074c"
          }
        ],
        "status": "unknown",
        "category": [
          {
            "coding": [
              {
                "system": "https://snomed.info.sct",
                "code": "68130003",
                "display": "Physical activity (observable entity)"
              }
            ]
          }
        ],
        "code": {
          "coding": [
            {
              "system": "http://loinc.org",
              "code": "55423-8",
              "display": "Number of steps in unspecified time Pedometer"
            }
          ]
        },
        "subject": {
          "reference": "p"
        },
        "effectivePeriod": {
          "start": "2018-11-01T11:07:50+00:00",
          "end": "2018-11-01T11:08:50+00:00"
        },
        "issued": "1970-01-18T20:42:43.352+00:00",
        "device": {
          "display": "Google Fit API,raw:com.google.step_count.cumulative:Huawei:Nexus 6P:c4ca435:BMI160 Step counter,1543363352"
        },
        "component": [
          {
            "code": {
              "coding": [
                {
                  "system": "http://hl7.org/fhir/observation-statistics",
                  "code": "maximum",
                  "display": "Maximum"
                }
              ],
              "text": "Maximum"
            },
            "valueQuantity": {
              "value": 8,
              "unit": "/{tot}",
              "system": "http://unitsofmeasure.org",
              "code": "{steps}/{tot}"
            }
          }
        ]
      }
    },


```