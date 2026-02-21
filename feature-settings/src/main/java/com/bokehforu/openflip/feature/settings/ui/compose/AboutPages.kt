package com.bokehforu.openflip.feature.settings.ui.compose

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.bokehforu.openflip.feature.settings.R
import com.bokehforu.openflip.feature.settings.ui.theme.ColorBuyMeACoffeeYellow
import com.bokehforu.openflip.feature.settings.ui.theme.LinkBlue
import com.bokehforu.openflip.feature.settings.ui.theme.OpenFlipTheme

data class VersionItem(
    val titleRes: Int,
    val descriptionRes: Int
)

private val versionHistory = listOf(
    VersionItem(R.string.titleV060, R.string.descriptionV060_details),
    VersionItem(R.string.titleV058, R.string.descriptionV058_details),
    VersionItem(R.string.titleV057, R.string.descriptionV057),
    VersionItem(R.string.titleV055, R.string.descriptionV055),
    VersionItem(R.string.titleV05, R.string.descriptionV15),
    VersionItem(R.string.titleV04, R.string.descriptionV11),
    VersionItem(R.string.titleV031, R.string.descriptionV101),
    VersionItem(R.string.titleV030, R.string.descriptionV10),
    VersionItem(R.string.titleV022, R.string.descriptionV08),
    VersionItem(R.string.titleV021, R.string.descriptionV07),
    VersionItem(R.string.titleV020, R.string.descriptionV06),
    VersionItem(R.string.titleV013, R.string.descriptionV05),
    VersionItem(R.string.titleV012, R.string.descriptionV04),
    VersionItem(R.string.titleV011, R.string.descriptionV03),
    VersionItem(R.string.titleV010, R.string.descriptionV02)
)

@Composable
fun VersionPage(
    isDarkTheme: Boolean // Kept for API compatibility, but we use MaterialTheme
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
    ) {
        item {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_version_header),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = stringResource(id = R.string.descriptionInspiredBy),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        items(versionHistory) { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(id = item.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = stringResource(id = item.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
fun AboutPage(
    isDarkTheme: Boolean // Kept for API compatibility, but we use MaterialTheme
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Image(
                painter = painterResource(id = R.drawable.img_mascot),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(200.dp)
            )

            Text(
                text = stringResource(id = R.string.aboutAndroidRobotCredit),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            AboutSectionCard {
                Text(
                    text = stringResource(id = R.string.aboutTributeTitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.labelProjectRepo),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                val repoUrl = stringResource(id = R.string.urlProjectRepo)
                Text(
                    text = repoUrl,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    color = LinkBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, repoUrl.toUri())
                            context.startActivity(intent)
                        }
                )
            }

            AboutSectionCard {
                Text(
                    text = stringResource(id = R.string.titleAcknowledgements),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.aboutIconSoundCredits),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(id = R.string.titleOpenSource),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.aboutOpenSourceCredits),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
            }

            AboutSectionCard {
                Text(
                    text = stringResource(id = R.string.aboutDeveloperSupport),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )

                Surface(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://www.buymeacoffee.com/yanjiaqiid5".toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(48.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium, // Rounded corners
                    color = ColorBuyMeACoffeeYellow 
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.actionBuyMeACoffeeShort),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AboutSectionCard {
                Text(
                    text = stringResource(id = R.string.aboutMainStory),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.aboutEvolutionStory),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.aboutV057Update),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
            }

            Text(
                text = stringResource(id = R.string.aboutEnjoySilence),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AboutSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Version Page Light")
@Composable
fun PreviewVersionPageLight() {
    OpenFlipTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            VersionPage(isDarkTheme = false)
        }
    }
}

@Preview(showBackground = true, name = "About Page Light")
@Composable
fun PreviewAboutPageLight() {
    OpenFlipTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            AboutPage(isDarkTheme = false)
        }
    }
}

@Preview(showBackground = true, name = "About Page Dark", backgroundColor = 0xFF000000)
@Composable
fun PreviewAboutPageDark() {
    OpenFlipTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            AboutPage(isDarkTheme = true)
        }
    }
}
