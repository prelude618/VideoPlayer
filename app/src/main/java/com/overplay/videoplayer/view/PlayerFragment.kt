package com.overplay.videoplayer.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.overplay.videoplayer.R
import com.overplay.videoplayer.SharedPreferenceUtil
import com.overplay.videoplayer.databinding.ActivityPlayerBinding
import com.overplay.videoplayer.entity.Axis
import com.overplay.videoplayer.entity.Coordinates
import com.overplay.videoplayer.entity.LocationInfo
import com.overplay.videoplayer.service.ForegroundOnlyLocationService
import com.overplay.videoplayer.viewmodel.PlayerViewModel
import org.koin.android.viewmodel.ext.android.viewModel


class PlayerFragment : Fragment(), SensorEventListener {
    private val playerViewModel: PlayerViewModel by viewModel()
    private var player: SimpleExoPlayer? = null
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var previousLocationInfo: LocationInfo? = null
    private var previousCoordinates: Coordinates? = null
    private var previousTime = 0L

    companion object {
        private const val TAG = "PlayerFragment"
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 22
    }

    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    private var foregroundOnlyLocationServiceBound = false

    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver
    private lateinit var sharedPreferences: SharedPreferences

    private var listener =
            OnSharedPreferenceChangeListener { prefs, key ->
                if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
                }
            }

    private val foregroundOnlyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
            startGetLocation()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private val sensorManager by lazy {
        requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()

        sharedPreferences =
                requireContext().getSharedPreferences(
                        getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE
                )

        return viewBinding.root
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()
        if (provideRationale) {
            AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                            "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    AlertDialog.Builder(requireContext())
                            .setTitle("Location Permission Needed")
                            .setMessage("This app needs the Location permission, please accept to use location functionality")
                            .setPositiveButton(
                                    "OK"
                            ) { _, _ ->
                                startActivity(
                                        Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                activity?.let {
                                                    Uri.fromParts("package", it.packageName, null)
                                                },
                                        ),
                                )
                            }
                            .create()
                            .show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            startPlay()
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        val serviceIntent = Intent(requireActivity(), ForegroundOnlyLocationService::class.java)
        requireActivity().bindService(
                serviceIntent,
                foregroundOnlyServiceConnection,
                Context.BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24 || player == null)) {
            startPlay()
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                foregroundOnlyBroadcastReceiver,
                IntentFilter(
                        ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
                )
        )
    }

    private fun startPlay() {
        initializePlayer()
        playMedia()
        registerSensor()
    }

    private fun registerSensor() {
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        )

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun startGetLocation() {
        if (foregroundPermissionApproved()) {
            foregroundOnlyLocationService?.subscribeToLocationUpdates()
                    ?: Log.d(TAG, "Service Not Bound")
        } else {
            requestForegroundPermissions()
        }
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext())
                .build()
                .also { exoPlayer ->
                    viewBinding.videoView.player = exoPlayer
                    val mediaItem = MediaItem.fromUri(getString(R.string.overplay_mp4))
                    exoPlayer.setMediaItem(mediaItem)
                }
    }

    private fun playMedia() {
        playFromDesignatedPosition(playbackPosition)

        playerViewModel.playMedia().apply {
            observe(viewLifecycleOwner, Observer {
                player?.run {
                    playWhenReady = false
                    stop()
                    seekTo(0)
                }
            })
        }
    }

    private fun playFromDesignatedPosition(startPosition: Long) {
        player?.run {
            playWhenReady = true
            seekTo(currentWindow, startPosition)
            prepare()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        viewBinding.videoView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
                foregroundOnlyBroadcastReceiver
        )
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
        sensorManager.unregisterListener(this)
    }


    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)

        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                    ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            if (location != null) {
                Toast.makeText(context, location.toString(), Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Latitude =" + location.latitude.toString())
                Log.d(TAG, "Longitude =" + location.longitude.toString())

                val locationInfo = LocationInfo(location.latitude, location.longitude)

                previousLocationInfo?.let {
                    if (playerViewModel.isOverTenMeters(it, locationInfo)) {
                        playFromDesignatedPosition(0)
                    }
                }

                previousLocationInfo = locationInfo
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val curTime = System.currentTimeMillis()
        val diffTime = curTime - previousTime

        if (previousTime == 0L) {
            previousTime = curTime
        } else if (diffTime > 100) {
            event?.let {
                isAccelerometer(it, diffTime)
                isGyroscope(it)
            }
            previousTime = curTime
        }
    }

    private fun isGyroscope(it: SensorEvent) {
        if (it.sensor.type === Sensor.TYPE_GYROSCOPE) {
            val axis = Axis(it.values[0], it.values[1], it.values[2])

            checkRollChange(axis)
            Log.d(TAG, "Rotation axis.x =" + axis.x)

            checkYawChange(axis)
            Log.d(TAG, "Rotation axis.z =" + axis.z)
        }
    }

    private fun checkYawChange(axis: Axis) {
        when {
            axis.z > 0.1 -> {
                player?.run {
                    seekTo(currentWindow, currentPosition + 1000)
                    prepare()
                }
            }
            axis.z < -0.1 -> {
                player?.run {
                    seekTo(currentWindow, currentPosition - 1000)
                    prepare()
                }
            }
            else -> {
            }
        }
    }

    private fun checkRollChange(axis: Axis) {
        when {
            axis.x > 0.1 -> {
                player?.run {
                    increaseDeviceVolume()
                }
            }
            axis.x < -0.1 -> {
                player?.run {
                    decreaseDeviceVolume()
                }
            }
            else -> {
            }
        }
    }

    private fun isAccelerometer(it: SensorEvent, diffTime: Long) {
        if (it.sensor.type === Sensor.TYPE_ACCELEROMETER) {
            val coordinates = Coordinates(it.values[0], it.values[1], it.values[2])

            previousCoordinates?.let { previousCoordinates ->
                isShakeOverThreshold(previousCoordinates, coordinates, diffTime)
            }
            previousCoordinates = coordinates
        }
    }

    private fun isShakeOverThreshold(previousCoordinates: Coordinates, coordinates: Coordinates, diffTime: Long) {
        if (playerViewModel.isShakeOverThreshold(previousCoordinates, coordinates, diffTime)) {
            player?.let { simpleExoPlayer ->
                simpleExoPlayer.playWhenReady = false
                simpleExoPlayer.playbackState
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
