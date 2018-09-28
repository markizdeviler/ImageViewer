package uz.mukhammadakbar.image.viewer.utils

object Converter {

    fun findNearest(differX: Double, differY: Double): Float{
        return Math.sqrt(Math.pow(differX, 2.0) + Math.pow(differY, 2.0)).toFloat()
    }

}