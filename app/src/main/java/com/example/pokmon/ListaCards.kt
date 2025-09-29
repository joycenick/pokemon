package com.example.pokmon

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokmon.API.PokemonService
import com.example.pokmon.adapter.PokemonAdapter
import com.example.pokmon.model.* // Certifique-se de que Item, Pokemon, etc. estão aqui
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ListaCards : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var busca: TextInputLayout
    private lateinit var service: PokemonService
    private lateinit var btnBuscar: MaterialButton
    private lateinit var pokebola: ImageView

    private val listaAtual = mutableListOf<Item>()
    private lateinit var pokemonAdapter: PokemonAdapter
    private val TAG = "ListaCardsLog"

    private val sorteioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val pokemonJson = data?.getStringExtra("pokemon_sorteado")

            if (pokemonJson != null) {
                val novoPokemon = Gson().fromJson(pokemonJson, Pokemon::class.java)
                adicionarPokemonSorteado(novoPokemon)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_cards)

        pokebola = findViewById(R.id.pokebola)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.CardsRecycleView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        pokemonAdapter = PokemonAdapter(listaAtual) { item: Item -> abrirDetalhe(item) }
        recyclerView.adapter = pokemonAdapter

        busca = findViewById(R.id.pesquisa)
        btnBuscar = findViewById(R.id.btnBuscar)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(PokemonService::class.java)

        chamarAPI()

        btnBuscar.setOnClickListener { chamarAPI() }
        pokebola.setOnClickListener{ abrirPokebola() }
    }

    private fun chamarAPI() {
        val query = busca.editText?.text.toString().trim().lowercase()
        listaAtual.clear()
        pokemonAdapter.notifyDataSetChanged()

        if (query.isEmpty()) {
            // Caso 1: Busca Vazia (Carrega a lista inicial)
            service.getInitialPokemonList().enqueue(object : Callback<PokemonListResponse> {
                override fun onResponse(call: Call<PokemonListResponse>, response: Response<PokemonListResponse>) {
                    val listaPokemons = response.body()?.results?.map { apiItem ->
                        val id = apiItem.url.trimEnd('/').split('/').last().toIntOrNull() ?: 0
                        // Cria o Item. O campo 'type' é nulo (null) inicialmente.
                        Item(id, apiItem.name, apiItem.url, null)
                    }
                    if (!listaPokemons.isNullOrEmpty()) {
                        listaAtual.addAll(listaPokemons)
                        listaAtual.sortBy { it.id }
                        pokemonAdapter.notifyDataSetChanged()

                        // 🚨 NOVO: Dispara a busca de tipos para CADA item da lista
                        listaAtual.forEachIndexed { index, item ->
                            if (item.id > 0) {
                                fetchTypeForPokemon(item, index)
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<PokemonListResponse>?, t: Throwable) {
                    Log.e(TAG, "Erro ao buscar lista inicial", t)
                    Toast.makeText(this@ListaCards, "Erro ao carregar lista inicial.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Caso 2: Busca Específica (Busca a cadeia de evolução)
            service.getPokemonDetail(query).enqueue(object : Callback<Pokemon> {
                override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                    val pokemon = response.body()

                    if (pokemon != null) {
                        fetchSpecies(pokemon.species.url)
                    } else {
                        Toast.makeText(this@ListaCards, "Pokémon '$query' não encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Pokemon>?, t: Throwable) {
                    Toast.makeText(this@ListaCards, "Erro na API ou Pokémon não encontrado.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Erro ao buscar detalhes de $query", t)
                }
            })
        }
    }

    // NOVO MÉTODO: Busca o Tipo de um Pokémon e atualiza o Adapter (usado para lista inicial)
    private fun fetchTypeForPokemon(item: Item, position: Int) {
        service.getPokemonDetail(item.id.toString()).enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                val types = response.body()?.types
                if (!types.isNullOrEmpty()) {
                    val primaryType = types.first().type.name

                    // Cria uma cópia do Item com o tipo atualizado
                    val updatedItem = item.copy(type = primaryType)

                    // Atualiza o item na listaAtual e notifica o Adapter
                    listaAtual[position] = updatedItem
                    pokemonAdapter.notifyItemChanged(position)
                }
            }
            override fun onFailure(call: Call<Pokemon>?, t: Throwable) {
                Log.e(TAG, "Falha ao buscar tipo para ${item.name}", t)
            }
        })
    }

    // Etapa 2: Buscar o URL da cadeia de evolução
    private fun fetchSpecies(speciesUrl: String) {
        service.getPokemonSpecies(speciesUrl).enqueue(object : Callback<PokemonSpeciesResponse> {
            override fun onResponse(call: Call<PokemonSpeciesResponse>, response: Response<PokemonSpeciesResponse>) {
                val evolutionUrl = response.body()?.evolution_chain?.url

                evolutionUrl?.let {
                    val id = it.trimEnd('/').split('/').last().toIntOrNull()
                    if (id != null) {
                        fetchEvolutionChain(id)
                    } else {
                        Toast.makeText(this@ListaCards, "Cadeia de evolução não encontrada.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<PokemonSpeciesResponse>?, t: Throwable) {
                Log.e(TAG, "Erro ao buscar espécie", t)
                Toast.makeText(this@ListaCards, "Erro ao buscar dados da espécie.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Etapa 3: Buscar a cadeia de evolução e popular a lista
    private fun fetchEvolutionChain(id: Int) {
        service.getEvolutionChain(id).enqueue(object : Callback<EvolutionChainResponse> {
            override fun onResponse(call: Call<EvolutionChainResponse>, response: Response<EvolutionChainResponse>) {
                response.body()?.chain?.let { chain ->
                    val evolutions = extractEvolutions(chain)

                    // Cria os Itens inicialmente com ID 0 e tipo NULO
                    val items = evolutions.map { name ->
                        val url = "https://pokeapi.co/api/v2/pokemon/$name"
                        Item(0, name, url, null)
                    }

                    listaAtual.clear()
                    listaAtual.addAll(items)
                    pokemonAdapter.notifyDataSetChanged()

                    // 🚨 NOVO: Busca o ID e o Tipo corretos assincronamente e atualiza o card
                    items.forEachIndexed { index, item ->
                        fetchPokemonDetailForEvolution(item.name, index)
                    }

                } ?: Toast.makeText(this@ListaCards, "Cadeia de evolução vazia.", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<EvolutionChainResponse>?, t: Throwable) {
                Log.e(TAG, "Erro ao buscar cadeia de evolução", t)
                Toast.makeText(this@ListaCards, "Erro ao buscar cadeia de evolução.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // NOVO MÉTODO: Busca o ID e Tipo de um Pokémon e atualiza o Adapter (usado para evoluções)
    private fun fetchPokemonDetailForEvolution(name: String, index: Int) {
        service.getPokemonDetail(name).enqueue(object : Callback<Pokemon> {
            override fun onResponse(call: Call<Pokemon>, response: Response<Pokemon>) {
                val pokemon = response.body()
                if (pokemon != null) {
                    val primaryType = pokemon.types.firstOrNull()?.type?.name

                    // Cria o Item com o ID e Tipo corretos
                    val updatedItem = listaAtual[index].copy(
                        id = pokemon.id,
                        type = primaryType
                    )

                    // Atualiza a lista e notifica o Adapter
                    listaAtual[index] = updatedItem
                    pokemonAdapter.notifyItemChanged(index)
                }
            }
            override fun onFailure(call: Call<Pokemon>?, t: Throwable) {
                Log.e(TAG, "Falha ao buscar detalhes para evolução: $name", t)
            }
        })
    }

    // Função recursiva para extrair todos os nomes da cadeia
    private fun extractEvolutions(chain: ChainLink): List<String> {
        val evolutions = mutableListOf<String>()
        evolutions.add(chain.species.name)
        chain.evolves_to.forEach { evolutions.addAll(extractEvolutions(it)) }
        return evolutions
    }

    private fun abrirDetalhe(item: Item) {
        val intent = Intent(this, Detalhe::class.java)
        intent.putExtra("pokemonName", item.name)
        startActivity(intent)
    }

    private fun abrirPokebola(){
        val intent = Intent(this, SorteioPokemon::class.java)
        sorteioLauncher.launch(intent)
    }

    private fun adicionarPokemonSorteado(pokemon: Pokemon) {
        val id = pokemon.id
        // No sorteio, já temos o tipo. Pega o primeiro.
        val primaryType = pokemon.types.firstOrNull()?.type?.name
        val novoItem = Item(id, pokemon.name, "https://pokeapi.co/api/v2/pokemon/$id", primaryType)

        if (listaAtual.none { it.id == id && it.id != 0 }) {

            listaAtual.add(novoItem)

            if (busca.editText?.text.toString().isEmpty()) {
                listaAtual.sortBy { it.id }
            }

            val newPosition = listaAtual.indexOfFirst { it.id == id }

            if (newPosition != -1) {
                pokemonAdapter.notifyItemInserted(newPosition)
                pokemonAdapter.notifyItemRangeChanged(0, listaAtual.size)
                recyclerView.smoothScrollToPosition(newPosition)
            } else {
                pokemonAdapter.notifyDataSetChanged()
            }

            Toast.makeText(this, "🎉 ${pokemon.name.replaceFirstChar { it.uppercase() }} sorteado e adicionado!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "${pokemon.name.replaceFirstChar { it.uppercase() }} já estava na lista!", Toast.LENGTH_SHORT).show()
        }
    }
}