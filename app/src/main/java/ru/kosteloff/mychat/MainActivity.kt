package ru.kosteloff.mychat

import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import ru.kosteloff.mychat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var userAdapter: UserAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setUpActionBar()
        val database =
            Firebase.database("https://mychat-3cb2f-default-rtdb.europe-west1.firebasedatabase.app")
        val myRef = database.getReference("message")

        binding.buttonSend.setOnClickListener {
            myRef.child(myRef.push().key ?: "=)")
                .setValue(User(auth.currentUser?.displayName, binding.editText.text.toString()))
        }

        onChangeListener(myRef)
        initRecyclerView()
    }

    private fun initRecyclerView() = with(binding) {
        userAdapter = UserAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = userAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sign_out_menu) {
            auth.signOut()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onChangeListener(listener: DatabaseReference) {
        listener.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<User>()
                for (i in snapshot.children) {
                    val user = i.getValue(User::class.java)
                    if (user != null) {
                        list.add(user)
                    }
                }
                userAdapter.submitList(list)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setUpActionBar() {
        val actionBar = supportActionBar
        Thread { // поток
            val loadPic =
                Picasso.get().load(auth.currentUser?.photoUrl).get()
            val drawableIcon = BitmapDrawable(resources, loadPic)
            runOnUiThread {
                actionBar?.setDisplayHomeAsUpEnabled(true)
                actionBar?.setHomeAsUpIndicator(drawableIcon)
            }
        }.start()
    }
}