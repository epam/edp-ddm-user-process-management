package com.epam.digital.data.platform.usrprcssmgt.mapper;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import java.util.List;
import java.util.Objects;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The class represents a mapper for process instance entity. The interface contains a methods for
 * converting camunda historic process instance. Abstract methods are implemented using the
 * MapStruct.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProcessInstanceMapper {

  @Autowired
  private MessageResolver messageResolver;

  /**
   * Method for converting camunda {@link HistoricProcessInstanceDto} entity to {@link
   * GetProcessInstanceResponse} entity using officer status mapping.
   *
   * @param historicProcessInstanceDto camunda historic process instance.
   * @return converted process instance.
   */
  @Mapping(target = "status.code", source = "state", qualifiedByName = "toOfficesStatus")
  @Named("toOfficerProcessInstanceResponse")
  public abstract GetProcessInstanceResponse toOfficerProcessInstanceResponse(
      HistoryProcessInstanceDto historicProcessInstanceDto);

  /**
   * Method for converting a list of camunda {@link HistoricProcessInstanceDto} entity to list of
   * {@link GetProcessInstanceResponse} entity using officer status mapping. (Used {@link
   * ProcessInstanceMapper#toOfficerProcessInstanceResponse(HistoryProcessInstanceDto)} for iterable
   * mapping)
   *
   * @param historicProcessInstanceDtos camunda historic process instance.
   * @return converted process instance.
   */
  @IterableMapping(qualifiedByName = "toOfficerProcessInstanceResponse")
  public abstract List<GetProcessInstanceResponse> toOfficerProcessInstanceResponses(
      List<HistoryProcessInstanceDto> historicProcessInstanceDtos);

  /**
   * Method for converting camunda {@link HistoricProcessInstanceDto} entity to {@link
   * GetProcessInstanceResponse} entity using citizen status mapping.
   *
   * @param historicProcessInstanceDto camunda historic process instance.
   * @return converted process instance.
   */
  @Mapping(target = "status.code", source = "state", qualifiedByName = "toCitizenStatus")
  @Named("toCitizenProcessInstanceResponse")
  public abstract GetProcessInstanceResponse toCitizenProcessInstanceResponse(
      HistoryProcessInstanceDto historicProcessInstanceDto);

  /**
   * Method for converting a list of camunda {@link HistoricProcessInstanceDto} entity to list of
   * {@link GetProcessInstanceResponse} entity using officer status mapping. (Used {@link
   * ProcessInstanceMapper#toCitizenProcessInstanceResponse(HistoryProcessInstanceDto)} for iterable
   * mapping)
   *
   * @param historicProcessInstanceDtos camunda historic process instance.
   * @return converted process instance.
   */
  @IterableMapping(qualifiedByName = "toCitizenProcessInstanceResponse")
  public abstract List<GetProcessInstanceResponse> toCitizenProcessInstanceResponses(
      List<HistoryProcessInstanceDto> historicProcessInstanceDtos);

  /**
   * Method for defining status#title in {@link GetProcessInstanceResponse} after the mapping
   *
   * @param target target in which it's needed to define status#title
   */
  @AfterMapping
  public void setStatusTitle(@MappingTarget GetProcessInstanceResponse target) {
    var code = target.getStatus().getCode();
    if (Objects.isNull(code)) {
      return;
    }
    target.getStatus().setTitle(messageResolver.getMessage(code));
  }

  /**
   * Mapping BPMS {@link HistoryProcessInstanceStatus} to officer {@link ProcessInstanceStatus}
   *
   * @param status BPMS status
   */
  @ValueMapping(source = "ACTIVE", target = "IN_PROGRESS")
  @ValueMapping(source = "INTERNALLY_TERMINATED", target = MappingConstants.NULL)
  @Named("toOfficesStatus")
  public abstract ProcessInstanceStatus toOfficesStatus(HistoryProcessInstanceStatus status);

  /**
   * Mapping BPMS {@link HistoryProcessInstanceStatus} to citizen {@link ProcessInstanceStatus}
   *
   * @param status BPMS status
   */
  @ValueMapping(source = "ACTIVE", target = "CITIZEN_IN_PROGRESS")
  @ValueMapping(source = "INTERNALLY_TERMINATED", target = MappingConstants.NULL)
  @ValueMapping(source = "PENDING", target = "CITIZEN_PENDING")
  @ValueMapping(source = "SUSPENDED", target = "CITIZEN_SUSPENDED")
  @Named("toCitizenStatus")
  public abstract ProcessInstanceStatus toCitizenStatus(HistoryProcessInstanceStatus status);

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
