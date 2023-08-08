package com.acme.authorization.deployment;

import com.acme.authorization.provider.ErrorMapper;
import com.acme.authorization.security.AuthorizationFilter;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.ContainerRequestFilterBuildItem;

class AuthorizationProcessor {

    private static final String FEATURE = "authorization";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem createErrorMapper() {
        return new AdditionalBeanBuildItem(ErrorMapper.class);
    }

    @BuildStep
    public ContainerRequestFilterBuildItem createContainerRequestFilterBuildItem() {
        return new ContainerRequestFilterBuildItem.Builder(AuthorizationFilter.class.getName())
                .setNonBlockingRequired(true)
                .setPreMatching(true)
                .setRegisterAsBean(true)
                .setPriority(0)
                .build();
    }
}
