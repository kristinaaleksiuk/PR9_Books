package com.example.httplab2

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException

object BookParser {

    private val gson = Gson()
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    fun parse(json: String): BookResponse {
        val result = gson.fromJson(json, BookResponse::class.java)
            ?: throw JsonParseException("JSON пустой")
        @Suppress("SENSELESS_COMPARISON")
        if (result.books == null) throw JsonParseException("В JSON нет массива books")
        return result
    }

    fun toJson(books: List<Book>): String =
        prettyGson.toJson(BookResponse(books, books.size))
}