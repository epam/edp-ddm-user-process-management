package contracts.processDefinition

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'should return started process instance'

    request {
        urlPath '/api/process-definition/processDefinitionId/start'
        method POST()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                id: 'processInstanceId',
                processDefinitionId: 'processDefinitionId',
                ended: true
        )
    }
}