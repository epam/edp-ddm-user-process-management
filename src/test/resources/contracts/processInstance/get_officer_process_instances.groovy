package contracts.processInstance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "should return officer process-instance list"

  request {
    urlPath "/api/officer/process-instance"
    method GET()
  }

  response {
    status OK()
    headers {
      contentType applicationJson()
    }
    body([
        [
            id                   : "id1",
            processDefinitionName: "name1",
            startTime            : "2020-12-01T12:00:00.000Z",
            status               : [
                code: "SUSPENDED"
            ]
        ],
        [
            id                   : "id2",
            processDefinitionName: "name2",
            startTime            : "2020-12-01T12:01:00.000Z",
            status               : [
                code: "PENDING"
            ]
        ]
    ])
  }
}