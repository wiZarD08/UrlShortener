package ru.web.mapper;

import org.mapstruct.Mapper;
import ru.model.UtmTag;
import ru.web.dto.UtmTagDto;

@Mapper(componentModel = "spring")
public interface UtmTagDtoMapper {
    UtmTagDto toDto(UtmTag utmTag);
}
