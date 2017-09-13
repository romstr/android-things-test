package lv.romstr.thingstest

import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.*


class FireButton(val name: String, val label: String, val service: PeripheralManagerService) {

    private val gpio = service.openGpio(name)
    private val db = FirebaseDatabase.getInstance()

    init {
        db.reference.child(label).setValue(0)
        with(gpio) {
            setDirection(Gpio.DIRECTION_IN)
            // Low voltage is considered active
            setActiveType(Gpio.ACTIVE_LOW)

            // Register for all state changes
            setEdgeTriggerType(Gpio.EDGE_BOTH)
            registerGpioCallback(ButtonCallback(this@FireButton))
        }
    }

    fun close() = gpio.close()

    class ButtonCallback(val button: FireButton): GpioCallback() {

        override fun onGpioError(gpio: Gpio?, error: Int) {
            Log.w("Button ${button.name}", "$gpio: Error event ${error}")
        }

        override fun onGpioEdge(gpio: Gpio?): Boolean {
            // Read the active low pin state
            Log.d("Button ${button.name}", "Input value is ${gpio?.value}")
            if (gpio != null && gpio.value) {
                button.db.reference.child(button.label).runTransaction(CounterHandler())
            }

            //Continue listening
            return true
        }
    }

    class CounterHandler: Transaction.Handler {
        override fun onComplete(error: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
            Log.d("Transaction", "${error ?: "Success"}")
        }

        override fun doTransaction(data: MutableData?): Transaction.Result {
            if (data?.value != null) {
                var current = data.getValue(Int::class.java)
                data.value = if (current == null) 0 else ++current
            }
            return Transaction.success(data)
        }
    }
}

