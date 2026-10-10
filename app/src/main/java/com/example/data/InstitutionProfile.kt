package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Universal Institution Profile representing any construction organization,
 * regulatory bureau, contractor, consulting firm, or client using this platform.
 * Enables the software to be seamlessly utilized by any entity anywhere in Ethiopia or internationally.
 */
data class InstitutionProfile(
    val name: String = "የኮንስትራክሽን ፈቃድና ቁጥጥር ባለስልጣን",
    val department: String = "የግንባታ ቁጥጥር፣ የሰው ኃይል ብቃትና ፍተሻ መምሪያ",
    val type: String = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regulatory Authority)",
    val regionOrCity: String = "አዲስ አበባ (Addis Ababa)",
    val licenseOrCode: String = "MUI-REG-2026/ETH-091",
    val officerInCharge: String = "በላይነህ የሻለም (Lead Civil Engineer & Inspector)",
    val contactPhone: String = "+251 91 122 3344",
    val contactEmail: String = "construction.authority@ethiopia.gov.et",
    val officialMotto: String = "በኢትዮጵያ አዲሱ ሕንፃ አዋጅ 1356/2017 ዓ.ም፣ EBCS/Eurocodes እና FIDIC/ISO/IBC ዓለም አቀፍ መመሪያዎች መሠረት"
) {
    companion object {
        val DEFAULT = InstitutionProfile()
        private const val PREF_NAME = "institution_settings_pref"
        private const val KEY_NAME = "inst_name"
        private const val KEY_DEPT = "inst_dept"
        private const val KEY_TYPE = "inst_type"
        private const val KEY_REGION = "inst_region"
        private const val KEY_CODE = "inst_code"
        private const val KEY_OFFICER = "inst_officer"
        private const val KEY_PHONE = "inst_phone"
        private const val KEY_EMAIL = "inst_email"
        private const val KEY_MOTTO = "inst_motto"

        val PRESET_ORGANIZATIONS = listOf(
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - ዋና መ/ቤት",
                department = "የኮንስትራክሽን ቁጥጥር፣ ፈቃድና የሰው ኃይል ብቃት ዳይሬክቶሬት",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ባህር ዳር (Bahir Dar) - አማራ ክልል",
                licenseOrCode = "AMH-REG-HQ-2026/01",
                officerInCharge = "በላይነህ የሻለም (Lead Civil Engineer & Regional Inspector)"
            ),
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - የባህር ዳርና ዙሪያ መምሪያ",
                department = "የከተማ ግንባታ ክትትልና የዲዛይን ፍተሻ ቡድን",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ባህር ዳር - አማራ ክልል",
                licenseOrCode = "AMH-BDR-CNST-2026/02",
                officerInCharge = "ኢንጂነር አብርሃም ደረጀ (Senior Inspector)"
            ),
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - የጎንደር ዞን መምሪያ",
                department = "የግንባታ ፈቃድና የባለሙያዎች ምዝገባ ቡድን",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ጎንደር (Gondar) - አማራ ክልል",
                licenseOrCode = "AMH-GDR-CNST-2026/03",
                officerInCharge = "ኢንጂነር ሙሉጌታ አሰፋ (Zonal Lead Engineer)"
            ),
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - የደሴና ደቡብ ወሎ መምሪያ",
                department = "የመሠረተ ልማት ጥራት ኦዲትና ቁጥጥር ክፍል",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ደሴ (Dessie) - አማራ ክልል",
                licenseOrCode = "AMH-DSS-CNST-2026/04",
                officerInCharge = "ኢንጂነር ፋሲል ታደሰ (Zonal Regulatory Auditor)"
            ),
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - የደብረ ማርቆስና ምስራቅ ጎጃም መምሪያ",
                department = "የሳይት ኢንስፔክሽንና የደህንነት ማረጋገጫ ክፍል",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ደብረ ማርቆስ - አማራ ክልል",
                licenseOrCode = "AMH-DM-CNST-2026/05",
                officerInCharge = "ኢንጂነር ተስፋዬ ተክሌ (Lead Inspector)"
            ),
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - የደብረ ብርሃንና ሰሜን ሸዋ መምሪያ",
                department = "የኢንዱስትሪና ህንፃ ግንባታ ቁጥጥር መምሪያ",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ደብረ ብርሃን - አማራ ክልል",
                licenseOrCode = "AMH-DB-CNST-2026/06",
                officerInCharge = "ኢንጂነር ዮናስ አለሙ (Industrial Inspector)"
            ),
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - የወልዲያና ሰሜን ወሎ መምሪያ",
                department = "የመልሶ ግንባታና ጥራት ቁጥጥር ቡድን",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ወልዲያ - አማራ ክልል",
                licenseOrCode = "AMH-WLD-CNST-2026/07",
                officerInCharge = "ኢንጂነር ጌታቸው ካሳ (Lead Engineer)"
            ),
            InstitutionProfile(
                name = "የአማራ ክልል ኮንስትራክሽን ባለስልጣን - የደብረ ታቦርና ደቡብ ጎንደር መምሪያ",
                department = "የግንባታ ክትትልና የቁሳቁስ ላቦራቶሪ ፍተሻ ቡድን",
                type = "የቁጥጥርና ፈቃድ ሰጪ ባለስልጣን (Regional Regulatory Authority)",
                regionOrCity = "ደብረ ታቦር - አማራ ክልል",
                licenseOrCode = "AMH-DT-CNST-2026/08",
                officerInCharge = "ኢንጂነር ሳሙኤል ወርቁ (Materials & QA Engineer)"
            ),
            InstitutionProfile(
                name = "የቲሊሊ ከተማ ኮንስትራክሽን ጽሕፈት ቤት (ምዕራብ ጎጃም)",
                department = "ግንባታ ክትትልና ቁጥጥር ቡድን",
                type = "የከተማ አስተዳደር ጽሕፈት ቤት (Municipal Office)",
                regionOrCity = "ቲሊሊ - ምዕራብ ጎጃም / አማራ ክልል",
                licenseOrCode = "AMH-TIL-CNST-2026/89",
                officerInCharge = "በላይነህ የሻለም (Civil Engineer)"
            ),
            InstitutionProfile(
                name = "የኮንስትራክሽን አማካሪ መሀንዲሶች ጥምረት (Consultancy Pool)",
                department = "የዲዛይን ምርመራና ሳይት ቁጥጥር ዘርፍ",
                type = "አማካሪ መሀንዲሶች ድርጅት (Engineering Consultancy)",
                regionOrCity = "አማራ ክልልና አገር አቀፍ",
                licenseOrCode = "MUI-CONS-AMH-2026",
                officerInCharge = "ኢንጂነር ሀይሌ ገብሬ (Lead Design Consultant)"
            ),
            InstitutionProfile(
                name = "የኮንስትራክሽን ጠቅላላ ተቋራጮች ማህበር (Contractors Pool)",
                department = "የግንባታ ስራዎችና ፕሮጀክት ማኔጅመንት ክፍል",
                type = "ጠቅላላ ተቋራጭ (General Contractor - GC)",
                regionOrCity = "አማራ ክልልና አገር አቀፍ",
                licenseOrCode = "MUI-GC-AMH-2026",
                officerInCharge = "ኢንጂነር ብርሃኑ ከበደ (Project Director)"
            )
        )

        fun load(context: Context): InstitutionProfile {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val default = PRESET_ORGANIZATIONS[0]
            return InstitutionProfile(
                name = prefs.getString(KEY_NAME, default.name) ?: default.name,
                department = prefs.getString(KEY_DEPT, default.department) ?: default.department,
                type = prefs.getString(KEY_TYPE, default.type) ?: default.type,
                regionOrCity = prefs.getString(KEY_REGION, default.regionOrCity) ?: default.regionOrCity,
                licenseOrCode = prefs.getString(KEY_CODE, default.licenseOrCode) ?: default.licenseOrCode,
                officerInCharge = prefs.getString(KEY_OFFICER, default.officerInCharge) ?: default.officerInCharge,
                contactPhone = prefs.getString(KEY_PHONE, default.contactPhone) ?: default.contactPhone,
                contactEmail = prefs.getString(KEY_EMAIL, default.contactEmail) ?: default.contactEmail,
                officialMotto = prefs.getString(KEY_MOTTO, default.officialMotto) ?: default.officialMotto
            )
        }

        fun save(context: Context, profile: InstitutionProfile) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_NAME, profile.name)
                .putString(KEY_DEPT, profile.department)
                .putString(KEY_TYPE, profile.type)
                .putString(KEY_REGION, profile.regionOrCity)
                .putString(KEY_CODE, profile.licenseOrCode)
                .putString(KEY_OFFICER, profile.officerInCharge)
                .putString(KEY_PHONE, profile.contactPhone)
                .putString(KEY_EMAIL, profile.contactEmail)
                .putString(KEY_MOTTO, profile.officialMotto)
                .apply()
        }
    }
}
