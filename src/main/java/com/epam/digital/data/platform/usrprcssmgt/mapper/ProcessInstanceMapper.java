package com.epam.digital.data.platform.usrprcssmgt.mapper;

import com.epam.digital.data.platform.bpms.api.dto.HistoryProcessInstanceDto;
import com.epam.digital.data.platform.bpms.api.dto.enums.HistoryProcessInstanceStatus;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrprcssmgt.enums.ProcessInstanceStatus;
import com.epam.digital.data.platform.usrprcssmgt.model.GetProcessInstanceResponse;
import com.epam.digital.data.platform.usrprcssmgt.model.HistoryUserProcessInstance;
import com.epam.digital.data.platform.usrprcssmgt.model.StartProcessInstanceResponse;
import java.util.List;
import java.util.Objects;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The interface represents a mapper for process instance entity. The interface contains a methods
 * for converting camunda historic process instance.The methods are implemented using the
 * MapStruct.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProcessInstanceMapper {

  @Autowired
  private MessageResolver messageResolver;

  /**
   * Method for converting camunda {@link HistoricProcessInstanceDto} entity to {@link
   * GetProcessInstanceResponse} entity.
   *
   * @param historicProcessInstanceDto camunda historic process instance.
   * @return converted process instance.
   */
  public abstract GetProcessInstanceResponse toGetProcessInstanceResponse(
      HistoryProcessInstanceDto historicProcessInstanceDto);

  /**
   * Method for converting list of camunda {@link HistoricProcessInstanceDto} entities to list of
   * {@link GetProcessInstanceResponse} entities.
   *
   * @param historicProcessInstanceDtos list of camunda historic process instances.
   * @return converted list of process instances.
   */
  @SuppressWarnings("unused")
  public abstract List<GetProcessInstanceResponse> toGetProcessInstanceResponses(
      List<HistoricProcessInstanceDto> historicProcessInstanceDtos);

  /**
   * Method for converting list of camunda {@link HistoricProcessInstanceDto} entities to list of
   * {@link HistoryUserProcessInstance} entities.
   *
   * @param historicProcessInstanceDtos list of camunda historic process instances.
   * @return converted list of finished process instances.
   */
  @IterableMapping(qualifiedByName = "toHistoryProcessInstance")
  public abstract List<HistoryUserProcessInstance> toHistoryProcessInstances(
      List<HistoryProcessInstanceDto> historicProcessInstanceDtos);

  /**
   * Method for converting camunda {@link HistoricProcessInstanceDto} entity to {@link
   * HistoryUserProcessInstance} entity.
   *
   * @param dto camunda historic process instance.
   * @return converted finished process instance.
   */
  @Named("toHistoryProcessInstance")
  @Mapping(target = "status.code", source = "state")
  @Mapping(target = "status.title", source = "dto")
  public abstract HistoryUserProcessInstance toHistoryProcessInstance(
      HistoryProcessInstanceDto dto);

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

  public String toStatusTitle(HistoryProcessInstanceDto processInstance) {
    var state = processInstance.getState();
    if (HistoryProcessInstanceStatus.EXTERNALLY_TERMINATED.equals(state)) {
      return messageResolver.getMessage(ProcessInstanceStatus.EXTERNALLY_TERMINATED);
    }

    if (!HistoryProcessInstanceStatus.COMPLETED.equals(state)) {
      return null;
    }

    return Objects.requireNonNullElseGet(processInstance.getProcessCompletionResult(),
        () -> messageResolver.getMessage(ProcessInstanceStatus.COMPLETED));
  }
}
