package com.example.pokmon

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pokmon.API.PokemonService // 🚨 CORREÇÃO: Usando a interface correta
import com.example.pokmon.model.Pokemon
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random

class SorteioPokemon : AppCompatActivity() {

    private lateinit var imgAnimacao: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var pokemonService: PokemonService // 🚨 CORREÇÃO: Tipo da variável

    private val handler = Handler(Looper.getMainLooper())
    private val TAG = "SORTEIO_POKEMON"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sorteio_pokemon)

        imgAnimacao = findViewById(R.id.imgAnimacao)
        txtStatus = findViewById(R.id.txtStatus)

        // Configuração do Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // 🚨 CRIAÇÃO CORRETA DA INTERFACE
        pokemonService = retrofit.create(PokemonService::class.java)

        iniciarAnimacaoESorteio()
    }

    private fun iniciarAnimacaoESorteio() {
        // ... (código da animação de rotação)
        imgAnimacao.setImageResource(R.drawable.pokebola)

        val rotate = RotateAnimation(0f, 360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 1000
        rotate.repeatCount = RotateAnimation.INFINITE
        rotate.interpolator = LinearInterpolator()

        imgAnimacao.startAnimation(rotate)

        txtStatus.text = "Sorteando um novo Pokémon..."

        handler.postDelayed({
            imgAnimacao.clearAnimation()
            txtStatus.text = "Abrindo a Pokebola..."
            realizarSorteio()
        }, 2000)
    }

    private fun realizarSorteio() {
        val minId = 51
        val maxId = 151

        val randomId = Random.nextInt(minId, maxId + 1)

        txtStatus.text = "Busca de Pokémon #${randomId}..."
        Log.d(TAG, "ID Sorteado: #$randomId")

        buscarPokemonPorId(randomId)
    }

    private fun buscarPokemonPorId(id: Int) {
        // 🚨 CHAMADA AGORA ESTÁ CORRETA
        pokemonService.getPokemonDetail(id.toString()).enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                if (response.isSuccessful) {
                    val novoPokemon = response.body()
                    if (novoPokemon != null) {
                        mostrarResultado(novoPokemon)
                        retornarResultado(novoPokemon)
                        return
                    }
                }

                // Trata Falha HTTP
                val errorBodyString = response.errorBody()?.string() ?: "Corpo do erro vazio"
                val errorMessage = "Falha no sorteio! Erro HTTP: ${response.code()}."
                Toast.makeText(this@SorteioPokemon, errorMessage, Toast.LENGTH_LONG).show()
                Log.e(TAG, "Falha HTTP: ${response.code()}. URL: ${call.request().url()}. Corpo: $errorBodyString")

                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            override fun onFailure(call: Call<Pokemon>, t: Throwable) {
                // Trata Falha de Rede ou Gson
                Toast.makeText(this@SorteioPokemon, "Erro de rede ao buscar: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Falha de Rede/Gson: ${t.message}. Causa: ${t.cause}", t)

                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })
    }

    private fun mostrarResultado(pokemon: Pokemon) {
        // ... (código para mostrar o Pokémon)
        val gifUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/${pokemon.id}.gif"

        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .error(pokemon.spritesFront)
            .into(imgAnimacao)

        txtStatus.text = "Parabéns! Você encontrou ${pokemon.name.replaceFirstChar { it.uppercase() }}!"

        handler.postDelayed({
            finish()
        }, 3000)
    }

    private fun retornarResultado(pokemon: Pokemon) {
        val pokemonJson = Gson().toJson(pokemon)
        val resultIntent = Intent()
        resultIntent.putExtra("pokemon_sorteado", pokemonJson)
        setResult(Activity.RESULT_OK, resultIntent)
    }
}