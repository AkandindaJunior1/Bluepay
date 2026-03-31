package com.example.bluepay.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Entity(tableName = "images")
data class ImageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false
)

@Dao
interface ImageDao {
    @Query("SELECT * FROM images WHERE is_synced = 1")
    fun getSyncedImages(): List<ImageRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: ImageRecord)

    @Query("DELETE FROM images WHERE is_synced = 1")
    fun deleteSyncedImages()
}

@Database(entities = [ImageRecord::class], version = 1, exportSchema = false)
abstract class GorillaDatabase : RoomDatabase() {

    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: GorillaDatabase? = null

        fun getDatabase(context: Context): GorillaDatabase {
            return INSTANCE ?: synchronized(this) {
                // Initialize SQLCipher native libraries
                SQLiteDatabase.loadLibs(context)

                val passphrase = SQLiteDatabase.getBytes("your-secure-passphrase".toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GorillaDatabase::class.java,
                    "gorilla_secure_db"
                )
                    .openHelperFactory(factory as SupportSQLiteOpenHelper.Factory)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}