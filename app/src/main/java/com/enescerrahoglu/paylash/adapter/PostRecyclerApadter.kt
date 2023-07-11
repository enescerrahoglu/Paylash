package com.enescerrahoglu.paylash.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.enescerrahoglu.paylash.R
import com.enescerrahoglu.paylash.model.PostModel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class PostRecyclerApadter(private val posts : ArrayList<PostModel>) : RecyclerView.Adapter<PostRecyclerApadter.PostHolder>() {
    class PostHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)
        return PostHolder(view)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.recyclerRowUserEmailText).text = posts[position].userEmail
        if(posts[position].description.isNotEmpty()){
            holder.itemView.findViewById<TextView>(R.id.recyclerRowDescriptionText).visibility = View.VISIBLE
            holder.itemView.findViewById<TextView>(R.id.recyclerRowDescriptionText).text = posts[position].description
        }else{
            holder.itemView.findViewById<TextView>(R.id.recyclerRowDescriptionText).visibility = View.GONE
        }
        holder.itemView.findViewById<TextView>(R.id.recyclerRowCreatedDate).text = convertTimestampToDate(posts[position].createdDate.toDate()).toString()
        Picasso.get().load(posts[position].imageUrl).into(holder.itemView.findViewById<ImageView>(R.id.recyclerRowImageView))
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun convertTimestampToDate(timestamp: Date): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm")
        sdf.timeZone = TimeZone.getDefault()
        val date = sdf.format(timestamp)
        return date
    }
}