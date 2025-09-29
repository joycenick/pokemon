package com.example.pokmon.model

data class EvolutionChainResponse(
    val chain: ChainLink
)

data class ChainLink(
    val species: Species,
    val evolves_to: List<ChainLink>
)

data class Species(
    val name: String,
    val url : String
)