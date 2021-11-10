package contracts.processDefinition

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "should return user-process count"

  request {
    urlPath "/api/process-definition/count"
    method GET()
  }

  response {
    status OK()
    headers {
      contentType applicationJson()
    }
    body(
        count: 2
    )
  }
}