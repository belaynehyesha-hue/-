package com.example.data

import java.util.Calendar

data class EthiopianDate(val year: Int, val month: Int, val day: Int) {
    fun format(): String {
        val monthNames = listOf(
            "መስከረም", "ጥቅምት", "ህዳር", "ታኅሣሥ", "ጥር", "የካቲት",
            "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
        )
        if (month in 1..13) {
            return "${monthNames[month - 1]} $day, $year ዓ.ም"
        }
        return "መስከረም $day, $year ዓ.ም"
    }

    fun formatShort(): String {
        val monthNames = listOf(
            "መስከረም", "ጥቅምት", "ህዳር", "ታኅሣሥ", "ጥር", "የካቲት",
            "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
        )
        if (month in 1..13) {
            return "${monthNames[month - 1].take(3)} $day, $year"
        }
        return "መስ $day, $year"
    }
}

object EthiopianCalendar {

    // Names of the Ethiopian months in Amharic
    val MONTH_NAMES = listOf(
        "መስከረም", "ጥቅምት", "ህዳር", "ታኅሣሥ", "ጥር", "የካቲት",
        "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
    )

    // Names of days of the week in Amharic (starting Monday)
    val WEEKDAYS = listOf(
        "ሰኞ", "ማክሰኞ", "ረቡዕ", "ሐሙስ", "አርብ", "ቅዳሜ", "እሁድ"
    )

