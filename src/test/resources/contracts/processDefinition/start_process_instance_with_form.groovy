package contracts.processDefinition

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'should return started process instance'

    request {
        urlPath '/api/process-definition/processDefinitionId/start-with-form'
        method POST()
        headers {
            contentType applicationJson()
            header("x-access-token", "testToken")

        }
        body(
                "data": ["formField1": "testValue"]
        )
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                id: 'processInstanceId',
                processDefinitionId: 'processDefinitionId',
                ended: false
        )
    }
}