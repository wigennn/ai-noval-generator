package com.viking.ai.novel.interfaces.mapper;

import com.viking.ai.novel.domain.model.Chapter;
import com.viking.ai.novel.interfaces.dto.ChapterDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ChapterMapper {
    ChapterMapper INSTANCE = Mappers.getMapper(ChapterMapper.class);
    
    ChapterDTO toDTO(Chapter chapter);
    Chapter toEntity(ChapterDTO chapterDTO);
}
