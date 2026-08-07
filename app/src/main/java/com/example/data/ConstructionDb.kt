package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "artisans")
data class Artisan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val tradeSector: String, // አናፂ (Carpenter), ግንበኛ (Mason), ፌሮዮ (Rebar), ኤሌክትሪሺያን (Electrician), ቧንቧ ሰራተኛ (Plumber)
    val cocNumber: String,
    val certificatePhotoPath: String, // String representation or actual path of the scanned certificate
    val experienceYears: Int = 1,
    val registrationDate: Long = System.currentTimeMillis(),
    val hasPermit: Boolean = true,
    val isAccredited: Boolean = true,
    val accreditationNote: String = "መደበኛ የሙያ ብቃት ዕውቅና",
    val lastAccreditationUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "professionals")
data class Professional(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val role: String, // አማካሪ (Consultant), ተቋራጭ (Contractor), ተቆጣጣሪ (Inspector)
    val grade: String, // e.g., Grade 1, Grade 5
    val licenseNumber: String,
    val registrationDate: Long = System.currentTimeMillis(),
    val isAccredited: Boolean = true,
    val accreditationNote: String = "የተቋራጭ/አማካሪ የከተማ ፈቃድ እውቅና",
    val lastAccreditationUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String,
    val buildingCategory: String, // ምድብ ሀ, ለ, ሐ, መ, የጭቃ ቤት, ፎቅ የሌላቸው, ጊዜያዊ
    val constructionType: String, // Residential, Commercial, Industrial, Public, Temporary
    val materials: String, // Comma separated list of materials used
    val consultantId: Int? = null, // Linked Professional ID
    val contractorId: Int? = null, // Linked Professional ID
    val inspectorId: Int? = null, // Linked Professional ID
    val status: String = "በዝግጅት ላይ", // በዝግጅት ላይ (Preparation), በግንባታ ላይ (Under Construction), የተጠናቀቀ (Completed)
    val timestamp: Long = System.currentTimeMillis(),
    
    // Engineering Software Integrations Data Fields
    val cadCoordinates: String? = null, // e.g. "X:45.2, Y:38.1; X:60.5, Y:38.1..."
    val bimEstimatedCost: Double? = null, // Estimated cost from Revit BIM export
    val scheduleProgressPercent: Int = 0, // Primavera/MS Project schedule percentage
    val gisLatitude: Double? = null, // GIS Latitude
    val gisLongitude: Double? = null, // GIS Longitude
    val etabsSafetyScore: Int? = null // ETABS Safety Score (0-100)
)

@Entity(tableName = "warnings")
data class Warning(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipientPhone: String,
    val recipientName: String,
    val recipientRole: String, // e.g. Contractor, Electrician
    val message: String,
    val severity: String, // ዝቅተኛ (Minor), ከፍተኛ (Major), ወሳኝ (Critical)
    val isSent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_progress")
data class DailyProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val date: Long = System.currentTimeMillis(),
    val taskDescription: String, // e.g., "Pouring C25 concrete for column column"
    val workQuantity: String, // e.g., "30 m³"
    val standardRefId: String, // Links to standard/code id e.g. "ebcs-2"
    val checkpointPassed: String, // e.g., "የኮንክሪት ጥንካሬ ቢያንስ C25 መሆኑ"
    val measuredValue: String, // e.g., "C25, Slump: 80mm"
    val status: String, // "ተገዢ (COMPLIANT)", "ያልተገዢ (NON-COMPLIANT)", "ማስጠንቀቂያ ያለው (WARNING)"
    val notes: String = "",
    val inspectorName: String = "በላይነህ የሻለም (Civil Engineer)",
    val isSynced: Boolean = true,
    val audioPath: String? = null
)

@Entity(tableName = "risk_assessments")
data class RiskAssessment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val hazardTitle: String, // e.g. "Unsecured deep excavation edge"
    val description: String, // e.g. "Potential falling hazard"
    val riskLevel: String, // "LOW" / "MEDIUM" / "HIGH" / "CRITICAL"
    val mitigationPlan: String, // e.g. "Install handrails"
    val ebcsStandardRefId: String, // Links to standard/code id e.g. "ebcs-1"
    val safetyClause: String, // e.g. "Clause 4.2"
    val status: String = "OPEN", // "OPEN", "RESOLVED"
    val dateLogged: Long = System.currentTimeMillis(),
    val loggedBy: String = "በላይነህ የሻለም (Civil Engineer)",
    val isSynced: Boolean = true
)

@Dao
interface ConstructionDao {
    // Artisans
    @Query("SELECT * FROM artisans ORDER BY name ASC")
    fun getAllArtisans(): Flow<List<Artisan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtisan(artisan: Artisan)

