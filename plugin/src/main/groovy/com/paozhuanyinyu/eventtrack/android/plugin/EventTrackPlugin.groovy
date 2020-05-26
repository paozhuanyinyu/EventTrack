package com.paozhuanyinyu.eventtrack.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class EventTrackPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        AppExtension appExtension = project.extensions.findByType(AppExtension.class)
        appExtension.registerTransform(new EventTrackTransform(project))
    }
}