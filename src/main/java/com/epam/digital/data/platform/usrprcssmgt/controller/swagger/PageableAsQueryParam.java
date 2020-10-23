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

package com.epam.digital.data.platform.usrprcssmgt.controller.swagger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to generate Swagger documentation on Pageable parameters
 */

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(in = ParameterIn.QUERY
    , description = "Pagination of results. Specifies the index of the first result to return."
    , name = "firstResult"
    , schema = @Schema(type = "integer"))
@Parameter(in = ParameterIn.QUERY
    , description = "Pagination of results. Specifies the maximum number of results to return. Will"
    + " return less results if there are no more results left."
    , name = "maxResult"
    , schema = @Schema(type = "integer"))
@Parameter(in = ParameterIn.QUERY
    , description = "Sort the results lexicographically by a given criterion. Valid values are instanceId,"
    + " caseInstanceId, dueDate, executionId, caseExecutionId,assignee, created, description, id, name,"
    + " nameCaseInsensitive and priority. Must be used in conjunction with the sortOrder parameter."
    , name = "sortBy"
    , schema = @Schema(type = "string"))
@Parameter(in = ParameterIn.QUERY
    , description = "Sort the results in a given order. Values may be asc for ascending order or desc"
    + " for descending order. Must be used in conjunction with the sortBy parameter."
    , name = "sortOrder"
    , schema = @Schema(type = "string"))
public @interface PageableAsQueryParam {

}
