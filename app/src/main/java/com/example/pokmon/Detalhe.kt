package com.example.pokmon

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.pokmon.API.Detalhe as DetalheApi
import com.example.pokmon.model.Pokemon
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Detalhe : AppCompatActivity() {

    private lateinit var cardContainer: CardView
    private lateinit var imgPokemon: ImageView
    private lateinit var txtNomePokemon: TextView
    private lateinit var txtNumeroPokemon: TextView
    private lateinit var txtTipoPokemon: TextView
    private lateinit var btnAbrirRA: MaterialButton

    private var pokemonId: Int = 1 // valor default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_detalhe)

        cardContainer = findViewById(R.id.cardContainer)
        imgPokemon = findViewById(R.id.imgPokemon)
        txtNomePokemon = findViewById(R.id.txtNomePokemon)
        txtNumeroPokemon = findViewById(R.id.txtNumeroPokemon)
        txtTipoPokemon = findViewById(R.id.txtTipoPokemon)
        btnAbrirRA = findViewById(R.id.btnAbrirRA)

        val pokemonName = intent.getStringExtra("pokemonName")
        if (pokemonName != null) {
            carregarDetalhes(pokemonName)
        } else {
            Toast.makeText(this, "Pokémon inválido!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnAbrirRA.setOnClickListener {
            val intent = Intent(this, ARActivity::class.java)
            intent.putExtra("pokemonId", pokemonId) // passa o ID para a AR
            startActivity(intent)
        }
    }

    private fun carregarDetalhes(name: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(DetalheApi::class.java)
        api.getPokemonDetail(name).enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                if (response.isSuccessful) {
                    val pokemon = response.body()
                    if (pokemon != null) {
                        pokemonId = pokemon.id
                        preencherDados(pokemon)
                    }
                } else {
                    Toast.makeText(this@Detalhe, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Pokemon>, t: Throwable) {
                Toast.makeText(this@Detalhe, "Falha: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun preencherDados(pokemon: Pokemon) {
        val typeName = pokemon.types.firstOrNull()?.type?.name ?: "normal"
        val cardColor = getTypeColor(typeName)
        cardContainer.setCardBackgroundColor(cardColor)

        txtNomePokemon.text = pokemon.name.replaceFirstChar { it.uppercase() }
        txtNumeroPokemon.text = String.format("#%03d", pokemon.id)
        txtTipoPokemon.text = typeName.replaceFirstChar { it.uppercase() }

        val gifUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/${pokemon.id}.gif"

        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .error(pokemon.sprites.front_default)
            .into(imgPokemon)
    }

    private fun getTypeColor(type: String): Int {
        return when (type.lowercase()) {
            "fire" -> Color.parseColor("#EE8130")
            "water" -> Color.parseColor("#6390F0")
            "grass" -> Color.parseColor("#7AC74C")
            "electric" -> Color.parseColor("#F7D02C")
            "psychic" -> Color.parseColor("#F95587")
            "ice" -> Color.parseColor("#96D9D6")
            "dragon" -> Color.parseColor("#6F35FC")
            "dark" -> Color.parseColor("#705746")
            "fairy" -> Color.parseColor("#D685AD")
            "normal" -> Color.parseColor("#A8A77A")
            "fighting" -> Color.parseColor("#C22E28")
            "flying" -> Color.parseColor("#A98FF3")
            "poison" -> Color.parseColor("#A33EA1")
            "ground" -> Color.parseColor("#E2BF65")
            "rock" -> Color.parseColor("#B6A136")
            "bug" -> Color.parseColor("#A6B91A")
            "ghost" -> Color.parseColor("#735797")
            "steel" -> Color.parseColor("#B7B7CE")
            else -> Color.parseColor("#808080")
        }
    }
}
