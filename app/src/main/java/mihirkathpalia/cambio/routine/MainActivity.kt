package mihirkathpalia.cambio.routine

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val sharedPrefFile = "routineSharedPrefFile"
    private val themeSharedPref = "themeSharedPref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        if (sharedPreferences.getInt(themeSharedPref, 0)  == 0) {
            setTheme(R.style.LightTheme)
        } else {
            setTheme(R.style.DarkTheme)
        }
        setContentView(R.layout.activity_main)
    }
}