package com.skillcraft.domain.mapper;

import com.skillcraft.domain.User;
import com.skillcraft.domain.Course;
import com.skillcraft.domain.dto.CourseDto;
import com.skillcraft.domain.dto.CreateCourseRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseMapper {

	@Mapping(target = "teacherId", source = "teacher.id")
	@Mapping(target = "teacherName", source = "teacher", qualifiedByName = "concatTeacherName")
	CourseDto toDto(Course entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "teacher", ignore = true) // Учитель устанавливается отдельно в Service по teacherId
	@Mapping(target = "isArchived", constant = "false")
	Course toEntity(CreateCourseRequest request);

	@Named("concatTeacherName")
	default String concatTeacherName(User teacher) {
		if (teacher == null) {
			return null;
		}

		return teacher.getFirstName() + " " + teacher.getLastName();
	}
}