package contracts.processDefinition

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user-process by key"

    request {
        url "/api/process-definition/processDefinitionKey"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                "id": "id1",
                "key": "key1",
                "name": "name1",
                "suspended": false,
                "formKey": "formKey1"
        )
    }
}