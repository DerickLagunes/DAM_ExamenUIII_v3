package mx.edu.utez.memorias.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utez.memorias.AppContainer
import mx.edu.utez.memorias.ui.screens.AddEditAvistamientoScreen
import mx.edu.utez.memorias.ui.screens.AvistamientoDetailScreen
import mx.edu.utez.memorias.ui.screens.HomeScreen
import mx.edu.utez.memorias.ui.screens.LoginScreen
import mx.edu.utez.memorias.ui.screens.RegisterScreen
import mx.edu.utez.memorias.ui.viewmodels.AddEditAvistamientoViewModel
import mx.edu.utez.memorias.ui.viewmodels.AuthViewModel
import mx.edu.utez.memorias.ui.viewmodels.AvistamientoDetailViewModel
import mx.edu.utez.memorias.ui.viewmodels.HomeViewModel

@Composable
fun AppNavGraph(appContainer: AppContainer) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(factory = simpleFactory {
        AuthViewModel(appContainer.authRepository)
    })
    val session by authViewModel.sessionState.collectAsStateWithLifecycle()

    val currentSession = session ?: return
    val startDestination = if (currentSession.isLoggedIn) AppRoutes.Home.route else AppRoutes.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoutes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(AppRoutes.Register.route) },
                onLoginSuccess = {
                    navController.navigate(AppRoutes.Home.route) {
                        popUpTo(AppRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(AppRoutes.Home.route) {
                        popUpTo(AppRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = simpleFactory {
                HomeViewModel(
                    appContainer.avistamientoRepository,
                    appContainer.authRepository,
                    appContainer.userPreferencesRepository
                )
            })
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToAddAvistamiento = {
                    navController.navigate(AppRoutes.AddEditAvistamiento.createRoute())
                },
                onNavigateToDetail = { avistamientoId ->
                    navController.navigate(AppRoutes.AvistamientoDetail.createRoute(avistamientoId))
                },
                onLoggedOut = {
                    navController.navigate(AppRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppRoutes.AddEditAvistamiento.route,
            arguments = listOf(
                navArgument("avistamientoId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getInt("avistamientoId") ?: -1
            val addEditViewModel: AddEditAvistamientoViewModel = viewModel(factory = simpleFactory {
                AddEditAvistamientoViewModel(
                    appContainer.avistamientoRepository,
                    appContainer.userPreferencesRepository
                )
            })
            AddEditAvistamientoScreen(
                avistamientoId = rawId.takeIf { it > 0 },
                viewModel = addEditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.AvistamientoDetail.route,
            arguments = listOf(navArgument("avistamientoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val avistamientoId = backStackEntry.arguments?.getInt("avistamientoId") ?: return@composable
            val detailViewModel: AvistamientoDetailViewModel = viewModel(factory = simpleFactory {
                AvistamientoDetailViewModel(
                    appContainer.avistamientoRepository,
                    appContainer.userPreferencesRepository
                )
            })
            AvistamientoDetailScreen(
                avistamientoId = avistamientoId,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(AppRoutes.AddEditAvistamiento.createRoute(id))
                }
            )
        }
    }
}

private inline fun <reified T : ViewModel> simpleFactory(
    crossinline creator: () -> T
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}
