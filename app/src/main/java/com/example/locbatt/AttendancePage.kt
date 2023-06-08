package com.example.locbatt

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.locbatt.databinding.FragmentAttendancePageBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AttendancePage.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class AttendancePage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener?= null
    private lateinit var binding : FragmentAttendancePageBinding
    private lateinit var courseName : TextView
    private lateinit var courseDesc : TextView
    private lateinit var professorName : TextView
    private lateinit var attendanceBtn : Button
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val arr = ArrayList<String>()
    private lateinit var navBtn : BottomNavigationView
  //  private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,com.example.locbatt.R.layout.fragment_attendance_page,container,false)
        courseName = binding.courseName
        courseDesc = binding.courseDescrp
        attendanceBtn = binding.attendance
        professorName = binding.professorName
        navBtn = binding.btnNav3
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)

        val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
        model.id.observe(this.viewLifecycleOwner,object: Observer<Any> {
            override fun onChanged(t: Any?) {
                val id = t.toString()!!
                // Log.d("Hey the id " , id)
                val courseRef = FirebaseDatabase.getInstance().getReference("Course")
                courseRef.orderByChild("courseId").equalTo(id!!).addListenerForSingleValueEvent(object:
                    ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(p0: DataSnapshot) {
                        for(e in p0.children){
                            val course = e.getValue(Course::class.java)
                            courseName.text = course?.courseName
                            courseDesc.text = course?.courseDescription
                            professorName.text = course?.professorName
                        }
                    }

                })
                detectAttendanceFun(id)
                getLocation(id)



                navBtn.setOnNavigationItemReselectedListener { item->
                    when(item.itemId){
                        com.example.locbatt.R.id.backHome2->{
                            backFun()
                        }
                        com.example.locbatt.R.id.dropCourse->{
                            dropClass(id!!)
                        }
                    }
                }


            }
        })

        val ft = fragmentManager!!.beginTransaction()
        if(Build.VERSION.SDK_INT>=26){
            ft.setReorderingAllowed(false)
        }
        ft.detach(this).attach(this)


        return binding.root

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getLocation(courseId: String){
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mFusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if(location!= null){
                    var tLatitude = 0.0
                    var tLongtitude = 0.0

                    val ref = FirebaseDatabase.getInstance().getReference("TeacherLocation")
                    ref.orderByChild("courseId").equalTo(courseId)
                        .addValueEventListener(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                for(e in p0.children){
                                    val tLocation = e.getValue(TeacherLocation::class.java)
                                    if (tLocation != null) {
                                        tLatitude = tLocation.latitude
                                    }
                                    if (tLocation != null) {
                                        tLongtitude = tLocation.longtitude
                                    }
                                }

                                val arr = FloatArray(1)
                                val distanceBtn1and2 = Location.distanceBetween(tLatitude,tLongtitude, location.latitude,location.longitude,arr)
                                Log.d("The teacher location is ",distanceBtn1and2.toString())
                                Log.d("The student location is " , location.latitude.toString() + " - " + location.longitude.toString())
                                Log.d("The arr", arr[0].toString())


                                if(arr[0] < 60.0){
                                 //   attendanceBtn.setOnClickListener { this@AttendancePage.takeAttendance(courseId) }
                                    attendanceBtn.setOnClickListener {
                                        val biometricPrompt =BiometricPrompt(requireActivity(),object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                // Authentication was successful, take attendance here
                                                this@AttendancePage.takeAttendance(courseId)
                                            }
                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                // Authentication error occurred, show error message here
                                                Toast.makeText(requireContext(), "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                                            }
                                            override fun onAuthenticationFailed() {
                                                // Authentication failed, show error message here
                                                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                            .setTitle("Fingerprint authentication")
                                            .setSubtitle("Use your fingerprint to take attendance")
                                            .setNegativeButtonText("Cancel")
                                            .build()
                                        biometricPrompt.authenticate(promptInfo)
                                    }
                                }else{
                                    attendanceBtn.setOnClickListener {
                                        val builder = AlertDialog.Builder(context)
                                        builder.setTitle("FATAL")
                                        builder.setMessage("YOU ARE NOT IN THE CLASSROOM!!")
                                        builder.setIcon(R.drawable.angry)
                                        builder.setPositiveButton("OK") { _, _ -> }
                                        val dialog = builder.create()
                                        dialog.show()
                                    }
                                }


                            }
                        })

                }
                else{
                    Toast.makeText(context!!,"Location is null", Toast.LENGTH_LONG).show()
                }


            }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun takeAttendance(courseId: String){

        val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)

        model.name.observe(this, object : Observer<Any> {
            @SuppressLint("SimpleDateFormat")
            override fun onChanged(t: Any?) {
                val name = t.toString()!!

                arr.add(name)
                Log.d("name is ", name)
                val attendanceResult =
                    FirebaseDatabase.getInstance().getReference("AttendanceResult")
                val key = attendanceResult.push().key
                val attendance = AttendanceResult(courseId, name)
                attendanceResult.child(key!!).setValue(attendance)

                val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
                val currentDate = sdf.format(Date())
                val record = Record(
                    courseId,
                    currentDate, arr
                )
                val ref1 = FirebaseDatabase.getInstance().getReference("Record")
                val keys = ref1.push().key!!
                ref1.child(keys).setValue(record)
            }
        })

        attendanceBtn.alpha = 0.5f
        attendanceBtn.isEnabled = false
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Success")
        builder.setMessage("Your attendance is recorded!")
        builder.setPositiveButton("Ok") { dialog, which ->

        }
        val alert = builder.create()
        alert.show()


    }

    private fun detectAttendanceFun(courseId : String){
        val attendanceIndicatorRef = FirebaseDatabase.getInstance().getReference("AttendanceIndicator")
        attendanceIndicatorRef.orderByChild("courseId").equalTo(courseId).addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        for(e in p0.children){
                            Log.d("first key",e.key!!)
                            val status = e.getValue(AttendanceIndicator::class.java)
                            if(status?.status == true){
                                Log.d("status is ", status?.status.toString())
                                attendanceBtn.alpha = 1.0f
                                attendanceBtn.isEnabled = true
                            }else{
                                Log.d("status is ", status?.status.toString())
                                attendanceBtn.alpha = .5f
                                attendanceBtn.isEnabled = false
                            }

                        }
                    }
                }

            }
        )
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun dropClass(courseId: String){
        val user = FirebaseAuth.getInstance().currentUser
        val studentRef = FirebaseDatabase.getInstance().getReference("Student")
        studentRef.orderByChild("email").equalTo(user!!.email).addListenerForSingleValueEvent(
            object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    for(e in p0.children){
                        Log.d("e.child is ", e.getValue().toString())
                        studentRef.child(e.key!!).child("courseId").addListenerForSingleValueEvent(
                            object : ValueEventListener{
                                override fun onDataChange(p1: DataSnapshot) {
                                    for(e1 in p1.children) {
                                        Log.d("e1.child is ", e1.getValue().toString())
                                        if(e1.getValue().toString() == courseId){
                                            studentRef.child(e.key!!).child("courseId").child(e1.key!!).removeValue()
                                        }
                                    }
                                }

                                override fun onCancelled(p1: DatabaseError) {}
                            }
                        )
                    }
                }
            }
        )
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Dropped")
        builder.setMessage("This course is successfully dropped!")
        builder.setIcon(com.example.locbatt.R.drawable.sad)
        builder.setPositiveButton("Ok"){dialog, which ->
            if(findNavController().currentDestination?.id == com.example.locbatt.R.id.attendancePage) {
                val user = FirebaseAuth.getInstance().currentUser
                val model =
                    ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
                model.setMsgCommunicator(user?.email!!)
                val myFragment = StudentHomePage()
                val fragmentTransaction = fragmentManager!!.beginTransaction()
                fragmentTransaction.replace(com.example.locbatt.R.id.myNavHostFragment, myFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
                findNavController().navigate(com.example.locbatt.R.id.action_attendancePage_to_studentHomePage)
            }
        }
        val alert = builder.create()
        alert.show()


    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun backFun(){

        if(findNavController().currentDestination?.id == com.example.locbatt.R.id.attendancePage) {
            val user = FirebaseAuth.getInstance().currentUser
            val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
            model.setMsgCommunicator(user?.email!!)
            val myFragment = StudentHomePage()
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(com.example.locbatt.R.id.myNavHostFragment, myFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            findNavController().navigate(com.example.locbatt.R.id.action_attendancePage_to_studentHomePage)
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AttendancePage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AttendancePage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

