/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

/**
 * Created by dug on 5/16/17.
 */

class DataProject {
    var id: Long = 0
    var serverId: Int = 0
    var name: String? = null
    var rootProject: String? = null
    var disabled: Boolean = false
    var isBootStrap: Boolean = false

    val subProject: String?
        get() = name

    val dashName: String
        get() = "${rootProject ?: ""} - ${name ?: ""}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataProject

        if (name != other.name) return false
        if (rootProject != other.rootProject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (rootProject?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "DataProject(id=$id, serverId=$serverId, name=$name, rootProject=$rootProject, disabled=$disabled, isBootStrap=$isBootStrap)"
    }

}
