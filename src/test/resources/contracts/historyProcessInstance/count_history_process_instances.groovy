package contracts.historyProcessInstance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "should return history process-instance count"

  request {
    urlPath "/api/history/process-instance/count"
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