/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    body([
        [
            id                   : "id3",
            processDefinitionName: "name3",
            startTime            : "2020-12-01T12:00:00.000Z",
            status               : [
                code: "SUSPENDED"
            ]
        ],
        [
            id                   : "id4",
            processDefinitionName: "name4",
            startTime            : "2020-12-01T12:01:00.000Z",
            status               : [
                code: "PENDING"
            ]
        ]
    ])
  }
}