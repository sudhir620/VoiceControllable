package com.example.voicecontrollable

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var tts: TextToSpeech

    private lateinit var adapter: TodoAdapter
    lateinit var input: EditText
    lateinit var btnAdd: Button
    private val todoList = mutableListOf<TodoItem>()
    private lateinit var speechRecognizer: SpeechRecognizer

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        input = findViewById(R.id.inputTask)
        btnAdd = findViewById(R.id.btnAdd)
        val btnVoice = findViewById<Button>(R.id.btnVoice)
        val recycler = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = TodoAdapter(todoList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Add button
        btnAdd.setOnClickListener {
            val text = input.text.toString()
            if (text.isNotEmpty()) {
                todoList.add(TodoItem(text))
                adapter.notifyDataSetChanged()
                input.text.clear()
            }
        }

        // Voice setup
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        btnVoice.setOnClickListener {
            tts.speak("Speak now", TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")

            Handler(Looper.getMainLooper()).postDelayed({
                speechRecognizer.startListening(intent)
            }, 1500)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = data?.get(0)?.lowercase() ?: ""

                Log.d("SPEAK_TASKS", "onResults: $spokenText")

                handleVoiceCommand(spokenText)
            }

            override fun onError(error: Int) {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleVoiceCommand(command: String) {
        if (command.startsWith("add")) {
            btnAdd.performClick()
        } else {
            val task = command.removePrefix("add ")
            input.setText(task)
        }
    }
}