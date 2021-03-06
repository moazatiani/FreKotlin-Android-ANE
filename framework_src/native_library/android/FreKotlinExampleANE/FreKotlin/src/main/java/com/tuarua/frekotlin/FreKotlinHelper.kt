/*
 *  Copyright 2017 Tua Rua Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.tuarua.frekotlin

import android.util.Log
import com.adobe.fre.FREASErrorException
import com.adobe.fre.FREArray
import com.adobe.fre.FREInvalidObjectException
import com.adobe.fre.FRENoSuchNameException
import com.adobe.fre.FREObject
import com.adobe.fre.FRETypeMismatchException
import com.adobe.fre.FREWrongThreadException
import com.tuarua.frekotlin.display.Bitmap
import com.tuarua.frekotlin.display.FreBitmapDataKotlin
import com.tuarua.frekotlin.geom.FrePointKotlin
import com.tuarua.frekotlin.geom.FreRectangleKotlin
import com.tuarua.frekotlin.geom.Point
import com.tuarua.frekotlin.geom.Rect
import java.util.*


/**
 * @suppress
 */
object FreKotlinHelper {
    internal var TAG = "com.tuarua.FreKotlinHelper"

    fun getAsString(rawValue: FREObject?): String? {
        try {
            return rawValue?.asString
        } catch (e: Exception) {
        }
        return null
    }

    fun getAsDouble(rawValue: FREObject?): Double? {
        try {
            return rawValue?.asDouble
        } catch (e: Exception) {
        }
        return null
    }

    fun getAsDate(rawValue: FREObject?): Date? {
        try {
            val l = rawValue?.getProperty("time")?.asDouble?.toLong() ?: return null
            return Date(l)
        } catch (e: Exception) {
        }
        return null
    }

    @Throws(FreException::class, FREASErrorException::class, FREInvalidObjectException::class,
            FREWrongThreadException::class, FRENoSuchNameException::class, FRETypeMismatchException::class)
    private fun getAsDictionary(rawValue: FREObject): Map<String, Any> {
        val ret = HashMap<String, Any>()
        try {
            val aneUtils = FREObject.newObject("com.tuarua.fre.ANEUtils", null)
            val args = arrayOfNulls<FREObject>(1)
            args[0] = rawValue
            val classProps1 = aneUtils.callMethod("getClassProps", args)

            if (classProps1 is FREArray) {
                var i = 0
                val numProps = classProps1.length.toInt()
                while (i < numProps) {
                    val elem = classProps1.getObjectAt(i.toLong())
                    if (elem != null) {
                        val propName = elem.getProperty("name").asString
                        if (propName != null && !propName.isEmpty()) {
                            val propval: FREObject? = rawValue.getProperty(propName)
                            if (propval is FREObject) {
                                FreObjectKotlin(propval).value?.let { ret.put(propName, it) }
                            }
                        }
                    }
                    i++
                }
            }
        } catch (e: Exception) {
            throw FreException(e)
        }
        return ret
    }

    fun getAsInt(rawValue: FREObject?): Int? {
        try {
            return rawValue?.asInt
        } catch (e: Exception) {
        }
        return null
    }

    fun getAsBoolean(rawValue: FREObject?): Boolean? {
        try {
            return rawValue?.asBool
        } catch (e: Exception) {
        }
        return null
    }

    fun getAsObject(rawValue: FREObject?): Any? {
        rawValue ?: return null
        try {
            return when (getType(rawValue)) {
                FreObjectTypeKotlin.STRING -> getAsString(rawValue)
                FreObjectTypeKotlin.NUMBER -> getAsDouble(rawValue)
                FreObjectTypeKotlin.BYTEARRAY -> ByteArray(rawValue)
                FreObjectTypeKotlin.ARRAY, FreObjectTypeKotlin.VECTOR -> getAsArrayList(rawValue as FREArray)
                FreObjectTypeKotlin.BITMAPDATA -> Bitmap(rawValue, true)
                FreObjectTypeKotlin.BOOLEAN -> getAsBoolean(rawValue)
                FreObjectTypeKotlin.NULL -> null
                FreObjectTypeKotlin.INT -> getAsInt(rawValue)
                FreObjectTypeKotlin.CLASS, FreObjectTypeKotlin.OBJECT -> getAsDictionary(rawValue)
                FreObjectTypeKotlin.RECTANGLE -> Rect(rawValue)
                FreObjectTypeKotlin.POINT -> Point(rawValue)
                FreObjectTypeKotlin.DATE -> getAsDate(rawValue)
            }
        } catch (e: FREInvalidObjectException) {
            Log.e(TAG, "getAsObject Error: " + e.message)
        } catch (e: FREASErrorException) {
            Log.e(TAG, "getAsObject Error: " + e.message)
        } catch (e: FRENoSuchNameException) {
            Log.e(TAG, "getAsObject Error: " + e.message)
        } catch (e: FRETypeMismatchException) {
            Log.e(TAG, "getAsObject Error: " + e.message)
        } catch (e: FREWrongThreadException) {
            Log.e(TAG, "getAsObject Error: " + e.message)
        }

        return null
    }

