package com.example.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun seed(
        studyMaterialDao: StudyMaterialDao,
        currentAffairsDao: CurrentAffairsDao
    ) = withContext(Dispatchers.IO) {
        
        val sampleMaterials = listOf(
            StudyMaterialEntity(
                id = "mat_1",
                title = "Indian Polity Basics",
                content = "Fundamental rights, duties, and structure of Indian governance.",
                category = "Polity"
            ),
            StudyMaterialEntity(
                id = "mat_2",
                title = "AP History - Satavahanas",
                content = "Detailed history of the Satavahana dynasty in Andhra region.",
                category = "History"
            )
        )

        val sampleAffairs = listOf(
            CurrentAffairsEntity(
                id = "ca_1",
                title = "State Budget 2026",
                description = "Key highlights of the latest state budget presentation.",
                date = "2026-09-20"
            ),
            CurrentAffairsEntity(
                id = "ca_2",
                title = "New Infrastructure Project",
                description = "Upcoming highways and smart city initiatives.",
                date = "2026-09-18"
            )
        )

        studyMaterialDao.insertMaterials(sampleMaterials)
        currentAffairsDao.insertCurrentAffairs(sampleAffairs)
    }
}
