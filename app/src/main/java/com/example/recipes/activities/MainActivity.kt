package com.example.recipes.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.recipes.R
import com.example.recipes.data.daos.RecipeDAO
import com.example.recipes.data.entities.Recipe
import com.example.recipes.data.serviceapis.RecipeServiceApi
import com.example.recipes.databinding.ActivityMainBinding
import com.example.recipes.utils.RetrofitProvider
import com.example.recipes.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate de que esta línea esté antes de `findViewById`

        val btnCambiarIdioma = findViewById<Button>(R.id.btnCambiarIdioma)

        btnCambiarIdioma.setOnClickListener {
            cambiarIdioma(this, "es") // Llama a la función que cambia el idioma
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        if (session.didFetchData) {
            navigateToRecipesList()
        } else {
            getAllRecipes()
        }

    }


    fun cambiarIdioma(context: Context, idioma: String) {
        val locale = Locale(idioma)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Reiniciar actividad para aplicar los cambios
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }




    private fun getAllRecipes() {
        val service: RecipeServiceApi = RetrofitProvider.getRecipeServiceApi()

        CoroutineScope(Dispatchers.IO).launch {
            // Llamada en segundo plano
            val response = service.findAll()

            runOnUiThread {
                // Modificar UI
                if (response.body() != null) {
                    Log.i("HTTP", "respuesta correcta :)")
                    var recipesList = response.body()?.results.orEmpty()

                    val recipeDAO = RecipeDAO(this@MainActivity)
                    recipeDAO.deleteAll()
                    for (recipe in recipesList) {
                        recipeDAO.insert(recipe)
                    }

                    session.didFetchData = true
                    navigateToRecipesList()
                } else {
                    Log.i("HTTP", "respuesta erronea :(")
                    Toast.makeText(this@MainActivity, "Hubo un error inesperado, vuelva a intentarlo más tarde", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun navigateToRecipesList() {
        val intent = Intent(this, RecipesActivity::class.java)
        startActivity(intent)
    }
}