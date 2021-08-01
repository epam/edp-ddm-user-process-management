package contracts.processInstance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return process-instance count"

    request {
        urlPath "/api/process-instance/count"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                count: 3
        )
    }
}