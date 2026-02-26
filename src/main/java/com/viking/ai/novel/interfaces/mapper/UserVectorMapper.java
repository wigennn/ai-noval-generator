package com.viking.ai.novel.interfaces.mapper;

import com.viking.ai.novel.domain.model.UserVector;
import com.viking.ai.novel.interfaces.dto.UserVectorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserVectorMapper {
    UserVectorMapper INSTANCE = Mappers.getMapper(UserVectorMapper.class);

    UserVectorDTO toDTO(UserVector userVector);
    UserVector toEntity(UserVectorDTO dto);
}
