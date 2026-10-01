package com.example.buildingfexfrontend.residents.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.model.SessionRole
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.residents.domain.DepartmentNumber
import com.example.buildingfexfrontend.residents.domain.model.CascadeDeleteResult
import com.example.buildingfexfrontend.residents.domain.model.LinkedDataPreview
import com.example.buildingfexfrontend.residents.domain.model.NewResident
import com.example.buildingfexfrontend.residents.domain.model.Resident
import com.example.buildingfexfrontend.residents.domain.repository.ResidentsRepository
import com.example.buildingfexfrontend.subscription.application.SubscriptionUseCases
import com.example.buildingfexfrontend.subscription.domain.model.SubscriptionPlan
import com.example.buildingfexfrontend.core.util.Dates

/**
 * Residents use cases: creation rules (department parsing, duplicate code,
 * plan limit) live here, not in the UI nor in the data source.
 */
class ResidentsUseCases(
    private val repository: ResidentsRepository,
    private val session: SessionRepository,
    subscription: SubscriptionUseCases,
) {
    val list = ListResidentsUseCase(repository, session)
    val add = AddResidentUseCase(repository, session, subscription)
    val previewDelete = PreviewResidentDeleteUseCase(repository)
    val removeCascade = RemoveResidentCascadeUseCase(repository)
    val planUsage = PlanUsageUseCase(subscription)
}

class ListResidentsUseCase(
    private val repository: ResidentsRepository,
    @Suppress("unused") private val session: SessionRepository,
) {
    suspend operator fun invoke(): List<Resident> {
        if (session.role() != SessionRole.ADMIN) return emptyList()
        return repository.list()
    }
}

class AddResidentUseCase(
    private val repository: ResidentsRepository,
    private val session: SessionRepository,
    private val subscription: SubscriptionUseCases,
) {
    suspend operator fun invoke(department: String, name: String): Resident {
        val cleanName = name.trim()
        val parsed = DepartmentNumber.parse(department)
            ?: throw AppException("INVALID_DEPARTMENT_NUMBER", "Invalid department")
        if (cleanName.isBlank()) {
            throw AppException("RESIDENT_FIELDS_REQUIRED", "Name required")
        }
        if (session.activeDataOwnerId.isNullOrBlank()) {
            throw AppException("RESIDENT_OWNER_REQUIRED", "Owner required")
        }
        val limit = subscription.residentLimit()
        val current = repository.list().size
        if (current >= limit) {
            throw AppException("RESIDENT_PLAN_LIMIT_REACHED", "Plan limit reached")
        }
        return repository.create(
            NewResident(
                name = cleanName,
                floor = parsed.second,
                code = uniqueInviteCode(parsed.first),
                admissionDate = Dates.todayYmd(),
            ),
        )
    }

    /**
     * The invite code must never be just the department number: with several
     * buildings two residents could share "304". Append a random suffix
     * ("304-k7m2") until it is unique for this admin.
     */
    private suspend fun uniqueInviteCode(department: String): String {
        var code = "$department-${DepartmentNumber.randomSuffix()}"
        var attempts = 1
        while (repository.existsWithCode(code)) {
            if (attempts >= 5) {
                throw AppException("RESIDENT_CODE_ALREADY_EXISTS", "Duplicate invite code")
            }
            code = "$department-${DepartmentNumber.randomSuffix()}"
            attempts++
        }
        return code
    }
}

class PreviewResidentDeleteUseCase(private val repository: ResidentsRepository) {
    suspend operator fun invoke(id: String): LinkedDataPreview = repository.previewLinkedData(id)
}

class RemoveResidentCascadeUseCase(private val repository: ResidentsRepository) {
    suspend operator fun invoke(id: String): CascadeDeleteResult = repository.removeCascade(id)
}

class PlanUsageUseCase(private val subscription: SubscriptionUseCases) {
    suspend operator fun invoke(): Pair<Int, Int> = try {
        val current = subscription.current()
        current.residentsCount to current.residentLimit
    } catch (e: Exception) {
        0 to SubscriptionPlan.FREE.residentLimit
    }
}
