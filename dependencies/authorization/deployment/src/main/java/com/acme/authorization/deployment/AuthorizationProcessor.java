package com.acme.authorization.deployment;

import com.acme.authorization.provider.ErrorMapper;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

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
}
