package com.example.pokmon.API

import com.example.pokmon.model.EvolutionChainResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface EvolucaoAPI {
    @GET("api/v2/evolution-chain{id}")
    fun getEvolucao(@Path("id") id: Int): Call<EvolutionChainResponse>
}