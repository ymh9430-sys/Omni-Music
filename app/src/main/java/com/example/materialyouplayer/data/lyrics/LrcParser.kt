package com.example.materialyouplayer.data.lyrics

import java.util.regex.Pattern

// كائن الكلمة المفردة داخل السطر مع توقيت بدئها ونهايتها بالميلي ثانية
data class LyricWord(
    val word: String,
    val startTime: Long,
    val endTime: Long
)

// كائن السطر المتكامل اللي بيحتوي على توقيت البدء، التوقيت الذكي للنهاية، والنص، والكلمات المفككة
data class LyricLine(
    val startTime: Long,
    var endTime: Long,
    val text: String,
    val words: List<LyricWord> = emptyList()
)

object LrcParser {

    // النماذج القياسية للتعرف على التوقيتات [mm:ss.xx] أو [mm:ss.xxx]
    private val lineTimePattern = Pattern.compile("\\[(\\d+):(\\d+)\\.(\\d+)\\]")
    private val wordTimePattern = Pattern.compile("<(\\d+):(\\d+)\\.(\\d+)>")

    fun parseLyrics(rawLyrics: String?): List<LyricLine> {
        if (rawLyrics.isNullOrBlank()) return emptyList()

        val lines = rawLyrics.lines()
        val parsedLines = mutableListOf<LyricLine>()

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            // 1. استخراج وقت بدء السطر الرئيسي
            val lineMatcher = lineTimePattern.matcher(trimmed)
            if (lineMatcher.find()) {
                val min = lineMatcher.group(1)?.toLong() ?: 0L
                val sec = lineMatcher.group(2)?.toLong() ?: 0L
                val msStr = lineMatcher.group(3) ?: "0"
                // تحويل الميلي ثانية بدقة سواء كانت رقمين أو ثلاثة
                val ms = padMilliseconds(msStr)
                val lineStartTime = (min * 60 * 1000) + (sec * 1000) + ms

                // تنظيف نص السطر من تايم كود البداية للتعامل مع محتواه
                val lineContent = trimmed.substring(lineMatcher.end())

                // 2. تفكيك الكلمات والتوقيتات الداخلية (Word-by-Word Karaoke)
                val wordList = mutableListOf<LyricWord>()
                val wordMatcher = wordTimePattern.matcher(lineContent)
                
                var lastIndex = 0
                var currentWordStart = lineStartTime
                var lastExtractedWord: String? = null

                // نقوم بمسح السطر بحثًا عن التوقيتات الداخلية لربط كل كلمة بوقتها
                while (wordMatcher.find()) {
                    val rawWord = lineContent.substring(lastIndex, wordMatcher.start())
                    val cleanWord = rawWord.trim()

                    if (cleanWord.isNotEmpty()) {
                        val wMin = wordMatcher.group(1)?.toLong() ?: 0L
                        val wSec = wordMatcher.group(2)?.toLong() ?: 0L
                        val wMsStr = wordMatcher.group(3) ?: "0"
                        val wMs = padMilliseconds(wMsStr)
                        val currentWordEnd = (wMin * 60 * 1000) + (wSec * 1000) + wMs

                        // معالجة عدم وجود مسافات بين المقاطع المقطعة (Syllables)
                        val isContinuous = !rawWord.startsWith(" ") && wordList.isNotEmpty()
                        
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

                // التقاط الكلمة الأخيرة في السطر إن وجدت بعد آخر تايم كود
                val remainingWord = lineContent.substring(lastIndex).trim()
                if (remainingWord.isNotEmpty()) {
                    // وقت النهاية الافتراضي للكلمة الأخيرة سيتم ضبطه لاحقاً عبر قاعدة الـ Smart End Time
                    wordList.add(
                        LyricWord(
                            word = remainingWord,
                            startTime = currentWordStart,
                            endTime = currentWordStart + 300L // مؤقتًا 300 ميلي ثانية
                        )
                    )
                }

                // تنظيف النص الكامل للسطر من أي تايم كودز لعرضه كـ Plain Text عند الحاجة
                val plainText = lineContent.replaceAll("<[^>]*>", "").trim()

                parsedLines.add(
                    LyricLine(
                        startTime = lineStartTime,
                        endTime = lineStartTime + 2000L, // قيمة افتراضية يتم تعديلها بالأسفل ديناميكيًا
                        text = plainText,
                        words = wordList
                    )
                )
            }
        }

        // ========================================================
        // تطبيق القواعد الذكية (Smart End Time & Overlap Resolution)
        // ========================================================
        val finalLines = parsedLines.sortedBy { it.startTime }.toMutableList()

        for (i in 0 until finalLines.size) {
            val currentLine = finalLines[i]
            
            if (i < finalLines.size - 1) {
                val nextLine = finalLines[i + 1]
                
                // قاعدة: لو السطرين بيبدأوا في نفس الوقت بالظبط، بنزود 1 ميلي ثانية على التاني لمنع تداخل الأنيميشن
                if (currentLine.startTime == nextLine.startTime) {
                    finalLines[i + 1] = nextLine.copy(startTime = nextLine.startTime + 1L)
                }

                // قاعدة الـ Smart End Time: نهاية السطر هي بداية السطر الجديد بسلاسة، 
                // إلا لو فيه بريك أو فاصل زمني طويل (أكبر من 1.5 ثانية)، السطر بيقفل بدري بـ 300ms بعد آخر كلمة لحفظ المظهر النظيف
                val gap = nextLine.startTime - currentLine.startTime
                if (gap > 1500L) {
                    val lastWordEnd = currentLine.words.lastOrNull()?.endTime ?: (currentLine.startTime + 1000L)
                    currentLine.endTime = lastWordEnd + 200L
                } else {
                    currentLine.endTime = nextLine.startTime
                }
            } else {
                // السطر الأخير في الأغنية يقفل بعد آخر كلمة فيه بـ 500 ميلي ثانية
                val lastWordEnd = currentLine.words.lastOrNull()?.endTime ?: (currentLine.startTime + 1500L)
                currentLine.endTime = lastWordEnd + 500L
            }

            // مزامنة أوقات نهاية الكلمات الداخلية لتطابق نهاية السطر الذكية تماماً
            if (currentLine.words.isNotEmpty()) {
                val lastWordIndex = currentLine.words.size - 1
                val lastWord = currentLine.words[lastWordIndex]
                currentLine.words[lastWordIndex] = lastWord.copy(endTime = currentLine.endTime)
            }
        }

        return finalLines
    }

    // دالة لتوحيد الميلي ثانية المكونة من رقمين (مثال: 45 تصبح 450) لضمان دقة العمليات الحسابية
    private fun padMilliseconds(msStr: String): Long {
        val padded = when (msStr.length) {
            1 -> "${msStr}00"
            2 -> "${msStr}0"
            else -> msStr.take(3)
        }
        return padded.toLongOrNull() ?: 0L
    }

    // دالة مساعدة لتنظيف النصوص البرمجية للـ Regex
    private fun String.replaceAll(regex: String, replacement: String): String {
        return Pattern.compile(regex).matcher(this).replaceAll(replacement)
    }
}
