package ai.autocraft3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("autocraft_core")
        setContent { AutoCraftWorkspace(nativeStatus()) }
    }

    private external fun nativeStatus(): String
}

@Composable
private fun AutoCraftWorkspace(engineStatus: String) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF10151D)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("AUTOCRAFT 3D", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text("0.1.0 • FOUNDATION", color = Color(0xFF9BB9D8))
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF202A36)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("3D VIEWPORT", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Viewport renderer: pending integration", color = Color(0xFFB7C6D8))
                    }
                }
                Text(engineStatus, color = Color(0xFF9BB9D8))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}) { Text("Model") }
                    Button(onClick = {}) { Text("Sculpt") }
                    Button(onClick = {}) { Text("Materials") }
                    Button(onClick = {}) { Text("Animate") }
                }
            }
        }
    }
}
