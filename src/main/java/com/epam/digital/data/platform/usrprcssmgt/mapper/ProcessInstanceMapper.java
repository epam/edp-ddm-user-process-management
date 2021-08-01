package com.epam.digital.data.platform.usrprcssmgt.mapper;

import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * The interface represents a mapper for process instance entity. The interface contains a methods
 * for converting camunda historic process instance.The methods are implemented using the
 * MapStruct.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProcessInstanceMapper {

  /**
   * Method for converting camunda {@link HistoricProcessInstanceDto} entity to {@link
   * GetProcessInstanceResponse} entity.
   *
   * @param historicProcessInstanceDto camunda historic process instance.
   * @return converted process instance.
   */
  GetProcessInstanceResponse toGetProcessInstanceResponse(
      HistoricProcessInstanceDto historicProcessInstanceDto);

  /**
   * Method for converting list of camunda {@link HistoricProcessInstanceDto} entities to list of
   * {@link GetProcessInstanceResponse} entities.
   *
   * @param historicProcessInstanceDtos list of camunda historic process instances.
   * @return converted list of process instances.
   */
  @SuppressWarnings("unused")
  List<GetProcessInstanceResponse> toGetProcessInstanceResponses(
      List<HistoricProcessInstanceDto> historicProcessInstanceDtos);

  /**
   * Method for converting list of camunda {@link HistoricProcessInstanceDto} entities to list of
   * {@link HistoryProcessInstance} entities.
   *
   * @param historicProcessInstanceDtos list of camunda historic process instances.
   * @return converted list of finished process instances.
   */
  @IterableMapping(qualifiedByName = "toHistoryProcessInstance")
  List<HistoryProcessInstance> toHistoryProcessInstances(
      List<HistoricProcessInstanceDto> historicProcessInstanceDtos);

  /**
   * Method for converting camunda {@link HistoricProcessInstanceDto} entity to {@link
   * HistoryProcessInstance} entity.
   *
   * @param historicProcessInstanceDto camunda historic process instance.
   * @return converted finished process instance.
   */
  @Named("toHistoryProcessInstance")
  @Mapping(target = "status.code", source = "state")
  HistoryProcessInstance toHistoryProcessInstance(
      HistoricProcessInstanceDto historicProcessInstanceDto);

  /**
   * Method for converting camunda {@link ProcessInstanceDto} entity to {@link
   * StartProcessInstanceResponse} entity.
   *
   * @param processInstanceDto camunda process instance.
   * @return started process instance.
   */
  @Mapping(source = "definitionId", target = "processDefinitionId")
  StartProcessInstanceResponse toStartProcessInstanceResponse(
      ProcessInstanceDto processInstanceDto);
}
