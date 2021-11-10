package contracts.processDefinition

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "should return user-process list"

  request {
    url "/api/process-definition/"
    method GET()
  }

  response {
    status OK()
    headers {
      contentType applicationJson()
    }
    body([
        [
            id       : "id1",
            key      : "key1",
            name     : "name1",
            suspended: false,
            formKey  : "formKey1"
        ],
        [
            id       : "id2",
            key      : "key2",
            name     : "name2",
            suspended: true,
            formKey  : "formKey2"
        ]
    ])
  }
}