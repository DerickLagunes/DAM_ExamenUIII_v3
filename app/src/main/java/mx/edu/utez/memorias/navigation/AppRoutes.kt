package mx.edu.utez.memorias.navigation

sealed class AppRoutes(val route: String) {
    data object Login : AppRoutes("login")
    data object Register : AppRoutes("register")
    data object Home : AppRoutes("home")
    data object AddEditAvistamiento : AppRoutes("add_edit_avistamiento?avistamientoId={avistamientoId}") {
        fun createRoute(avistamientoId: Int? = null): String =
            if (avistamientoId != null) "add_edit_avistamiento?avistamientoId=$avistamientoId" else "add_edit_avistamiento"
    }
    data object AvistamientoDetail : AppRoutes("avistamiento_detail/{avistamientoId}") {
        fun createRoute(avistamientoId: Int): String = "avistamiento_detail/$avistamientoId"
    }
}
