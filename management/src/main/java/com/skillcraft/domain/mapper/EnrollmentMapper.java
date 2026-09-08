package com.skillcraft.domain.mapper;

import com.skillcraft.domain.Enrollment;
import com.skillcraft.domain.Student;
import com.skillcraft.domain.dto.EnrollmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EnrollmentMapper {

	@Mapping(target = "studentId", source = "student.id")
	@Mapping(target = "studentName", source = "student", qualifiedByName = "concatStudentName")
	@Mapping(target = "courseId", source = "course.id")
	@Mapping(target = "courseTitle", source = "course.title")
	EnrollmentDto toDto(Enrollment entity);

	@Named("concatStudentName")
	default String concatStudentName(Student student) {
		if (student == null || student.getUser() == null) return null;
		return student.getUser().getFirstName() + " " + student.getUser().getLastName();
	}
}
