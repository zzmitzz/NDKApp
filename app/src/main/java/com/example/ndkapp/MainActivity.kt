package com.example.ndkapp

import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.example.ndkapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Objects

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
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
                                binding.imageView.setImageBitmap(getBitmapFromByteArray(result,bitmap.width, bitmap.height))
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
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray{
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun getBitmapFromByteArray(byteArray: ByteArray,width: Int, height: Int): Bitmap {
        Log.d("MainActivity", byteArray.joinToString { "" })
        val yuvImage = YuvImage(
            byteArray,
            ImageFormat.NV21,  // or ImageFormat.YUV_420_SP
            width,
            height,
            null
        )

        // Convert to JPEG
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val jpegBytes = out.toByteArray()

        // Now we can safely decode to Bitmap
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }



    private external fun calculateNthFibonacci(a: Long): Long
    external fun processImage(image: ByteArray, width: Int, height: Int): ByteArray
    private external fun readNameObject(a: Person): String





    companion object {
        // Used to load the 'ndkapp' library on application startup.
        init {
            System.loadLibrary("ndkapp")
        }
        const val PICK_IMAGE = 102
        val permissionRequire = listOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_AUDIO
        ).toTypedArray()
    }


}