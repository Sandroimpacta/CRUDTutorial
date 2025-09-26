package com.example.crudtutorial

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var bancoDados: SQLiteDatabase
    private lateinit var listViewDados: ListView
    private lateinit var botaoInserir: Button
    private lateinit var botaoDeletar: Button
    private lateinit var editTextNome: EditText
    private lateinit var editTextIdade: EditText

    private var idSelecionado: Int? = null  // Id do item selecionado na lista

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar views
        listViewDados = findViewById(R.id.ListViewDados)
        botaoInserir = findViewById(R.id.buttonInserir)
        botaoDeletar = findViewById(R.id.buttonDeletar)
        editTextNome = findViewById(R.id.editTextNome)
        editTextIdade = findViewById(R.id.editTextIdade)

        criarBancoDados()
        listarDados()

        botaoInserir.setOnClickListener {
            val nome = editTextNome.text.toString().trim()
            val idadeStr = editTextIdade.text.toString().trim()

            if (nome.isEmpty() || idadeStr.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha nome e idade", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idade = idadeStr.toIntOrNull()
            if (idade == null || idade <= 0) {
                Toast.makeText(this, "Idade inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            inserirDados(nome, idade)
            listarDados()

            editTextNome.text.clear()
            editTextIdade.text.clear()
            idSelecionado = null
            Toast.makeText(this, "Pessoa inserida", Toast.LENGTH_SHORT).show()
        }

        botaoDeletar.setOnClickListener {
            if (idSelecionado != null) {
                deletarPessoa(idSelecionado!!)
                listarDados()
                Toast.makeText(this, "Pessoa deletada", Toast.LENGTH_SHORT).show()
                idSelecionado = null
            } else {
                Toast.makeText(this, "Selecione uma pessoa para deletar", Toast.LENGTH_SHORT).show()
            }
        }

        listViewDados.setOnItemClickListener { _, _, position, _ ->
            val cursor = bancoDados.rawQuery("SELECT id FROM pessoas", null)
            if (cursor.moveToPosition(position)) {
                idSelecionado = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                Toast.makeText(this, "Selecionado ID: $idSelecionado", Toast.LENGTH_SHORT).show()
            }
            cursor.close()
        }
    }

    private fun criarBancoDados() {
        try {
            bancoDados = openOrCreateDatabase("crud.db", MODE_PRIVATE, null)
            bancoDados.execSQL(
                "CREATE TABLE IF NOT EXISTS pessoas (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nome TEXT, " +
                        "idade INTEGER)"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun inserirDados(nome: String, idade: Int) {
        try {
            val sql = "INSERT INTO pessoas (nome, idade) VALUES (?, ?)"
            val stmt: SQLiteStatement = bancoDados.compileStatement(sql)
            stmt.bindString(1, nome)
            stmt.bindLong(2, idade.toLong())
            stmt.executeInsert()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deletarPessoa(id: Int) {
        try {
            bancoDados.execSQL("DELETE FROM pessoas WHERE id = ?", arrayOf(id.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun listarDados() {
        try {
            val cursor = bancoDados.rawQuery("SELECT * FROM pessoas", null)
            val dados = ArrayList<String>()

            if (cursor.moveToFirst()) {
                do {
                    val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                    val idade = cursor.getInt(cursor.getColumnIndexOrThrow("idade"))
                    dados.add("$nome - $idade anos")
                } while (cursor.moveToNext())
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dados)
            listViewDados.adapter = adapter

            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
