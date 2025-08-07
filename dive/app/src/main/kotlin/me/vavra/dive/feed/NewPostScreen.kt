package me.vavra.dive.feed

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import me.vavra.dive.common.theme.DiveTheme
import me.vavra.dive.common.ui.BottomSheetTopBar
import me.vavra.dive.common.ui.SendUp


@Composable
fun NewPostScreen() {
    val viewModel = viewModel<NewPostViewModel>()
    NewPostScreenContent(viewModel.state, onSend = {
        viewModel.sendPost()
    }, onChangeText = {
        viewModel.changeText(it)
    }, onChangeImageUri = {
        viewModel.changeImageUri(it)
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostScreenContent(
    state: NewPostState,
    onChangeText: (String) -> Unit = {},
    onChangeImageUri: (Uri) -> Unit = {},
    onSend: () -> Unit = { }
) {
    when (state.progress) {
        NewPostState.Progress.INITIAL, NewPostState.Progress.SENDING -> {

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = GetContent(),
                onResult = { uri: Uri? ->
                    if (uri != null) {
                        onChangeImageUri(uri)
                    }
                }
            )

            Scaffold(
                topBar = {
                    BottomSheetTopBar("Nový příspěvek")
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = state.text,
                        onValueChange = { onChangeText(it) },
                        label = { Text("Co se ti honí hlavou?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(horizontal = 20.dp),
                        enabled = state.progress == NewPostState.Progress.INITIAL
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(state.imageUri),
                            contentDescription = "Attached image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                enabled = state.progress == NewPostState.Progress.INITIAL
                            ) {
                                Text("Vybrat fotku")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    SendUp(
                        Modifier.padding(horizontal = 20.dp),
                        state.progress == NewPostState.Progress.INITIAL && state.imageUri != null && !state.text.isEmpty(),
                        state.progress == NewPostState.Progress.SENDING,
                        onSend
                    )
                }
            }
        }
        NewPostState.Progress.SUCCESS, NewPostState.Progress.FAIL -> {
            val activity = LocalActivity.current as ComponentActivity?
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewPostScreenInitialPreview() {
    DiveTheme {
        NewPostScreenContent(NewPostState())
    }
}

@Preview(showBackground = true)
@Composable
fun NewPostScreenInitialToSendPreview() {
    DiveTheme {
        NewPostScreenContent(NewPostState("test", "https://test.com".toUri()))
    }
}


