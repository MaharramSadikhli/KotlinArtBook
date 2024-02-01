package com.imsoft.kotlinartbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.imsoft.kotlinartbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artBookList: ArrayList<ArtBook>
    private lateinit var artBookAdapter: ArtBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artBookList = ArrayList<ArtBook>()


        artBookAdapter = ArtBookAdapter(artBookList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.recyclerView.adapter = artBookAdapter

        try {

            val db = this.openOrCreateDatabase("ArtBookDB", MODE_PRIVATE, null)

            val cursor = db.rawQuery("SELECT * FROM artbook", null)
            val idIndex = cursor.getColumnIndex("id")
            val nameIndex = cursor.getColumnIndex("artname")

            while (cursor.moveToNext()) {

                val id = cursor.getInt(idIndex)
                val name = cursor.getString(nameIndex)
                val artBook = ArtBook(id, name)

                artBookList.add(artBook)
            }

            artBookAdapter.notifyDataSetChanged()


            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater

        menuInflater.inflate(R.menu.menu_artbook, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menuItem) {

            val intent = Intent(this@MainActivity, DetailsActivity::class.java)
            intent.putExtra("art", "newArt")
            startActivity(intent)

        }

        return super.onOptionsItemSelected(item)
    }

}