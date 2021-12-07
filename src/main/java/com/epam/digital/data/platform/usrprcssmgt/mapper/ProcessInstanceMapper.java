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

package com.epam.digital.data.platform.usrprcssmgt.mapper;

import com.epam.digital.data.platform.bpms.api.dto.DdmProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.DdmProcessInstanceStatus;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.starter.security.SystemRole;
import com.epam.digital.data.platform.usrprcssmgt.i18n.ProcessInstanceStatusMessageTitle;
import com.epam.digital.data.platform.usrprcssmgt.model.response.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.response.StartProcessInstanceResponse;
import java.util.List;
import java.util.Objects;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The class represents a mapper for process instance entity. The interface contains a methods for
 * converting camunda process instance. Abstract methods are implemented using the MapStruct.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProcessInstanceMapper {

  @Autowired
  private MessageResolver messageResolver;

  /**
   * Method for converting BPMS {@link DdmProcessInstanceStatus} to localized title base by role
   *
   * @param processInstanceStatus the process instance status
   * @param systemRole            the role
   * @return localized title
   */
  @Named("toStatusTitle")
  public String toStatusTitle(DdmProcessInstanceStatus processInstanceStatus,
      @Context SystemRole systemRole) {
    var code = ProcessInstanceStatusMessageTitle.from(processInstanceStatus, systemRole);

    return Objects.isNull(code) ? null : messageResolver.getMessage(code);
  }

  /**
   * Method for converting {@link DdmProcessInstanceDto} entity to {@link
   * GetProcessInstanceResponse} entity using officer status mapping.
   *
   * @param dto process instance dto
   * @return converted process instance.
   */
  @Mapping(target = "status.code", source = "dto.state")
  @Mapping(target = "status.title", source = "dto.state", qualifiedByName = "toStatusTitle")
  @Named("toProcessInstanceResponse")
  public abstract GetProcessInstanceResponse toProcessInstanceResponse(
      DdmProcessInstanceDto dto, @Context SystemRole systemRole);

  /**
   * Method for converting a list of {@link DdmProcessInstanceDto} entity to list of {@link
   * GetProcessInstanceResponse} entity using officer status mapping. (Used {@link
   * ProcessInstanceMapper#toProcessInstanceResponse(DdmProcessInstanceDto, SystemRole)} for
   * iterable mapping)
   *
   * @param processInstanceDtos process instance dto list
   * @return converted process instance.
   */
  @IterableMapping(qualifiedByName = "toProcessInstanceResponse")
  public abstract List<GetProcessInstanceResponse> toProcessInstanceResponses(
      List<DdmProcessInstanceDto> processInstanceDtos, @Context SystemRole systemRole);

  /**
   * Method for converting camunda {@link ProcessInstanceDto} entity to {@link
   * StartProcessInstanceResponse} entity.
   *
   * @param processInstanceDto camunda process instance.
   * @return started process instance.
   */
  @Mapping(source = "definitionId", target = "processDefinitionId")
  public abstract StartProcessInstanceResponse toStartProcessInstanceResponse(
      ProcessInstanceDto processInstanceDto);
}
