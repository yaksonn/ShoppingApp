package com.yaksonn.shoppingapp.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.yaksonn.shoppingapp.R
import com.yaksonn.shoppingapp.data.model.ShoppingList
import com.yaksonn.shoppingapp.databinding.ActivityHomeBinding
import com.yaksonn.shoppingapp.ui.grocery.GroceryActivity
import com.yaksonn.shoppingapp.utils.CustomDividerItemDecorator
import com.yaksonn.shoppingapp.utils.GPSTracker
import com.yaksonn.shoppingapp.utils.OnItemSwipe
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), ShoppingListsAdapter.OnShoppingListClickListener,
    OnItemSwipe.OnSwipe,
    ShoppingListDialog.ShoppingListDialogListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var shoppingListAdapter: ShoppingListsAdapter
    val homeViewModel: HomeViewModel by viewModels()

    private val REQUEST_LOCATION = 1234

    @Inject
    @Named("shopping_list_id_key")
    lateinit var shoppingListIdKey: String

    @Inject
    @Named("is_shopping_list_archived_key")
    lateinit var isShoppingListArchivedKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        initRecyclerView()
        initTabs()

        homeViewModel.shoppingListFilteredByArchived.observe(this) {
            shoppingListAdapter.submitList(it)
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_LOCATION
                )
            } else {
                getMyLocationAddress()
            }
        } else {
            getMyLocationAddress()
        }

        val itemTouchHelper = ItemTouchHelper(
            OnItemSwipe(
                this@HomeActivity,
                binding.recyclerView,
                this@HomeActivity,
                ContextCompat.getDrawable(
                    this@HomeActivity,
                    R.drawable.ic_round_remove_shopping_cart
                )!!
            )
        )

        homeViewModel.selectedTab.observe(this, {
            itemTouchHelper.attachToRecyclerView(
                if (it == 0) {
                    binding.recyclerView
                } else {
                    null
                }
            )
        })

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    homeViewModel.setSelectedTab(tab.position)
                    binding.recyclerView.scheduleLayoutAnimation()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        binding.addShoppingListButton.setOnClickListener {
            openDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocationAddress()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    private fun getMyLocationAddress() {
        val gpsTracker = GPSTracker(this)
        if (gpsTracker.canGetLocation()) {
            val geoCoder = Geocoder(this, Locale.getDefault())
            try {
                val addresses =
                    geoCoder.getFromLocation(gpsTracker.latitude, gpsTracker.longitude, 1)

                if (addresses != null) {
                    if (addresses.size > 0) {
                        val fetchedAddress = addresses[0]
                        val strAddress = StringBuilder()

                        for (i in 0..fetchedAddress.maxAddressLineIndex) {
                            strAddress.append(fetchedAddress.getAddressLine(i)).append(" ")
                        }
                        if (strAddress.toString().isNotEmpty()) {

                            var address = ""
                            if (fetchedAddress.thoroughfare != null) {
                                address = fetchedAddress.thoroughfare
                            }
                            if (fetchedAddress.subThoroughfare != null) {
                                address = address + " NO: " + fetchedAddress.subThoroughfare
                            }
                            val cityname = fetchedAddress.adminArea
                            val towname = fetchedAddress.subAdminArea

                        }
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
            gpsTracker.stopUsingGPS()
        }
    }

    private fun openDialog() {
        val shoppingListDialog = ShoppingListDialog().newInstance()
        shoppingListDialog.show(
            supportFragmentManager,
            getString(R.string.shopping_list_dialog_tag)
        )
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            shoppingListAdapter = ShoppingListsAdapter(this@HomeActivity, this@HomeActivity)
            adapter = shoppingListAdapter

            val itemDecoration = CustomDividerItemDecorator(
                ContextCompat.getDrawable(
                    this@HomeActivity,
                    R.drawable.divider
                )
            )
            addItemDecoration(itemDecoration)
        }
    }

    private fun initTabs() {
        val listOfTitles =
            listOf(getString(R.string.shopping_lists), getString(R.string.archived_shopping_lists))
        val listOfIcons = listOf(
            ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_round_list),
            ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_round_archive)
        )

        //add tabs
        val size = listOfTitles.size - 1
        for (i in 0..size) {
            val tab = binding.tabLayout.newTab().setText(listOfTitles[i]).apply {
                icon = listOfIcons[i]
            }
            binding.tabLayout.addTab(tab, i)
        }

        //select tab on screen rotation
        homeViewModel.selectedTab.value?.let {
            binding.tabLayout.getTabAt(it)
        }?.select()
    }

    override fun onShoppingListClick(shoppingListId: Int, isArchived: Boolean) {
        intent = Intent(this, GroceryActivity::class.java)
        intent.putExtra(shoppingListIdKey, shoppingListId)
        intent.putExtra(isShoppingListArchivedKey, isArchived)
        startActivity(intent)
    }

    override fun onInsertButtonClick(shoppingListName: String) {
        homeViewModel.insertNewShoppingList(shoppingListName)
    }

//    override fun onInsertDescAndPhotoButtonClick(desc: String) {
//        homeViewModel.updateNewShoppingList(shoppingList = desc)
//    }

    override fun deleteOnItemSwipe(item: Any) {
        homeViewModel.deleteShoppingListOnSwipe(item as ShoppingList)
    }

    override fun undoDeletedItem(item: Any) {
        homeViewModel.undoDeletedShoppingList(item as ShoppingList)
    }

}