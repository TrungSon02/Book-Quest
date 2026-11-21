package com.example.bookquest.ui.reading

import android.content.Context
import data.entity.BookEntity
import data.entity.ChapterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URLDecoder
import java.util.zip.ZipInputStream

/**
 * Extracts chapters and clean paragraph text from an EPUB asset.
 * Huge single-file spine sections are split into manageable chapter parts so
 * on-screen dynamic pagination (Kindle-style) stays fast at any zoom level.
 */
object EpubBookLoader {

    const val DEFAULT_WORDS_PER_PAGE = 300
    private const val MAX_WORDS_PER_CHAPTER_PART = 2400

    suspend fun loadFromAssets(
        context: Context,
        book: BookEntity,
        epubAssetPath: String?,
        dbChapters: List<ChapterEntity> = emptyList(),
        wordsPerPage: Int = DEFAULT_WORDS_PER_PAGE
    ): ReadingBook = withContext(Dispatchers.IO) {
        val chapters = if (!epubAssetPath.isNullOrBlank()) {
            runCatching {
                context.assets.open(epubAssetPath).use { stream ->
                    parseEpubStream(stream, wordsPerPage)
                }
            }.getOrNull().orEmpty()
        } else {
            emptyList()
        }

        val resolvedChapters = chapters.ifEmpty {
            buildFallbackChapters(book, dbChapters, wordsPerPage)
        }

        ReadingBook(
            title = book.title,
            author = book.author,
            coverImage = book.coverPath,
            chapters = resolvedChapters
        )
    }

    internal fun parseEpubStream(
        inputStream: InputStream,
        wordsPerPage: Int = DEFAULT_WORDS_PER_PAGE
    ): List<ReadingChapter> {
        val textEntries = linkedMapOf<String, String>()
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && isRelevantTextEntry(entry.name)) {
                    val normalizedName = normalizeZipPath(entry.name)
                    textEntries[normalizedName] = zis.readBytes().toString(Charsets.UTF_8)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val opfPath = textEntries.keys.firstOrNull { it.endsWith(".opf", ignoreCase = true) }
            ?: return emptyList()
        val opfDir = opfPath.substringBeforeLast('/', missingDelimiterValue = "")
        val opfXml = textEntries[opfPath].orEmpty()

        val manifest = mutableMapOf<String, String>()
        val spineIds = mutableListOf<String>()
        var ncxHref: String? = null

        val itemTagRegex = Regex("<item\\b([^>]*)/?>", RegexOption.IGNORE_CASE)
        for (match in itemTagRegex.findAll(opfXml)) {
            val attrs = parseXmlAttributes(match.groupValues[1])
            val id = attrs["id"]
            val href = attrs["href"]
            val mediaType = attrs["media-type"].orEmpty()
            if (!id.isNullOrBlank() && !href.isNullOrBlank()) {
                manifest[id] = href
                if (ncxHref == null && (mediaType.contains("ncx", ignoreCase = true) || href.endsWith(".ncx", ignoreCase = true))) {
                    ncxHref = href
                }
            }
        }

        val itemRefRegex = Regex("<itemref\\b([^>]*)/?>", RegexOption.IGNORE_CASE)
        for (match in itemRefRegex.findAll(opfXml)) {
            val attrs = parseXmlAttributes(match.groupValues[1])
            val idref = attrs["idref"]
            if (!idref.isNullOrBlank()) {
                spineIds += idref
            }
        }

        val tocTitlesByPath = mutableMapOf<String, String>()
        if (!ncxHref.isNullOrBlank()) {
            val ncxPath = resolveRelativePath(opfDir, ncxHref!!)
            val ncxDir = ncxPath.substringBeforeLast('/', missingDelimiterValue = "")
            val ncxXml = textEntries[ncxPath]
            if (!ncxXml.isNullOrBlank()) {
                parseNcxTitles(ncxXml, ncxDir, tocTitlesByPath)
            }
        }

        val rawSections = mutableListOf<Pair<String, String>>()
        val visitedPaths = mutableSetOf<String>()

        for (idref in spineIds) {
            val rawHref = manifest[idref] ?: continue
            val resolvedPath = resolveRelativePath(opfDir, rawHref)
            if (!visitedPaths.add(resolvedPath)) continue
            val html = textEntries[resolvedPath] ?: continue

            val subChapters = splitHtmlIntoHeadingChapters(html)
            if (subChapters.size > 1 && spineIds.size <= 5) {
                rawSections.addAll(subChapters)
            } else {
                val extractedHeading = extractFirstHeading(html)
                val cleanText = htmlToFormattedText(html)
                if (countWordsAndCjk(cleanText) >= 35) {
                    val title = tocTitlesByPath[resolvedPath]
                        ?.takeIf { it.isNotBlank() }
                        ?: extractedHeading?.takeIf { it.isNotBlank() && it.length <= 90 }
                        ?: ""
                    rawSections += title to cleanText
                }
            }
        }

        val expandedSections = mutableListOf<Pair<String, String>>()
        for ((index, section) in rawSections.withIndex()) {
            val baseTitle = section.first.ifBlank { "Chapter ${index + 1}" }
            val parts = paginateText(section.second, MAX_WORDS_PER_CHAPTER_PART)
            if (parts.size <= 1) {
                expandedSections += baseTitle to section.second
            } else {
                parts.forEachIndexed { partIndex, partText ->
                    expandedSections += "$baseTitle (Part ${partIndex + 1})" to partText
                }
            }
        }

        return expandedSections.mapNotNull { (displayTitle, bodyText) ->
            val initialPages = paginateText(bodyText, wordsPerPage)
            if (initialPages.isEmpty()) return@mapNotNull null
            ReadingChapter(
                title = displayTitle,
                pages = initialPages
            )
        }
    }

