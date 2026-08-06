package com.decisionhub.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsResponse {

    private Long totalUsers;

    private Long activeUsers;

    private Long inactiveUsers;

    private Long suspendedUsers;

}