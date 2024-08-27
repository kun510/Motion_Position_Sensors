package com.kun510.sensors

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kun510.sensors.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager

    private val sensors = mutableMapOf<Int, Sensor?>()
    private val sensorValues = mutableMapOf<Int, FloatArray>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val sensorTypes = listOf(
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
            Sensor.TYPE_PROXIMITY
        )

        sensorTypes.forEach { type ->
            sensors[type] = sensorManager.getDefaultSensor(type)
            sensorValues[type] = FloatArray(4)
        }

        val sensorError = getString(R.string.error_no_sensor)
        with(binding) {
            if (sensors[Sensor.TYPE_MAGNETIC_FIELD] == null) applyErrorToMagViews(sensorError)
            if (sensors[Sensor.TYPE_ACCELEROMETER] == null) applyErrorToAccViews(sensorError)
            if (sensors[Sensor.TYPE_GYROSCOPE] == null) applyErrorToGyroViews(sensorError)
            if (sensors[Sensor.TYPE_ROTATION_VECTOR] == null) applyErrorToRotvViews(sensorError)
            if (sensors[Sensor.TYPE_GRAVITY] == null) applyErrorToGravViews(sensorError)
            if (sensors[Sensor.TYPE_LINEAR_ACCELERATION] == null) applyErrorToLineViews(sensorError)
            if (sensors[Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR] == null) applyErrorToGrtViews(sensorError)
            if (sensors[Sensor.TYPE_PROXIMITY] == null) labelProx.text = sensorError
            if (sensors[Sensor.TYPE_MAGNETIC_FIELD] == null && sensors[Sensor.TYPE_ACCELEROMETER] == null) {
                labelAzimuth.text = sensorError
                labelPitch.text = sensorError
                labelRoll.text = sensorError
            }
        }
    }

    override fun onStart() {
        super.onStart()
        sensors.forEach { (_, sensor) ->
            sensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        }
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        sensorValues[event.sensor.type] = event.values
        updateUI(event.sensor.type)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    private fun updateUI(sensorType: Int) {
        with(binding) {
            when (sensorType) {
                Sensor.TYPE_MAGNETIC_FIELD -> updateMagViews(sensorValues[sensorType]!!)
                Sensor.TYPE_ACCELEROMETER -> {
                    updateAccViews(sensorValues[sensorType]!!)
                    computeOrientation()
                }
                Sensor.TYPE_GYROSCOPE -> updateGyroViews(sensorValues[sensorType]!!)
                Sensor.TYPE_ROTATION_VECTOR -> updateRotvViews(sensorValues[sensorType]!!)
                Sensor.TYPE_GRAVITY -> updateGravViews(sensorValues[sensorType]!!)
                Sensor.TYPE_LINEAR_ACCELERATION -> updateLineViews(sensorValues[sensorType]!!)
                Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> updateGrtViews(sensorValues[sensorType]!!)
                Sensor.TYPE_PROXIMITY -> labelProx.text = getString(R.string.label_prox, sensorValues[sensorType]!![0])
            }
        }
    }

    private fun computeOrientation() {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, sensorValues[Sensor.TYPE_ACCELEROMETER], sensorValues[Sensor.TYPE_MAGNETIC_FIELD])

        val orientationAngles = FloatArray(3)
        val radian = SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val angles = radian.map { Math.toDegrees(it.toDouble()).toFloat() }.toFloatArray()

        with(binding) {
            labelAzimuth.text = getString(R.string.label_azimuth, angles[0])
            labelPitch.text = getString(R.string.label_pitch, angles[1])
            labelRoll.text = getString(R.string.label_roll, angles[2])
        }
    }

    private fun updateMagViews(values: FloatArray) {
        binding.labelMagX.text = getString(R.string.label_magneticFieldX, values[0])
        binding.labelMagY.text = getString(R.string.label_magneticFieldY, values[1])
        binding.labelMagZ.text = getString(R.string.label_magneticFieldZ, values[2])
    }

    private fun updateAccViews(values: FloatArray) {
        binding.labelAccX.text = getString(R.string.label_accelerometerX, values[0])
        binding.labelAccY.text = getString(R.string.label_accelerometerY, values[1])
        binding.labelAccZ.text = getString(R.string.label_accelerometerZ, values[2])
    }

    private fun updateGyroViews(values: FloatArray) {
        binding.labelGyroX.text = getString(R.string.label_gyroscopeX, values[0])
        binding.labelGyroY.text = getString(R.string.label_gyroscopeY, values[1])
        binding.labelGyroZ.text = getString(R.string.label_gyroscopeZ, values[2])
    }

    private fun updateRotvViews(values: FloatArray) {
        binding.labelRotvX.text = getString(R.string.label_rotvX, values[0])
        binding.labelRotvY.text = getString(R.string.label_rotvY, values[1])
        binding.labelRotvZ.text = getString(R.string.label_rotvZ, values[2])
        binding.labelRotvS.text = getString(R.string.label_rotvS, values[3])
    }

    private fun updateGravViews(values: FloatArray) {
        binding.labelGravX.text = getString(R.string.label_gravX, values[0])
        binding.labelGravY.text = getString(R.string.label_gravY, values[1])
        binding.labelGravZ.text = getString(R.string.label_gravZ, values[2])
    }

    private fun updateLineViews(values: FloatArray) {
        binding.labelLineX.text = getString(R.string.label_lineX, values[0])
        binding.labelLineY.text = getString(R.string.label_lineY, values[1])
        binding.labelLineZ.text = getString(R.string.label_lineZ, values[2])
    }

    private fun updateGrtViews(values: FloatArray) {
        binding.labelGrtX.text = getString(R.string.label_grtX, values[0])
        binding.labelGrtY.text = getString(R.string.label_grtY, values[1])
        binding.labelGrtZ.text = getString(R.string.label_grtZ, values[2])
    }

    private fun ActivityMainBinding.applyErrorToMagViews(sensorError: String) {
        labelMagX.text = sensorError
        labelMagY.text = sensorError
        labelMagZ.text = sensorError
    }

    private fun ActivityMainBinding.applyErrorToAccViews(sensorError: String) {
        labelAccX.text = sensorError
        labelAccY.text = sensorError
        labelAccZ.text = sensorError
    }

    private fun ActivityMainBinding.applyErrorToGyroViews(sensorError: String) {
        labelGyroX.text = sensorError
        labelGyroY.text = sensorError
        labelGyroZ.text = sensorError
    }

    private fun ActivityMainBinding.applyErrorToRotvViews(sensorError: String) {
        labelRotvX.text = sensorError
        labelRotvY.text = sensorError
        labelRotvZ.text = sensorError
        labelRotvS.text = sensorError
    }

    private fun ActivityMainBinding.applyErrorToGravViews(sensorError: String) {
        labelGravX.text = sensorError
        labelGravY.text = sensorError
        labelGravZ.text = sensorError
    }

    private fun ActivityMainBinding.applyErrorToLineViews(sensorError: String) {
        labelLineX.text = sensorError
        labelLineY.text = sensorError
        labelLineZ.text = sensorError
    }

    private fun ActivityMainBinding.applyErrorToGrtViews(sensorError: String) {
        labelGrtX.text = sensorError
        labelGrtY.text = sensorError
        labelGrtZ.text = sensorError
    }
}
