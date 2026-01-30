package com.homebanking.port.in.profile;

import com.homebanking.application.dto.profile.request.GetUserProfileInputRequest;
import com.homebanking.application.dto.profile.response.UserProfileOutputResponse;

public interface GetUserProfileInputPort {

    UserProfileOutputResponse getUserProfile(GetUserProfileInputRequest request);
}