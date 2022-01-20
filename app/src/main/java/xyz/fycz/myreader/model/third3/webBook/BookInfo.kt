package xyz.fycz.myreader.model.third3.webBook


import android.util.Log
import xyz.fycz.myreader.model.third3.analyzeRule.AnalyzeRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import xyz.fycz.myreader.R
import xyz.fycz.myreader.application.App
import xyz.fycz.myreader.greendao.entity.Book
import xyz.fycz.myreader.greendao.entity.rule.BookSource
import xyz.fycz.myreader.model.third3.NoStackTraceException
import xyz.fycz.myreader.util.utils.HtmlFormatter
import xyz.fycz.myreader.util.utils.NetworkUtils

/**
 * 获取详情
 */
object BookInfo {

    @Throws(Exception::class)
    fun analyzeBookInfo(
        scope: CoroutineScope,
        bookSource: BookSource,
        book: Book,
        redirectUrl: String,
        baseUrl: String,
        body: String?,
        canReName: Boolean,
    ) {
        body ?: throw NoStackTraceException(
            App.getmContext().getString(R.string.error_get_web_content, baseUrl)
        )
        Log.d(bookSource.sourceUrl, "≡获取成功:${baseUrl}")
        Log.d(bookSource.sourceUrl, body)
        val analyzeRule = AnalyzeRule(book, bookSource)
        analyzeRule.setContent(body).setBaseUrl(baseUrl)
        analyzeRule.setRedirectUrl(redirectUrl)
        analyzeBookInfo(scope, book, body, analyzeRule, bookSource, baseUrl, redirectUrl, canReName)
    }

    fun analyzeBookInfo(
        scope: CoroutineScope,
        book: Book,
        body: String,
        analyzeRule: AnalyzeRule,
        bookSource: BookSource,
        baseUrl: String,
        redirectUrl: String,
        canReName: Boolean,
    ) {
        val infoRule = bookSource.infoRule
        infoRule.init?.let {
            if (it.isNotBlank()) {
                scope.ensureActive()
                Log.d(bookSource.sourceUrl, "≡执行详情页初始化规则")
                analyzeRule.setContent(analyzeRule.getElement(it))
            }
        }
        //val mCanReName = canReName && !infoRule.canReName.isNullOrBlank()
        val mCanReName = false
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取书名")
        BookList.formatBookName(analyzeRule.getString(infoRule.name)).let {
            if (it.isNotEmpty() && (mCanReName || book.name.isEmpty())) {
                book.name = it
            }
            Log.d(bookSource.sourceUrl, "└${it}")
        }
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取作者")
        BookList.formatBookAuthor(analyzeRule.getString(infoRule.author)).let {
            if (it.isNotEmpty() && (mCanReName || book.author.isEmpty())) {
                book.author = it
            }
            Log.d(bookSource.sourceUrl, "└${it}")
        }
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取分类")
        try {
            analyzeRule.getStringList(infoRule.type)
                ?.joinToString(",")
                ?.let {
                    if (it.isNotEmpty()) book.type = it
                }
            Log.d(bookSource.sourceUrl, "└${book.type}")
        } catch (e: Exception) {
            Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
        }
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取字数")
        try {
            /*wordCountFormat(analyzeRule.getString(infoRule.wordCount)).let {
                if (it.isNotEmpty()) book.wordCount = it
            }*/
            analyzeRule.getString(infoRule.wordCount).let {
                if (it.isNotEmpty()) book.wordCount = it
            }
            Log.d(bookSource.sourceUrl, "└${book.wordCount}")
        } catch (e: Exception) {
            Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
        }
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取最新章节")
        try {
            analyzeRule.getString(infoRule.lastChapter).let {
                if (it.isNotEmpty()) book.newestChapterTitle = it
            }
            Log.d(bookSource.sourceUrl, "└${book.newestChapterTitle}")
        } catch (e: Exception) {
            Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
        }
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取简介")
        try {
            analyzeRule.getString(infoRule.desc).let {
                if (it.isNotEmpty()) book.desc = HtmlFormatter.format(it)
            }
            Log.d(bookSource.sourceUrl, "└${book.desc}")
        } catch (e: Exception) {
            Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
        }
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取封面链接")
        try {
            analyzeRule.getString(infoRule.imgUrl).let {
                if (it.isNotEmpty()) book.imgUrl = NetworkUtils.getAbsoluteURL(baseUrl, it)
            }
            Log.d(bookSource.sourceUrl, "└${book.imgUrl}")
        } catch (e: Exception) {
            Log.d(bookSource.sourceUrl, "└${e.localizedMessage}")
        }
        scope.ensureActive()
        Log.d(bookSource.sourceUrl, "┌获取目录链接")
        book.chapterUrl = analyzeRule.getString(infoRule.tocUrl, isUrl = true)
        if (book.chapterUrl.isEmpty()) book.chapterUrl = redirectUrl
        if (book.chapterUrl == redirectUrl) {
            book.putCathe("tocHtml", body)
            //book.tocHtml = body
        }
        Log.d(bookSource.sourceUrl, "└${book.chapterUrl}")
    }

}