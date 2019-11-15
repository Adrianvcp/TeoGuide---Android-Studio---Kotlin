package com.example.teoguideas.Controllers.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.teoguideas.Common.Common
import com.beust.klaxon.*

import com.example.teoguideas.R
import com.example.teoguideas.Retrofit.IComicAPI
import com.example.teoguideas.Service.PicassoImageLoadingService
import com.example.teoguideas.perfilRecursoActivity
import com.google.gson.GsonBuilder
import com.google.protobuf.Parser
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.imageView
import kotlinx.android.synthetic.main.activity_perfil_recurso.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ss.com.bannerslider.Slider
import java.io.*
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PlanesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PlanesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlanesFragment : Fragment() {
    internal var compositeDisposable = CompositeDisposable()
    internal lateinit var iComicAPI: IComicAPI
    internal var mBitmap: Bitmap? = null
    lateinit var datoencontrado: String

    internal lateinit var apiService: IComicAPI
    internal var picUri: Uri? = null
    private var permissionsToRequest: ArrayList<String>? = null
    private val permissionsRejected = ArrayList<String>()
    private val permissions = ArrayList<String>()

    internal lateinit var fabCamera: Button
    internal lateinit var fabUpload: Button
    internal lateinit var textView: TextView

    private fun getPathFromURI(contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        //var sorting = ContactsContract.Contacts.DISPLAY_NAME + " DESC"
        var cursor = getActivity()?.getContentResolver()
            ?.query(contentUri!!, proj, null, null, null)
        //val cursor = contentResolver.query(contentUri!!, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    val pickImageChooserIntent: Intent
        get() {

            val outputFileUri = captureImageOutputUri

            val allIntents = ArrayList<Intent>()
            val packageManager = requireActivity().packageManager

            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val listCam = packageManager.queryIntentActivities(captureIntent, 0)
            for (res in listCam) {
                val intent = Intent(captureIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                if (outputFileUri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                }
                allIntents.add(intent)
            }

            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
            galleryIntent.type = "image/*"
            val listGallery = packageManager.queryIntentActivities(galleryIntent, 0)
            for (res in listGallery) {
                val intent = Intent(galleryIntent)
                intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                allIntents.add(intent)
            }

            var mainIntent = allIntents[allIntents.size - 1]
            for (intent in allIntents) {
                if (intent.component!!.className == "com.android.documentsui.DocumentsActivity") {
                    mainIntent = intent
                    break
                }
            }
            allIntents.remove(mainIntent)

            val chooserIntent = Intent.createChooser(mainIntent, "Select source")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toTypedArray<Parcelable>())

            println(chooserIntent)
            return chooserIntent
        }

    private val captureImageOutputUri: Uri?
        get() {
            var outputFileUri: Uri? = null
            println("URI :" + outputFileUri)

            //val getImage = getExternalFilesDir("")
            //val getImage = Environment.getExternalStorageDirectory().toString()
            val getImage = requireActivity().getExternalFilesDir("")
            println("getImage :" + getImage)
            if (getImage != null) {
                println("image :" + getImage)
                outputFileUri = Uri.fromFile(File(getImage.path,"profile.png"))
            }
            return outputFileUri
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val r = inflater.inflate(R.layout.activity_camarabuscar, container, false)



        return r
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        askPermissions()
        //Init API
        iComicAPI = Common.api

/*
        Slider.init(PicassoImageLoadingService(requireActivity()))

        recycler_comic.setHasFixedSize(true)
        recycler_comic.layoutManager = GridLayoutManager(requireActivity(),2)

        swipe_refresh.setColorSchemeResources(R.color.colorPrimary,android.R.color.holo_orange_dark,android.R.color.background_dark)
        swipe_refresh.setOnRefreshListener {
            if (Common.isConnectedToInternet(requireActivity().baseContext)){

                fetchComic()
            }
            else{
                Toast.makeText(requireActivity().baseContext,"Please check u connection", Toast.LENGTH_SHORT).show()

            }
        }
        swipe_refresh.post(Runnable {
            if (Common.isConnectedToInternet(requireActivity().baseContext)){

                fetchComic()
            }
            else{
                Toast.makeText(requireActivity().baseContext,"Please check u connection", Toast.LENGTH_SHORT).show()

            }
        })
*/


        btnProbando.setOnClickListener {
            println("Funcionando")

            startActivityForResult(pickImageChooserIntent, PlanesFragment.IMAGE_RESULT);
        }

        btnSubir.setOnClickListener {
            //val intent: Intent = Intent(activity, perfilRecursoActivity::class.java)
            if (mBitmap != null) {

                multipartImageUpload()
                //if (datoencontrado != null) intent.putExtra("SD",datoencontrado)
                //startActivity(intent)
            }
            else {
                Toast.makeText(requireActivity().applicationContext, "Bitmap is null. Try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askPermissions() {
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionsToRequest = findUnAskedPermissions(permissions)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest!!.size > 0)
                requestPermissions(permissionsToRequest!!.toTypedArray<String>(),
                    PlanesFragment.ALL_PERMISSIONS_RESULT
                )
        }
    }

    private fun findUnAskedPermissions(wanted: ArrayList<String>): ArrayList<String> {
        val result = ArrayList<String>()

        for (perm in wanted) {
            if (!hasPermission(perm)) {
                result.add(perm)
            }
        }

        return result
    }
    private fun hasPermission(permission: String): Boolean {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return requireActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
    }
    private fun canMakeSmores(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }
    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun multipartImageUpload() {
        try {
            val filesDir = requireActivity().applicationContext.filesDir
            val file = File(filesDir, "image" +".png")


            val bos = ByteArrayOutputStream()
            mBitmap!!.compress(Bitmap.CompressFormat.PNG, 0, bos)
            val bitmapdata = bos.toByteArray()


            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()


            val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
            val body = MultipartBody.Part.createFormData("upload", file.name, reqFile)
            val name = RequestBody.create(MediaType.parse("text/plain"), "upload")

            //val req = apiService.postImage(body, name)
            val req = iComicAPI.postImage(body, name)
            req.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                    if (response.code() == 200) {
                        //datoencontrado = response.body()!!.string()
                        val gar = response.body()?.charStream()
                        val varsd: JSONArray = JSONArray(response.body()?.string())
                        //MOSTRAR EN EL FRAGMENT
                        println("OKKKKKKKKKKKKKKKKKKKKKKKKKK")

                        fun String.toWords() = trim().splitToSequence(' ').filter { it.isNotEmpty() }.toList()


                        val verga = varsd.getJSONObject(0)

                        var assdf = response.body()?.string()

                        val gson = GsonBuilder().create()

                        val parser: com.beust.klaxon.Parser = com.beust.klaxon.Parser.default()

                        txtHistoria.text = verga.get("dHistroria").toString()
                        txtNombre.text = verga.get("nNombre").toString()
                        var urlImage = verga.get("imgportada").toString()
                        Picasso.get().load(urlImage).into(imageView)

                    }
                    Toast.makeText(requireActivity().applicationContext, response.code().toString() + " ", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    //textView.text = "Uploaded Failed!"
                    //textView.setTextColor(Color.RED)
                    println("MALLLLLLLLLLLL")
                    Toast.makeText(requireActivity().applicationContext, "Request failed", Toast.LENGTH_SHORT).show()
                    t.printStackTrace()
                }
            })


        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getImageFromFilePath(data: Intent?): String? {
        val isCamera = data == null || data.data == null

        return if (isCamera)
            captureImageOutputUri!!.path
        else {
            println(data)
            getPathFromURI(data!!.data)
        }

    }

    fun getImageFilePath(data: Intent?): String? {
        return getImageFromFilePath(data)
    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        if (resultCode == Activity.RESULT_OK) {

            //val imageView = findViewById<ImageView>(R.id.imageView)

            if (requestCode == PlanesFragment.IMAGE_RESULT) {


                val filePath = getImageFilePath(data)
                if (filePath != null) {
                    mBitmap = BitmapFactory.decodeFile(filePath)
                    imageView.setImageBitmap(mBitmap)
                }
            }
        }
    }

   @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {

            PlanesFragment.ALL_PERMISSIONS_RESULT -> {
                for (perms in permissionsToRequest!!) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms)
                    }
                }

                if (permissionsRejected.size > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected[0])) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                DialogInterface.OnClickListener { dialog, which -> requestPermissions(permissionsRejected.toTypedArray<String>(),
                                    PlanesFragment.ALL_PERMISSIONS_RESULT
                                ) })
                            return
                        }
                    }

                }
            }
        }

    }


    companion object {
        private val ALL_PERMISSIONS_RESULT = 107
        private val IMAGE_RESULT = 200
    }
}
