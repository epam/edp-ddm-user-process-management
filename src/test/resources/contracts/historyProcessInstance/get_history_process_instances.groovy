package contracts.historyProcessInstance

import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "should return history process-instances list"

  request {
    urlPath "/api/history/process-instance"
    method GET()
  }

  response {
    status OK()
    headers {
      contentType applicationJson()
    }
    body([
        [
            id                   : "historyProcessInstanceId1",
            processDefinitionId  : "processDefinitionId1",
            processDefinitionName: "name3",
            startTime            : "2020-12-01T12:00:00.000Z",
            endTime              : "2020-12-01T13:00:00.000Z",
            status               : [
                code: "ENDED"
            ],
            excerptId            : "excerptId1"
        ],
        [
            id                   : "historyProcessInstanceId2",
            processDefinitionId  : "processDefinitionId2",
            processDefinitionName: "name4",
            startTime            : "2020-12-01T12:00:00.000Z",
            endTime              : "2020-12-01T13:00:00.000Z",
            status               : [
                code: "ENDED"
            ],
            excerptId            : null
        ]])
  }
}