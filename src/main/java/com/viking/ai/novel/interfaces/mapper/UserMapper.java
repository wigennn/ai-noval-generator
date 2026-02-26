package com.viking.ai.novel.interfaces.mapper;

import com.viking.ai.novel.domain.model.User;
import com.viking.ai.novel.interfaces.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);
}
