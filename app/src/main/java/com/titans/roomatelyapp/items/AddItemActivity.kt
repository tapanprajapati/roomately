package com.titans.roomatelyapp.items

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.titans.roomatelyapp.Data
import kotlinx.android.synthetic.main.actionbar.*
import kotlinx.android.synthetic.main.activity_item_crud.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.titans.roomatelyapp.DataModels.Item
import com.titans.roomatelyapp.DataModels.Transaction
import com.titans.roomatelyapp.R
import com.titans.roomatelyapp.barcodeReader.BarcodeReaderActivity



class AddItemActivity: AppCompatActivity() {
    val BARCODE_READER_ACTIVITY_REQUEST = 1208
    val LOCATION_ACTIVITY_REQUEST = 1308
    lateinit var categoryList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_crud)


        backButton.setOnClickListener { _ -> onBackPressed() }
        txtToolbarLabel.text = getString(R.string.add_item)

        checkValues()

        categoryList = intent.getStringArrayListExtra("CATEGORIES")
        var adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,categoryList)
        ETProductCategory.setAdapter(adapter)

        addLocationBtn.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivityForResult(intent,LOCATION_ACTIVITY_REQUEST)
        }

        var b = intent.extras?.getString("barcode")
        if(b==null)
            b=""
        txtBarcode.text = "Barcodes: \n"+b

        backButton.setOnClickListener { _ -> onBackPressed() }
        txtToolbarLabel.text = Data.selectedGroup + " > " + getString(R.string.add_item)
        checkValues()

        setBarcode()

        saveItemFloatingButton.setOnClickListener {
            saveData()
        }

        barcodeScannerFloatingButton.setOnClickListener { v ->
            val launchIntent: Intent = BarcodeReaderActivity.getLaunchIntent(this, true, false)
            startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST)

        }
    }

    fun saveData() {
        if (ETProductName.text?.length == 0 || ETDesc.text?.length == 0 || ETProductCategory.text.isEmpty()) {
            showError()
            Toast.makeText(
                baseContext,
                "Provide name / desc / category details",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            /* Code for adding item to Firebase */
            val name: String = ETProductName.text.toString()
            val desc: String = ETDesc.text.toString()
            val status = checkStatus.isChecked
            val category = ETProductCategory.text.toString()
            var barcode = txtBarcode.text.toString().split("\n")
            var location = tvLocation.text.toString()

            val item = Item(
                name = name,
                desc = desc,
                locations = location,
                inStock = status,
                barcodes = ArrayList<String>(barcode)
            )

            if(!categoryList.contains(category))
            {
                var alert = AlertDialog.Builder(this)

                alert.setTitle("Category $category Not Available")
                alert.setMessage("Do you want to create new category: $category ?")

                alert.setPositiveButton("Create New",{dialog, which ->
                    saveConfirmed(item, category)
                    dialog.dismiss()
                })

                alert.setNegativeButton("Select Another",{dialog, which -> dialog.dismiss() })
                alert.show()
            }
            else
                saveConfirmed(item,category)

        }
    }

    fun saveConfirmed(item: Item, category: String)
    {
        var i = hashMapOf(item.name to item)

        if (Data.selectedGroup.equals("Self")) {
            Data.db.collection(Data.USERS).document(Data.currentUser.phone).collection("items")
                .document(category).set(
                    i,
                    SetOptions.merge()
                )
                .addOnSuccessListener { void ->


                    var transaction = Transaction(
                        title = "Item Added: " + item.name,
                        subTitle = "Added By : " + Data.currentUser.name,
                        date = SimpleDateFormat("dd-MMM-yyyy").format(
                            Calendar.getInstance().getTime()
                        )
                    )

                    Data.db.collection(Data.USERS).document(Data.currentUser.phone)
                        .collection("transactions")
                        .document(Data.getTimeStamp()).set(transaction)

                    Toast.makeText(this, "Item Added", Toast.LENGTH_LONG).show()
                    onBackPressed()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Error Adding Item",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }
        else {
            Data.db.collection(Data.GROUPS)
                .document(Data.currentUser.groups[Data.groups.indexOf(Data.selectedGroup) - 1])
                .collection("items").document(category).set(
                    i,
                    SetOptions.merge()
                )
                .addOnSuccessListener { void ->

                    var transaction = Transaction(
                        title = "Item Added: " + item.name,
                        subTitle = "Added By : " + Data.currentUser.name,
                        date = SimpleDateFormat("dd-MMM-yyyy").format(
                            Calendar.getInstance().getTime()
                        )
                    )

                    Data.db.collection(Data.GROUPS)
                        .document(Data.currentUser.groups[Data.groups.indexOf(Data.selectedGroup) - 1])
                        .collection("transactions")
                        .document(Data.getTimeStamp()).set(transaction)

                    Toast.makeText(this, "Item Added", Toast.LENGTH_LONG).show()
                    onBackPressed()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Error Adding Item",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
    private fun showError() {
        if (ETProductName.text?.length == 0) {
            ETProductName.error = "Product name cannot be blank."
        }
        if (ETDesc.text?.length == 0) {
            ETDesc.error = "Product description cannot be blank."
        }
        if (ETProductCategory.text.length == 0) {
            ETProductCategory.error = "Product category cannot be blank."
        }
    }

    private fun checkValues() {
        ETProductName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNullOrBlank()) {
                    ETProductName.error = "Product name cannot be null"
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isNullOrBlank()) {
                    ETProductName.error = "Product name cannot be null"
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    ETProductName.error = "Product name cannot be null"
                }
            }
        })

        ETDesc.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNullOrBlank()) {
                    ETDesc.error = "Product description cannot be null"
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isNullOrBlank()) {
                    ETDesc.error = "Product description cannot be null"
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    ETDesc.error = "Product description cannot be null"
                }
            }
        })

        ETProductCategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNullOrBlank()) {
                    ETProductCategory.error = "Product category cannot be null"
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isNullOrBlank()) {
                    ETProductCategory.error = "Product category cannot be null"
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    ETProductCategory.error = "Product category cannot be null"
                }
            }
        })
    }

    private fun setBarcode() {
        var barcode = intent.getStringExtra("barcode")

        if (barcode != null)
            txtBarcode.setText(barcode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == LOCATION_ACTIVITY_REQUEST)
        {
            if (data == null)
                return

            var locationName = data?.getStringExtra("Name")
            var locationAddress = data?.getStringExtra("Address")


            tvLocation.text = locationName+"-"+locationAddress

            return
        }

        if (requestCode == BARCODE_READER_ACTIVITY_REQUEST)
        {
            if (data == null)
                return

            var barcode = data?.getStringExtra(BarcodeReaderActivity.KEY_CAPTURED_RAW_BARCODE)

            if (txtBarcode.text.toString().split("\n").contains(barcode)) {
                Toast.makeText(this, "Barcode Already Added", Toast.LENGTH_LONG).show()
            }
            else
            {

                if (txtBarcode.text.length != 0)
                    barcode = "\n" + barcode

                txtBarcode.text = txtBarcode.text.toString() + barcode
            }

        }
    }

    /* Hide keyboard when user touches outside of EditText. */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}

