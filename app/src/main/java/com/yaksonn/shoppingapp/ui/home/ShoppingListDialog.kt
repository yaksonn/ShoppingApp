package com.yaksonn.shoppingapp.ui.home

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.yaksonn.shoppingapp.R
import com.yaksonn.shoppingapp.databinding.DialogInsertShoppingListBinding
import com.yaksonn.shoppingapp.utils.Button
import com.yaksonn.shoppingapp.utils.GPSTracker
import com.yaksonn.shoppingapp.utils.NameValidation
import com.yaksonn.shoppingapp.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class ShoppingListDialog : BottomSheetDialogFragment() {
    @Inject
    @Named("name_error")
    lateinit var notValidNameErrorMessage: String
    private lateinit var binding: DialogInsertShoppingListBinding
    private lateinit var listener: ShoppingListDialogListener
    private var isNameValid = false
    val PLACE_PICKER_REQUEST = 1

    private var latLng: LatLng? = null
    private var gpsTracker: GPSTracker? = null

    fun newInstance(): ShoppingListDialog {
        return ShoppingListDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener { dialog ->
            val bottomSheetDialog = dialog as BottomSheetDialog
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            initValidation()

            gpsTracker = GPSTracker(requireContext())
            latLng = LatLng(gpsTracker!!.latitude, gpsTracker!!.longitude)
            binding.locationTextInput.setOnClickListener {
                openPlacePicker()
            }
            binding.addNewShoppingListButton.setOnClickListener {
                val shoppingListName = binding.nameTextInput.text.toString().trim()
                listener.onInsertButtonClick(shoppingListName)

                Toast.makeText(context, getString(R.string.shopping_list_added), Toast.LENGTH_SHORT)
                    .show()
                bottomSheetDialog.dismiss()
            }


            //set button state on view created or recreated
            setAddButtonState(NameValidation.validate(binding.nameTextInput.text))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogInsertShoppingListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ShoppingListDialogListener
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initValidation() {
        binding.nameTextInput.onTextChanged {
            when (NameValidation.validate(it)) {
                true -> {
                    isNameValid = true
                    setNameErrorMessage(null)
                }
                false -> {
                    isNameValid = false
                    setNameErrorMessage(notValidNameErrorMessage)
                }
            }

            setAddButtonState(isNameValid)
        }
    }

    private fun setAddButtonState(isEnabled: Boolean) {
        if (isEnabled) {
            binding.addNewShoppingListButton.apply {
                isClickable = true
                alpha = Button.State.Enable.alpha
            }
        } else {
            binding.addNewShoppingListButton.apply {
                isClickable = false
                alpha = Button.State.Disable.alpha
            }
        }
    }

    private fun TextInputEditText.onTextChanged(onTextChanged: (CharSequence?) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                onTextChanged.invoke(p0)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun setNameErrorMessage(name: String?) {
        binding.nameTextInputLayout.error = name
    }

    private fun setLocationErrorMessage(location: String?) {
        binding.locationTextInputLayout.error = location
    }

    interface ShoppingListDialogListener {
        fun onInsertButtonClick(shoppingListName: String)
    }

    private fun openPlacePicker() {
        if (activity != null && GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS
        ) {
            if (!Places.isInitialized()) {
                Places.initialize(requireActivity(), getString(R.string.google_maps_key))
            }
            val fields =
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            var intentBuilder = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            val bounds = RectangularBounds.newInstance(
                latLng!!, LatLng(gpsTracker!!.latitude, gpsTracker!!.longitude)
            )
            try {
                val geo = Geocoder(context, Locale.getDefault())
                val addresses = geo.getFromLocation(
                    gpsTracker!!.latitude, gpsTracker!!.longitude, 1
                )
                if (addresses.isNotEmpty()) {
                    intentBuilder =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                            .setLocationBias(bounds)
                }
            } catch (ex: Exception) {
                Toast.makeText(context, "No Location Name Found", Toast.LENGTH_SHORT).show()

            }
            val intent = intentBuilder.build(requireContext())
            startActivityForResult(intent, PLACE_PICKER_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                if (resultCode == RESULT_OK) {
                    val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                    if (place != null) {
                        binding.locationTextInput.setText(place.name)
                    } else {
                        binding.locationTextInput.setText("")
                    }
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    requireActivity().toast(status.statusMessage)
                }

            }

        }
    }
}