    private fun getAsArrayList(rawValue: FREArray): ArrayList<Any> {
        val al = ArrayList<Any>()
        try {
            val len = rawValue.length
            for (i in 0 until len) {
                getAsObject(rawValue.getObjectAt(i))?.let { al.add(it) }
            }
        } catch (e: FREWrongThreadException) {
            Log.e(TAG, e.message)
        } catch (e: FREInvalidObjectException) {
            Log.e(TAG, e.message)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }

        return al
    }

    fun getType(rawValue: FREObject?): FreObjectTypeKotlin {
        try {
            val aneUtils = FREObject.newObject("com.tuarua.fre.ANEUtils", null)
            val args = arrayOfNulls<FREObject>(1)
            args[0] = rawValue
            val classType = aneUtils.callMethod("getClassType", args)
            val type = getAsString(classType)?.toLowerCase() ?: return FreObjectTypeKotlin.NULL
            when {
                type.contains("__as3__.vec::vector") -> return FreObjectTypeKotlin.VECTOR
                else -> when (type) {
                    "object" -> return FreObjectTypeKotlin.OBJECT
                    "number" -> return FreObjectTypeKotlin.NUMBER
                    "string" -> return FreObjectTypeKotlin.STRING
                    "bytearray" -> return FreObjectTypeKotlin.BYTEARRAY
                    "array" -> return FreObjectTypeKotlin.ARRAY
                    "vector" -> return FreObjectTypeKotlin.VECTOR
                    "bitmapdata" -> return FreObjectTypeKotlin.BITMAPDATA
                    "boolean" -> return FreObjectTypeKotlin.BOOLEAN
                    "null" -> return FreObjectTypeKotlin.NULL
                    "int" -> return FreObjectTypeKotlin.INT
                    "flash.geom::rectangle" -> return FreObjectTypeKotlin.RECTANGLE
                    "flash.geom::point" -> return FreObjectTypeKotlin.POINT
                    "date" -> return FreObjectTypeKotlin.DATE
                    else -> return FreObjectTypeKotlin.CLASS
                }
            }
        } catch (e: FRETypeMismatchException) {
            return FreObjectTypeKotlin.NULL
        } catch (e: FREWrongThreadException) {
            return FreObjectTypeKotlin.NULL
        } catch (e: FRENoSuchNameException) {
            return FreObjectTypeKotlin.NULL
        } catch (e: FREASErrorException) {
            return FreObjectTypeKotlin.NULL
        } catch (e: FREInvalidObjectException) {
            return FreObjectTypeKotlin.NULL
        }
    }

    @Throws(FreException::class)
    fun getProperty(rawValue: FREObject, name: String): FREObject? {
        val ret: FREObject?
        try {
            ret = rawValue.getProperty(name)
        } catch (e: Exception) {
            throw FreException(e)
        }
        return ret
    }

    @Throws(FreException::class)
    fun setProperty(rawValue: FREObject, name: String, prop: FREObject?) {
        try {
            rawValue.setProperty(name, prop)
        } catch (e: Exception) {
            throw FreException(e)
        }
    }

    @Throws(FreException::class)
    fun call(rawValue: FREObject, method: String, args: Array<out Any>): FREObject? {
        val argsArray = arrayOfNulls<FREObject>(args.size)
        for ((i, item) in args.withIndex()) {
            argsArray[i] = FreObjectKotlin(item).rawValue
        }
        val ret: FREObject?
        try {
            ret = rawValue.callMethod(method, argsArray)
        } catch (e: Exception) {
            throw FreException(e)
        }
        return ret
    }

    @Throws(FreException::class)
    fun call(rawValue: FREObject, name: String): FREObject? {
        val argsArray = arrayOfNulls<FREObject>(0)
        val ret: FREObject?
        try {
            ret = rawValue.callMethod(name, argsArray)
        } catch (e: Exception) {
            throw FreException(e)
        }
        return ret
    }

}