    internal fun paginateText(
        text: String,
        wordsPerPage: Int = DEFAULT_WORDS_PER_PAGE
    ): List<String> {
        val targetWords = wordsPerPage.coerceAtLeast(50)
        val paragraphs = text
            .split(Regex("\\n\\s*\\n+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (paragraphs.isEmpty()) return emptyList()

        val pages = mutableListOf<String>()
        val currentParagraphs = mutableListOf<String>()
        var currentWordCount = 0

        fun flushCurrentPage() {
            if (currentParagraphs.isNotEmpty()) {
                pages += currentParagraphs.joinToString("\n\n")
                currentParagraphs.clear()
                currentWordCount = 0
            }
        }

        for (paragraph in paragraphs) {
            val paragraphUnits = tokenizeForPagination(paragraph)
            val paragraphWordCount = paragraphUnits.size

            if (paragraphWordCount == 0) continue

            if (currentWordCount > 0 && currentWordCount + paragraphWordCount > targetWords) {
                flushCurrentPage()
            }

            if (paragraphWordCount <= targetWords) {
                currentParagraphs += paragraph
                currentWordCount += paragraphWordCount
            } else {
                var offset = 0
                while (offset < paragraphUnits.size) {
                    val remainingNeeded = (targetWords - currentWordCount).coerceAtLeast(1)
                    val end = (offset + remainingNeeded).coerceAtMost(paragraphUnits.size)
                    val slice = joinTokens(paragraphUnits.subList(offset, end))
                    if (slice.isNotBlank()) {
                        currentParagraphs += slice
                        currentWordCount += (end - offset)
                    }
                    if (currentWordCount >= targetWords) {
                        flushCurrentPage()
                    }
                    offset = end
                }
            }
        }

        flushCurrentPage()
        return pages
    }

    private fun buildFallbackChapters(
        book: BookEntity,
        dbChapters: List<ChapterEntity>,
        wordsPerPage: Int
    ): List<ReadingChapter> {
        if (dbChapters.isNotEmpty()) {
            return dbChapters.map { ch ->
                ReadingChapter(
                    title = "Chapter ${ch.number}: ${ch.title}",
                    pages = paginateText("${ch.title}\n\n${book.description}", wordsPerPage)
                )
            }
        }
        return listOf(
            ReadingChapter(
                title = "Chapter 1",
                pages = paginateText(book.description, wordsPerPage).ifEmpty { listOf(book.description) }
            )
        )
    }

    private fun isRelevantTextEntry(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".opf") ||
            lower.endsWith(".ncx") ||
            lower.endsWith(".xhtml") ||
            lower.endsWith(".html") ||
            lower.endsWith(".htm") ||
            lower.endsWith(".xml")
    }

    private fun normalizeZipPath(path: String): String {
        val parts = mutableListOf<String>()
        for (segment in path.replace('\\', '/').split('/')) {
            when (segment) {
                "", "." -> Unit
                ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.lastIndex)
                else -> parts += segment
            }
        }
        return parts.joinToString("/")
    }

    private fun resolveRelativePath(baseDir: String, rawHref: String): String {
        val cleanHref = runCatching {
            URLDecoder.decode(rawHref.substringBefore('#'), "UTF-8")
        }.getOrDefault(rawHref.substringBefore('#'))
        val combined = if (baseDir.isEmpty()) cleanHref else "$baseDir/$cleanHref"
        return normalizeZipPath(combined)
    }

    private fun parseXmlAttributes(attrString: String): Map<String, String> {
        val attrs = mutableMapOf<String, String>()
        val attrRegex = Regex("([a-zA-Z0-9_:-]+)\\s*=\\s*([\"'])(.*?)\\2")
        for (match in attrRegex.findAll(attrString)) {
            attrs[match.groupValues[1].lowercase()] = decodeHtmlEntities(match.groupValues[3])
        }
        return attrs
    }

