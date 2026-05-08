package homes.snaix.app.yank.domain.zxing

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.GenericMultipleBarcodeReader

data class DecodedCode(val text: String, val format: String)

class ZxingDecoder {

    fun decodeAll(bitmap: Bitmap): List<DecodedCode> {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val binary = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader().apply {
            setHints(mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.PDF_417,
                ),
            ))
        }
        val multi = GenericMultipleBarcodeReader(reader)

        return try {
            multi.decodeMultiple(binary).map { DecodedCode(it.text, it.barcodeFormat.name) }
        } catch (_: NotFoundException) {
            try {
                val r = reader.decode(binary)
                listOf(DecodedCode(r.text, r.barcodeFormat.name))
            } catch (_: NotFoundException) {
                emptyList()
            }
        }
    }
}
