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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        realTimeDatabaseManager = RealTimeDatabaseManager() // Inicializa tu RealTimeDatabaseManager
        auth = FirebaseAuth.getInstance()
        setClicks()
    }

    // TODO FUNCIONA EL SIGN UP PERO SE PUEDE MEJORAR EL RENDIMIENTO,
    //TODO AGREGAR UN SEGUNDO CAMPO DE PSSWORD

    //region --- UI Related ---
    // Limpia los capos de texto
    private fun cleanData() {
        binding.etSignUpEmail.setText("")
        binding.etSignUpPassword.setText("")
        binding.etSignUpName.setText("")
        binding.etSignUpUserName.setText("")
    }

    // Maneja las funciones de los botones
    private fun setClicks() {
        binding.btnSignUp.setOnClickListener {
            val userEmail = binding.etSignUpEmail.text.toString().trim()
            val userPassword = binding.etSignUpPassword.text.toString().trim()
            val h_userName: String = binding.etSignUpUserName.text.toString().trim()
            val h_fullName: String = binding.etSignUpName.text.toString().trim()
            if (isDataValid()) {
                createFirebaseUser(userEmail, userPassword, h_userName, h_fullName)
            } else {
                showInvalidDataMessage()
            }
        }
    }

    //endregion --- UI Related ---

    //region --- Firebase ---
    private fun createFirebaseUser(email: String, password: String, userName: String, fullName: String) {
        val firebaseAuth = EmailAndPasswordAuthenticationManager()
        lifecycleScope.launch(Dispatchers.IO) {
            if (email.isNotEmpty() && password.isNotEmpty()) {
                val resultIsSuccessful =
                    firebaseAuth.createUserFirebaseEmailAndPassword(email, password)
                if (resultIsSuccessful) {
// TODO Si el email ya esta registrado y lo queremos registrar nuevamente, la app peta
                    // Si la respuesta es exitosa, cojemos el UID y usandol oguardamos el resto de campos del user
                    var uid: String = ""
                    uid = auth.currentUser!!.uid
                    reference = FirebaseDatabase.getInstance().reference.child("users").child(uid)

                    val hashMap = HashMap<String, Any>()
                    hashMap["uid"] = uid
                    hashMap["userName"] = userName
                    hashMap["userEmail"] = email
                    hashMap["userPassword"] = password
                    hashMap["userFullName"] = fullName

                    reference.updateChildren(hashMap).addOnCompleteListener {}
                        .addOnFailureListener {}
                    Log.i("createFirebaseMailAndPasswordUser", "Usuario creado en Firebase")

                    // Creamos usuario en dataStore
                    dataStoreManager.saveUserData(email, password, uid, fullName, userName)
                    Log.i("saveUserData", "Usuario creado en DataStore")

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), R.string.sign_up_saved_user_message, Toast.LENGTH_SHORT).show()
                    }
                    // Volvemos al login
                    parentFragmentManager.popBackStack()
                } else {
                    Log.e("createFirebaseMailAndPasswordUser", "ERROR AL CREAR EL USUARIO")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), R.string.sign_up_failed_error, Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            }
        }
        cleanData()
    }
    //endregion --- Firebase ---

    //region --- Data validation ---
    private fun isDataValid(): Boolean {
        // Expresiones regulares para email y password
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}".toRegex()
        val passwordPattern = ".{6,10}".toRegex()
        val email = binding.etSignUpEmail.text.toString().trim()
        val password = binding.etSignUpPassword.text.toString().trim()
        val userName = binding.etSignUpUserName.text.toString().trim()
        val fullName = binding.etSignUpName.text.toString().trim()

        return email.matches(emailPattern) && email.isNotEmpty() &&
                password.matches(passwordPattern) && password.isNotEmpty() &&
                userName.isNotEmpty() && fullName.isNotEmpty()
    }
    //endregion --- Data validation ---


    //region --- Messages ---
     private fun showWelcomeMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_saved_user_message, Toast.LENGTH_SHORT)
            .show()
    }

    private fun showInvalidDataMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_invalid_data_error, Toast.LENGTH_SHORT)
            .show()
    }

   private fun showNameError() {
        Toast.makeText(requireContext(), R.string.sign_up_name_taken_error, Toast.LENGTH_SHORT)
            .show()
    }

    private fun showFailMessage() {
        Toast.makeText(requireContext(), R.string.sign_up_failed_error, Toast.LENGTH_SHORT)
            .show()
    }
    //endregion --- Messages ---

    //region --- Navigation ---
//    private fun goToSmsVerification(phoneNumber: String) {
//        val transaction = parentFragmentManager.beginTransaction()
//        val bundle = Bundle()
//        bundle.putString("phoneNumber", phoneNumber)
//        val fragment = SmsVerificationCodeFragment()
//        fragment.arguments = bundle
//
//        transaction.setReorderingAllowed(true)
//            .replace(R.id.fcv_login, fragment)
//            .addToBackStack(null)
//            .commit()
//    }
//    //endregion --- Navigation ---

}
