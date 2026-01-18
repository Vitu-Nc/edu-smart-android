package com.example.edu_smart.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// 📘 Search Result Response
data class BookSearchResponse(
    val docs: List<Book>
)

// 📘 Book item from search (fields selected via `fields=` param)
data class Book(
    val key: String,                    // e.g. "/works/OL12345W"
    val title: String?,
    val author_name: List<String>?,
    val cover_i: Int?,

    // ✅ availability-related fields returned by search when requested via `fields`
    val edition_key: List<String>?,     // for Borrow page URL
    val ebook_access: String?,          // "public" or "borrowable"
    val ia: List<String>?               // Internet Archive identifiers for the reader
)

// (Optional) If you still use this elsewhere, keep it; search.json doesn't return this by default.
data class Availability(
    val is_readable: Boolean
)

// 📗 Book Details Response
data class BookDetails(
    val title: String,
    val description: Any?,  // String or Map
    val subjects: List<String>?,
    val covers: List<Int>?
)

// 📙 Book Editions Response (for extra metadata if you need)
data class EditionsResponse(
    val entries: List<EditionEntry>
)

data class EditionEntry(
    val title: String?,
    val url: String?,              // fallback
    val preview_url: String?,      // sometimes available
    val identifiers: Map<String, List<String>>?
)

// 🌐 Retrofit Interface
interface OpenLibraryApi {

    /**
     * Ebooks-only search with fulltext results and availability fields.
     * This dramatically increases the chances the user can read or borrow the book online.
     */
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("mode") mode: String = "ebooks",                 // ✅ only ebooks
        @Query("has_fulltext") hasFulltext: Boolean = true,     // ✅ ensure there is fulltext
        @Query("limit") limit: Int = 40,
        @Query("fields") fields: String =                       // ✅ ask only what we need
            "key,title,author_name,cover_i,edition_key,ebook_access,ia"
    ): BookSearchResponse

    @GET("works/{workId}.json")
    suspend fun getBookDetails(@Path("workId") workId: String): BookDetails

    @GET("works/{workId}/editions.json")
    suspend fun getBookEditions(@Path("workId") workId: String): EditionsResponse
}

/**
 * Helper: compute the best URL to open for a given book.
 * - If public domain with Internet Archive ID -> open Archive reader directly.
 * - If borrowable and we have an edition key -> open OL Borrow page.
 * - Else fall back to the Work page.
 */
fun Book.bestReaderUrl(): String {
    val access = (ebook_access ?: "").lowercase()
    return when {
        access == "public" && !ia.isNullOrEmpty() ->
            "https://archive.org/details/${ia.first()}?view=theater"

        access == "borrowable" && !edition_key.isNullOrEmpty() ->
            "https://openlibrary.org/books/${edition_key.first()}"

        else ->
            "https://openlibrary.org$key"
    }
}

