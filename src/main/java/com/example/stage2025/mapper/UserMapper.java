package com.example.stage2025.mapper;

import com.example.stage2025.dto.UserDto;
import com.example.stage2025.entity.Admin;
import com.example.stage2025.entity.Client;
import com.example.stage2025.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "firstName", expression = "java(user instanceof Client ? ((Client) user).getFirstName() : null)")
    @Mapping(target = "lastName", expression = "java(user instanceof Client ? ((Client) user).getLastName() : null)")
    @Mapping(target = "phone", expression = "java(user instanceof Client ? ((Client) user).getPhone() : null)")
    UserDto toDto(User user);

    // This mapping from DTO to Entity will need to be handled in the service layer
    // because it involves creating specific subclass instances (Admin, Client)
    // based on the role. MapStruct cannot dynamically create subclasses.
    // So, this method is intentionally left abstract or ignored for direct DTO-to-Entity mapping.
    // The service will handle the creation and population of the correct User subclass.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Password is set separately
    @Mapping(target = "role", ignore = true) // Handled by subclass creation
    @Mapping(target = "active", ignore = true) // Handled by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto userDto);
}
