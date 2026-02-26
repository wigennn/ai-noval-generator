package com.viking.ai.novel.interfaces.mapper;

import com.viking.ai.novel.domain.model.NovelVector;
import com.viking.ai.novel.interfaces.dto.NovelVectorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NovelVectorMapper {
    NovelVectorMapper INSTANCE = Mappers.getMapper(NovelVectorMapper.class);

    NovelVectorDTO toDTO(NovelVector novelVector);
    NovelVector toEntity(NovelVectorDTO dto);
}
