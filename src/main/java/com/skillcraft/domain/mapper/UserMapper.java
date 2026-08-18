package com.skillcraft.domain.mapper;

import com.skillcraft.domain.User;
import com.skillcraft.domain.dto.CreateUserRequest;
import com.skillcraft.domain.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

	UserDto toDto(User entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", source = "password")
	@Mapping(target = "isActive", constant = "true")
	@Mapping(target = "createdAt", ignore = true)
	User toEntity(CreateUserRequest request);
}
