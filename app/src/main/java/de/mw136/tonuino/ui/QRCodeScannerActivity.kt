package de.mw136.tonuino.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QRCodeScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        scannerView!!.setFormats(listOf(BarcodeFormat.QR_CODE))
        scannerView!!.setLaserEnabled(false)
        scannerView!!.setBorderColor(Color.WHITE)
        setContentView(scannerView)
    }

    override fun handleResult(rawResult: Result) {
        val intent = Intent()
        intent.putExtra("de.mw136.tonuino.ui.qrcode_result", rawResult.text)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        scannerView!!.setResultHandler(this)
        scannerView!!.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView!!.stopCamera()
    }
}