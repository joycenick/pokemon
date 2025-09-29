package com.example.pokmon.model

data class PokemonSpeciesResponse(
    val evolution_chain: EvolutionChainUrl
)

data class EvolutionChainUrl(
    val url: String
)
