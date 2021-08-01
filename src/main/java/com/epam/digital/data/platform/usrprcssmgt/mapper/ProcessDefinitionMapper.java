package com.epam.digital.data.platform.usrprcssmgt.mapper;

import com.epam.digital.data.platform.usrprcssmgt.model.UserProcessDefinitionDto;
import java.util.List;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * The interface represents a mapper for process definition entity. The interface contains a methods
 * for converting camunda process definition instance.The methods are implemented using the
 * MapStruct.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProcessDefinitionMapper {

  /**
   * Method for converting camunda {@link ProcessDefinitionDto} entity to {@link
   * UserProcessDefinitionDto} entity
   *
   * @param processDefinitionDto camunda process definition entity
   * @return user process definition
   */
  UserProcessDefinitionDto toUserProcessDefinitionDto(ProcessDefinitionDto processDefinitionDto);

  /**
   * Method for converting list of camunda {@link ProcessDefinitionDto} entities to list of {@link
   * UserProcessDefinitionDto} entities
   *
   * @param processDefinitionDto camunda process definition entity
   * @return user process definition
   */
  List<UserProcessDefinitionDto> toUserProcessDefinitionDtos(List<ProcessDefinitionDto> processDefinitionDto);
}
