package com.example.pokmon.API

import com.example.pokmon.model.Pokemon
import com.example.pokmon.model.PokemonListResponse
import com.example.pokmon.model.PokemonSpeciesResponse // 🚨 NOVO: Você precisará deste modelo
import com.example.pokmon.model.EvolutionChainResponse // 🚨 NOVO: Você precisará deste modelo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url // 🚨 NOVO: Para usar URLs completas

interface PokemonService {

    // Método para buscar a lista inicial
    @GET("pokemon?limit=50&offset=0")
    fun getInitialPokemonList(): Call<PokemonListResponse>

    // Método para buscar detalhes (usado no card de detalhe e no sorteio)
    @GET("pokemon/{idOrName}")
    fun getPokemonDetail(@Path("idOrName") idOrName: String): Call<Pokemon>

    // 🚨 NOVO: Método para buscar a espécie do Pokémon (usando URL completa)
    @GET
    fun getPokemonSpecies(@Url speciesUrl: String): Call<PokemonSpeciesResponse>

    // 🚨 NOVO: Método para buscar a cadeia de evolução (usando URL completa)
    @GET("evolution-chain/{id}") // OU @GET
    fun getEvolutionChain(@Path("id") id: Int): Call<EvolutionChainResponse>

    // OU, se você usa o URL completo no fetchSpecies:
    /*
    @GET
    fun getEvolutionChain(@Url evolutionUrl: String): Call<EvolutionChainResponse>
    */
}