    // Bidirectional JDN conversions
    private fun gregorianToJdn(year: Int, month: Int, day: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun jdnToGregorian(jdn: Int): Triple<Int, Int, Int> {
        val l = jdn + 68569
        val n = (4 * l) / 146097
        val l1 = l - (146097 * n + 3) / 4
        val i = (4000 * (l1 + 1)) / 1461001
        val l2 = l1 - (1461 * i) / 4 + 31
        val j = (80 * l2) / 2447
        val day = l2 - (2447 * j) / 80
        val l3 = j / 11
        val month = j + 2 - 12 * l3
        val year = 100 * (n - 49) + i + l3
        return Triple(year, month, day)
    }

    private fun ethiopianToJdn(year: Int, month: Int, day: Int): Int {
        val era = 1723856
        return era + 365 * (year - 1) + (year / 4) + 30 * (month - 1) + day - 1
    }

    // Convert Gregorian Date to Ethiopian Date
    fun gregorianToEthiopian(year: Int, month: Int, day: Int): EthiopianDate {
        var ethiopianYear = year - 8
        val newYearDay = if ((year % 4 == 3) && (month > 9 || (month == 9 && day >= 12))) {
            12
        } else if ((year % 4 == 0) && (month < 9 || (month == 9 && day < 12))) {
            12
        } else {
            11
        }
        
        if (month > 9 || (month == 9 && day >= newYearDay)) {
            ethiopianYear = year - 7
        } else {
            ethiopianYear = year - 8
        }
        
        val startYear = if (month > 9 || (month == 9 && day >= newYearDay)) year else year - 1
        val actualNewYearDay = if (startYear % 4 == 3) 12 else 11
        
        val jdnStart = gregorianToJdn(startYear, 9, actualNewYearDay)
        val jdnCurrent = gregorianToJdn(year, month, day)
        val diffDays = jdnCurrent - jdnStart
        
        val ethiopianMonth = (diffDays / 30) + 1
        val ethiopianDay = (diffDays % 30) + 1
        
        return EthiopianDate(ethiopianYear, ethiopianMonth, ethiopianDay)
    }

    // Convert Ethiopian Date to Gregorian Date
    fun ethiopianToGregorianDate(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        val jdn = ethiopianToJdn(year, month, day)
        return jdnToGregorian(jdn)
    }

    // Get current Ethiopian date
    fun getCurrentEthiopianDate(): EthiopianDate {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToEthiopian(year, month, day)
    }

    // Get number of days in an Ethiopian month
    fun getDaysInMonth(year: Int, month: Int): Int {
        if (month in 1..12) return 30
        if (month == 13) {
            // Leap year if (year + 1) % 4 == 0
            return if ((year + 1) % 4 == 0) 6 else 5
        }
        return 30
    }

    // Get weekday of the first day of an Ethiopian month (0 for Mon, 6 for Sun)
    fun getFirstDayWeekday(year: Int, month: Int): Int {
        val jdn = ethiopianToJdn(year, month, 1)
        // JDN 0 is a Monday. (jdn % 7) gives 0 for Monday, 1 for Tuesday ... 6 for Sunday
        return (jdn % 7)
    }

    // Calculate Lease Proclamation 721/2011 milestones
    fun calculateLeaseMilestones(startYear: Int, startMonth: Int, startDay: Int, scale: String): List<LeaseMilestone> {
        // scale: "Small" (አነስተኛ), "Medium" (መካከለኛ), "Large" (ከፍተኛ)
        val milestones = mutableListOf<LeaseMilestone>()

        // 1. Commencement deadline
        val commencementMonths = when (scale) {
            "Small" -> 6
            "Medium" -> 12
            else -> 18
        }
        val (commYear, commMonth) = addMonths(startYear, startMonth, commencementMonths)
        milestones.add(
            LeaseMilestone(
                name = "የግንባታ መጀመሪያ የመጨረሻ ቀን (Commencement Deadline)",
                description = "በአዋጅ ቁጥር 721/2011 አንቀጽ 16 መሠረት ግንባታ መጀመር ያለበት የመጨረሻው ቀን ($commencementMonths ወራት)።",
                date = EthiopianDate(commYear, commMonth, startDay),
                lawArticle = "አዋጅ 721/2011 አንቀጽ 16",
                penalty = "የሊዝ ይዞታ መቋረጥና ካሳ ሳይከፈል መሬቱ ለከተማ አስተዳደሩ መመለስ።"
            )
        )

        // 2. Completion deadline
        val completionMonths = when (scale) {
            "Small" -> 18
            "Medium" -> 24
            else -> 36
        }
        val (compYear, compMonth) = addMonths(startYear, startMonth, completionMonths)
        milestones.add(
            LeaseMilestone(
                name = "የግንባታ ማጠናቀቂያ የመጨረሻ ቀን (Completion Deadline)",
                description = "በአዋጅ ቁጥር 721/2011 አንቀጽ 17 መሠረት ግንባታው ሙሉ በሙሉ ተጠናቆ ለአገልግሎት ዝግጁ መሆን ያለበት ቀን ($completionMonths ወራት)።",
                date = EthiopianDate(compYear, compMonth, startDay),
                lawArticle = "አዋጅ 721/2011 አንቀጽ 17",
                penalty = "ከፍተኛ የገንዘብ መቀጮ መጣልና ለተጨማሪ የሊዝ መሬት ጨረታዎች መታገድ።"
            )
        )

        // 3. Technical building inspection milestones
        milestones.add(
            LeaseMilestone(
                name = "የመጀመሪያ ደረጃ የጥራት ኦዲት (Excavation & Foundation Audit)",
                description = "በአዲሱ የኢትዮጵያ ሕንፃ አዋጅ 1356/2017 ዓ.ም አንቀጽ 32 መሠረት መሠረት ቁፋሮ እንደተጠናቀቀ የሚደረግ ፍተሻ።",
                date = EthiopianDate(startYear, startMonth, startDay).addDays(90),
                lawArticle = "የህንፃ አዋጅ 1356/2017 ዓ.ም አንቀጽ 32",
                penalty = "ቀጣይ የግንባታ ፈቃድ እገዳና የመዋቅር ደህንነት ስጋት ቅጣት።"
            )
        )

        milestones.add(
            LeaseMilestone(
                name = "መካከለኛ ደረጃ መዋቅራዊ ኦዲት (Structural Frame Check)",
                description = "የምሶሶዎችና የኮንክሪት ወለል (Columns & Slabs) ጥንካሬ በEBCS-2 መሠረት የሚፈተሽበት ቀን።",
                date = EthiopianDate(startYear, startMonth, startDay).addDays(180),
                lawArticle = "EBCS-2 / ESEN 1992 የኮንክሪት ደንብ",
                penalty = "የኮንክሪት ፍንዳታ ወይም ጥራት ጉድለት ካለ ግንባታ ማቆም ትዕዛዝ።"
            )
        )

        return milestones
    }

    private fun addMonths(year: Int, month: Int, mToAdd: Int): Pair<Int, Int> {
        var newMonth = month + mToAdd
        var newYear = year
        while (newMonth > 12) {
            newMonth -= 12
            newYear += 1
        }
        return Pair(newYear, newMonth)
    }
}

data class LeaseMilestone(
    val name: String,
    val description: String,
    val date: EthiopianDate,
    val lawArticle: String,
    val penalty: String
)

// Helper extension function to add days to an EthiopianDate
fun EthiopianDate.addDays(days: Int): EthiopianDate {
    val greg = EthiopianCalendar.ethiopianToGregorianDate(this.year, this.month, this.day)
    val cal = Calendar.getInstance()
    cal.set(greg.first, greg.second - 1, greg.third)
    cal.add(Calendar.DAY_OF_MONTH, days)
    return EthiopianCalendar.gregorianToEthiopian(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}
