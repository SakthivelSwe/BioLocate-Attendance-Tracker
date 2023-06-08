package com.example.locbatt

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.locbatt.databinding.FragmentStudentHomePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentHomePage.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudentHomePage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var binding : FragmentStudentHomePageBinding
    private lateinit var addClassBtn : Button
    private lateinit var name : TextView
    private var temp : String?= null
    private lateinit var courseList : RecyclerView
    //private lateinit var databaseReference : DatabaseReference
    private var arrayList = ArrayList<Course>()
    private lateinit var btmNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet", "FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,com.example.locbatt.R.layout.fragment_student_home_page,container,false)
        name = binding.nameOfStudent
//        addClassBtn = binding.button3
        courseList = binding.courseList
        btmNav = binding.bottomNav
        val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
        model.message.observe(this,object: Observer<Any> {
            override fun onChanged(t: Any?) {
                temp = t!!.toString()
                var key = ""
                val ref = FirebaseDatabase.getInstance().reference
                val ordersRef = ref.child("Student").orderByChild("email").equalTo(temp)
                val valueEventListener = object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()) {
                            for (ds in p0.children) {
                                val nameTemp =
                                    ds.child("firstName").getValue(String::class.java) + " " + ds.child(
                                        "lastName"
                                    ).getValue(String::class.java)

                                key = ds.key!!
                                //  Log.d("zaccccc",key)
                                name.text = nameTemp
                            }
                            sendKeyToEnrollment(key)


                            val courseRef = FirebaseDatabase.getInstance().getReference("Course")
                            val databaseReference = FirebaseDatabase.getInstance().getReference("Student")
                            ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}
                                override fun onDataChange(p0: DataSnapshot) {
                                    for (e in p0.children) {
                                        arrayList.clear()
                                        // this for first code Log.d("jimmm", e.key)
                                        //This is second code
                                        Log.d("jimmm", e.key.toString())
                                        databaseReference.child(e.key!!)
                                            .child("courseId")
                                            .addValueEventListener(
                                                object : ValueEventListener {
                                                    override fun onDataChange(p2: DataSnapshot) {

                                                        for (e2 in p2.children) {
                                                            Log.d("e2",e2.getValue().toString())
                                                            courseRef.orderByChild(
                                                                "courseId"
                                                            ).equalTo(
                                                                e2.getValue(
                                                                    String::class.java)
                                                            )
                                                                .addListenerForSingleValueEvent(
                                                                    object :
                                                                        ValueEventListener {
                                                                        override fun onCancelled(
                                                                            p3: DatabaseError
                                                                        ) {
                                                                        }

                                                                        override fun onDataChange(
                                                                            p3: DataSnapshot
                                                                        ) {

                                                                            for (e3 in p3.children) {

                                                                                val course =
                                                                                    e3.getValue(
                                                                                        Course::class.java
                                                                                    )
                                                                                arrayList.add(
                                                                                    course!!
                                                                                )
                                                                            }

                                                                            val adapter =
                                                                                CourseAdapter(
                                                                                    arrayList,"Student"
                                                                                )
                                                                            courseList.adapter =
                                                                                adapter
                                                                        }

                                                                    })

                                                        }
                                                    }

                                                    override fun onCancelled(p2: DatabaseError) {}
                                                }
                                            )
                                    }
                                }
                            })
                        }


                    }
                    override fun onCancelled(p0: DatabaseError) {
                    }

                }
                ordersRef.addListenerForSingleValueEvent(valueEventListener)
            }
        })

        courseList.addOnItemTouchListener(RecyclerItemClickListener(context!!, courseList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if(findNavController().currentDestination?.id == com.example.locbatt.R.id.studentHomePage) {
                    model.setIdCommunicator(CourseAdapter(arrayList,"Student").getID(position))
                    Log.d("I clicked ", CourseAdapter(arrayList,"Student").getID(position))
                    model.setNameCommunicator(name.text.toString())
                    val myFragment = AttendancePage()
                    val fragmentTransaction = fragmentManager!!.beginTransaction()
                    fragmentTransaction.replace(com.example.locbatt.R.id.myNavHostFragment, myFragment)
                    fragmentTransaction.commit()
                    view.findNavController().navigate(com.example.locbatt.R.id.action_studentHomePage_to_attendancePage)
                }
            }
            override fun onItemLongClick(view: View?, position: Int) {}
        }))





        //   setHasOptionsMenu(true)


        //sends user back to the log in page if he/she is logged out
        val user = FirebaseAuth.getInstance().currentUser
        if(user==null){
            if(findNavController().currentDestination?.id == com.example.locbatt.R.id.studentHomePage) {
                findNavController().navigate(com.example.locbatt.R.id.mainPage)
            }
        }


        val text = activity!!.findViewById<TextView>(com.example.locbatt.R.id.textView20)
        val au = FirebaseAuth.getInstance().currentUser
        text.text = au!!.email
        val navigationView = activity!!.findViewById<NavigationView>(com.example.locbatt.R.id.navView)
        val drawer = activity!!.findViewById<DrawerLayout>(com.example.locbatt.R.id.drawerLayout)

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                com.example.locbatt.R.id.logout -> {
                    FirebaseAuth.getInstance().signOut()
                    text.text = "Welcome User"
                    findNavController().navigate(com.example.locbatt.R.id.mainPage)
                    drawer.closeDrawers()
                }
                com.example.locbatt.R.id.about ->{
                    val i = Intent(activity, com.example.locbatt.AboutActivity::class.java)
                    drawer.closeDrawers()
                    startActivity(i)
                }



            }
            false
        }

        activity!!.actionBar?.setDisplayHomeAsUpEnabled(false)
        activity!!.actionBar?.setHomeButtonEnabled(false)

        return binding.root
    }

    private fun sendKeyToEnrollment(str : String) {
        btmNav.setOnNavigationItemReselectedListener { item ->
            when (item.itemId) {
                com.example.locbatt.R.id.add -> {
                    if (findNavController().currentDestination?.id == com.example.locbatt.R.id.studentHomePage) {
                        var bundle: Bundle = bundleOf("key" to str)
                        findNavController().navigate(
                            com.example.locbatt.R.id.action_studentHomePage_to_studentEnroll,
                            bundle
                        )
                    }
                }
                com.example.locbatt.R.id.manageAccount -> {
                    if (findNavController().currentDestination?.id == com.example.locbatt.R.id.studentHomePage) {
                        findNavController().navigate(com.example.locbatt.R.id.action_studentHomePage_to_studentAccountManagement2)
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu!!, inflater!!)
        inflater?.inflate(com.example.locbatt.R.menu.menu, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            com.example.locbatt.R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(com.example.locbatt.R.id.mainPage)
            }
        }
        return super.onOptionsItemSelected(item)
    }

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
         * @return A new instance of fragment StudentHomePage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentHomePage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}