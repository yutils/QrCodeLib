package com.google.zxing.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.FormatException
import com.google.zxing.NotFoundException
import com.google.zxing.Result
import com.google.zxing.WriterException
import com.google.zxing.activity.CaptureActivity
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.decoding.RGBLuminanceSource
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Hashtable

/**
 * 二维码工具类
 *
 * @author yujing 2023年11月10日15:01:31
 * 需要导入包  implementation 'com.google.zxing:core:3.5.3'
 */
object QRCodeUtil {
    /**
     * 调用相机扫描二维码 （自动判断相机权限，如果没有权限，会先请求相机权限）
     */
    /*
        ScanQRCode.openCamera(activity) { it ->
            //it 扫码结果
        }
     */
    @JvmStatic
    fun openCamera(activity: ComponentActivity, listener: (String?) -> Unit) {
        activity.activityResultRegistry.register("相机权限", ActivityResultContracts.RequestPermission()) {
            //没有相机权限
            if (!it) return@register Toast.makeText(activity, "请打开相机权限", Toast.LENGTH_SHORT).show()
            //监听扫码页面返回
            activity.activityResultRegistry.register("跳转", ActivityResultContracts.StartActivityForResult()) { result ->
                if (result?.resultCode != Activity.RESULT_OK) return@register //没有扫描到二维码
                val scanResult = result.data?.extras?.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN)
                listener(scanResult)
            }.run { launch(Intent(activity, CaptureActivity::class.java)) }
        }.run { launch(Manifest.permission.CAMERA) }
    }

    /**
     * 相册选取
     */
    fun openAlbum(activity: ComponentActivity, listener: (String?) -> Unit) {
        //打开手机中的相册
        activity.activityResultRegistry.register("打开手机中的相册", ActivityResultContracts.StartActivityForResult()) { result ->
            if (result?.resultCode != Activity.RESULT_OK) return@register //没有选择照片
            val uri: Uri? = result.data?.data
            val bitmap = BitmapUtil.decodeUri(activity, uri, 500, 500)
            val result = decode(bitmap)
            if (result == null) {
                listener(null)
                return@register Toast.makeText(activity, "识别失败", Toast.LENGTH_SHORT).show()
            }
            listener(result.text)
        }.run { launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }) }
    }

    /**
     * 生成自定义二维码
     * Bitmap qrBitmap = QRCodeUtil.encode("二维码内容", 500, 500, "UTF-8", "L", "0", Color.BLACK, Color.WHITE, null, 0, null);
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @param logoBitmap             logo图片（传null时不添加logo）
     * @param logoPercent            logo所占百分比
     * @param bitmap_black           用来代替黑色色块的图片（传null时不代替）
     * @return
     */
    @JvmStatic
    fun encode(content: String?, width: Int, height: Int, character_set: String?, error_correction_level: String?, margin: String?, color_black: Int, color_white: Int, logoBitmap: Bitmap?, logoPercent: Float, bitmap_black: Bitmap?): Bitmap? {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null
        }
        // 宽和高>=0
        return if (width < 0 || height < 0) {
            null
        } else try {
            /** 1.设置二维码相关配置,生成BitMatrix(位矩阵)对象  */
            val hints = Hashtable<EncodeHintType, String?>()
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints[EncodeHintType.CHARACTER_SET] = character_set
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints[EncodeHintType.ERROR_CORRECTION] = error_correction_level
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints[EncodeHintType.MARGIN] = margin
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象  */
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值  */
            var bitmapBlackNew = bitmap_black
            if (bitmapBlackNew != null) {
                //从当前位图按一定的比例创建一个新的位图
                bitmapBlackNew = Bitmap.createScaledBitmap(bitmapBlackNew, width, height, false)
            }
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix[x, y]) { // 黑色色块像素设置
                        //图片不为null，则将黑色色块换为新位图的像素。
                        pixels[y * width + x] = bitmapBlackNew?.getPixel(x, y) ?: color_black
                    } else {
                        pixels[y * width + x] = color_white // 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象  */
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            /** 5.为二维码添加logo图标  */
            if (logoBitmap != null) {
                addLogo(bitmap, logoBitmap, logoPercent)
            } else bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun decode(context: Context?, uri: Uri?): Result? {
        if (uri == null) return null
        val bitmap = BitmapUtil.decodeUri(context, uri, 500, 500)
        return decode(bitmap)
    }

    /**
     * 识别图片中的二维码
     *
     * @param bitmap
     * @return result.getText()
     */
    @JvmStatic
    fun decode(bitmap: Bitmap?): Result? {
        val hints = Hashtable<DecodeHintType, String?>()
        hints[DecodeHintType.CHARACTER_SET] = "UTF8" //设置二维码内容的编码
        val source = RGBLuminanceSource(bitmap)
        val bitmap1 = BinaryBitmap(HybridBinarizer(source))
        val reader = QRCodeReader()
        try {
            return reader.decode(bitmap1, hints)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 向二维码中间添加logo图片(图片合成)
     * 读取资源文件夹下面的图片
     * Resources res = getResources();
     * Bitmap logoBitmap= BitmapFactory.decodeResource(res,R.mipmap.logo);
     *
     * @param srcBitmap   原图片（生成的简单二维码图片）
     * @param logoBitmap  logo图片
     * @param logoPercent 百分比 (用于调整logo图片在原图片中的显示大小, 取值范围[0,1] )
     * 原图片是二维码时,建议使用0.2F,百分比过大可能导致二维码扫描失败。
     * @return
     */
    @JvmStatic
    fun addLogo(srcBitmap: Bitmap?, logoBitmap: Bitmap?, logoPercent: Float): Bitmap? {
        var percent = logoPercent
        if (srcBitmap == null) return null
        if (logoBitmap == null) return srcBitmap
        //传值不合法时使用0.2F
        if (percent < 0f || percent > 1f) percent = 0.2f
        val srcWidth = srcBitmap.width
        val srcHeight = srcBitmap.height
        val logoWidth = logoBitmap.width
        val logoHeight = logoBitmap.height
        val bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(srcBitmap, 0f, 0f, null)
        canvas.scale(srcWidth * percent / logoWidth, srcHeight * percent / logoHeight, srcWidth / 2f, srcHeight / 2f)
        canvas.drawBitmap(logoBitmap, srcWidth / 2f - logoWidth / 2f, srcHeight / 2f - logoHeight / 2f, null)
        return bitmap
    }
}