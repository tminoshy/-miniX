package minhdo.swe.project.mapper;

import minhdo.swe.project.dto.response.UserProfileDetailResponse;
import minhdo.swe.project.dto.response.UserProfileResponse;
import minhdo.swe.project.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toProfileResponse(User user);

    UserProfileDetailResponse toProfileDetailResponse(User user);
}
