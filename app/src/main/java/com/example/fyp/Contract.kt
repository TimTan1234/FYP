package com.example.fyp


import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fyp.adapter.ContractAdapter
import com.example.fyp.adapter.ContractCard
import com.example.fyp.adapter.ContractCardAdapter
import com.example.fyp.adapter.Contracts
import com.example.fyp.adapter.FeedbackEnd
import com.example.fyp.adapter.FeedbackEndAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.forms.fields.PdfFormField
import com.itextpdf.io.IOException
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.File



class Contract :AppCompatActivity(), ContractAdapter.OnItemClickedListener, ContractCardAdapter.OnContractClickListener{

    private lateinit var houseAddress: String
    private lateinit var ownerName: String
    private lateinit var rentalAmount: String
    private lateinit var rentalPaymentDate: String
    private lateinit var paymentReceiver: String
    private lateinit var gasPaymentPercentage: String
    private lateinit var gasPaymentAmount: String
    private lateinit var waterPaymentPercentage: String
    private lateinit var waterPaymentAmount: String
    private lateinit var phonePaymentPercentage: String
    private lateinit var phonePaymentAmount: String
    private lateinit var otherPaymentName: String
    private lateinit var otherPaymentPercentage: String
    private lateinit var otherPaymentAmount: String
    private lateinit var lastMonthRentDate: String
    private lateinit var lastMonthRentAmount: String
    private lateinit var securityDepositDate: String
    private lateinit var securityDepositAmount: String
    private lateinit var otherDepositDate: String
    private lateinit var otherDepositAmount: String
    private lateinit var otherDepositDateRange: String

    // Declaring variables for String
    private lateinit var gas: String
    private lateinit var water: String
    private lateinit var phone: String
    private lateinit var other: String
    private lateinit var household: String
    private lateinit var thirdParty: String
    private lateinit var majority: String
    private lateinit var principalTenant: String
    private lateinit var owner: String

