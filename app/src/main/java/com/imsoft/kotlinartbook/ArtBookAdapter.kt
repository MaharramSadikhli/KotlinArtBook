package com.imsoft.kotlinartbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imsoft.kotlinartbook.databinding.RecyclerRowBinding

class ArtBookAdapter(val artBookList: ArrayList<ArtBook>): RecyclerView.Adapter<ArtBookAdapter.ArtBookHolder>() {

    class ArtBookHolder (val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtBookHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtBookHolder(binding)
    }


    override fun onBindViewHolder(holder: ArtBookHolder, position: Int) {
        holder.binding.recyclerviewText.text = artBookList[position].name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("art", "oldArt")
            intent.putExtra("id", artBookList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artBookList.size
    }

}