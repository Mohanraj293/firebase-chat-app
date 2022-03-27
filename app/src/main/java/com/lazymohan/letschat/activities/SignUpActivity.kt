package com.lazymohan.letschat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.firebase.firestore.FirebaseFirestore
import com.lazymohan.letschat.databinding.ActivitySignUpBinding
import com.lazymohan.letschat.utils.Constants
import com.lazymohan.letschat.utils.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {

  private lateinit var binding: ActivitySignUpBinding
  private var encodedImage: String? = null
  private lateinit var preferenceManager: PreferenceManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySignUpBinding.inflate(layoutInflater)
    setContentView(binding.root)
    preferenceManager = PreferenceManager(applicationContext)
    setListeners()
  }

  private fun setListeners() {
    binding.textSignIn.setOnClickListener {
      onBackPressed()
    }
    binding.signUpBtn.setOnClickListener {
      if (isValidSignUpDetails()) {
        signUp()
      }
    }
    binding.layoutImage.setOnClickListener {
      val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      pickImage.launch(intent)
    }
  }

  private fun showToastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  private fun signUp() {
    loading(true)
    val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    val user: HashMap<String, Any> = HashMap()
    user.put(Constants.KEY_NAME, binding.inputName.text.toString())
    user.put(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
    user.put(Constants.KEY_PASSWORD, binding.inputPassword.text.toString())
    user.put(Constants.KEY_IMAGE, encodedImage!!)
    database.collection(Constants.KEY_COLLECTION_USERS)
        .add(user)
        .addOnSuccessListener {
          loading(false)
          preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
          preferenceManager.putString(Constants.KEY_USER_ID, it.id)
          preferenceManager.putString(Constants.KEY_NAME, binding.inputName.text.toString())
          preferenceManager.putString(Constants.KEY_IMAGE,encodedImage!!)
          val intent = Intent(applicationContext,MainActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
          startActivity(intent)
        }
        .addOnFailureListener {
          loading(false)
          showToastMessage(it.message.toString())
        }
  }

  private fun encodedImage(bitmap: Bitmap): String {
    val previewWidth: Int = 150
    val previewHeight: Int = bitmap.height * previewWidth / bitmap.width
    val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
    val byteArrayOutputStream = ByteArrayOutputStream()
    previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
    var bytes: ByteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
  }

  private val pickImage: ActivityResultLauncher<Intent> =
    registerForActivityResult(StartActivityForResult()) { result ->
      if (result.resultCode == RESULT_OK) {
        if (result.data != null) {
          val imageUri: Uri? = result.data!!.data
          try {
            val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.imageProfile.setImageBitmap(bitmap)
            binding.textAddImage.visibility = View.GONE
            encodedImage = encodedImage(bitmap)
          } catch (e: FileNotFoundException) {
            e.printStackTrace()
          }
        }
      }
    }

  private fun isValidSignUpDetails(): Boolean {
    if (encodedImage == null){
      showToastMessage("Select Profile Picture")
      return false
    }
    if (binding.inputName.text.toString().trim().isEmpty()) {
      showToastMessage("Enter Name")
      return false
    } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
      showToastMessage("Enter Email")
      return false
    } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
      showToastMessage("Enter valid Email")
      return false
    } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
      showToastMessage("Enter Password")
      return false
    } else if (binding.inputConfirmPassword.text.toString().trim().isEmpty()) {
      showToastMessage("Confirm your Password")
      return false
    } else if (!binding.inputPassword.text.toString()
            .equals(binding.inputConfirmPassword.text.toString())
    ) {
      showToastMessage("Password and Confirm Password must be same")
      return false
    } else return true
  }

  private fun loading(isLoading: Boolean) {
    if (isLoading) {
      binding.signUpBtn.visibility = View.INVISIBLE
      binding.progresBar.visibility = View.VISIBLE
    } else {
      binding.progresBar.visibility = View.INVISIBLE
      binding.signUpBtn.visibility = View.VISIBLE
    }
  }
}