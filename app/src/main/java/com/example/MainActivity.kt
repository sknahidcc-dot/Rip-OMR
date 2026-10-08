package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ExamEditScreen
import com.example.ui.screens.ExamListScreen
import com.example.ui.screens.ExamQrShareScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrintableSheetScreen
import com.example.ui.screens.ResultDetailScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentListScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.RipOmrTheme
import com.example.ui.viewmodel.OmrViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RipOmrTheme {
                RipOmrApp()
            }
        }
    }
}

@Composable
fun RipOmrApp(
    viewModel: OmrViewModel = viewModel()
) {
    val currentScreen by viewModel.screenState.collectAsStateWithLifecycle()

    // Handle back button according to current screen
    when (currentScreen) {
        Screen.Splash, Screen.Home -> {
            // Default system back behavior (exit app)
        }
        is Screen.ExamEdit, is Screen.ExamQrShare -> {
            BackHandler { viewModel.navigateTo(Screen.Exams) }
        }
        is Screen.StudentDetail -> {
            BackHandler { viewModel.navigateTo(Screen.Students) }
        }
        Screen.Verification -> {
            BackHandler { viewModel.navigateTo(Screen.Scanner) }
        }
        else -> {
            BackHandler { viewModel.navigateTo(Screen.Home) }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AmoledBlack
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                Screen.Splash -> {
                    SplashScreen(
                        onAnimationFinish = {
                            viewModel.navigateTo(Screen.Home)
                        }
                    )
                }
                Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                Screen.Exams -> {
                    ExamListScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                is Screen.ExamEdit -> {
                    ExamEditScreen(
                        examId = screen.examId,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Exams) }
                    )
                }
                is Screen.ExamQrShare -> {
                    ExamQrShareScreen(
                        exam = screen.exam,
                        onBack = { viewModel.navigateTo(Screen.Exams) }
                    )
                }
                Screen.Students -> {
                    StudentListScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                is Screen.StudentDetail -> {
                    StudentDetailScreen(
                        roll = screen.roll,
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateTo(Screen.Students) }
                    )
                }
                Screen.Analytics -> {
                    AnalyticsScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                Screen.Scanner -> {
                    ScannerScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                Screen.Verification -> {
                    VerificationScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateTo(Screen.Scanner) }
                    )
                }
                is Screen.ResultDetail -> {
                    ResultDetailScreen(
                        resultId = screen.resultId,
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                Screen.PrintableSheet -> {
                    PrintableSheetScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                Screen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }
            }
        }
    }
}
