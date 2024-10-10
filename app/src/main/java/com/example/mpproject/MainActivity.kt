package com.example.mpproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
import com.example.mpproject.db.PMDatabase
import com.example.mpproject.db.ParliamentMember
import com.example.mpproject.ui.theme.MPProjectTheme

enum class Screens {
    Info
}

val COLORS = mapOf(
    "primary" to Color(ContextCompat.getColor(appContext, R.color.primary))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            MPProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = navController, startDestination = Screens.Info.name + "/") {
                        composable(route = Screens.Info.name + "/{hetekaId}?") {
                            val hetekaId: Int? = it.arguments?.getString("hetekaId")?.toIntOrNull()
                            MemberView(navController, hetekaId = hetekaId, modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        val db = PMDatabase.getInstance()
        if (db.isOpen) {
            db.openHelper.close()
        }
        super.onDestroy()
    }
}

@Composable
fun MemberView(nav: NavController, modifier: Modifier = Modifier, hetekaId: Int? = null) {
    val viewModel = PMViewModel()
    val members = viewModel.members.collectAsState(initial = listOf()).value
    var idx: Int = members.indexOfFirst { it.hetekaId == hetekaId }
    if (idx == -1) {
        idx = (0..members.size).random()
    }
    val member: ParliamentMember? = members.getOrNull(idx)
    viewModel.notes = member?.notes ?: ""

    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(member?.pictureUrl) {
        image = ImageLoader.getImage(member?.pictureUrl)
    }

    Column(modifier = Modifier.padding(0.dp, 62.dp, 0.dp, 0.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(4.dp, COLORS["primary"]!!),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val prevId: Int? = if (idx == 0) members.first().hetekaId else members.getOrNull(idx - 1)?.hetekaId
                        nav.navigate(Screens.Info.name + "/$prevId")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = COLORS["primary"]!!)
                ) {
                    Text(
                        text = "<-",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        val nextId: Int? = members.getOrNull((idx + 1).mod(members.size))?.hetekaId
                        nav.navigate(Screens.Info.name + "/$nextId")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = COLORS["primary"]!!)
                ) {
                    Text(
                        text = "->",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (image != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Image(
                        bitmap = image!!,
                        contentDescription = "${member?.firstname} ${member?.lastname}"
                    )
                }
            }
            Text(
                text = "${member?.firstname ?: ""} ${member?.lastname ?: ""} (${member?.bornYear ?: ""})",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = "Party: ${member?.party ?: ""}, Constituency: ${member?.constituency ?: ""}",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = "Rating: ${member?.rating ?: ""}",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = "Rate and comment:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                for (i in 1..5) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = COLORS["primary"]!!),
                        onClick = {
                            member?.rating = i.toString()
                            viewModel.updateMember(member!!)
                        }
                    ) {
                        Text(
                            text = i.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            TextField(
                value = viewModel.notes,
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp).fillMaxWidth(),
                textStyle = TextStyle.Default.copy(fontSize = 24.sp),
                onValueChange = {
                    viewModel.notes = it
                    member?.notes = it
                    viewModel.updateMember(member!!)
                }
            )
        }
    }
}