    private fun parseNcxTitles(
        ncxXml: String,
        opfDir: String,
        outMap: MutableMap<String, String>
    ) {
        val ncxPathDir = opfDir
        val navPointPattern = Regex(
            "<navPoint[^>]*>.*?<navLabel[^>]*>\\s*<text[^>]*>(.*?)</text>\\s*</navLabel>\\s*<content[^>]*src=[\"']([^\"']+)[\"']",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )
        for (match in navPointPattern.findAll(ncxXml)) {
            val label = decodeHtmlEntities(match.groupValues[1].replace(Regex("<[^>]+>"), "").trim())
            val src = match.groupValues[2]
            val resolved = resolveRelativePath(ncxPathDir, src)
            if (label.isNotBlank() && !outMap.containsKey(resolved)) {
                outMap[resolved] = label
            }
        }
    }

    private fun splitHtmlIntoHeadingChapters(html: String): List<Pair<String, String>> {
        val bodyHtml = stripNonBodyHtml(html)
        val headingRegex = Regex("<h[12][^>]*>(.*?)</h[12]>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val matches = headingRegex.findAll(bodyHtml).toList()
        if (matches.size < 2) return emptyList()

        val result = mutableListOf<Pair<String, String>>()
        for (i in matches.indices) {
            val title = decodeHtmlEntities(
                matches[i].groupValues[1].replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
            )
            val start = matches[i].range.last + 1
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else bodyHtml.length
            if (start < end) {
                val sectionText = htmlToFormattedText(bodyHtml.substring(start, end))
                if (countWordsAndCjk(sectionText) >= 50) {
                    result += title to sectionText
                }
            }
        }
        return result
    }

    private fun extractFirstHeading(html: String): String? {
        val body = stripNonBodyHtml(html)
        val match = Regex("<h[1-3][^>]*>(.*?)</h[1-3]>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            .find(body) ?: return null
        return decodeHtmlEntities(
            match.groupValues[1]
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        )
    }

    private fun stripNonBodyHtml(html: String): String {
        return html
            .replace(Regex("<(head|style|script|svg)[^>]*>.*?</\\1>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)), " ")
    }

    internal fun htmlToFormattedText(html: String): String {
        val withoutMeta = stripNonBodyHtml(html)
        val withParagraphBreaks = withoutMeta
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</(p|div|h[1-6]|li|blockquote|section|article|tr)>", RegexOption.IGNORE_CASE), "\n\n")
        val stripped = withParagraphBreaks.replace(Regex("<[^>]+>"), " ")
        val decoded = decodeHtmlEntities(stripped)

        return decoded
            .lines()
            .map { line -> line.replace(Regex("[\\t\\x0B\\f\\r ]+"), " ").trim() }
            .joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun decodeHtmlEntities(input: String): String {
        var text = input
            .replace("&nbsp;", " ")
            .replace("&#160;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&rsquo;", "\u2019")
            .replace("&lsquo;", "\u2018")
            .replace("&rdquo;", "\u201D")
            .replace("&ldquo;", "\u201C")
            .replace("&mdash;", "\u2014")
            .replace("&ndash;", "\u2013")
            .replace("&hellip;", "\u2026")

        text = Regex("&#(\\d+);").replace(text) { m ->
            val code = m.groupValues[1].toIntOrNull()
            if (code != null && code in 32..0x10FFFF) String(Character.toChars(code)) else ""
        }
        text = Regex("&#x([0-9a-fA-F]+);").replace(text) { m ->
            val code = m.groupValues[1].toIntOrNull(16)
            if (code != null && code in 32..0x10FFFF) String(Character.toChars(code)) else ""
        }
        return text
    }

    private fun isCjk(ch: Char): Boolean {
        val code = ch.code
        return code in 0x4E00..0x9FFF || code in 0x3400..0x4DBF || code in 0x3040..0x30FF
    }

    private fun countWordsAndCjk(text: String): Int =
        tokenizeForPagination(text).size

    private fun tokenizeForPagination(text: String): List<String> {
        val tokens = mutableListOf<String>()
        for (rawWord in text.trim().split(Regex("\\s+"))) {
            if (rawWord.isEmpty()) continue
            if (rawWord.any { isCjk(it) } && rawWord.length > 1) {
                val sb = StringBuilder()
                for (ch in rawWord) {
                    if (isCjk(ch)) {
                        if (sb.isNotEmpty()) {
                            tokens += sb.toString()
                            sb.clear()
                        }
                        tokens += ch.toString()
                    } else {
                        sb.append(ch)
                    }
                }
                if (sb.isNotEmpty()) {
                    tokens += sb.toString()
                }
            } else {
                tokens += rawWord
            }
        }
        return tokens
    }

    private fun joinTokens(tokens: List<String>): String {
        if (tokens.isEmpty()) return ""
        val sb = StringBuilder(tokens.first())
        for (i in 1 until tokens.size) {
            val prev = tokens[i - 1]
            val curr = tokens[i]
            val bothCjk = prev.length == 1 && isCjk(prev[0]) && curr.length == 1 && isCjk(curr[0])
            if (!bothCjk) {
                sb.append(' ')
            }
            sb.append(curr)
        }
        return sb.toString()
    }
}
