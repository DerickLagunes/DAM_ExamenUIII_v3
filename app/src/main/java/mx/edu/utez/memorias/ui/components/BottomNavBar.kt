package mx.edu.utez.memorias.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class HomeTab(val label: String) {
    EXPLORAR("Explorar"),
    MIS_AVISTAMIENTOS("Mis Avistamientos")
}

@Composable
fun BottomNavBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == HomeTab.EXPLORAR,
            onClick = { onTabSelected(HomeTab.EXPLORAR) },
            icon = { Icon(Icons.Default.Explore, contentDescription = "Explorar") },
            label = { Text(HomeTab.EXPLORAR.label) }
        )
        NavigationBarItem(
            selected = selectedTab == HomeTab.MIS_AVISTAMIENTOS,
            onClick = { onTabSelected(HomeTab.MIS_AVISTAMIENTOS) },
            icon = { Icon(Icons.Default.Visibility, contentDescription = "Mis Avistamientos") },
            label = { Text(HomeTab.MIS_AVISTAMIENTOS.label) }
        )
    }
}
