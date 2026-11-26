package com.example.httplab2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParseException

class JsonActivity : AppCompatActivity() {

    private val allBooks = mutableListOf<Book>()
    private var shown: List<Book> = emptyList()
    private val adapter = BookAdapter()

    private lateinit var tvStatus: TextView
    private lateinit var tvSum: TextView
    private lateinit var tvInfo: TextView
    private lateinit var tvJson: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_json)

        tvStatus = findViewById(R.id.tvStatus)
        tvSum = findViewById(R.id.tvSum)
        tvInfo = findViewById(R.id.tvInfo)
        tvJson = findViewById(R.id.tvJson)

        val rv = findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // 1-2. Чтение res/raw/data.json и разбор через Gson
        try {
            val jsonText = resources.openRawResource(R.raw.data)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            val response = BookParser.parse(jsonText)
            allBooks.addAll(response.books)
            tvStatus.text = "Загружено записей: ${allBooks.size} (total в JSON: ${response.total})"
        } catch (e: JsonParseException) {
            tvStatus.text = "Некорректный JSON: ${e.message}"
        } catch (e: Exception) {
            tvStatus.text = "Не удалось прочитать данные: ${e.message}"
        }
        show(allBooks)
        updateSum()

        // 4. Фильтр: price строго больше введённого числа
        val etFilter = findViewById<EditText>(R.id.etFilter)
        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            val limit = etFilter.text.toString().trim().replace(',', '.').toDoubleOrNull()
            if (limit == null) {
                etFilter.error = "Введите число, например 420.0"
                return@setOnClickListener
            }
            val result = allBooks.filter { it.price > limit }
            show(result)
            tvInfo.text = "Цена > $limit: найдено ${result.size}"
        }
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            show(allBooks)
            tvInfo.text = ""
        }

        // 5. Сортировка по price по убыванию
        findViewById<Button>(R.id.btnSort).setOnClickListener {
            show(shown.sortedByDescending { it.price })
            tvInfo.text = "Отсортировано по цене (по убыванию)"
        }

        // Дополнительно: inStock = true
        findViewById<Button>(R.id.btnInStock).setOnClickListener {
            val result = allBooks.filter { it.inStock }
            show(result)
            tvInfo.text = "В наличии: ${result.size}"
        }

        // 7. Новая запись -> JSON с отступами
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etAuthor = findViewById<EditText>(R.id.etAuthor)
        val etCountry = findViewById<EditText>(R.id.etCountry)
        val etGenres = findViewById<EditText>(R.id.etGenres)
        val cbInStock = findViewById<CheckBox>(R.id.cbInStock)

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val price = etPrice.text.toString().trim().replace(',', '.').toDoubleOrNull()
            val authorName = etAuthor.text.toString().trim()

            if (title.isEmpty()) { etTitle.error = "Введите название"; return@setOnClickListener }
            if (price == null || price < 0) { etPrice.error = "Введите цену"; return@setOnClickListener }
            if (authorName.isEmpty()) { etAuthor.error = "Введите автора"; return@setOnClickListener }

            val genres = etGenres.text.toString()
                .split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val book = Book(
                id = (allBooks.maxOfOrNull { it.id } ?: 0) + 1,
                title = title,
                price = price,
                inStock = cbInStock.isChecked,
                author = Author(authorName, etCountry.text.toString().trim()),
                genres = genres
            )
            allBooks.add(book)
            show(allBooks)
            updateSum()

            val json = BookParser.toJson(allBooks)
            Log.d("JSON_LAB", json)
            tvJson.text = json
            tvInfo.text = "Добавлена книга «$title», всего: ${allBooks.size}"

            etTitle.text.clear(); etPrice.text.clear(); etAuthor.text.clear()
            etCountry.text.clear(); etGenres.text.clear(); cbInStock.isChecked = false
        }
    }

    private fun show(list: List<Book>) {
        shown = list
        adapter.submit(list)
    }

    // 6. Сумма значений price
    private fun updateSum() {
        val sum = allBooks.sumOf { it.price }
        tvSum.text = "Сумма цен: ${"%.2f".format(sum)} ₽"
    }
}