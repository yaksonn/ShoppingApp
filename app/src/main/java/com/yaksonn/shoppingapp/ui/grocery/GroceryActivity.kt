package com.yaksonn.shoppingapp.ui.grocery

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.yaksonn.shoppingapp.BuildConfig
import com.yaksonn.shoppingapp.R
import com.yaksonn.shoppingapp.data.model.Grocery
import com.yaksonn.shoppingapp.data.model.ShoppingList
import com.yaksonn.shoppingapp.databinding.ActivityGroceryBinding
import com.yaksonn.shoppingapp.receivers.AlarmReceiver
import com.yaksonn.shoppingapp.ui.alarm.DatePickerFragment
import com.yaksonn.shoppingapp.ui.alarm.TimePickerFragment
import com.yaksonn.shoppingapp.utils.Button
import com.yaksonn.shoppingapp.utils.CustomDividerItemDecorator
import com.yaksonn.shoppingapp.utils.OnItemSwipe
import com.yaksonn.shoppingapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.properties.Delegates


@AndroidEntryPoint
class GroceryActivity : AppCompatActivity(), GroceryAdapter.OnGroceryClickListener,
    OnItemSwipe.OnSwipe,
    GroceryDialog.GroceryDialogListener, DatePickerFragment.DialogDateListener,
    TimePickerFragment.DialogTimeListener {

    private lateinit var binding: ActivityGroceryBinding
    private lateinit var groceryAdapter: GroceryAdapter
    private lateinit var shoppingList: ShoppingList
    private var groceryList: ArrayList<Grocery> = arrayListOf()
    private val groceryViewModel: GroceryViewModel by viewModels()
    private var shoppingListId by Delegates.notNull<Int>()
    private var isShoppingListIdArchived by Delegates.notNull<Boolean>()

    private lateinit var alarmReceiver: AlarmReceiver
    private var strDate: String? = null
    private var strTime: String? = null
    private var strRepeat: String? = null
    private var byteArray: ByteArray? = null


    var file: File? = null
    var groceryFile: String? = null

    companion object {
        private const val DATE_PICKER_TAG = "DatePicker"
        private const val TIME_PICKER_ONCE_TAG = "TimePickerOnce"
    }

    @Inject
    @Named("shopping_list_id_key")
    lateinit var shoppingListIdKey: String

    @Inject
    @Named("is_shopping_list_archived_key")
    lateinit var isShoppingListArchivedKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroceryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //get extras
        shoppingListId = intent.extras!!.getInt(shoppingListIdKey)
        isShoppingListIdArchived = intent.extras!!.getBoolean(isShoppingListArchivedKey)

        alarmReceiver = AlarmReceiver()

        initRecyclerView(isShoppingListIdArchived)
        //get shoppingList object
        shoppingList = groceryViewModel.getShoppingList(shoppingListId)



        groceryViewModel.getGroceryForShoppingList(shoppingListId).observe(this) {
            groceryList = it as ArrayList<Grocery>
            groceryAdapter.submitList(it)
            //check if this is new empty list (all groceries = 0)
            isShoppingListIdArchived = if (shoppingList.allGroceries == 0)
                false
            else
                shoppingList.allGroceries == shoppingList.doneGroceries
        }

        if (!shoppingList.desc?.isEmpty()!!) {
            binding.shoppingDesc.visibility = View.VISIBLE
            binding.descAndPhotoLinear.visibility = View.VISIBLE
            binding.shoppingDesc.text = shoppingList.desc
        }


        if (shoppingList.image != null) {
            binding.descAndPhotoLinear.visibility = View.VISIBLE
            binding.shoppingReceiptImageView.visibility = View.VISIBLE
            val bmp = shoppingList.image?.let {
                BitmapFactory.decodeByteArray(
                    shoppingList.image,
                    0,
                    it?.size
                )
            }
            val image: ImageView = findViewById<View>(R.id.shoppingReceiptImageView) as ImageView
            image.setImageBitmap(
                bmp?.let {
                    Bitmap.createScaledBitmap(
                        it,
                        200,
                        200,
                        false
                    )
                }
            )
        }

        if (isShoppingListIdArchived)
            disableAddingButton()
        else {
            binding.addGroceryButton.setOnClickListener {
                openDialog()
            }
        }


        binding.shareListButton.setOnClickListener {
            groceryFile = shoppingList.name + "_ShopList" + ".txt".also {
                groceryFile = it
            }
            file = File(filesDir.toString() + File.separator + "_ShopList")
            if (!file!!.exists()) file!!.mkdir()
            file = File(
                filesDir.toString() + File.separator + "_ShopList"
                        + File.separator + groceryFile
            )

            try {
                val fileOutputStream: FileOutputStream = FileOutputStream(file)
                fileOutputStream.write(("Liste Adı : " + shoppingList.name).toByteArray())
                fileOutputStream.write("\n".toByteArray())
                var i = 1
                for (grocery in groceryList) {

                    fileOutputStream.write(("Shopping " + i++.toString() + " -) ").toByteArray())
                    fileOutputStream.write(grocery.name.toByteArray())
                    fileOutputStream.write("\n".toByteArray())

                }
                Toast.makeText(
                    this@GroceryActivity,
                    "Alışveriş listesi başarılı şekilde kaydedildi!",
                    Toast.LENGTH_SHORT
                ).show()
                fileOutputStream.close()
            } catch (e: Exception) {
                println(e)
            }
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "*/txt"
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    this@GroceryActivity,
                    BuildConfig.APPLICATION_ID.toString() + ".provider",
                    file!!
                )
            )
            startActivity(Intent.createChooser(shareIntent, "Choose Application"))
        }
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.alarmButton.setOnClickListener {
            binding.addAlarmContainer.visibility = View.VISIBLE
        }

        binding.alarmDate.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)
        }

        binding.alarmTime.setOnClickListener {
            val timePickerFragmentOne = TimePickerFragment()
            timePickerFragmentOne.show(supportFragmentManager, TIME_PICKER_ONCE_TAG)
        }

        binding.addAlarm.setOnClickListener {
            strTime?.let { it1 ->
                strDate?.let { it2 ->
                    alarmReceiver.setOneTimeAlarm(
                        this, AlarmReceiver.TYPE_ONE_TIME,
                        it2,
                        it1,
                        shoppingList.name + " Alışverişini yapmayı unutma! :)"
                    )
                }
            }
            binding.addAlarmContainer.visibility = View.GONE
            binding.alarmDate.setText("")
            binding.alarmTime.setText("")
            toast("Alarm başarılı şekilde eklendi")
        }

        binding.cancelAlarm.setOnClickListener {
            binding.addAlarmContainer.visibility = View.GONE
            strDate = ""
            strRepeat = ""
            strTime = ""
            binding.alarmDate.setText("")
            binding.alarmTime.setText("")
        }
    }

    private fun openDialog() {
        val groceryDialog = GroceryDialog().newInstance()
        groceryDialog.show(supportFragmentManager, getString(R.string.grocery_dialog_tag))
    }

    private fun initRecyclerView(isArchived: Boolean) {
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            groceryAdapter = GroceryAdapter(this@GroceryActivity, this@GroceryActivity, isArchived)
            adapter = groceryAdapter
            val itemDecoration = CustomDividerItemDecorator(
                ContextCompat.getDrawable(
                    this@GroceryActivity,
                    R.drawable.divider
                )
            )
            addItemDecoration(itemDecoration)
            scheduleLayoutAnimation()

            if (!isShoppingListIdArchived)
                ItemTouchHelper(
                    OnItemSwipe(
                        this@GroceryActivity,
                        binding.recyclerView,
                        this@GroceryActivity,
                        ContextCompat.getDrawable(
                            this@GroceryActivity,
                            R.drawable.ic_round_delete
                        )!!
                    )
                ).attachToRecyclerView(this)
        }
    }

    private fun disableAddingButton() {
        binding.addGroceryButton.isClickable = false
        binding.addGroceryButton.alpha = Button.State.Disable.alpha
    }

    override fun onGroceryClick(grocery: Grocery) {
        groceryViewModel.updateShoppingListDoneGroceriesValue(shoppingList, grocery.isDone)
        groceryViewModel.updateGroceryStatus(grocery)
    }

    override fun onInsertButtonClick(groceryName: String, quantity: Int, amount: Int) {
        groceryViewModel.updateShoppingListAllGroceriesValue(shoppingList)
        groceryViewModel.insertNewGrocery(groceryName, quantity, amount, shoppingListId)
    }

    override fun onUpdateButtonClick(desc: String, image: ByteArray?) {
        groceryViewModel.updateShoppingListAllGroceriesValue(shoppingList)
//        shoppingList.desc = desc
        groceryViewModel.updateShoppingDescList(shoppingList, desc, image)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (shoppingList.isArchived != isShoppingListIdArchived) {
            groceryViewModel.setShoppingListAsArchived(shoppingList)
        }
    }

    override fun deleteOnItemSwipe(item: Any) {
        groceryViewModel.deleteGroceryOnSwipe(item as Grocery, shoppingList)
    }

    override fun undoDeletedItem(item: Any) {
        groceryViewModel.undoDeletedGrocery(item as Grocery, shoppingList)
    }


    override fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        strDate = dateFormat.format(calendar.time)
        binding.alarmDate.setText(strDate)
    }

    override fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        when (tag) {
            TIME_PICKER_ONCE_TAG -> {
                strTime = dateFormat.format(calendar.time)
                binding.alarmTime.setText(strTime)
            }
            else -> {
            }
        }
    }


}