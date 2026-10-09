package dev.orionlabs.orionmusic.playback.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.ListenableFuture
import dev.orionlabs.orionmusic.common.models.ImageHolder
import dev.orionlabs.orionmusic.utils.CoroutineUtils.futureCatching
import dev.orionlabs.orionmusic.utils.Serializer.toData
import dev.orionlabs.orionmusic.utils.image.ImageUtils.loadDrawable
import kotlinx.coroutines.CoroutineScope

@UnstableApi
class PlayerBitmapLoader(
    val context: Context,
    private val scope: CoroutineScope
) : BitmapLoader {

    override fun supportsMimeType(mimeType: String) = true

    override fun decodeBitmap(data: ByteArray) = scope.futureCatching {
        BitmapFactory.decodeByteArray(data, 0, data.size) ?: error("Failed to decode bitmap")
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> = scope.futureCatching {
        val cover = uri.getQueryParameter("actual_data")!!.toData<ImageHolder>().getOrThrow()
        cover.loadDrawable(context)?.toBitmapOrNull()
            ?: error("Failed to load bitmap of $cover")
    }
}
