package mihirkathpalia.cambio.routine.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RoutinesDatabaseDao {

    @Insert
    fun insert(routine: Routine)

    @Update
    fun update(routine: Routine)

    @Query("DELETE from routines_table WHERE routineId = :key")
    fun delete(key: Long)

    @Query("SELECT * from routines_table WHERE routineId = :key")
    fun get(key: Long): Routine?

    @Query("SELECT * FROM routines_table ORDER BY startTime ASC")
    fun getAllRoutines(): LiveData<List<Routine>>

    @Query("SELECT * FROM routines_table WHERE recurrence LIKE :key ORDER BY startTime ASC")
    fun getDayRoutines(key: String): List<Routine>

    @Query("SELECT * FROM routines_table WHERE recurrence LIKE :day AND endTime > :time ORDER BY startTime ASC")
    fun getRemainingDayRoutines(day: String, time: Double): List<Routine>

}