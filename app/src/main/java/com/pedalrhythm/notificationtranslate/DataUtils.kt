package com.pedalrhythm.notificationtranslate

import androidx.room.*

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "sender") val sender: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "time") val time: Long
) {
    constructor(sender: String, content: String, time: Long) : this(0, sender, content, time)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification")
    fun getAll(): List<NotificationEntity>

    @Insert
    fun insert(notification: NotificationEntity)

    @Query("DELETE FROM notification")
    fun deleteAll()
}

@Database(entities = [NotificationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}