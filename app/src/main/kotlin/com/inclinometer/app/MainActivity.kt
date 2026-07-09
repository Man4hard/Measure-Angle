package com.inclinometer.app

  import android.os.Bundle
  import android.view.WindowManager
  import androidx.appcompat.app.AppCompatActivity
  import com.inclinometer.app.databinding.ActivityMainBinding
  import com.inclinometer.app.util.ThemeManager
  import dagger.hilt.android.AndroidEntryPoint
  import javax.inject.Inject

  @AndroidEntryPoint
  class MainActivity : AppCompatActivity() {

      private lateinit var binding: ActivityMainBinding

      @Inject
      lateinit var themeManager: ThemeManager

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          // Keep the screen on while this app is open
          window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
          themeManager.applyTheme()
          binding = ActivityMainBinding.inflate(layoutInflater)
          setContentView(binding.root)
      }
  }