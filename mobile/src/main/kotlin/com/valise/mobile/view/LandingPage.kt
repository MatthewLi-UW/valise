package com.valise.mobile.view


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.valise.mobile.Home
import com.valise.mobile.R
import com.valise.mobile.controller.LoginController
import com.valise.mobile.model.Model
import com.valise.mobile.ui.theme.Black60
import com.valise.mobile.ui.theme.OffWhite
import com.valise.mobile.ui.theme.Transparent


// types of actions that our controller understands
 enum class LoginViewEvent { Refresh }

const val TAG="OAUTH"
//const val WEB_CLIENT_ID = "768377809743-uba67th0lured51322dhe9a8p151iglh.apps.googleusercontent.com"

@Composable
fun LandingPage(
    landingPageViewModel: LoginViewModel,
    landingPageController: LoginController,
    navController: NavController,
) {
    val viewModel by remember { mutableStateOf(landingPageViewModel) }
    val controller by remember { mutableStateOf(landingPageController) }
    val context = LocalContext.current
    val josefinsans = FontFamily(
        Font(R.font.josefinsans_bold, FontWeight.Black, FontStyle.Normal),
    )

    if(Firebase.auth.currentUser != null){
//        controller.invoke(LoginViewEvent.Refresh, 0)
        navController.navigate(Home)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.landing_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // black to transparent gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient( colors = listOf(Transparent, Black60) )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // title wrapper
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Valise",
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Bold,
                    color = OffWhite,
                    fontFamily = josefinsans,
                )
            }

            // caption wrapper
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "\"The world is a book, and those who do not travel read only one page.\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OffWhite,
                    textAlign = TextAlign.Center,
                )
            }

            // button wrapper
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 70.dp)
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FilledTonalButton( // google signin
                    onClick = {
                        //not sure if vm should handle this but vms have their own built in coroutinescope
                        viewModel.handleGoogleSignIn(context,
                            onSuccess = { result ->
                                viewModel.googleSignInSuccess(result,
                                    onFSSuccess = {
                                        viewModel
                                        navController.navigate(Home)
                                    },
                                    onFSFailure = { e ->
                                        Log.w(TAG, "Error adding document", e)
                                    }
                                )
                            },
                            onFailure = {}
                        )
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = OffWhite,
                        contentColor = Color.Black
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        Spacer(modifier = Modifier.width(14.dp))
                        Image(
                            painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 5.dp).size(20.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "Continue with Google")
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer (modifier = Modifier.height(5.dp))

                Text(
                    text = "or Continue as Guest",
                    fontSize = 14.sp,
                    color = OffWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable {
                            viewModel.handleGuestSignIn(context,
                                onSuccess = { result ->
                                    viewModel.googleSignInSuccess(result,
                                        onFSSuccess = {
                                            viewModel
                                            navController.navigate(Home)
                                        },
                                        onFSFailure = { e ->
                                            Log.w(TAG, "Error adding document", e)
                                        }
                                    )
                                },
                                onFailure = {}
                            )
                        }
                        .padding(vertical = 5.dp)
                        .align(Alignment.CenterHorizontally),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}


@Composable
fun PreviewLandingPage() {
    val model = Model()
    val viewModel = LoginViewModel(model)
    val controller = LoginController(model)

//    LandingPage(viewModel, controller)
}


