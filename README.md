
# Android Pdf Library 📚 [![](https://jitpack.io/v/talento-mobile/android-pdf-library.svg)](https://jitpack.io/#talento-mobile/android-pdf-library) ![Workflow result](https://github.com/talento-mobile/android-pdf-library/workflows/Check/badge.svg)
## Step 1.
Add the JitPack repository to your build file
``` Grooby
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
## Step 2.
Add the dependency
```Grooby
dependencies {
	implementation 'com.github.talento-mobile:android-pdf-library:Tag'
}
```
### Documentation
- In the layout:
```XML
<com.talentomobile.pdf.feature.pdf.PDFViewer  
  android:id="@+id/pdf"  
  android:layout_width="0dp"  
  android:layout_height="0dp"  
  app:layout_constraintBottom_toBottomOf="parent"  
  app:layout_constraintEnd_toEndOf="parent"  
  app:layout_constraintStart_toStartOf="parent"  
  app:layout_constraintTop_toTopOf="parent"  
  app:pdf_animation="true"  
  app:pdf_direction="vertical" />
```
- Declare the function `getPdfStatusListener()` wich is used to get the PDF.
```Kotlin
private fun getPdfStatusListener() =  
    object : PDFViewer.PDFViewerStatusListener {  
        override fun onStartDownload() {}  
        override fun onPageChanged(position: Int, total: Int) {}    
        override fun onProgressDownload(currentStatus: Int) {}  
        override fun onSuccessDownLoad(path: String) {  
	        binding.pdf.fileInit(path)  
        }  
        override fun onFail(error: Throwable) {}  
        override fun unsupportedDevice() {}  
    }
```
- To get it from url:
```Kotlin
private fun urlPDF() {
    val url = ""
    binding.pdf.initializePDFDownloader(url, getPdfStatusListener())
}
```
- To get it from `ByteArray` :
```Kotlin
private fun bytesPDF(bytes: ByteArray) {  
    val pdf = bytes.savePDF(requireContext())  
    if (pdf != null) {  
        binding.pdf.initStatusListener(getPdfStatusListener())  
        binding.pdf.fileInit(pdf.path)  
    }  
}
```
- in the `On Destroy` apply:
```Kotlin
override fun onDestroy() {  
    super.onDestroy()  
    binding.pdf.pdfRendererCore?.clear()  
}
```

💙 Made with love by the TalentoMobile's Android team 💙
