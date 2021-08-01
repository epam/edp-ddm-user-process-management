package com.epam.digital.data.platform.usrprcssmgt.model.swagger;

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
