package com.phenikaa.tourService.client;

import com.phenikaa.tourService.dto.response.UserInfoResponse;
import com.phenikaa.filter.FeignTokenInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "api-gateway", contextId = "userServiceClient", path = "/user-service", configuration = FeignTokenInterceptor.class)
public interface UserServiceClient {
    @GetMapping("/api/users/profile/{userId}")
    UserInfoResponse getUserInfo(@PathVariable("userId") Integer userId);
}
