package lv.romstr.thingstest

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.android.things.pio.Pwm

private val TAG = MainActivity::class.java.simpleName

class MainActivity : Activity() {

    private lateinit var mPwm: Pwm
    private lateinit var mDatabase: FirebaseDatabase
    private val buttons = mutableListOf<FireButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        mDatabase = FirebaseDatabase.getInstance()
        val service = PeripheralManagerService()

        buttons.add(FireButton("GPIO_33", "Awesome", service))
        buttons.add(FireButton("GPIO_10", "Neutral", service))
        buttons.add(FireButton("GPIO_35", "Bad", service))

        mPwm = service.openPwm("PWM1")
        configurePwm(mPwm)

        handleSeekBar()

        Log.d(TAG, "Available GPIO: ${service.getGpioList()}")
        Log.d(TAG, "Available PWM: ${service.pwmList}")
    }

    private fun handleSeekBar() {
        findViewById<SeekBar>(R.id.seekBar)
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        Log.d(TAG, "Progress is $p1")
                        mPwm.setPwmDutyCycle(p1.toDouble())
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }

                })
    }

    override fun onDestroy() {
        super.onDestroy()
        buttons.forEach { button -> button.close() }
        mPwm.close()
    }

    fun configurePwm(pwm: Pwm) {
        pwm.setPwmFrequencyHz(100.0);
        pwm.setPwmDutyCycle(100.0);

        // Enable the PWM signal
        pwm.setEnabled(true);
    }
}
