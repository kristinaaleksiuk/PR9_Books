package com.example.httplab2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParseException
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etId = findViewById<EditText>(R.id.etId)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnLoad.setOnClickListener {
            // 1. Проверка ввода: пустое значение на сервер не отправляем
            val input = etId.text.toString().trim()
            if (input.isEmpty()) {
                etId.error = "Введите номер поста"
                return@setOnClickListener
            }
            val id = input.toIntOrNull()
            if (id == null || id < 1) {
                etId.error = "Номер должен быть целым числом от 1"
                return@setOnClickListener
            }

            // 2. Запрос в корутине, привязанной к жизненному циклу Activity
            lifecycleScope.launch {
                progress.visibility = View.VISIBLE
                btnLoad.isEnabled = false
                tvStatus.text = "Загрузка..."
                tvResult.text = ""

                try {
                    val response = RetrofitClient.api.getPost(id)
                    val code = response.code()
                    tvStatus.text = "Код ответа: $code ${statusName(code, response.message())}"

                    when {
                        response.isSuccessful -> {
                            val post = response.body()
                            tvResult.text = if (post != null) {
                                "title:\n${post.title}\n\nbody:\n${post.body}"
                            } else {
                                "Сервер вернул пустой ответ"
                            }
                        }
                        code == 404 -> tvResult.text = "Пост не найден"
                        else -> tvResult.text = "Ошибка HTTP: $code"
                    }
                } catch (e: IOException) {                 // нет сети, таймаут, неизвестный хост
                    tvStatus.text = "Ошибка сети"
                    tvResult.text = "Нет соединения: ${e.message}"
                } catch (e: JsonParseException) {          // сервер прислал не тот JSON
                    tvStatus.text = "Ошибка данных"
                    tvResult.text = "Не удалось разобрать ответ: ${e.message}"
                } finally {
                    progress.visibility = View.GONE
                    btnLoad.isEnabled = true
                }
            }
        }
    }

    // При HTTP/2 текст статуса (message) бывает пустым - подставляем свой
    private fun statusName(code: Int, message: String): String {
        if (message.isNotBlank()) return message
        return when (code) {
            200 -> "OK"
            201 -> "Created"
            204 -> "No Content"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            429 -> "Too Many Requests"
            500 -> "Internal Server Error"
            503 -> "Service Unavailable"
            else -> ""
        }
    }
}
