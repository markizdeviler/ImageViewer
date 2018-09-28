package uz.mukhammadakbar.image.viewer.listeners

interface OnDragChangeListener{

    fun onDragFinished()

    fun onDragChanged(difference: Float)

}