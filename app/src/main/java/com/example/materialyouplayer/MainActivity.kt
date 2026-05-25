package com.example.materialyouplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.materialyouplayer.ui.screens.home.MainNavigationContainer
import com.example.materialyouplayer.ui.screens.home.HomeScreen
import com.example.materialyouplayer.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // تسجيل عقد طلب الصلاحيات بشكل آمن ومتوافق مع إصدارات أندرويد الحديثة
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // إذا وافق المستخدم، نقوم بتحديث المكتبة فورا وقراءة الملفات
            viewModel.refreshMediaLibrary()
        } else {
            Toast.makeText(this, "Permission denied. Cannot scan music.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // التحقق من الصلاحيات وتفعيلها بناءً على إصدار الأندرويد (تجنباً للمشاكل في Android 13+)
        checkAndRequestPermissions()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // تشغيل حاوية الملاحة الشاملة التي تجمع البار السفلي والمشغلات
                    MainNavigationContainer(viewModel = viewModel) {
                        // شاشة الـ Home الرئيسية التي تعرض بداخل الحاوية
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToArtist = { artistId -> /* التنقل لصفحة الفنان مستقبلاً */ },
                            onNavigateToAlbum = { albumId -> /* التنقل لصفحة الألبوم مستقبلاً */ }
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // الصلاحية ممنوحة بالفعل، السكنر يعمل تلقائياً من الـ ViewModel Init
            }
            else -> {
                // طلب الصلاحية من المستخدم
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}
