package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.SiswaViewModel
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.TeacherDashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: SiswaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "landing",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("landing") {
                            LandingScreen(
                                viewModel = viewModel,
                                onNavigateToTeacher = {
                                    navController.navigate("teacher_dashboard")
                                },
                                onNavigateToStudent = {
                                    navController.navigate("student_dashboard")
                                }
                            )
                        }

                        composable("teacher_dashboard") {
                            TeacherDashboardScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = {
                                    navController.navigate("student_detail")
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("student_dashboard") {
                            StudentDashboardScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("student_detail") {
                            StudentDetailScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
