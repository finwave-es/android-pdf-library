package com.talentomobile.pdf.feature.pdf.scaleimage

import java.lang.reflect.InvocationTargetException

interface DecoderFactory<T> {
    /**
     * Produce a new instance of a decoder with type [T].
     *
     * @return a new instance of your decoder.
     * @throws IllegalAccessException    if the factory class cannot be instantiated.
     * @throws InstantiationException    if the factory class cannot be instantiated.
     * @throws NoSuchMethodException     if the factory class cannot be instantiated.
     * @throws InvocationTargetException if the factory class cannot be instantiated.
     */
    @Throws(
        IllegalAccessException::class,
        InstantiationException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class
    )
    fun make(): T
}