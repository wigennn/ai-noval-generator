package com.viking.ai.novel.interfaces.mapper;

import com.viking.ai.novel.domain.model.Novel;
import com.viking.ai.novel.interfaces.dto.NovelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NovelMapper {
    NovelMapper INSTANCE = Mappers.getMapper(NovelMapper.class);
    
    NovelDTO toDTO(Novel novel);
    Novel toEntity(NovelDTO novelDTO);
}
