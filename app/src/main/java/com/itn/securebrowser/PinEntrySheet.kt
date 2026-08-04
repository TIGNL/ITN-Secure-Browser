package com.itn.securebrowser

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PinEntrySheet(
    private val mode: String = MODE_VERIFY,
    private val subtitle: String = "",
    private val onPinVerified: () -> Unit = {}
) : BaseBottomSheet() {

    companion object {
        const val MODE_VERIFY = "verify"
        const val MODE_SET    = "set"
    }

    private lateinit var pinSubtitle: TextView
    private lateinit var pinError:    TextView
    private val dots = arrayOfNulls<View>(6)

    private val entered  = StringBuilder()
    private var firstPin = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_pin_entry, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            if (mode == MODE_SET) getString(R.string.pin_title_new)
            else getString(R.string.pin_title_enter)

        pinSubtitle = view.findViewById(R.id.pinSubtitle)
        pinError    = view.findViewById(R.id.pinError)

        pinSubtitle.text = subtitle

        dots[0] = view.findViewById(R.id.dot1); dots[1] = view.findViewById(R.id.dot2)
        dots[2] = view.findViewById(R.id.dot3); dots[3] = view.findViewById(R.id.dot4)
        dots[4] = view.findViewById(R.id.dot5); dots[5] = view.findViewById(R.id.dot6)

        wireKeys(view)
        refreshDots()
    }

    private fun wireKeys(root: View) {
        val keyIds = mapOf(
            R.id.key0 to '0', R.id.key1 to '1', R.id.key2 to '2',
            R.id.key3 to '3', R.id.key4 to '4', R.id.key5 to '5',
            R.id.key6 to '6', R.id.key7 to '7', R.id.key8 to '8',
            R.id.key9 to '9'
        )
        keyIds.forEach { (id, digit) ->
            root.findViewById<View>(id).setOnClickListener { pressDigit(digit) }
        }
        root.findViewById<View>(R.id.keyBack).setOnClickListener   { pressBackspace() }
        root.findViewById<View>(R.id.keyCancel).setOnClickListener { dismiss() }
    }

    private fun pressDigit(digit: Char) {
        if (entered.length >= 6) return
        entered.append(digit)
        refreshDots()
        hideError()
        if (entered.length == 6) handleComplete()
    }

    private fun pressBackspace() {
        if (entered.isEmpty()) return
        entered.deleteCharAt(entered.length - 1)
        refreshDots()
        hideError()
    }

    private fun handleComplete() {
        val pin = entered.toString()
        when (mode) {
            MODE_VERIFY -> verifyPin(pin)
            MODE_SET    -> handleSetFlow(pin)
        }
    }

    private fun verifyPin(pin: String) {
        if (PinManager.verify(requireContext(), pin)) {
            onPinVerified()
            dismiss()
        } else {
            showError(getString(R.string.pin_error_wrong))
            shakeAndClear()
        }
    }

    private fun handleSetFlow(pin: String) {
        if (firstPin.isEmpty()) {
            firstPin = pin
            entered.clear()
            refreshDots()
            view?.findViewById<TextView>(R.id.sheetHeaderTitle)?.text =
                getString(R.string.pin_title_confirm)
            pinSubtitle.text = ""
        } else {
            if (pin == firstPin) {
                PinManager.savePin(requireContext(), pin)
                onPinVerified()
                dismiss()
            } else {
                firstPin = ""
                showError(getString(R.string.pin_error_mismatch))
                view?.findViewById<TextView>(R.id.sheetHeaderTitle)?.text =
                    getString(R.string.pin_title_new)
                pinSubtitle.text = ""
                shakeAndClear()
            }
        }
    }

    private fun refreshDots() {
        val filled = 0xFFE94560.toInt()
        val empty  = 0xFF2A2A4A.toInt()
        dots.forEachIndexed { i, dot ->
            dot?.setBackgroundColor(if (i < entered.length) filled else empty)
        }
    }

    private fun showError(msg: String) {
        pinError.text       = msg
        pinError.visibility = View.VISIBLE
    }

    private fun hideError() {
        pinError.visibility = View.INVISIBLE
    }

    private fun shakeAndClear() {
        val shake = ObjectAnimator.ofFloat(
            dots[0]?.parent as? View ?: return,
            "translationX",
            0f, -18f, 18f, -14f, 14f, -8f, 8f, 0f
        ).apply { duration = 400 }
        shake.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                entered.clear()
                refreshDots()
            }
        })
        shake.start()
    }
}
