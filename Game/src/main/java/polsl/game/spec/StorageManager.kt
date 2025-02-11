package polsl.game.spec

import android.content.Context
import android.content.SharedPreferences

class StorageManager(context: Context) {

    private val nameKey = "PlayerData"
    private val prefs: SharedPreferences = context.getSharedPreferences(nameKey, Context.MODE_PRIVATE)

    fun saveData(key: String = nameKey, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun loadData(key: String = nameKey): String? {
        return prefs.getString(key, null)
    }


}