package com.example.ndkapp

import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.net.toUri
import com.example.ndkapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.util.Objects

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }
    private var scope =
        CoroutineScope(Dispatchers.Default) + CoroutineExceptionHandler { _, throwable ->
            run {
                Log.d("MainActivity", throwable.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        Toast.makeText(this, "Hello ${readNameObject(Person("Ngo Tuan Anh",20))}", Toast.LENGTH_LONG).show()
    }
    private fun initView(){
        handleUC1()
        handleUC2()
        handleUC3()
    }

    private fun handleUC1(){
        binding.button.setOnClickListener{
            val a = binding.editText.text
            if(a.isNotEmpty()){
                try {
                    val n = a.toString().toLong()
                    scope.launch {

                        val now = System.currentTimeMillis()
                        withContext(Dispatchers.Main){
                            binding.kotlin.text = "Loading..."
                        }
                        val result = calculateFibNth(n)
                        Log.d("MainActivity","Kotlin " + result.toString())
                        withContext(Dispatchers.Main){
                            binding.kotlin.text = "Kotlin time taken: ${(System.currentTimeMillis() - now)} ms"
                        }
                    }
                    scope.launch {

                        val now = System.currentTimeMillis()
                        withContext(Dispatchers.Main){
                            binding.nativeTime.text = "Loading..."
                        }
                        val result = calculateNthFibonacci(n)

                        Log.d("MainActivity", "Native" + result.toString())
                        withContext(Dispatchers.Main){
                            binding.nativeTime.text = "Native time taken: ${result} ms"
                        }
                    }
                }catch (e: Exception){}
            }

        }
    }

    private fun handleUC2(){
        binding.submitTask2.setOnClickListener{
            if(!checkPermissionGranted()){
                requestPermissions(permissionRequire, 200)
            }
            else{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE)
            }
        }

    }

    private fun handleUC3(){
        binding.uploadAudio.setOnClickListener{
            if(!checkPermissionGranted()){
                requestPermissions(permissionRequire, 200)
            }
            else{
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "audio/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                startActivityForResult(intent, PICK_AUDIO)
            }
        }
        binding.playPause.setOnClickListener{

            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
            }
            else{
                mediaPlayer.start()
            }
        }
    }

    private fun calculateFibNth(n: Long): Long{
        var result = 0L
        var a = 0L
        var b = 1L
        for(i in 1 .. n){
            result = a + b
            a = b
            b = result
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    val image = data?.data
                    if (image != null) {
                        scope.launch {
                            Log.d("MainActivity", image.toString())
                            val now = System.currentTimeMillis()
                            withContext(Dispatchers.Main) {
                                binding.kotlin.text = "Loading..."
                            }
                            var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, image)
                            val result = processImage(bitmapToByteArray(bitmap), bitmap.width, bitmap.height)
                            withContext(Dispatchers.Main){
                                binding.imageView.setImageBitmap(byteArrayToBitmapRaw(result,bitmap.width, bitmap.height, bitmap.config!!))
                            }
                            Log.d("MainActivity", "Native" + result.toString())
                            withContext(Dispatchers.Main) {
                                binding.nativeTime.text = "Native time taken: ${System.currentTimeMillis() - now} ms"
                                binding.kotlin.text = "Success"
                            }
                        }
                    }
                }
            }
            PICK_AUDIO -> {
                if (resultCode == RESULT_OK) {
                    val audio = data?.data
                    if (audio != null) {
                        Log.d("MainActivity", audio.path.toString())
                        mediaPlayer.setDataSource(this@MainActivity, "file://${audio.path}".toUri())
                        mediaPlayer.setOnPreparedListener {
                            mediaPlayer.start()
                        }
                        mediaPlayer.prepareAsync()
                    }
                }
            }
        }
    }
    private fun checkPermissionGranted(vararg permissions : String): Boolean {
        for(permission in permissions){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bytesPerPixel = when (bitmap.config) {
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.RGB_565 -> 2
            Bitmap.Config.ALPHA_8 -> 1
            else -> throw IllegalArgumentException("Unsupported Bitmap.Config")
        }
        val bufferSize = bitmap.width * bitmap.height * bytesPerPixel
        val byteArray = ByteArray(bufferSize)
        val buffer = java.nio.ByteBuffer.wrap(byteArray)
        bitmap.copyPixelsToBuffer(buffer)
        return byteArray
    }

    private fun byteArrayToBitmapRaw(byteArray: ByteArray, width: Int, height: Int, config: Bitmap.Config): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, config)
        val buffer = java.nio.ByteBuffer.wrap(byteArray)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }



    private external fun calculateNthFibonacci(a: Long): Long
    private external fun processImage(image: ByteArray, width: Int, height: Int): ByteArray
    private external fun readNameObject(a: Person): String





    companion object {
        // Used to load the 'ndkapp' library on application startup.
        init {
            System.loadLibrary("ndkapp")
        }
        const val PICK_AUDIO = 101
        const val PICK_IMAGE = 102
        val permissionRequire = listOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.READ_MEDIA_IMAGES"
        ).toTypedArray()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}