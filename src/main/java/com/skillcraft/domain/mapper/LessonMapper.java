package com.skillcraft.domain.mapper;

import com.skillcraft.domain.Lesson;
import com.skillcraft.domain.dto.LessonDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LessonMapper {

	@Mapping(target = "courseId", source = "course.id")
	@Mapping(target = "courseTitle", source = "course.title")
	LessonDto toDto(Lesson entity);
}