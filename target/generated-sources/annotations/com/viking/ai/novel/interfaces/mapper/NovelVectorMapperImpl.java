package com.viking.ai.novel.interfaces.mapper;

import com.viking.ai.novel.domain.model.NovelVector;
import com.viking.ai.novel.interfaces.dto.NovelVectorDTO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-28T22:11:04+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Azul Systems, Inc.)"
)
public class NovelVectorMapperImpl implements NovelVectorMapper {

    @Override
    public NovelVectorDTO toDTO(NovelVector novelVector) {
        if ( novelVector == null ) {
            return null;
        }

        NovelVectorDTO.NovelVectorDTOBuilder novelVectorDTO = NovelVectorDTO.builder();

        novelVectorDTO.id( novelVector.getId() );
        novelVectorDTO.novelId( novelVector.getNovelId() );
        novelVectorDTO.vectorId( novelVector.getVectorId() );
        novelVectorDTO.createdAt( novelVector.getCreatedAt() );

        return novelVectorDTO.build();
    }

    @Override
    public NovelVector toEntity(NovelVectorDTO dto) {
        if ( dto == null ) {
            return null;
        }

        NovelVector.NovelVectorBuilder novelVector = NovelVector.builder();

        novelVector.id( dto.getId() );
        novelVector.novelId( dto.getNovelId() );
        novelVector.vectorId( dto.getVectorId() );
        novelVector.createdAt( dto.getCreatedAt() );

        return novelVector.build();
    }
}
