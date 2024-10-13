package com.example.mpproject.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mpproject.PMApplication.Companion.appContext
import com.example.mpproject.R
import com.example.mpproject.db.PMDatabase
import com.example.mpproject.db.ParliamentMember
import com.example.mpproject.ui.theme.MPProjectTheme
import com.example.mpproject.utils.ImageLoader
import com.example.mpproject.viewmodel.PMViewModel

// 13.10.2024 by Hafiz
enum class Screens {
    Info
}

val COLORS = mapOf(
    "primary" to Color(ContextCompat.getColor(appContext, R.color.primary))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = PMViewModel(applicationContext)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            MPProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navController, startDestination = Screens.Info.name + "/") {
                        composable(route = Screens.Info.name + "/{hetekaId}?") {
                            val hetekaId: Int? = it.arguments?.getString("hetekaId")?.toIntOrNull()
                            viewModel.setMember(hetekaId)
                            MemberView(navController, viewModel, modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        val db = PMDatabase.getInstance(applicationContext)
        if (db.isOpen) {
            db.openHelper.close()
        }
        super.onDestroy()
    }
}

@Composable
fun MemberView(nav: NavController, viewModel: PMViewModel, modifier: Modifier = Modifier) {
    val member: State<ParliamentMember?> = viewModel.member.collectAsState(initial = null)
    val nextMember: State<ParliamentMember?> = viewModel.nextMember.collectAsState(initial = null)
    val previousMember: State<ParliamentMember?> = viewModel.previousMember.collectAsState(initial = null)
    val image = ImageLoader.getImage(member.value?.pictureUrl)
    val rating = remember { mutableStateOf(member.value?.rating?.toIntOrNull() ?: 0) }
    val notes = remember { mutableStateOf(member.value?.notes ?: "") }

    Column(modifier = Modifier.padding(0.dp, 62.dp, 0.dp, 0.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color.Gray),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        nav.navigate(Screens.Info.name + "/${previousMember.value?.hetekaId}")
                    },
                    modifier = Modifier.background(Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = {
                        nav.navigate(Screens.Info.name + "/${nextMember.value?.hetekaId}")
                    },
                    modifier = Modifier.background(Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            if (image != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Image(
                        bitmap = image,
                        contentDescription = "${member.value?.firstname} ${member.value?.lastname}"
                    )
                }
            }
            Text(
                text = "${member.value?.firstname ?: ""} ${member.value?.lastname ?: ""} (${member.value?.bornYear ?: ""})",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = "Party: ${member.value?.party ?: ""}, Constituency: ${member.value?.constituency ?: ""}",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = "Rating: ${rating.value}",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            RatingBar(rating = rating)
            Text(
                text = "Rate and comment:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)
            )
            TextField(
                value = notes.value,
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp).fillMaxWidth(),
                textStyle = TextStyle.Default.copy(fontSize = 24.sp),
                onValueChange = {
                    notes.value = it
                }
            )
            Button(
                onClick = {
                    member.value?.notes = notes.value
                    viewModel.updateMember(member.value!!)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Save")
            }
        }
    }
}
@Composable
fun RatingBar(rating: MutableState<Int>, maxRating: Int = 5) {
    val goldColor = Color(ContextCompat.getColor(appContext, R.color.gold))
    val defaultColor = Color.Gray

    Row(
        horizontalArrangement = Arrangement.Start, // Align to start (left)
    ) {
        for (i in 1..maxRating) {
            IconButton(
                onClick = { rating.value = i }
            ) {
                Image(
                    painter = painterResource(
                        id = if (i <= rating.value) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(if (i <= rating.value) goldColor else defaultColor) // Apply gold color for selected stars and default color for others
                )
            }
        }
    }
}