package com.example.pokmon

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // Adicionado
import com.bumptech.glide.Glide
import com.example.pokmon.API.Detalhe as DetalheApi
import com.example.pokmon.model.Pokemon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast

class Detalhe : AppCompatActivity() {

    private lateinit var cardContainer: CardView // NOVO: Para mudar a cor de fundo
    private lateinit var imgPokemon: ImageView
    private lateinit var txtNomePokemon: TextView
    private lateinit var txtNumeroPokemon: TextView // NOVO
    private lateinit var txtTipoPokemon: TextView

    private lateinit var progressVida: ProgressBar
    private lateinit var progressAtaque: ProgressBar
    private lateinit var progressDefesa: ProgressBar
    private lateinit var progressAtqEsp: ProgressBar
    private lateinit var progressDefEsp: ProgressBar
    private lateinit var progressVelocidade: ProgressBar

    private lateinit var numVida: TextView
    private lateinit var numAtaque: TextView
    private lateinit var numDefesa: TextView
    private lateinit var numAtqEsp: TextView
    private lateinit var numDefEsp: TextView
    private lateinit var numVelocidade: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_detalhe)

        // Inicializa o CardView e as views
        cardContainer = findViewById(R.id.cardContainer)
        imgPokemon = findViewById(R.id.imgPokemon)
        txtNomePokemon = findViewById(R.id.txtNomePokemon)
        txtNumeroPokemon = findViewById(R.id.txtNumeroPokemon)
        txtTipoPokemon = findViewById(R.id.txtTipoPokemon)

        progressVida = findViewById(R.id.progressVida)
        progressAtaque = findViewById(R.id.progressAtaque)
        progressDefesa = findViewById(R.id.progressDefesa)
        progressAtqEsp = findViewById(R.id.progressAtqEsp)
        progressDefEsp = findViewById(R.id.progressDefEsp)
        progressVelocidade = findViewById(R.id.progressVelocidade)

        numVida = findViewById(R.id.numVida)
        numAtaque = findViewById(R.id.numAtaque)
        numDefesa = findViewById(R.id.numDefesa)
        numAtqEsp = findViewById(R.id.numAtqEsp)
        numDefEsp = findViewById(R.id.numDefEsp)
        numVelocidade = findViewById(R.id.numVelocidade)

        val pokemonName = intent.getStringExtra("pokemonName")
        if (pokemonName != null) {
            carregarDetalhes(pokemonName)
        } else {
            Toast.makeText(this, "Pokémon inválido!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Mapeamento de cores para os tipos de Pokémon (hex codes do TCG/Games)
    private fun getTypeColor(type: String): Int {
        return when (type.lowercase()) {
            "fire" -> Color.parseColor("#EE8130") // Laranja
            "water" -> Color.parseColor("#6390F0") // Azul
            "grass" -> Color.parseColor("#7AC74C") // Verde
            "electric" -> Color.parseColor("#F7D02C") // Amarelo
            "psychic" -> Color.parseColor("#F95587") // Rosa
            "ice" -> Color.parseColor("#96D9D6") // Ciano
            "dragon" -> Color.parseColor("#6F35FC") // Roxo escuro
            "dark" -> Color.parseColor("#705746") // Marrom escuro
            "fairy" -> Color.parseColor("#D685AD") // Lilás
            "normal" -> Color.parseColor("#A8A77A") // Marrom claro
            "fighting" -> Color.parseColor("#C22E28") // Vermelho Tijolo
            "flying" -> Color.parseColor("#A98FF3") // Lavanda
            "poison" -> Color.parseColor("#A33EA1") // Roxo
            "ground" -> Color.parseColor("#E2BF65") // Bege
            "rock" -> Color.parseColor("#B6A136") // Cinza-oliva
            "bug" -> Color.parseColor("#A6B91A") // Verde-oliva
            "ghost" -> Color.parseColor("#735797") // Roxo acinzentado
            "steel" -> Color.parseColor("#B7B7CE") // Prata
            else -> Color.parseColor("#808080") // Cinza Neutro
        }
    }

    private fun carregarDetalhes(name: String) {
        // ... (Configuração do Retrofit)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(DetalheApi::class.java)
        api.getPokemonDetail(name).enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                if (response.isSuccessful) {
                    val pokemon = response.body()
                    if (pokemon != null) preencherDados(pokemon)
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

        // 1. Cor do CardView
        cardContainer.setCardBackgroundColor(cardColor)

        // 2. Cores do Texto (Se o fundo for claro, o texto deve ser escuro para contraste)
        val red = Color.red(cardColor)
        val green = Color.green(cardColor)
        val blue = Color.blue(cardColor)
        // Lógica simples de luminância: se a soma for alta, use texto preto
        val isLight = (red + green + blue) / 3 > 160
        val textColor = if (isLight) Color.BLACK else Color.WHITE

        // 3. Dados e Textos
        txtNomePokemon.text = pokemon.name.replaceFirstChar { it.uppercase() }
        txtNomePokemon.setTextColor(textColor)

        txtNumeroPokemon.text = String.format("#%03d", pokemon.id)
        txtNumeroPokemon.setTextColor(textColor)

        txtTipoPokemon.text = pokemon.type1 ?: "Desconhecido"

        // 4. GIF
        val gifUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/${pokemon.id}.gif"

        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .error(pokemon.spritesFront)
            .into(imgPokemon)

        // 5. Stats (mantendo as cores individuais para as barras)
        progressVida.progress = pokemon.hp
        progressAtaque.progress = pokemon.attack
        progressDefesa.progress = pokemon.defense
        progressAtqEsp.progress = pokemon.specialAttack
        progressDefEsp.progress = pokemon.specialDefense
        progressVelocidade.progress = pokemon.speed

        numVida.text = pokemon.hp.toString()
        numAtaque.text = pokemon.attack.toString()
        numDefesa.text = pokemon.defense.toString()
        numAtqEsp.text = pokemon.specialAttack.toString()
        numDefEsp.text = pokemon.specialDefense.toString()
        numVelocidade.text = pokemon.speed.toString()
    }
}