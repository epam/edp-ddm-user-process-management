package contracts.processInstance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return citizen process-instance list"

    request {
        urlPath "/api/citizen/process-instance"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body('''\
            [{
                "id": "id3",
                "processDefinitionName": "name3",
                "startTime": "2020-12-01T12:00:00.000Z",
                "status": {
                    "code": "CITIZEN_SUSPENDED"
                }
            },
            {
                "id": "id4",
                "processDefinitionName": "name4",
                "startTime": "2020-12-01T12:01:00.000Z",
                "status": {
                    "code": "CITIZEN_PENDING"
                }
            }]
            ''')
    }
}