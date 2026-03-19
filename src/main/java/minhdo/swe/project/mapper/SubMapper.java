package minhdo.swe.project.mapper;

import minhdo.swe.project.dto.response.SubDetailResponse;
import minhdo.swe.project.dto.response.SubInfo;
import minhdo.swe.project.dto.response.SubResponse;
import minhdo.swe.project.entity.Sub;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface SubMapper {

    @Mapping(source = "sub.id", target = "id")
    @Mapping(source = "sub.name", target = "name")
    @Mapping(source = "sub.description", target = "description")
    @Mapping(source = "sub.iconUrl", target = "iconUrl")
    @Mapping(source = "sub.createdBy.id", target = "createdBy")
    @Mapping(source = "memberCount", target = "memberCount")
    @Mapping(source = "sub.createdAt", target = "createdAt")
    SubResponse toSubResponse(Sub sub, long memberCount);

    @Mapping(source = "sub.id", target = "id")
    @Mapping(source = "sub.name", target = "name")
    @Mapping(source = "sub.description", target = "description")
    @Mapping(source = "sub.iconUrl", target = "iconUrl")
    @Mapping(source = "sub.createdBy", target = "createdBy")
    @Mapping(source = "memberCount", target = "memberCount")
    @Mapping(source = "isMember", target = "member")
    @Mapping(source = "sub.createdAt", target = "createdAt")
    SubDetailResponse toDetailResponse(Sub sub, long memberCount, boolean isMember);

    SubInfo toSubInfo(Sub sub);

//    default SubDetailResponse.CreatorInfo mapCreator(minhdo.swe.project.entity.User user) {
//        if (user == null)
//            return null;
//        return new SubDetailResponse.CreatorInfo(user.getId(), user.getUsername());
//    }
}
