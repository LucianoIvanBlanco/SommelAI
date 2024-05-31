import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blanco.somelai.R
import com.blanco.somelai.data.firebase.authentification.EmailAndPasswordAuthenticationManager
import com.blanco.somelai.data.firebase.realtime_database.RealTimeDatabaseManager
import com.blanco.somelai.data.storage.DataStoreManager
import com.blanco.somelai.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {

    private lateinit var realTimeDatabaseManager: RealTimeDatabaseManager
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var _binding: FragmentSignUpBinding
    private val binding: FragmentSignUpBinding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext())
        realTimeDatabaseManager = RealTimeDatabaseManager()
        auth = FirebaseAuth.getInstance()
        setClicks()
    }

    private fun cleanData() {
        binding.etSignUpEmail.setText("")
        binding.etSignUpPassword.setText("")
        binding.etSignUpName.setText("")
        binding.etSignUpUserName.setText("")
        binding.etRepeatPassword.setText("")
    }

    private fun setClicks() {
        binding.btnSignUp.setOnClickListener {
            val userEmail = binding.etSignUpEmail.text.toString().trim()
            val userPassword = binding.etSignUpPassword.text.toString().trim()
            val userName: String = binding.etSignUpUserName.text.toString().trim()
            val fullName: String = binding.etSignUpName.text.toString().trim()
            if (isDataValid()) {
                lifecycleScope.launch {
                    if (isEmailRegistered(userEmail)) {
                        withContext(Dispatchers.Main) {
                            showEmailAlreadyRegisteredMessage()
                        }
                    } else {
                        createFirebaseUser(userEmail, userPassword, userName, fullName)
                    }
                }
            } else {
                showInvalidDataMessage()
            }
        }
    }

    private suspend fun isEmailRegistered(email: String): Boolean {
        val firebaseAuth = EmailAndPasswordAuthenticationManager()
        return firebaseAuth.isEmailRegistered(email)
    }

    private fun createFirebaseUser(email: String, password: String, userName: String, fullName: String) {
        val firebaseAuth = EmailAndPasswordAuthenticationManager()
        lifecycleScope.launch(Dispatchers.IO) {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                try {
                    val resultIsSuccessful = firebaseAuth.createUserFirebaseEmailAndPassword(email, password)
                    if (resultIsSuccessful) {
                        val uid = auth.currentUser!!.uid
                        reference = FirebaseDatabase.getInstance().reference.child("users").child(uid)

                        // Creamos usuario en dataStore
                        saveUserToDataStore(email, password, uid, fullName, userName)

                        val hashMap = HashMap<String, Any>()
                        hashMap["uid"] = uid
                        hashMap["userName"] = userName
                        hashMap["userEmail"] = email
                        hashMap["userPassword"] = password
                        hashMap["userFullName"] = fullName

                        reference.updateChildren(hashMap).addOnCompleteListener {}
                            .addOnFailureListener {}
                        Log.i("createFirebaseMailAndPasswordUser", "Usuario creado en Firebase")

                        withContext(Dispatchers.Main) {
                            showWelcomeMessage()
                            parentFragmentManager.popBackStack()
                        }
                    } else {
                        Log.e("createFirebaseMailAndPasswordUser", "ERROR AL CREAR EL USUARIO")
                        withContext(Dispatchers.Main) {
                            showFailMessage()
                        }
                    }
                } catch (e: FirebaseAuthUserCollisionException) {
                    withContext(Dispatchers.Main) {
                        showEmailAlreadyRegisteredMessage()
                    }
                }
            }
        }
        cleanData()
    }

    private suspend fun saveUserToDataStore(email: String, password: String, uid: String, fullName: String, userName: String) {
        Log.d("saveUserToDataStore", "Guardando datos: email=$email, password=$password, uid=$uid, fullName=$fullName, userName=$userName")
        dataStoreManager.saveUserData(email, password, uid, fullName, userName)
        Log.i("saveUserData", "Usuario creado en DataStore")
    }

    private fun isDataValid(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}".toRegex()
        val passwordPattern = ".{6,10}".toRegex()
        val userNamePattern = "^[a-zA-Z0-9]{4,}$".toRegex()
        val fullNamePattern = "^[a-zA-Z]{3,20}\\s[a-zA-Z]{4,20}$".toRegex()
        val email = binding.etSignUpEmail.text.toString().trim()
        val password = binding.etSignUpPassword.text.toString().trim()
        val repeatPassword = binding.etRepeatPassword.text.toString().trim()
        val userName = binding.etSignUpUserName.text.toString().trim()
        val fullName = binding.etSignUpName.text.toString().trim()

        if (!email.matches(emailPattern) || email.isEmpty()) {
            return false
        }

        if (!userName.matches(userNamePattern) || userName.isEmpty()) {
            return false
        }

        if (!fullName.matches(fullNamePattern) || fullName.isEmpty()) {
            return false
        }

        if (!password.matches(passwordPattern) || password.isEmpty()) {
            return false
        }
        if (!repeatPassword.matches(passwordPattern) || repeatPassword.isEmpty()) {
            return false
        }
        if (password != repeatPassword) {
            showPasswordMismatchMessage()
            return false
        }

        return true
    }

    private fun showPasswordMismatchMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_password_mismatch_error, Toast.LENGTH_SHORT).show()
    }

    private fun showWelcomeMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_saved_user_message, Toast.LENGTH_SHORT).show()
    }

    private fun showInvalidDataMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_invalid_data_error, Toast.LENGTH_SHORT).show()
    }

    private fun showEmailAlreadyRegisteredMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_email_taken_error, Toast.LENGTH_SHORT).show()
    }

    private fun showFailMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_failed_error, Toast.LENGTH_SHORT).show()
    }
}
