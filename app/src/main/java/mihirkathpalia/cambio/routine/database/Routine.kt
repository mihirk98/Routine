package mihirkathpalia.cambio.routine.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines_table")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    var routineId: Long = 0L,

    @ColumnInfo(name = "routineTitle")
    var routineTitle: String = "",

    @ColumnInfo(name = "startTime")
    var startTime: Double = -1.0,

    @ColumnInfo(name = "endTime")
    var endTime: Double = -1.0,

    @ColumnInfo(name = "recurrence")
    var recurrence: String = "0"
)

// Recurrence
// 1 -> Mo
// 2 -> Tu
// 3 -> We
// 4 -> Th
// 5 -> Fr
// 6 -> Sa
// 7 -> Su
// 12 -> Mo, Tu
// 256 -> Tu, Fr, Sa
