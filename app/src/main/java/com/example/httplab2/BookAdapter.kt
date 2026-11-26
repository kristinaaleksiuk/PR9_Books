package com.example.httplab2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(private var items: List<Book> = emptyList()) :
    RecyclerView.Adapter<BookAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(android.R.id.text1)
    }

    fun submit(newItems: List<Book>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val book = items[position]
        val author = book.author?.name ?: "автор не указан"
        val genresCount = book.genres?.size ?: 0
        holder.text.text =
            "${book.title}\n${book.price} ₽  •  $author  •  жанров: $genresCount"
    }

    override fun getItemCount() = items.size
}