    private lateinit var cleaningValue: String
    private lateinit var cleaningNo: String
    private lateinit var kitchenUseValue: String
    private lateinit var kitchenUseNo: String
    private lateinit var overnightGuestValue: String
    private lateinit var overnightGuestNo: String
    private lateinit var kitchenAppliancesValue: String
    private lateinit var kitchenAppliancesNo: String
    private lateinit var smokingValue: String
    private lateinit var smokingNo: String
    private lateinit var commonAreaValue: String
    private lateinit var commonAreaNo: String
    private lateinit var alcoholValue: String
    private lateinit var alcoholNo: String
    private lateinit var telephoneValue: String
    private lateinit var telephoneNo: String
    private lateinit var studyValue: String
    private lateinit var studyNo: String
    private lateinit var personalItemValue: String
    private lateinit var personalItemNo: String
    private lateinit var musicValue: String
    private lateinit var musicNo: String
    private lateinit var bedroomAssignmentValue: String
    private lateinit var bedroomAssignmentNo: String
    private lateinit var petsValue: String
    private lateinit var petsNo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contract)




        val recyclerView: RecyclerView = findViewById(R.id.Contract)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        db.collection("Contract Template").document("uniqueUserId").collection("usedContracts").document("usedContractsAmount").get()
            .addOnSuccessListener { document ->
                val usedContracts = document.data?.get("usedContracts") as? Map<String, Long>
                val topContracts = usedContracts?.toList()?.sortedByDescending { it.second }?.take(6)
                val contractDetailsList = mutableListOf<Contracts>()

                topContracts?.forEach { (contractId, count) ->
                    val docRef = db.collection("Contract Template").document("uniqueUserId").collection("con1").document(contractId)

                    docRef.get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val text = documentSnapshot.getString("name") ?: "Unknown"
                                val id = document.id
                                contractDetailsList.add(Contracts(text, "Contract",id))

                                if (contractDetailsList.size == topContracts.size) {
                                    // Update RecyclerView in the main thread
                                    runOnUiThread {
                                        recyclerView.adapter = ContractAdapter(contractDetailsList, this)
                                    }
                                }
                            } else {
                                Log.d("Firestore", "No such document")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Firestore", "get failed with ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting document: ", exception)
            }




            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                // Ensure that we have a layoutManager and that the snapHelper has a snapped view
                val layoutManager = rv.layoutManager as LinearLayoutManager?
                val snapView = snapHelper.findSnapView(layoutManager)
                val snapPosition = snapView?.let { layoutManager?.getPosition(it) }

                for (i in 0 until rv.childCount) {
                    val child = rv.getChildAt(i)
                    val childPosition = layoutManager?.getPosition(child)

                    // Normalize the scale based on the child's position relative to the snapped view
                    val isCentered = childPosition == snapPosition
                    val scale = if (isCentered) 1f else 0.8f // Full size for centered, scaled down for others
                    val alpha = if (isCentered) 1f else 0.5f // Fully opaque for centered, translucent for others

                    child.scaleX = scale
                    child.scaleY = scale
                    child.alpha = alpha
                }
            }
        })
        val leftArrowButton: ImageButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: ImageButton = findViewById(R.id.rightArrowButton)

        leftArrowButton.setOnClickListener {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val snapView = snapHelper.findSnapView(layoutManager)
            snapView?.let {
                val currentPosition = layoutManager.getPosition(it)
                val targetPosition = currentPosition - 1
                if (targetPosition >= 0) {
                    recyclerView.smoothScrollToPosition(targetPosition)
                    Log.d("PositionLeft", "Scrolling to: $targetPosition")
                }
            }
        }

        rightArrowButton.setOnClickListener {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val snapView = snapHelper.findSnapView(layoutManager)
            snapView?.let {
                val currentPosition = layoutManager.getPosition(it)
                val targetPosition = currentPosition + 1
                if (targetPosition < recyclerView.adapter?.itemCount ?: 0) {
                    recyclerView.smoothScrollToPosition(targetPosition)
                    Log.d("PositionRight", "Scrolling to: $targetPosition")
                }
            }
        }

        val contractCard = findViewById<RecyclerView>(R.id.contractCard)
        contractCard.layoutManager =LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        val db = FirebaseFirestore.getInstance()
        val yourCollectionReference = db.collection("Contract Template").document("uniqueUserId").collection("con1")
        yourCollectionReference.get()
            .addOnSuccessListener { documents ->
                val items = documents.mapNotNull { document ->
                    val text = document.getString("name")
                    val content = "Contract"
                    val imageResId = R.drawable.ic_launcher_background
                    val id = document.id
                    if (text != null) {
                        ContractCard(text,content,imageResId,id)
                    } else {
                        null
                    }
                }
                val adapter = ContractCardAdapter(items,this)
                contractCard.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error fetching documents: ", exception)
            }


    }
    private val db = FirebaseFirestore.getInstance()
    override fun onContractClick(contractCard: ContractCard) {
        val userRef =db.collection("Contract Template").document("uniqueUserId").collection("usedContracts").document("usedContractsAmount")
        val contractId = contractCard.id

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCount = snapshot.getLong("usedContracts.$contractId") ?: 0
            transaction.update(userRef, "usedContracts.$contractId", currentCount + 1)
        }
        fetchEntireCollection(contractCard.name,contractCard.id)
    }

    override fun onItemClicked(position: Int) {
        // Code to handle item click at the position
    }

    override fun onButtonClicked(position: Int,contracts: Contracts) {
        fetchEntireCollection(contracts.name,contracts.id)
    }

    override fun onEditButtonClick(contractCard: ContractCard, position: Int) {

    }

    override fun onSendButtonClick(contractCard: ContractCard, position: Int) {
        val docRef = db.collection("Sent Contract").document("uniqueUserId")
        val dialogView = LayoutInflater.from(this).inflate(R.layout.contract_dialog_box, null)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        val editText = dialogView.findViewById<EditText>(R.id.editTextInput)
        val sendButton = dialogView.findViewById<Button>(R.id.buttonSend)

        val alertDialog = dialogBuilder.create()

        sendButton.setOnClickListener {
            val inputText = editText.text.toString()
            val contract = hashMapOf(
                "contract" to contractCard.id,
                "owner" to "userID"
            )
            docRef.set(contract)
                .addOnSuccessListener {
                    Log.d("Firestore", "Document successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error writing document", e)
                }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
    private fun fetchEntireCollection(name:String,id:String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Contract Template").document("uniqueUserId").collection(name).document(id)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document was found
                    houseAddress = documentSnapshot.getString("houseAddress")?: "-"
                    ownerName = documentSnapshot.getString("ownerName")?: "-"
                    rentalAmount = documentSnapshot.getString("rentalAmount")?: "-"
                    rentalPaymentDate =documentSnapshot.getString("rentalPaymentDate")?: "-"
                    paymentReceiver = documentSnapshot.getString("paymentReceiver")?: "-"
                    gasPaymentPercentage = documentSnapshot.getString("gasPaymentPercentage")?: "-"
                    gasPaymentAmount = documentSnapshot.getString("gasPaymentAmount")?: "-"
                    waterPaymentPercentage = documentSnapshot.getString("waterPaymentPercentage")?: "-"
                    waterPaymentAmount = documentSnapshot.getString("waterPaymentAmount")?: "-"
                    phonePaymentAmount = documentSnapshot.getString("phonePaymentAmount")?: "-"
                    phonePaymentPercentage = documentSnapshot.getString("phonePaymentPercentage")?: "-"
                    otherPaymentName = documentSnapshot.getString("otherPaymentName") ?: "-"
                    otherPaymentPercentage = documentSnapshot.getString("otherPaymentPercentage") ?: "-"
                    otherPaymentAmount = documentSnapshot.getString("otherPaymentAmount") ?: "-"
                    lastMonthRentDate = documentSnapshot.getString("lastMonthRentDate") ?: "-"
                    lastMonthRentAmount = documentSnapshot.getString("lastMonthRentAmount") ?: "-"
                    securityDepositDate = documentSnapshot.getString("securityDepositDate") ?: "-"
                    securityDepositAmount = documentSnapshot.getString("securityDepositAmount") ?: "-"
                    otherDepositDate = documentSnapshot.getString("otherDepositDate") ?: "-"
                    otherDepositAmount = documentSnapshot.getString("otherDepositAmount") ?: "-"
                    otherDepositDateRange = documentSnapshot.getString("otherDepositDateRange") ?: "-"
                    gas = documentSnapshot.getString("gas") ?: "Yes"
                    water = documentSnapshot.getString("water") ?: "Yes"
                    phone = documentSnapshot.getString("phone") ?: "Yes"
                    other = documentSnapshot.getString("other") ?: "Yes"
                    household = documentSnapshot.getString("household") ?: "-"
                    thirdParty = documentSnapshot.getString("thirdParty") ?: "-"
                    majority = documentSnapshot.getString("majority") ?: "-"
                    principalTenant = documentSnapshot.getString("principalTenant") ?: "-"
                    owner = documentSnapshot.getString("owner") ?: "-"
                    cleaningValue = documentSnapshot.getString("cleaningValue") ?: "-"
                    kitchenUseValue = documentSnapshot.getString("kitchenUseValue") ?: "-"
                    overnightGuestValue = documentSnapshot.getString("overnightGuestValue") ?: "-"
                    kitchenAppliancesValue = documentSnapshot.getString("kitchenAppliancesValue") ?: "-"
                    smokingValue = documentSnapshot.getString("smokingValue") ?: "-"
                    commonAreaValue = documentSnapshot.getString("commonAreaValue") ?: "-"
                    alcoholValue = documentSnapshot.getString("alcoholValue") ?: "-"
                    telephoneValue = documentSnapshot.getString("telephoneValue") ?: "-"
                    studyValue = documentSnapshot.getString("studyValue") ?: "-"
                    personalItemValue = documentSnapshot.getString("personalItemValue") ?: "-"
                    musicValue = documentSnapshot.getString("musicValue") ?: "-"
                    bedroomAssignmentValue = documentSnapshot.getString("bedroomAssignmentValue") ?: "-"
                    petsValue = documentSnapshot.getString("petsValue") ?: "-" // Correctly access the field
                    // You now have the data from the "tips" field, do something with it

                    main()
                } else {
                    // No such document
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
                Log.d("Firestore", "get failed with ", exception)
            }
    }



    fun main() {
        val context = this // Assuming 'this' is a Context instance.
        val inputPdf = "rental-agreement-room.pdf" // The template PDF with placeholders

        // The path where the filled PDF will be saved
        val outputPdfPath =
            File(context.getExternalFilesDir(null), "rental-agreement-room-filled.pdf").absolutePath

        val data = mapOf(
            "Address" to houseAddress,
            "RentalAmount" to rentalAmount,
            "RentalPaymentDate" to rentalPaymentDate,
            "Cleaning" to cleaningValue,
            "Kitchen" to kitchenUseValue,
            "OvernightGuest" to overnightGuestValue,
            "Smoking" to smokingValue,
            "Alcohol" to alcoholValue,
            "Study" to studyValue,
            "Music" to musicValue,
            "Pets" to petsValue,
            "KitchenAppliances" to kitchenAppliancesValue,
            "CommonArea" to commonAreaValue,
            "Telephone" to telephoneValue,
            "BedroomAssignment" to bedroomAssignmentValue,
            "RefundableRent" to lastMonthRentDate,
            "RefundableRentAmount" to lastMonthRentAmount,
            "SecurityDeposit" to securityDepositDate,
            "SecurityDepositAmount" to securityDepositAmount,
            "OtherDepositAmount" to otherDepositAmount,
            "OtherDepositDate" to otherDepositDate,
            "OtherDepositRange" to otherDepositDateRange,
            "UtitlityAmount" to gasPaymentAmount,
            "WaterAmount" to waterPaymentAmount,
            "PhoneAmount" to phonePaymentAmount,
            "OtherAmount" to otherPaymentAmount,
            "UtilityPercentage" to gasPaymentPercentage,
            "WaterPercentage" to waterPaymentPercentage,
            "PhonePercentage" to phonePaymentPercentage,
            "OtherName" to otherPaymentName,
            "OtherPercentage" to otherPaymentPercentage,


        )

        try {
            context.assets.open(inputPdf).use { assetInputStream ->
                PdfReader(assetInputStream).use { reader ->
                    PdfWriter(outputPdfPath).use { writer ->
                        PdfDocument(reader, writer).use { pdfDoc ->
                            val form = PdfAcroForm.getAcroForm(pdfDoc, true)

                            // Select a radio button. Replace "radio_field_name" and "option" with actual values.
                            val UtilityNotInclude = form.getField("UtilityNotInclude")
                            UtilityNotInclude?.setValue("Value_znks")
                            if(gas == "Yes"){
                                val utilitys = form.getField("utilitys")
                                utilitys?.setValue("utility")
                                val UtilityPrice = form.getField("UtilityPrice")
                                UtilityPrice?.setValue("UtilityPrice")

                            }

                            if(water == "Yes"){
                            val Waters = form.getField("Waters")
                                Waters?.setValue("Water")
                                val WaterPrices = form.getField("WaterPrices")
                                WaterPrices?.setValue("WaterPrice")
                        }

                            if(phone == "Yes"){
                                val Phones = form.getField("Phones")
                                Phones?.setValue("Phone")
                                val PhonePrices = form.getField("PhonePrices")
                                PhonePrices?.setValue("PhonePrice")
                            }

                            if(other == "Yes"){
                                val Others = form.getField("Others")
                                Others?.setValue("Other")
                                val OtherPrices = form.getField("OtherPrices")
                                OtherPrices?.setValue("OtherPrice")
                            }

                            if(principalTenant == "Yes"){
                                val PrincipalTenants = form.getField("PrincipalTenants")
                                PrincipalTenants?.setValue("PrincipalTenant")

                            }

                            if(household == "Yes"){
                                val Consensuss = form.getField("Consensuss")
                                Consensuss?.setValue("Consensus")

                            }

                            if(thirdParty == "Yes"){
                                val ThirdPartys = form.getField("ThirdPartys")
                                ThirdPartys?.setValue("ThirdParty")

                            }

                            if(majority == "Yes"){
                                val Majoritys = form .getField("Majoritys")
                                Majoritys?.setValue("Majority")

                            }

                            if(owner == "Yes"){
                                val Owners = form.getField("Owners")
                                Owners?.setValue("Owner")

                            }

                            data.forEach { (fieldName, fieldValue) ->
                                val formField = form.getField(fieldName)
                                formField?.setValue(fieldValue)
                            }

                            form.flattenFields() // Make the form read-only if you don't need to edit after saving.
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun updateArrowButtons() {
        val recyclerView: RecyclerView = findViewById(R.id.Contract)
        val leftArrowButton: ImageButton = findViewById(R.id.leftArrowButton)
        val rightArrowButton: ImageButton = findViewById(R.id.rightArrowButton)
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        leftArrowButton.isEnabled = layoutManager.findFirstCompletelyVisibleItemPosition() > 0
        rightArrowButton.isEnabled = layoutManager.findLastCompletelyVisibleItemPosition() < (recyclerView.adapter?.itemCount?.minus(1) ?: 0)
    }

}


