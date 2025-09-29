package com.example.pokmon.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokmon.R
import com.example.pokmon.model.Item

class PokemonAdapter(
    private val listaAtual: List<Item>,
    private val onItemClicked: (Item) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cards, parent, false)
        return PokemonViewHolder(view)
    }

    override fun getItemCount(): Int = listaAtual.size

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val item = listaAtual[position]
        val pokemonId = item.id

        // 🚨 CORREÇÃO: Garante que o nome usado para a URL esteja em minúsculas
        val pokemonNameLower = item.name.lowercase()

        // Define a query para a imagem (ID ou Nome minúsculo)
        val spriteQuery = if (pokemonId > 0) pokemonId.toString() else pokemonNameLower

        // 1. Atualiza o número do card
        holder.numeroCarta.text = if (pokemonId > 0) String.format("#%03d", pokemonId) else "#---"

        // 2. Atualiza o nome (mantendo a primeira letra maiúscula para exibição)
        holder.nomePokemon.text = item.name.replaceFirstChar { it.uppercase() }

        holder.tipoPokemon.text = item.type?.replaceFirstChar { it.uppercase() } ?: "..."


        // 3. Carrega a imagem: usa spriteQuery (ID ou Nome)
        val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${spriteQuery}.png"

        // Log para depuração (opcional, mas útil!)
        Log.d("PokemonAdapter", "Carregando sprite de: $spriteQuery, URL: $spriteUrl")

        Glide.with(holder.itemView.context)
            .load(spriteUrl)
            .error(R.drawable.pokebola)
            .into(holder.imagemPokemon)

        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val numeroCarta: TextView = view.findViewById(R.id.numeroCarta)
        val nomePokemon: TextView = view.findViewById(R.id.nome)
        val imagemPokemon: ImageView = view.findViewById(R.id.img)
        val tipoPokemon: TextView = view.findViewById(R.id.tipo)
    }
}