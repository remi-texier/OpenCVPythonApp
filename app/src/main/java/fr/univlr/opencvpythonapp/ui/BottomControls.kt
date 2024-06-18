package fr.univlr.opencvpythonapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Composable pour les éléments de contrôle en bas de l'écran
@Composable
fun BottomControls(sliderPosition: MutableState<Float>, onButtonClick: (String) -> Unit, onSliderChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { onButtonClick("Button 1") }) {
                Text("+")
            }
            Button(onClick = { onButtonClick("Button 2") }) {
                Text("-")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderPosition.value,
            onValueChange = {
                sliderPosition.value = it
                onSliderChange(it)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
