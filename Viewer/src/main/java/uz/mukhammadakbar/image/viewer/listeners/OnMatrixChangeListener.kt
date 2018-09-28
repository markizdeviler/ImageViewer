package uz.mukhammadakbar.image.viewer.listeners

interface OnMatrixChangeListener {

    fun postTranslate(transX: Int, transY: Int)

    fun getValues(array: FloatArray?)

    fun onSetImageMatrix()

}