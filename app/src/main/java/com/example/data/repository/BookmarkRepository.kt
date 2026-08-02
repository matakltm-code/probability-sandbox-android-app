package com.example.data.repository

import com.example.data.local.Bookmark
import com.example.data.local.BookmarkDao
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    suspend fun insertBookmark(bookmark: Bookmark) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark)
    }
    
    fun isBookmarked(url: String): Flow<Boolean> {
        return bookmarkDao.isBookmarked(url)
    }
}
