package uz.mukhammadakbar.image.viewer.listeners

import uz.mukhammadakbar.image.viewer.utils.Coordination

interface OnCoordinationChangeListener {

    fun coordinationChanged(coordination: Coordination, duration: Long)

}