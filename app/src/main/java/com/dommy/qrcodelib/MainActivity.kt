package com.dommy.qrcodelib

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dommy.qrcodelib.databinding.ActivityMainBinding
import com.google.zxing.util.QRCodeUtil
import com.google.zxing.util.QrCodeGenerator


/**
 * 扫码或者生成二维码
 * @author yujing 2021年9月8日11:31:47
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        //开始扫码
        binding.btnQrCode.setOnClickListener {
            //获取权限后打开摄像头扫码，并解析结果
            QRCodeUtil.openCamera(this) { binding.tvResult.text = it }
        }
        //生成二维码
        binding.btnGenerate.setOnClickListener { generateQrCode() }
        //相册选取
        binding.btnAlbum.setOnClickListener {
            //选择照片并解析二维码
            QRCodeUtil.openAlbum(this) { binding.tvResult.text = it }
        }
    }

    /**
     * 生成二维码
     */
    private fun generateQrCode() {
        if (binding.etContent.text.toString() == "") return Toast.makeText(this, "请输入二维码内容", Toast.LENGTH_SHORT).show()
        val bitmap = QrCodeGenerator.getQrCodeImage(binding.etContent.text.toString(), binding.imgQrcode.width, binding.imgQrcode.height)
        binding.imgQrcode.setImageBitmap(bitmap)
        if (bitmap == null) return Toast.makeText(this, "生成二维码出错", Toast.LENGTH_SHORT).show()
    }
}