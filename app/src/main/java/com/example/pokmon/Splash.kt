package com.example.pokmon

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pokmon.databinding.ActivitySplashBinding

class Splash : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o binding primeiro
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilita EdgeToEdge
        enableEdgeToEdge()

        // Ajuste de insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura vídeo
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.splash}")
        binding.splashVideoView.setVideoURI(videoUri)
        binding.splashVideoView.start()

        // Aguarda 10s e abre MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ListaCards::class.java)
            startActivity(intent)
            finish()
        }, 4000)
    }
}
