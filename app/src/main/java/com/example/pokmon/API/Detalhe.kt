    package com.example.pokmon.API

    import com.example.pokmon.model.Pokemon
    import com.example.pokmon.model.PokemonListResponse
    import retrofit2.Call
    import retrofit2.http.GET
    import retrofit2.http.Path

    interface Detalhe {
        @GET("pokemon/{name}")
        fun getPokemonDetail(@Path("name") name: String): Call<Pokemon>

    }