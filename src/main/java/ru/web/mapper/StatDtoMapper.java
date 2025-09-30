package ru.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.model.Statistics;
import ru.web.dto.StatisticsDto;

@Mapper(componentModel = "spring")
public interface StatDtoMapper {
    @Mapping(source = "OS", target = "os")
    StatisticsDto toDto(Statistics statistics);
}
