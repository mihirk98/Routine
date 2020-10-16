package mihirkathpalia.cambio.routine.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Routine::class], version = 5, exportSchema = false)
abstract class RoutinesDatabase : RoomDatabase() {

    abstract val routinesDatabaseDao: RoutinesDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: RoutinesDatabase? = null

        fun getInstance(context: Context): RoutinesDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RoutinesDatabase::class.java,
                        "routines_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}