package com.rkbk60.quickflick

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.preference.PreferenceManager
import android.util.Log

/**
 * Created by s-iwamoto on 9/29/17.
 */

class KeyboardManager(ime: InputMethodService, private val keyboardView: KeyboardView) {

    val keyboard = Keyboard(ime, R.xml.keyboard)

    enum class Adjustment { NONE, LEFT, RIGHT }
    private var adjustment = Adjustment.NONE
    set(value) {
        field = value
        setAdjustmentSettings(value)
    }

    private val adjustmentCacheName = "cache_last_adjustment_is_right"

    companion object {
        var INDEX_INDICATOR = 0
            private set
        private var INDEX_ARROW = 3
        private var INDEX_META_ALT = 9
        private var INDEX_CTRL_ALT = 16
    }

    private lateinit var keyArrow: Keyboard.Key
    private lateinit var keyMetaAlt: Keyboard.Key
    private lateinit var keyCtrlAlt: Keyboard.Key

    init {
        setAdjustmentFromSettings()
        changeKeyWidth(true)
    }

    fun changeKeyAdjustment() {
        adjustment = when (adjustment) {
            Adjustment.NONE  -> return
            Adjustment.LEFT  -> Adjustment.RIGHT
            Adjustment.RIGHT -> Adjustment.LEFT
        }
        changeKeyWidth()
        keyboardView.invalidateAllKeys()
    }

    fun updateArrowKeyFace(state: ArrowKey.State) {
        keyArrow.label = when (state) {
            ArrowKey.State.DEFAULT -> "arw"
            ArrowKey.State.REPEATING -> "ARW"
            ArrowKey.State.PAGE_MOVE -> "pmv"
        }
        keyboardView.invalidateKey(INDEX_ARROW)
    }

    fun updateMetaAltKeyFace(enableMeta: Boolean, enableAlt: Boolean) {
        val faceCode = (if (enableMeta) 0b10 else 0).or(if (enableAlt) 0b01 else 0)
        keyMetaAlt.label = when (faceCode) {
            0b11 -> "M/A"
            0b10 -> "M/a"
            0b01 -> "m/A"
            else -> "m/a"
        }
        keyboardView.invalidateKey(INDEX_META_ALT)
    }

    fun updateCtrlAltKeyFace(enableCtrl: Boolean, enableAlt: Boolean) {
        val faceCode = (if (enableCtrl) 0b10 else 0).or(if (enableAlt) 0b01 else 0)
        keyCtrlAlt.label = when (faceCode) {
            0b11 -> "C/A"
            0b10 -> "C/a"
            0b01 -> "c/A"
            else -> "c/a"
        }
        keyboardView.invalidateKey(INDEX_CTRL_ALT)
    }

    private fun changeKeyWidth(runKeyRecorder: Boolean = false) {
        var x = 0
        var sum = 0
        val screenWidth = keyboard.keys[0]!!.width
        // 1-1. set key width
        // 1-2. record key index
        // 1-3. remove function keys label
        keyboard.keys.forEachIndexed { index, key ->
            val code = key.codes[0]
            val width = when (adjustment) {
                Adjustment.NONE  -> getWidthInNone(screenWidth, code)
                Adjustment.LEFT  -> getWidthInLeft(screenWidth, code)
                Adjustment.RIGHT -> getWidthInRight(screenWidth, code)
            }
            key.width = width
            key.x = x
            if (code in KeyNumbers.LIST_LAST_OF_ROW) x = 0 else x += width
            if (code != KeyNumbers.INDICATOR) sum += width

            if (runKeyRecorder) when (code) {
                KeyNumbers.INDICATOR -> INDEX_INDICATOR = index
                KeyNumbers.ARROW -> {
                    INDEX_ARROW = index
                    keyArrow = key
                }
                KeyNumbers.META_ALT -> {
                    INDEX_META_ALT = index
                    keyMetaAlt = key
                }
                KeyNumbers.CTRL_ALT -> {
                    INDEX_CTRL_ALT = index
                    keyCtrlAlt = key
                }
            }

            if (code in KeyNumbers.LIST_FUNCTIONS) key.label = ""
        }
        // 2-1. fix width
        // 2-2. set function keys label
        val listFixable = when (adjustment) {
            Adjustment.NONE  -> KeyNumbers.LIST_NEXT_TO_FUNCTIONS
            Adjustment.LEFT  -> KeyNumbers.LIST_NEXT_TO_LEFT_FUNCTIONS
            Adjustment.RIGHT -> KeyNumbers.LIST_NEXT_TO_RIGHT_FUNCTIONS
        }
        val rowNum = 4
        val correction = ((rowNum * screenWidth) - sum) / listFixable.count()
        keyboard.keys.forEach {
            val code = it.codes[0]
            if (code in listFixable) it.width += correction
            it.x = x
            if (code in KeyNumbers.LIST_LAST_OF_ROW) x = 0 else x += it.width
            setFunctionKeyFace(it)
        }
    }

    private fun getWidthInNone(screenWidth: Int, code: Int): Int = when (code) {
        KeyNumbers.INDICATOR -> screenWidth
        in KeyNumbers.LIST_FUNCTIONS -> (0.08 * screenWidth).toInt()
        in KeyNumbers.LIST_NEXT_TO_FUNCTIONS -> (0.18 * screenWidth).toInt()
        in KeyNumbers.LIST_VALID -> (0.16 * screenWidth).toInt()
        else -> 0
    }

    private fun getWidthInLeft(screenWidth: Int, code: Int): Int = when (code) {
        KeyNumbers.INDICATOR -> screenWidth
        in KeyNumbers.LIST_LEFT_FUNCTIONS -> 0
        in KeyNumbers.LIST_RIGHT_FUNCTIONS -> (0.15 * screenWidth).toInt()
        in KeyNumbers.LIST_VALID -> (0.17 * screenWidth).toInt()
        else -> 0
    }

    private fun getWidthInRight(screenWidth: Int, code: Int): Int = when (code) {
        KeyNumbers.INDICATOR -> screenWidth
        in KeyNumbers.LIST_LEFT_FUNCTIONS -> (0.15 * screenWidth).toInt()
        in KeyNumbers.LIST_RIGHT_FUNCTIONS -> 0
        in KeyNumbers.LIST_VALID -> (0.17 * screenWidth).toInt()
        else -> 0
    }

    private fun setAdjustmentFromSettings() {
        val pref = PreferenceManager.getDefaultSharedPreferences(keyboardView.context)
        val isEnable = pref.getBoolean(
                        keyboardView.resources.getString(R.string.preferences_enable_adjustment),
                        keyboardView.resources.getBoolean(R.bool.preferences_enable_adjustment_default))
        if (!isEnable) {
            adjustment = Adjustment.NONE
            return
        }

        val isRight = pref.getBoolean(adjustmentCacheName, true)
        adjustment = if (isRight) Adjustment.RIGHT else Adjustment.LEFT
    }

    private fun setAdjustmentSettings(adjustment: Adjustment) {
        if (adjustment == Adjustment.NONE) return
        val isRight = adjustment == Adjustment.RIGHT
        PreferenceManager.getDefaultSharedPreferences(keyboardView.context).edit()
                .putBoolean(adjustmentCacheName, isRight)
                .commit()
    }

    private fun setFunctionKeyFace(key: Keyboard.Key) {
        val leftList  = KeyNumbers.LIST_LEFT_FUNCTIONS
        val rightList = KeyNumbers.LIST_RIGHT_FUNCTIONS
        key.label = when (key.codes[0]) {
            in leftList  -> ">  "
            in rightList -> "  <"
            else -> return
        }
    }

}