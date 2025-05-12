package com.example.osm.ui.routing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.osm.model.Step
import com.example.osm.ui.mapUtils.convertSecondToText
import com.example.osm.utils.RoutingHelper
import com.example.osm.utils.RoutingHelper.formatDistance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoutingFragment: Fragment() {

    private val viewModel: RoutingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MainContent()
            }
        }
    }

    @Composable
    private fun MainContent() {

        val currentState = viewModel.updateRoutingState.observeAsState()
        val steps = arrayListOf<Step>()

        viewModel.route?.let {
            it.features.getOrNull(0)?.properties?.segments?.getOrNull(0)?.let { summary ->
                steps.addAll(summary.steps)
                val eta = calculateETA(summary.duration.toInt())
                currentState.value?.let { step ->
                    val distanceLeft = calculateDistanceLeft(summary.distance, step.distance)
                    val durationLeft = calculateDurationLeft(summary.duration, step.duration)

                    NavigationInfoScreen(
                        durationLeft, distanceLeft, eta, steps
                    ) {

                    }
                }
            }
        }

    }

    @Composable
    fun NavigationInfoScreen(
        time: String,
        distance: String,
        eta: String,
        steps: List<Step>,
        onClose: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = time,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(48.dp)) // Equal space to icon button
                }


                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = distance,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = ".",
                        fontSize = 21.sp
                    )
                    Text(
                        text = eta,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
                NavigationSteps(steps)
            }
        }
    }

    @Composable
    private fun NavigationSteps(steps: List<Step>) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(steps.size) { index ->
                NavigationStepCard(
                    steps[index]
                )
            }
        }
    }

    @Composable
    private fun NavigationStepCard(step: Step) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = step.instruction,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )

                Image(
                    painter = painterResource(
                        RoutingHelper.getManeuverImage(step.type.toInt())
                    ),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .size(40.dp).background(Color.Gray, shape = RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatDistance(step.distance),
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    @Preview
    @Composable
    private fun Preview() {
        NavigationInfoScreen(
            "1h59m",
            "1.2km",
            "11:52",
            arrayListOf(
                Step(92.0, 1200.0, 1, "turn left onto", "street name", arrayListOf())
            )
        ) { }
    }

    private fun calculateDistanceLeft(distance: Double, stepDistance: Double): String {
        return formatDistance(distance - stepDistance)
    }

    private fun calculateDurationLeft(duration: Double, stepDuration: Double): String {
        return convertSecondToText((duration - stepDuration).toInt())
    }

    private fun calculateETA(durationInSeconds: Int): String {
        val currentTimeMillis = System.currentTimeMillis()
        val etaMillis = currentTimeMillis + durationInSeconds * 1000

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(etaMillis))
    }
}