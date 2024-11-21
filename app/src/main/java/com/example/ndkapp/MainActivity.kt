package com.example.ndkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.ndkapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
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
        Toast.makeText(this, "Hello ${readNameObject(Person("Ngo Tuan Anh",20))}", Toast.LENGTH_LONG).show()
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


    external fun calculateNthFibonacci(a: Long): Long
    external fun readNameObject(a: Person): String
    companion object {
        // Used to load the 'ndkapp' library on application startup.
        init {
            System.loadLibrary("ndkapp")
        }
    }


}