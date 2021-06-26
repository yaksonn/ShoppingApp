package com.yaksonn.shoppingapp.ui.grocery

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.yaksonn.shoppingapp.databinding.DialogInsertGroceryBinding
import com.yaksonn.shoppingapp.interfaces.AddMediaClickListener
import com.yaksonn.shoppingapp.ui.home.ShoppingListDialog
import com.yaksonn.shoppingapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class GroceryDialog : BottomSheetDialogFragment() {
    @Inject
    @Named("name_error")
    lateinit var notValidNameErrorMessage: String


    @Inject
    @Named("amount_error")
    lateinit var notValidAmountErrorMessage: String
    private lateinit var binding: DialogInsertGroceryBinding
    private lateinit var listener: GroceryDialogListener
    private lateinit var listenerShopping: ShoppingListDialog.ShoppingListDialogListener
    private var isNameValid = false
    private var isAmountValid = false
    private var groceryName: String? = null
    var imageInByte: ByteArray? = null


    fun newInstance(): GroceryDialog {
        return GroceryDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener { dialog ->
            val bottomSheetDialog = dialog as BottomSheetDialog
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//            initValidation()

            binding.moreTextView.setOnClickListener {
                binding.descAndPhotoLinear.visibility = View.VISIBLE
                binding.groceryLinear.visibility = View.GONE

            }

            binding.addPhotoTextView.setOnClickListener {
                imageChoose()
            }

            if (binding.descAndPhotoLinear.visibility == View.VISIBLE) {
                binding.moreTextView.visibility = View.GONE
            }

            binding.closeMoreActionTextView.setOnClickListener {
                binding.descAndPhotoLinear.visibility = View.GONE
                binding.moreTextView.visibility = View.VISIBLE
                binding.groceryLinear.visibility = View.VISIBLE
            }

            binding.addNewGroceryButton.setOnClickListener {

                if (binding.descAndPhotoLinear.visibility == View.VISIBLE) {
                    val desc = binding.descTextInput.text.toString().trim()
                    //
//                    listenerShopping.onInsertDescAndPhotoButtonClick(desc)
                    listener.onUpdateButtonClick(desc, imageInByte)
                } else {
                    val groceryName = binding.groceryNameTextInput.text.toString().trim()
                    val quantity = binding.quantityTextInput.text.toString().toInt()
                    val amount = binding.amountTextInput.text.toString().toInt()
                    listener.onInsertButtonClick(groceryName, quantity, amount)
                }


                bottomSheetDialog.dismiss()
            }

            //set button state on view created or recreated
            setAddButtonState(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogInsertGroceryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    fun imageChoose() {
        if ((ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    != PackageManager.PERMISSION_GRANTED) && /*(ActivityCompat.checkSelfPermission(this@WelcomeActivity2, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) && */(ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    /* Manifest.permission.MANAGE_EXTERNAL_STORAGE,*/
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                12
            )
        } else {
            alertImage(requireContext(), object : AddMediaClickListener {
                override fun clickPhoto(type: Int) {
                    when (type) {
                        0 -> {
                            //gallery
                            val intent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            intent.type = "image/*";
                            startActivityForResult(
                                Intent.createChooser(intent, "Dosya Seç"), SELECT_FILE
                            )
                        }

                        1 -> {
                            //camera
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                                takePictureIntent.resolveActivity(requireContext().packageManager!!)
                                    .also {

                                        val photoFile: File? = try {
                                            createImageFile()
                                        } catch (ex: java.io.IOException) {

                                            null
                                        }
                                        photoFile?.also {
                                            val photoURI: Uri = FileProvider.getUriForFile(
                                                requireContext(),
                                                FILE_PROVIDER,
                                                it
                                            )
                                            takePictureIntent.putExtra(
                                                MediaStore.EXTRA_OUTPUT,
                                                photoURI
                                            )
                                            startActivityForResult(
                                                takePictureIntent,
                                                REQUEST_CAMERA
                                            )
                                        }
                                    }
                            }

                        }
                    }
                }


            })
        }
    }

    lateinit var currentPhotoFile: File
    private fun createImageFile(): File? {
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            POST_IMAGE_NAME,
            ".jpg",
            storageDir
        )
        currentPhotoFile = image
        return image
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as GroceryDialogListener
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initValidation() {
        binding.groceryNameTextInput.onTextChanged {
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

//            setAddButtonState(isNameValid && isAmountValid)
        }

        binding.amountTextInput.onTextChanged {
            when (AmountValidation.validate(it)) {
                true -> {
                    isAmountValid = true
                    setAmountErrorMessage(null)
                }
                false -> {
                    isAmountValid = false
                    setAmountErrorMessage(notValidAmountErrorMessage)
                }
            }

//            setAddButtonState(isAmountValid && isAmountValid)
        }
    }

    private fun setNameErrorMessage(string: String?) {
        binding.groceryNameTextInputLayout.error = string
    }

    private fun setAmountErrorMessage(string: String?) {
        binding.amountTextInputLayout.error = string
    }

    private fun setAddButtonState(isEnabled: Boolean) {
        if (isEnabled) {
            binding.addNewGroceryButton.apply {
                isClickable = true
                alpha = Button.State.Enable.alpha
            }
        } else {
            binding.addNewGroceryButton.apply {
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

    interface GroceryDialogListener {
        fun onInsertButtonClick(groceryName: String, quantity: Int, amount: Int)
        fun onUpdateButtonClick(desc: String, image: ByteArray?)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                try {
                    val selectedImageUri: Uri = data?.getData()!!
                    val tempPath = getPathLocalUri(selectedImageUri, requireActivity())
                    val btmapOptions = BitmapFactory.Options()
                    btmapOptions.inSampleSize = 2;
                    var bm: Bitmap = BitmapFactory.decodeFile(tempPath, btmapOptions)
                    val exif: ExifInterface
                    try {
                        exif = ExifInterface(tempPath)
                        if (exif.getAttribute(ExifInterface.TAG_ORIENTATION)
                                .equals("6")
                        ) {
                            bm = rotate(bm, 90)!!
                        } else if (exif.getAttribute(
                                ExifInterface.TAG_ORIENTATION
                            )
                                .equals("8")
                        ) {
                            bm = rotate(bm, 270)!!
                        } else if (exif.getAttribute(
                                ExifInterface.TAG_ORIENTATION
                            )
                                .equals("3")
                        ) {
                            bm = rotate(bm, 180)!!
                        }
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }

                    bm = saveImage(bm)
                    // startCropAndFilterActivity();

                } catch (e: Exception) {
                }

            } else if (requestCode == REQUEST_CAMERA) {

            }
        }
    }

    private fun saveImage(bmp: Bitmap): Bitmap {
        var os: FileOutputStream? = null
        try {

            os = FileOutputStream(File(requireActivity().externalCacheDir, POST_PROFILE_IMG_NAME))
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            imageInByte = baos.toByteArray()
        } catch (ignore: Exception) {
        } finally {
            try {
                os?.close()
            } catch (ignore: Throwable) {
            }
        }
        return bmp
    }
}