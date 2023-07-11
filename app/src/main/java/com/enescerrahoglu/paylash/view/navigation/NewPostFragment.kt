package com.enescerrahoglu.paylash.view.navigation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.enescerrahoglu.paylash.R
import com.enescerrahoglu.paylash.databinding.FragmentNewPostBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.min


class NewPostFragment : Fragment() {
    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var firestore : FirebaseFirestore
    var selectedImage : Uri? = null
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true);
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener {
            val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(pickImg)
            //selectImage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_post, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.share_new_post){
            share()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun share(){
        val uuid = UUID.randomUUID()
        val imageName = "${uuid}.jpg"
        val reference = storage.reference
        val imageReference = reference.child("images").child(imageName)
        if(selectedImage != null){
            //selectedImage = compressUriBitmap(requireContext(), selectedImage!!, 1080)

            imageReference.putFile(selectedImage!!).addOnSuccessListener { _ ->
                val imageRef = FirebaseStorage.getInstance().reference.child("images").child(imageName)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val userEmail = auth.currentUser!!.email.toString()
                    val description = binding.descriptionTextField.text.toString()
                    val createdDate = Timestamp.now()
                    val postHashMap = hashMapOf<String, Any>()
                    postHashMap["userEmail"] = userEmail
                    postHashMap["imageUrl"] = imageUrl
                    postHashMap["description"] = description
                    postHashMap["createdDate"] = createdDate

                    firestore.collection("posts").add(postHashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Toast.makeText(requireContext(), "Successful",Toast.LENGTH_LONG).show()
                            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.home
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                    }

                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }
        }else{
            Toast.makeText(requireContext(), "Please select an image!",Toast.LENGTH_LONG).show()
        }
    }

    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                selectedImage = data?.data
                binding.imageView.setPadding(0,0,0,0)
                selectedImage = compressUriBitmap(requireContext(), selectedImage!!, 1080)
                binding.imageView.setImageURI(selectedImage)
            }
        }

    fun selectImage(){
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }else{
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
            selectedImage = data.data
            try {
                context?.let {
                    if(selectedImage != null){
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver, selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView?.let { it ->
                                it.setPadding(0,0,0,0)
                                it.setImageBitmap(selectedBitmap)
                            }
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver, selectedImage)
                            binding.imageView?.let { it ->
                                it.setPadding(0,0,0,0)
                                it.setImageBitmap(selectedBitmap)
                            }
                        }
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun compressUriBitmap(context: Context, uri: Uri, maximumSize: Int): Uri? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val orientation = getOrientation(context, uri)
        val rotatedBitmap = rotateBitmap(bitmap, orientation)

        val croppedBitmap = cropToSquare(rotatedBitmap)

        val compressedBitmap = Bitmap.createScaledBitmap(croppedBitmap!!, maximumSize, maximumSize, true)
        val outputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val compressedBytes = outputStream.toByteArray()
        val compressedUri = getImageUri(context, compressedBytes)
        outputStream.close()

        return compressedUri
    }

    private fun getOrientation(context: Context, uri: Uri): Int {
        val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.ORIENTATION), null, null, null)
        val orientation = cursor?.use {
            if (it.moveToFirst()) {
                it.getInt(0)
            } else {
                0
            }
        } ?: 0
        cursor?.close()
        return orientation
    }

    private fun rotateBitmap(bitmap: Bitmap?, degrees: Int): Bitmap? {
        if (degrees == 0 || bitmap == null) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getImageUri(context: Context, imageBytes: ByteArray): Uri? {
        val imagePath = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size),
            Timestamp.now().seconds.toString(),
            null
        )
        return Uri.parse(imagePath)
    }

    private fun cropToSquare(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null

        val width = bitmap.width
        val height = bitmap.height
        val size = min(width, height)

        val x = (width - size) / 2
        val y = (height - size) / 2

        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
}