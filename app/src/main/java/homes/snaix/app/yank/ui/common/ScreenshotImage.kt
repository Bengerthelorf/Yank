package homes.snaix.app.yank.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@Composable
fun ScreenshotImage(
    path: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(File(path))
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
    )
}
