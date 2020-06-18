package com.mycelium.bequant.kyc.steps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mycelium.wallet.BuildConfig
import com.mycelium.wallet.R
import com.mycelium.wallet.Utils
import kotlinx.android.synthetic.main.dialog_bequant_document_attach.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class DocumentAttachDialog : BottomSheetDialogFragment() {

    private var hasCameraPermission: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_bequant_document_attach, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        camera.setOnClickListener {

            hasCameraPermission = Utils.hasOrRequestAccess(this, Manifest.permission.CAMERA, CAMERA_REQUEST_CODE)
            if (!hasCameraPermission) {
                // finishError(R.string.no_camera_permission);
                return@setOnClickListener
            }
            startCamera()

            dismissAllowingStateLoss()
        }
        upload.setOnClickListener {
            targetFragment?.startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                    targetRequestCode or REQURST_CODE_GALARY)

            dismissAllowingStateLoss()
        }
    }

    private fun startCamera() {
        val photoFile = createImageFile()
        val authority = "${BuildConfig.APPLICATION_ID}.files"
        val photoURI = FileProvider.getUriForFile(requireContext(), authority, photoFile)
        targetFragment?.startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, photoURI), targetRequestCode or REQURST_CODE_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoFile = this
        }
    }

    companion object {
        const val REQURST_CODE_CAMERA = 1000
        const val REQURST_CODE_GALARY = 2000
        private val CAMERA_REQUEST_CODE = 504

        var currentPhotoFile: File? = null
    }
}