    @Delete
    suspend fun deleteArtisan(artisan: Artisan)

    // Professionals
    @Query("SELECT * FROM professionals ORDER BY name ASC")
    fun getAllProfessionals(): Flow<List<Professional>>

    @Query("SELECT * FROM professionals WHERE role = :role ORDER BY name ASC")
    fun getProfessionalsByRole(role: String): Flow<List<Professional>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessional(professional: Professional)

    @Delete
    suspend fun deleteProfessional(professional: Professional)

    // Projects
    @Query("SELECT * FROM projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    // Warnings
    @Query("SELECT * FROM warnings ORDER BY timestamp DESC")
    fun getAllWarnings(): Flow<List<Warning>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarning(warning: Warning)

    @Delete
    suspend fun deleteWarning(warning: Warning)

    // Daily Progress
    @Query("SELECT * FROM daily_progress ORDER BY date DESC")
    fun getAllDailyProgress(): Flow<List<DailyProgress>>

    @Query("SELECT * FROM daily_progress WHERE projectId = :projectId ORDER BY date DESC")
    fun getDailyProgressForProject(projectId: Int): Flow<List<DailyProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyProgress(progress: DailyProgress)

    @Delete
    suspend fun deleteDailyProgress(progress: DailyProgress)

    // Risk Assessments
    @Query("SELECT * FROM risk_assessments ORDER BY dateLogged DESC")
    fun getAllRiskAssessments(): Flow<List<RiskAssessment>>

    @Query("SELECT * FROM risk_assessments WHERE projectId = :projectId ORDER BY dateLogged DESC")
    fun getRiskAssessmentsForProject(projectId: Int): Flow<List<RiskAssessment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskAssessment(assessment: RiskAssessment)

    @Delete
    suspend fun deleteRiskAssessment(assessment: RiskAssessment)
}

@Database(entities = [Artisan::class, Professional::class, Project::class, Warning::class, DailyProgress::class, RiskAssessment::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun constructionDao(): ConstructionDao
}

class ConstructionRepository(private val dao: ConstructionDao) {
    val allArtisans: Flow<List<Artisan>> = dao.getAllArtisans()
    val allProfessionals: Flow<List<Professional>> = dao.getAllProfessionals()
    val allProjects: Flow<List<Project>> = dao.getAllProjects()
    val allWarnings: Flow<List<Warning>> = dao.getAllWarnings()
    val allDailyProgress: Flow<List<DailyProgress>> = dao.getAllDailyProgress()
    val allRiskAssessments: Flow<List<RiskAssessment>> = dao.getAllRiskAssessments()

    fun getDailyProgressForProject(projectId: Int) = dao.getDailyProgressForProject(projectId)
    fun getProfessionalsByRole(role: String) = dao.getProfessionalsByRole(role)
    fun getRiskAssessmentsForProject(projectId: Int) = dao.getRiskAssessmentsForProject(projectId)

    suspend fun insertArtisan(artisan: Artisan) = dao.insertArtisan(artisan)
    suspend fun deleteArtisan(artisan: Artisan) = dao.deleteArtisan(artisan)

    suspend fun insertProfessional(professional: Professional) = dao.insertProfessional(professional)
    suspend fun deleteProfessional(professional: Professional) = dao.deleteProfessional(professional)

    suspend fun insertProject(project: Project) = dao.insertProject(project)
    suspend fun deleteProject(project: Project) = dao.deleteProject(project)

    suspend fun insertWarning(warning: Warning) = dao.insertWarning(warning)
    suspend fun deleteWarning(warning: Warning) = dao.deleteWarning(warning)

    suspend fun insertDailyProgress(progress: DailyProgress) = dao.insertDailyProgress(progress)
    suspend fun deleteDailyProgress(progress: DailyProgress) = dao.deleteDailyProgress(progress)

    suspend fun insertRiskAssessment(assessment: RiskAssessment) = dao.insertRiskAssessment(assessment)
    suspend fun deleteRiskAssessment(assessment: RiskAssessment) = dao.deleteRiskAssessment(assessment)
}

object DatabaseProvider {
    private var database: AppDatabase? = null

    fun getDatabase(context: android.content.Context): AppDatabase {
        return database ?: synchronized(this) {
            val db = androidx.room.Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "construction_db"
            )
            .fallbackToDestructiveMigration()
            .build()
            database = db
            db
        }
    }

    private var repository: ConstructionRepository? = null

    fun getRepository(context: android.content.Context): ConstructionRepository {
        return repository ?: synchronized(this) {
            val repo = ConstructionRepository(getDatabase(context).constructionDao())
            repository = repo
            repo
        }
    }
}
