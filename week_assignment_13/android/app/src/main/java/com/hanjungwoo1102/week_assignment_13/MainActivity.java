package com.hanjungwoo1102.week_assignment_13;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String channel = "com.example/Battery";
    private static final String eventChannel = "com.example/Accel";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), channel)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("getBatteryLevel")) {
                                int batteryLevel = getBatteryLevel();
                                if (batteryLevel != -1) {
                                    result.success(batteryLevel);
                                }
                            } else {
                                result.notImplemented();
                            }
                        }
                );

        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), eventChannel)
                .setStreamHandler(
                        new AccelerometerStreamHandler()
                );
    }

    class AccelerometerStreamHandler implements EventChannel.StreamHandler {
        private Sensor sensor;
        private SensorManager sensorManager;
        private SensorEventListener sensorEventListener;

        AccelerometerStreamHandler() {
            this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        @Override
        public void onListen(Object arguments, EventChannel.EventSink events) {
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    double[] sensorValues = new double[event.values.length];

                    for (int i = 0; i < event.values.length; i++) {
                        sensorValues[i] = event.values[i];
                    }

                    events.success(sensorValues);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onCancel(Object arguments) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getBatteryLevel() {
        int batteryLevel = -1;

        BatteryManager batteryManager = (BatteryManager)getSystemService(BATTERY_SERVICE);

        batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        return batteryLevel;
    }
}
