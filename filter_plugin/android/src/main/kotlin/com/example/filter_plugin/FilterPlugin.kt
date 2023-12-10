package com.example.filter_plugin

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import com.example.filter_plugin.filter.GaussianBlur
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.view.TextureRegistry.SurfaceTextureEntry
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel.Result
import java.nio.ByteBuffer
import androidx.annotation.NonNull

/** FilterPlugin */
class FilterPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private var gaussianBlur: GaussianBlur? = null
    private var pluginBinding: FlutterPluginBinding? = null
    private var flutterSurfaceTexture: SurfaceTextureEntry? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPluginBinding) {
        // Create a communication channel between Flutter land and Android
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "filter_plugin")
        channel.setMethodCallHandler(this)
        this.pluginBinding = flutterPluginBinding
    }

    // This will be called whenever we get a message from Android land
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "create" -> {
                if (pluginBinding == null) {
                    result.error("NOT_READY", "plugin binding is null", null)
                    return
                }

                createFilter(call, result)

                Log.e("creating", "done creating")
            }
            "draw" -> {
                if (gaussianBlur != null) {
                    // Get the radius parameter
                    val radius: Double = call.argument("radius")!!

                    gaussianBlur!!.draw(radius.toFloat(), true)
                    result.success(null)
                } else {
                    result.error("NOT_INITIALIZED", "Filter not initialized", null)
                }
            }
            "dispose" -> {
                gaussianBlur?.destroy()

                if (flutterSurfaceTexture != null) flutterSurfaceTexture!!.release()
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        this.pluginBinding = null
    }

    private fun createFilter(@NonNull call: MethodCall, @NonNull result: Result) {
        // Get request parameters
        val width: Int = call.argument("width")!!
        val height: Int = call.argument("height")!!
        val srcImage = call.argument("img") as? ByteArray

        // Our response will be a dictionary
        val reply: MutableMap<String, Any> = HashMap()

        if (srcImage != null) {
            // Convert input image to bitmap
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(ByteBuffer.wrap(srcImage))

            // Create a surface for our filter to draw on, it is backed by a texture we get from Flutter
            flutterSurfaceTexture = pluginBinding!!.textureRegistry.createSurfaceTexture()
            val nativeSurfaceTexture: SurfaceTexture = flutterSurfaceTexture!!.surfaceTexture()
            nativeSurfaceTexture.setDefaultBufferSize(width, height)
            val nativeSurface = Surface(nativeSurfaceTexture)

            // Create our filter and tell it to draw to the surface we just created (which is backed
            // by the Flutter texture)
            gaussianBlur = GaussianBlur(nativeSurface, bmp)
        }

        // Return the Flutter texture id, the "Texture" widget in our app will display it
        reply["textureId"] = flutterSurfaceTexture?.id() ?: -1
        result.success(reply)
    }
}
