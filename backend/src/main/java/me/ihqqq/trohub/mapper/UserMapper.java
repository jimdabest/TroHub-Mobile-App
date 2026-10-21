package me.ihqqq.trohub.mapper;


import me.ihqqq.trohub.dto.request.UserCreationRequest;
import me.ihqqq.trohub.dto.request.UserUpdateRequest;
import me.ihqqq.trohub.dto.response.UserResponse;
import me.ihqqq.trohub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}