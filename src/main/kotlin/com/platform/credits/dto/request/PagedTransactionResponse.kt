package com.platform.credits.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page

data class PagedTransactionResponse(
    val transactions: List<TransactionResponse>,

    @JsonProperty("total_elements")
    val totalElements: Long,

    @JsonProperty("total_pages")
    val totalPages: Int,

    @JsonProperty("current_page")
    val currentPage: Int,

    @JsonProperty("page_size")
    val pageSize: Int
) {
    companion object {
        fun fromPage(page: Page<TransactionResponse>): PagedTransactionResponse {
            return PagedTransactionResponse(
                transactions = page.content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number,
                pageSize = page.size
            )
        }
    }
}
