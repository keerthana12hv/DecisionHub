package com.decisionhub.dto.response.community;

public record CategoryResponse(

    Long id,

    String name,

    String slug,

    Boolean isActive
) {}