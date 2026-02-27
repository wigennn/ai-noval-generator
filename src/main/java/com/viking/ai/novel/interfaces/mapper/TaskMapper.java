package com.viking.ai.novel.interfaces.mapper;

import com.viking.ai.novel.domain.model.Task;
import com.viking.ai.novel.interfaces.dto.TaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskMapper {
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);
    
    TaskDTO toDTO(Task task);
    Task toEntity(TaskDTO taskDTO);
}
