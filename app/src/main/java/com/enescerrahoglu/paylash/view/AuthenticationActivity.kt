package com.enescerrahoglu.paylash.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.enescerrahoglu.paylash.databinding.ActivityAuthenticationBinding
import com.enescerrahoglu.paylash.view.navigation.NavigationActivity
import com.google.firebase.auth.FirebaseAuth

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if(currentUser != null){
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun login(view:View){
        val email = binding.emailTextField.text.toString()
        val password = binding.passwordTextField.text.toString()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val intent = Intent(this, NavigationActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception->
            Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    fun register(view:View){
        val email = binding.emailTextField.text.toString()
        val password = binding.passwordTextField.text.toString()
        if(email.isNotEmpty() && password.length >= 8){
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext, "Your account has been successfully created.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, NavigationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener{exception->
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(applicationContext, "Please enter your information correctly.", Toast.LENGTH_LONG).show()
        }

    }
}