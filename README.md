# QrCodeLib

## 在原版上优化如下

1.谷歌api升级到com.google.zxing:core:3.5.2
2.开发sdk升级到34（安卓14）
3.简化使用，新增QRCodeUtil工具类
4.一行代码实现相机扫码解析
5.一行代码实现选择照片并解析
6.一行代码解析bitmap
7.扩张生成二维码的样式和风格，可以在二维码中插入图片

## 致谢

- ZXing
- ahuyangdong  https://github.com/ahuyangdong/QrCodeLib


### 引入

#### 项目工程gradle中添加　　　　[当前最新版：————> 1.0.2　　　　![最新版](https://img.shields.io/badge/%E6%9C%80%E6%96%B0%E7%89%88-1.0.2-green.svg)](https://search.maven.org/artifact/com.kotlinx/zxing-lib)

```gradle
implementation 'com.kotlinx:zxing-lib:1.0.2'
```

### 扫描二维码

```kotlin
//获取权限后打开摄像头扫码
QRCodeUtil.openCamera(activity) {
    binding.tvResult.text = it //扫到二维码后解析结果
}

//选择照片并解析二维码
QRCodeUtil.openAlbum(activity) {
    binding.tvResult.text = it //选照片后解析结果
}

//生成二维码
var bitmap = QrCodeGenerator.getQrCodeImage("二维码内容", 512, 512)
//或者
var bitmap = QRCodeUtil.encode("二维码内容", 512, 512, "UTF-8", "H", "0", Color.BLACK, Color.WHITE, null, 0F, null)

//解析图片中的二维码
val value = QRCodeUtil.decode(bitmap)?.text
```

注：开摄像头扫码AndroidManifest中需要添加 <activity android:name="com.google.zxing.activity.CaptureActivity" />

### 也可以自己申请权限后再扫码（不推荐）

```kotlin
//举例：获取相机权限后扫码
activityResultRegistry.register("相机权限", ActivityResultContracts.RequestPermission()) {
    //没有相机权限
    if (!it) return@register Toast.makeText(this, "请打开相机权限", Toast.LENGTH_SHORT).show()
    //监听扫码页面返回
    activityResultRegistry.register("跳转", ActivityResultContracts.StartActivityForResult()) { result ->
        if (result?.resultCode != Activity.RESULT_OK) return@register //没有扫描到二维码
        val scanResult = result.data?.extras?.getString(com.google.zxing.util.Constant.INTENT_EXTRA_KEY_QR_SCAN)
        //扫码结果
        binding.tvResult.text = scanResult
    }.run { launch(Intent(this@MainActivity, CaptureActivity::class.java)) }
}.run { launch(Manifest.permission.CAMERA) }


//举例：扫码相册中二维码
activityResultRegistry.register("打开手机中的相册", ActivityResultContracts.StartActivityForResult()) { result ->
    if (result?.resultCode != Activity.RESULT_OK) return@register //没有选择照片
    val uri: Uri? = result.data?.data
    val bitmap = BitmapUtil.decodeUri(this, uri, 500, 500)
    val result = QRCodeUtil.decode(bitmap)
    if (result == null) {
        binding.tvResult.text = ""
        return@register Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show()
    }
    //扫码结果
    binding.tvResult.text = result.text
}.run { launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }) }
```