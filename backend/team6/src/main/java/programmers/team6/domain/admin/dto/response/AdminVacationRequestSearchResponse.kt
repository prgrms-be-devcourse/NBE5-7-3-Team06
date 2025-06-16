package programmers.team6.domain.admin.dto.response

import org.springframework.data.domain.Page
import programmers.team6.domain.admin.dto.CodeInfo
import programmers.team6.domain.admin.enums.Quarter

data class AdminVacationRequestSearchResponse(
    val vacationRequestSearchResponses: Page<VacationRequestSearchResponse>,
    val dropdownContents: DropdownContents?
) {
    constructor(
        vacationRequestSearchResponses: Page<VacationRequestSearchResponse>,
        positionCodes: List<CodeInfo>, vacationTypeCodes: List<CodeInfo>
    ) : this(
        vacationRequestSearchResponses,
        DropdownContents(positionCodes, vacationTypeCodes, Quarter.entries)
    )

    data class DropdownContents(
        val positionCodes: List<CodeInfo>,
        val vacationTypeCodes: List<CodeInfo>,
        val quarters: List<Quarter>
    )
}
