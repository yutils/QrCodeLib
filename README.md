# QrCodeLib

## 开发环境准备
**推荐使用jetBrains Toolbox 中的android studio，并更新到最新正式版**  

【必须】打开AS的安装目录，在bin目录下找到这两个文件（studio.exe.vmoptions，studio64.exe.vmoptions）  
在其中最后一行添加	-Dfile.encoding=UTF-8   
```bat
安装目录位置
C:\Users\用户名\AppData\Local\JetBrains\Toolbox\apps\AndroidStudio\ch-0\版本\bin
如：
C:\Users\yujing\AppData\Local\JetBrains\Toolbox\apps\AndroidStudio\ch-0\211.7628.21.2111.8139111\bin
```

## 致谢
- ZXing 
- ahuyangdong  https://github.com/ahuyangdong/QrCodeLib

# zxing-lib 二维码使用

### 引入
#### 主gradle中添加
```gradle
buildscript {
    repositories {
        mavenCentral()
    }
}
```
#### 项目工程gradle中添加　　　　[当前最新版：————> 1.0.0　　　　![最新版](https://img.shields.io/badge/%E6%9C%80%E6%96%B0%E7%89%88-1.0.0-green.svg)](https://search.maven.org/artifact/com.kotlinx/zxing-lib)

```gradle
implementation 'com.kotlinx:zxing-lib:1.0.0'
```

### 扫描二维码
```kotlin
//开始扫码，代码调用这一行。
registerPermission.launch(Manifest.permission.CAMERA)


//请求权限结果 (这是成员变量，注册事件)
private val registerPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
    if (it) registerCapture.launch(Intent(this, CaptureActivity::class.java))
}

//扫码结果 (这是成员变量，注册事件)
private val registerCapture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val scanResult = result.data?.extras?.getString(com.google.zxing.util.Constant.INTENT_EXTRA_KEY_QR_SCAN)
        //这儿是后续操作
        binding.tvResult.text = scanResult
    }
}
```

### 生成二维码
```java
Bitmap bitmap = QrCodeGenerator.getQrCodeImage("yujing", 512, 512);
```