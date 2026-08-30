package com.example.bookstore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookstore.BuildConfig
import com.example.bookstore.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface GeminiUiState {
    object Idle : GeminiUiState
    object Loading : GeminiUiState
    data class Success(val text: String) : GeminiUiState
    data class Error(val message: String) : GeminiUiState
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiViewModel : ViewModel() {

    private val _recommendState = MutableStateFlow<GeminiUiState>(GeminiUiState.Idle)
    val recommendState: StateFlow<GeminiUiState> = _recommendState.asStateFlow()

    private val _summaryState = MutableStateFlow<GeminiUiState>(GeminiUiState.Idle)
    val summaryState: StateFlow<GeminiUiState> = _summaryState.asStateFlow()

    private val _chatState = MutableStateFlow<GeminiUiState>(GeminiUiState.Idle)
    val chatState: StateFlow<GeminiUiState> = _chatState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Groq API — free, fast, permanent keys (gsk_...)
    // Fallback: also supports Gemini AIzaSy keys via generativelanguage.googleapis.com
    private suspend fun callAi(messages: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            throw Exception("AI API key is not configured. Add GEMINI_API_KEY to local.properties.")
        }

        return@withContext when {
            apiKey.startsWith("gsk_") -> callGroq(apiKey, messages)
            apiKey.startsWith("AIzaSy") -> callGeminiDirect(apiKey, messages.last().second)
            else -> throw Exception("Unrecognized API key format. Please use a Groq key (gsk_...) or Gemini key (AIzaSy...).")
        }
    }

    /** Groq API — uses OpenAI-compatible chat completions endpoint. Free tier, no expiry. */
    private fun callGroq(apiKey: String, messages: List<Pair<String, String>>): String {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val messagesArray = JSONArray()
        // Add a system message for bookstore context
        messagesArray.put(JSONObject()
            .put("role", "system")
            .put("content", "You are a helpful AI assistant for a bookstore app. You help users find books, summarize reviews, and discuss literature. Be concise and friendly."))
        messages.forEach { (role, content) ->
            messagesArray.put(JSONObject().put("role", role).put("content", content))
        }

        val body = JSONObject()
            .put("model", "openai/gpt-oss-20b")
            .put("messages", messagesArray)
            .put("max_tokens", 1024)
            .toString()

        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        if (code == HttpURLConnection.HTTP_OK) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
            throw Exception("Groq API Error $code: $err")
        }
    }

    /** Gemini REST API — for AIzaSy keys from Google AI Studio. */
    private fun callGeminiDirect(apiKey: String, prompt: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

        val body = JSONObject().put("contents", JSONArray().put(
            JSONObject().put("parts", JSONArray().put(
                JSONObject().put("text", prompt)
            ))
        )).toString()

        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        if (code == HttpURLConnection.HTTP_OK) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                return candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
            }
            throw Exception("Empty response from Gemini")
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
            throw Exception("Gemini API Error $code: $err")
        }
    }

    fun getBookRecommendations(userPrompt: String, books: List<Book>) {
        if (userPrompt.isBlank()) return
        _recommendState.value = GeminiUiState.Loading
        viewModelScope.launch {
            try {
                val catalog = books.take(40).joinToString("\n") {
                    "- \"${it.title}\" by ${it.author} (Price: \$${it.price})"
                }
                val prompt = """
                    You are an expert bookstore assistant. Here is our catalog:
                    $catalog

                    The user is looking for: "$userPrompt"

                    Recommend 3-5 books from our catalog that best match. For each, give the Title, Author, and 1-2 sentences why they'd enjoy it. Use bullet points. Only recommend books listed above.
                """.trimIndent()

                val result = callAi(listOf("user" to prompt))
                _recommendState.value = GeminiUiState.Success(result)
            } catch (e: Exception) {
                _recommendState.value = GeminiUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun summarizeReviews(bookTitle: String, comments: List<String>) {
        _summaryState.value = GeminiUiState.Loading
        viewModelScope.launch {
            try {
                if (comments.isEmpty()) {
                    _summaryState.value = GeminiUiState.Success("No reviews yet. Be the first to share your thoughts!")
                    return@launch
                }
                val reviewsText = comments.joinToString("\n") { "- $it" }
                val prompt = """
                    Summarize these user reviews for "$bookTitle" in 2-3 concise sentences. Mention overall sentiment, what readers liked, and any common criticisms:
                    $reviewsText
                """.trimIndent()

                val result = callAi(listOf("user" to prompt))
                _summaryState.value = GeminiUiState.Success(result)
            } catch (e: Exception) {
                _summaryState.value = GeminiUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun sendChatMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val updatedMessages = _chatMessages.value.toMutableList()
        updatedMessages.add(ChatMessage(text = userMessage, isUser = true))
        _chatMessages.value = updatedMessages
        _chatState.value = GeminiUiState.Loading

        viewModelScope.launch {
            try {
                // Build conversation history for context (last 10 messages)
                val history = updatedMessages.takeLast(10).map { msg ->
                    val role = if (msg.isUser) "user" else "assistant"
                    role to msg.text
                }

                val reply = callAi(history)

                val finalMessages = _chatMessages.value.toMutableList()
                finalMessages.add(ChatMessage(text = reply, isUser = false))
                _chatMessages.value = finalMessages
                _chatState.value = GeminiUiState.Success(reply)
            } catch (e: Exception) {
                _chatState.value = GeminiUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
        _chatState.value = GeminiUiState.Idle
    }

    fun clearAll() {
        _chatMessages.value = emptyList()
        _chatState.value = GeminiUiState.Idle
        _recommendState.value = GeminiUiState.Idle
        _summaryState.value = GeminiUiState.Idle
    }
}
