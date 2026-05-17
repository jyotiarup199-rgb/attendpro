package com.dec.attendpro.utils

object FaceDataHolder {
    var centerFace: ByteArray? = null
    var rightFace: ByteArray? = null
    var leftFace: ByteArray? = null

    fun clear() {
        centerFace = null
        rightFace = null
        leftFace = null
    }
}
