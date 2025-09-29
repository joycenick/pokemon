package com.example.pokmon.API

import com.example.pokmon.model.EvolutionChainResponse
import com.example.pokmon.model.Item
import com.example.pokmon.model.PokemonDetailResponse
import com.example.pokmon.model.PokemonListResponse
import com.example.pokmon.model.PokemonSpeciesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface CardApi {

    // Pega os primeiros 50 Pokémon
    @GET("pokemon")
    fun getAllPokemon(@Query("limit") limit: Int = 50): Call<PokemonListResponse    >

    @GET("pokemon/{name}")
    fun getPokemonByName(@Path("name") name: String): Call<PokemonDetailResponse>


    @GET
    fun getPokemonSpecies(@Url url: String): Call<PokemonSpeciesResponse>

    // 🔹 Busca evolution chain pelo id
    @GET("evolution-chain/{id}")
    fun getEvolutionChain(@Path("id") id: Int): Call<EvolutionChainResponse>
}
