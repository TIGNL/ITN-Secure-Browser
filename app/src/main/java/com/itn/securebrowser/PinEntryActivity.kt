package com.itn.securebrowser

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

/**
 * شاشة إدخال الرمز السري — تُستخدم في وضعين:
 *   MODE_VERIFY  → التحقق من رمز موجود (لفتح الإعدادات)
 *   MODE_SET     → تعيين رمز جديد (من داخل إعدادات الرمز)
 */
class PinEntryActivity : BaseActivity() {

    // ── Constants ──────────────────────────────────────────────────────────
    companion object {
        const val MODE_VERIFY = "verify"
        const val MODE_SET    = "set"

        private const val EXTRA_MODE     = "extra_mode"
        private const val EXTRA_SUBTITLE = "extra_subtitle"

        /** نتيجة ناجحة يرجعها الـ Activity */
        const val RESULT_PIN_OK = RESULT_FIRST_USER + 1

        fun intentVerify(ctx: Context, subtitle: String = "لفتح الإعدادات"): Intent =
            Intent(ctx, PinEntryActivity::class.java)
                .putExtra(EXTRA_MODE, MODE_VERIFY)
                .putExtra(EXTRA_SUBTITLE, subtitle)

        fun intentSet(ctx: Context): Intent =
            Intent(ctx, PinEntryActivity::class.java)
                .putExtra(EXTRA_MODE, MODE_SET)
                .putExtra(EXTRA_SUBTITLE, "اختر رمزاً سرياً جديداً")
    }

    // ── Views ──────────────────────────────────────────────────────────────
    private lateinit var pinTitle:    TextView
    private lateinit var pinSubtitle: TextView
    private lateinit var pinError:    TextView
    private val dots = arrayOfNulls<View>(6)

    // ── State ──────────────────────────────────────────────────────────────
    private val entered   = StringBuilder()
    private var mode      = MODE_VERIFY
    private var firstPin  = ""   // في MODE_SET: الإدخال الأول للتأكيد

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_entry)

        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_VERIFY

        pinTitle    = findViewById(R.id.pinTitle)
        pinSubtitle = findViewById(R.id.pinSubtitle)
        pinError    = findViewById(R.id.pinError)

        dots[0] = findViewById(R.id.dot1); dots[1] = findViewById(R.id.dot2)
        dots[2] = findViewById(R.id.dot3); dots[3] = findViewById(R.id.dot4)
        dots[4] = findViewById(R.id.dot5); dots[5] = findViewById(R.id.dot6)

        pinTitle.text    = if (mode == MODE_SET) "أدخل رمزاً جديداً" else "أدخل الرمز السري"
        pinSubtitle.text = intent.getStringExtra(EXTRA_SUBTITLE) ?: ""

        wireKeys()
        refreshDots()
    }

    // ── Key wiring ─────────────────────────────────────────────────────────

    private fun wireKeys() {
        val keyIds = mapOf(
            R.id.key0 to '0', R.id.key1 to '1', R.id.key2 to '2',
            R.id.key3 to '3', R.id.key4 to '4', R.id.key5 to '5',
            R.id.key6 to '6', R.id.key7 to '7', R.id.key8 to '8',
            R.id.key9 to '9'
        )
        keyIds.forEach { (id, digit) ->
            findViewById<View>(id).setOnClickListener { pressDigit(digit) }
        }
        findViewById<View>(R.id.keyBack).setOnClickListener   { pressBackspace() }
        findViewById<View>(R.id.keyCancel).setOnClickListener { finish() }
    }

    // ── Input logic ────────────────────────────────────────────────────────

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

    // ── MODE_VERIFY ────────────────────────────────────────────────────────

    private fun verifyPin(pin: String) {
        if (PinManager.verify(this, pin)) {
            setResult(RESULT_PIN_OK)
            finish()
        } else {
            showError("رمز خاطئ، حاول مجدداً")
            shakeAndClear()
        }
    }

    // ── MODE_SET ───────────────────────────────────────────────────────────

    private fun handleSetFlow(pin: String) {
        if (firstPin.isEmpty()) {
            // إدخال أول — انتظر التأكيد
            firstPin = pin
            entered.clear()
            refreshDots()
            pinTitle.text    = "أعد إدخال الرمز للتأكيد"
            pinSubtitle.text = ""
        } else {
            // إدخال ثانٍ — تحقق من التطابق
            if (pin == firstPin) {
                PinManager.savePin(this, pin)
                setResult(RESULT_PIN_OK)
                finish()
            } else {
                firstPin = ""
                showError("الرمزان غير متطابقين، ابدأ من جديد")
                pinTitle.text    = "أدخل رمزاً جديداً"
                pinSubtitle.text = ""
                shakeAndClear()
            }
        }
    }

    // ── UI helpers ─────────────────────────────────────────────────────────

    private fun refreshDots() {
        val filled  = 0xFFE94560.toInt()
        val empty   = 0xFF2A2A4A.toInt()
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
