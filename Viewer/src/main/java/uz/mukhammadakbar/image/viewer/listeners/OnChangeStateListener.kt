package uz.mukhammadakbar.image.viewer.listeners

import uz.mukhammadakbar.image.viewer.utils.State

interface OnChangeStateListener {
    fun onSetState(newState: State)
}
