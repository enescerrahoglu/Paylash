package com.enescerrahoglu.paylash.view.navigation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.enescerrahoglu.paylash.model.PostModel
import com.enescerrahoglu.paylash.adapter.PostRecyclerApadter
import com.enescerrahoglu.paylash.databinding.FragmentHomeBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var recyclerViewAdapter : PostRecyclerApadter
    lateinit var swipeContainer: SwipeRefreshLayout

    var posts = ArrayList<PostModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        getPosts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = PostRecyclerApadter(posts)
        binding.recyclerView.adapter = recyclerViewAdapter

        swipeContainer = binding.homeSwipeRefreshLayout
        swipeContainer.setOnRefreshListener {
            getPosts()
        }
    }

    private fun getPosts(){
        posts.clear()
        firestore.collection("posts").orderBy("createdDate", Query.Direction.DESCENDING).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            } else {
                if (snapshot != null) {
                    if (!snapshot.isEmpty) {
                        val documents = snapshot.documents
                        for (document in documents) {
                            val userEmail = document.get("userEmail") as String
                            val imageUrl = document.get("imageUrl") as String
                            val description = document.get("description") as String
                            val createdDate = document.get("createdDate") as Timestamp
                            val post = PostModel(userEmail, imageUrl, description, createdDate)
                            posts.add(post)
                        }
                    }
                }
                recyclerViewAdapter.notifyDataSetChanged()
                swipeContainer.isRefreshing = false
            }
        }
    }
}