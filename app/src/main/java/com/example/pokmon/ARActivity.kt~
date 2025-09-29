package com.example.pokmon

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ARActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var btnAddPokemon: FloatingActionButton

    // Lista para armazenar os ImageViews dos Pokémon adicionados
    private val pokemonsRA = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aractivity)

        cameraPreview = findViewById(R.id.cameraPreview)
        btnAddPokemon = findViewById(R.id.btnAddPokemon)

        startCamera()

        // Adiciona o primeiro Pokémon que veio do Detalhe
        val pokemonId = intent.getIntExtra("pokemonId", 1)
        addPokemon(pokemonId)

        // Botão para adicionar mais Pokémon
        btnAddPokemon.setOnClickListener {
            // Aqui você pode escolher outro Pokémon ou repetir o mesmo
            addPokemon(pokemonId)
        }
    }

    private fun addPokemon(pokemonId: Int) {
        val imgPokemon = ImageView(this).apply {
            layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(150, 150)
            // Centraliza o Pokémon na tela
            x = (cameraPreview.width / 2).toFloat()
            y = (cameraPreview.height / 2).toFloat()
        }

        // Carrega o GIF
        val gifUrl =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/${pokemonId}.gif"
        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .into(imgPokemon)

        // Permite arrastar e redimensionar
        val scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var scaleFactor = 1f
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(0.5f, 15f)
                imgPokemon.scaleX = scaleFactor
                imgPokemon.scaleY = scaleFactor
                return true
            }
        })

        var dX = 0f
        var dY = 0f

        imgPokemon.setOnTouchListener { view, event ->
            scaleDetector.onTouchEvent(event)
            if (event.pointerCount == 1) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        view.x = event.rawX + dX
                        view.y = event.rawY + dY
                    }
                }
            }
            true
        }

        // Adiciona à tela
        val arLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.arLayout)
        arLayout.addView(imgPokemon)
        pokemonsRA.add(imgPokemon)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(cameraPreview.surfaceProvider) }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
