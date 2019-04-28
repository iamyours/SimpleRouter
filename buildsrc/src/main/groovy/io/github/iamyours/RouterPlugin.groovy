package io.github.iamyours

import io.github.iamyours.transform.RouterTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.android.registerTransform(new RouterTransform(project))
    }
}