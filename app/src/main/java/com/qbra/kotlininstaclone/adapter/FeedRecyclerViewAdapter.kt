package com.qbra.kotlininstaclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qbra.kotlininstaclone.databinding.RecyclerRowBinding
import com.qbra.kotlininstaclone.model.Post
import com.squareup.picasso.Picasso

class FeedRecyclerViewAdapter(val postArrayList: ArrayList<Post>) : RecyclerView.Adapter<FeedRecyclerViewAdapter.FeedHolder>(){
    class FeedHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedHolder(binding)
    }

    override fun getItemCount(): Int {
        return postArrayList.size
    }

    override fun onBindViewHolder(holder: FeedHolder, position: Int) {
        holder.binding.userMailText.text = postArrayList[position].email
        holder.binding.commentText.text = postArrayList[position].comment
        Picasso.get().load(postArrayList[position].downloadUrl).into(holder.binding.recyclerViewImageView)
    }
}