package com.wanted.momocity.user.application.port;

import com.wanted.momocity.user.domain.model.BuildingInfo;

import java.util.List;

public interface GetUserBuildingsPort {
    List<BuildingInfo> getUserBuildings(Long userId);


}
