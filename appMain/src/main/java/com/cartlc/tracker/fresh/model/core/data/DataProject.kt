/*
 * Copyright 2017-2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

/**
 * Created by dug on 5/16/17.
 */
class DataProject {
    var id: Long = 0
    var serverId: Int = 0
    var subProject: String? = null
    var rootProject: String? = null
    var disabled: Boolean = false
    var isBootStrap: Boolean = false

    val dashName: String
        get() {
            if (subProject.isNullOrBlank()) {
                return rootProject ?: ""
            }
            return "$rootProject - $subProject"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataProject

        if (subProject != other.subProject) return false
        if (rootProject != other.rootProject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subProject?.hashCode() ?: 0
        result = 31 * result + (rootProject?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "DataProject(id=$id, serverId=$serverId, subProject=$subProject, rootProject=$rootProject, disabled=$disabled, isBootStrap=$isBootStrap)"
    }

}
