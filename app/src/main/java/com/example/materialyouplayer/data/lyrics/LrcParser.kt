package com.example.materialyouplayer.data.lyrics

import java.util.regex.Pattern

// كائن الكلمة المفردة داخل السطر مع توقيت بدئها ونهايتها بالميلي ثانية
data class LyricWord(
    val word: String,
    val startTime: Long,
    val endTime: Long
)

// كائن السطر المتكامل: تم تغيير startTime لـ var لتمكين معالجة التداخل اللحظي ذكياً
data class LyricLine(
    var startTime: Long,
    var endTime: Long,
    val text: String,
    var words: MutableList<LyricWord> = mutableListOf()
)

object LrcParser {

    private val lineTimePattern = Pattern.compile("\\[(\\d+):(\\d+)\\.(\\d+)\\]")
    private val wordTimePattern = Pattern.compile("<(\\d+):(\\d+)\\.(\\d+)>")

    fun parseLyrics(rawLyrics: String?): List<LyricLine> {
        if (rawLyrics.isNullOrBlank()) return emptyList()

        val lines = rawLyrics.lines()
        val parsedLines = mutableListOf<LyricLine>()

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            // قاعدة مخصصة: إزالة ونسف أقواس الـ span والـ HTML لضمان نظافة النص وعرضه بأمان
            val cleanedHtmlLine = trimmed.replace(Regex("<span[^>]*>"), "").replace("</span>", "")

            // 1. استخراج وقت بدء السطر الرئيسي
            val lineMatcher = lineTimePattern.matcher(cleanedHtmlLine)
            if (lineMatcher.find()) {
                val min = lineMatcher.group(1)?.toLong() ?: 0L
                val sec = lineMatcher.group(2)?.toLong() ?: 0L
                val msStr = lineMatcher.group(3) ?: "0"
                val ms = padMilliseconds(msStr)
                val lineStartTime = (min * 60 * 1000) + (sec * 1000) + ms

                val lineContent = cleanedHtmlLine.substring(lineMatcher.end())

                // 2. تفكيك الكلمات والتوقيتات الداخلية (Word-by-Word Karaoke)
                val wordList = mutableListOf<LyricWord>()
                val wordMatcher = wordTimePattern.matcher(lineContent)
                
                var lastIndex = 0
                var currentWordStart = lineStartTime

                while (wordMatcher.find()) {
                    val rawWord = lineContent.substring(lastIndex, wordMatcher.start())
                    
                    // قاعدة مخصصة: مسح المسافات قبل الكلمة وترك المسافة بعدها كما هي تماماً لثبات التزامن
                    val cleanWord = rawWord.trimStart()

                    if (cleanWord.isNotEmpty()) {
                        val wMin = wordMatcher.group(1)?.toLong() ?: 0L
                        val wSec = wordMatcher.group(2)?.toLong() ?: 0L
                        val wMsStr = wordMatcher.group(3) ?: "0"
                        val wMs = padMilliseconds(wMsStr)
                        val currentWordEnd = (wMin * 60 * 1000) + (wSec * 1000) + wMs
                        
                        wordList.add(
                            LyricWord(
                                word = cleanWord,
                                startTime = currentWordStart,
                                endTime = currentWordEnd
                            )
                        )
                        currentWordStart = currentWordEnd
                    }
                    lastIndex = wordMatcher.end()
                }

                // التقاط الكلمة الأخيرة في السطر
                val remainingWord = lineContent.substring(lastIndex).trimStart()
                if (remainingWord.isNotEmpty()) {
                    wordList.add(
                        LyricWord(
                            word = remainingWord,
                            startTime = currentWordStart,
                            endTime = currentWordStart + 300L
                        )
                    )
                }

                // استخراج النص الصافي لعرض الـ Plain Text عند الحاجة
                val plainText = lineContent.replace(Regex("<[^>]*>"), "").trim()
                parsedLines.add(
                    LyricLine(
                        startTime = lineStartTime,
                        endTime = lineStartTime + 2000L,
                        text = plainText,
                        words = wordList
                    )
                )
            }
        }

        // ترتيب الأسطر زمنياً
        val finalLines = parsedLines.sortedBy { it.startTime }.toMutableList()

        // قاعدة مخصصة: لو جملتين بيبدأوا في نفس الوقت بالظبط، بنزود 00:00.001 ثانية (1 ميلي ثانية) على واحدة منهم لمنع الـ Overlap الإملائي
        for (i in 0 until finalLines.size - 1) {
            if (finalLines[i].startTime == finalLines[i + 1].startTime) {
                finalLines[i + 1].startTime = finalLines[i + 1].startTime + 1
            }
        }

        // إعادة الفرز بعد حل التداخلات لتأكيد الترتيب
        finalLines.sortBy { it.startTime }

        // ربط أوقات النهاية الذكية للأسطر والكلمات الأخيرة
        for (i in 0 until finalLines.size) {
            val currentLine = finalLines[i]
            if (i < finalLines.size - 1) {
                val nextLine = finalLines[i + 1]
                // قاعدة مخصصة: توقيت نهاية السطر (وآخر كلمة فيه) يطابق بالملّي توقيت بداية السطر الجديد
                currentLine.endTime = nextLine.startTime
            } else {
                // السطر الأخير في الأغنية ككل
                val lastWordEnd = currentLine.words.lastOrNull()?.endTime ?: (currentLine.startTime + 1500L)
                currentLine.endTime = lastWordEnd + 500L
            }

            // مزامنة أوقات نهاية الكلمة الأخيرة داخل السطر لتطابق نهاية السطر الذكية تماماً
            if (currentLine.words.isNotEmpty()) {
                val lastWordIndex = currentLine.words.size - 1
                val lastWord = currentLine.words[lastWordIndex]
                currentLine.words[lastWordIndex] = lastWord.copy(endTime = currentLine.endTime)
            }
        }

        return finalLines
    }

    private fun padMilliseconds(msStr: String): Long {
        val padded = when (msStr.length) {
            1 -> "${msStr}00"
            2 -> "${msStr}0"
            else -> msStr.take(3)
        }
        return padded.toLongOrNull() ?: 0L
    }
}
