package com.example.data

/**
 * Universal Construction Regulatory System
 * Covers Ethiopian National Laws (incorporating the new Ethiopian Building Proclamation No. 1356/2017 ዓ.ም),
 * Ethiopian Building Code Standards (EBCS 1-11 / ES EN),
 * and International Building Codes & Standards (FIDIC, IBC, Eurocode, ISO, OSHA, ACI, ASTM)
 * for both Office Works and Site Works.
 */

enum class JurisdictionScope(val labelAm: String, val labelEn: String) {
    ETHIOPIA_NATIONAL("የኢትዮጵያ ፌዴራል (አዋጅ 1356/2017 & EBCS)", "Ethiopia Federal (Proc 1356/2017 & EBCS)"),
    INTERNATIONAL_GLOBAL("ዓለም አቀፍ (FIDIC, IBC, Eurocode, ISO)", "International / Global (FIDIC, IBC, Eurocode)"),
    AFRICAN_UNION("የአፍሪካ ቀንድና አህጉራዊ (AU / COMESA)", "Pan-African & Regional (AU / COMESA)"),
    MIDDLE_EAST_GCC("መካከለኛው ምስራቅና ገልፍ (GCC / Dubai/SBC)", "Middle East & GCC (GCC, SBC, Dubai Code)"),
    EUROPE_UK("አውሮፓና ብሪታንያ (Eurocodes & BS EN)", "Europe & UK (Eurocodes EN, BS)")
}

enum class WorkDomain(val labelAm: String, val labelEn: String) {
    ALL("ሁሉም (All)", "All Domains"),
    OFFICE_WORKS("የቢሮ ስራዎች (Office Works)", "Office & Design Works"),
    SITE_WORKS("የሳይት ስራዎች (Site Works)", "Site & Field Works"),
    CONTRACT_LEGAL("የውልና ሕግ አስተዳደር (Legal & Contracts)", "Contractual & Legal Administration")
}

data class LawRegulationItem(
    val id: String,
    val codeOrRef: String,
    val titleAm: String,
    val titleEn: String,
    val jurisdiction: JurisdictionScope,
    val workDomain: WorkDomain,
    val category: String, // "PROCLAMATION", "CODE_STANDARD", "HSE", "CONTRACT", "PERMIT"
    val summaryAm: String,
    val summaryEn: String,
    val mandatoryRequirements: List<String>,
    val officeWorksGuidelines: List<String>,
    val siteWorksGuidelines: List<String>,
    val penaltyOrRisk: String,
    val internationalEquivalent: String
)

data class ComplianceCheckItem(
    val id: String,
    val taskTitleAm: String,
    val taskTitleEn: String,
    val governingLawRef: String,
    val domain: WorkDomain,
    val descriptionAm: String,
    val penaltyIfIgnored: String,
    var isCompliant: Boolean = false,
    var remarks: String = ""
)

object RegulatoryLawRepository {

    val ALL_LAWS_AND_REGULATIONS: List<LawRegulationItem> = listOf(
        // 1. New Ethiopian Building Proclamation 1356/2017
        LawRegulationItem(
            id = "eth-proc-1356-2017",
            codeOrRef = "አዋጅ ቁጥር 1356/2017 ዓ.ም (Proclamation No. 1356/2024)",
            titleAm = "አዲሱ የኢትዮጵያ ሕንፃ አዋጅ ቁጥር 1356/2017 ዓ.ም",
            titleEn = "New Ethiopian Building Proclamation No. 1356/2017 (2024/2025)",
            jurisdiction = JurisdictionScope.ETHIOPIA_NATIONAL,
            workDomain = WorkDomain.ALL,
            category = "PROCLAMATION",
            summaryAm = "የቀድሞውን አዋጅ 624/2001 በመተካት የወጣ ዘመናዊ አዋጅ ሲሆን፣ የህንፃዎች ምድብ (Category A, B, C, Mega)፣ ዲጂታል የግንባታ ፈቃድ፣ የአካል ጉዳተኞች ምቹነት፣ የአካባቢ ጥበቃና የጋራ ኃላፊነትን (Joint & Several Liability) በዝርዝር ይደነግጋል።",
            summaryEn = "Repealing and replacing Proclamation 624/2001, this modern statutory framework establishes building categories (A, B, C, Mega), digital permitting, mandatory accessibility for persons with disabilities, green building norms, and strict joint liability.",
            mandatoryRequirements = listOf(
                "ማንኛውም ግንባታ ከመጀመሩ በፊት በከተማው ወይም በፌዴራል ባለስልጣን የፀደቀ ህጋዊ የግንባታ ፈቃድ (Building Permit) መያዝ ግዴታ ነው።",
                "ምድብ ሀ (Category A: ≤2 ፎቅ፣ ስፓን ≤7m)፣ ምድብ ለ (Category B: 3-8 ፎቅ፣ ስፓን ≤12m)፣ ምድብ ሐ (Category C: >8 ፎቅ፣ ጥልቅ ምድር ቤት) እና ሜጋ ፕሮጀክቶች መመደብ አለባቸው።",
                "የአካል ጉዳተኞች መወጣጫ ራምፕ (1:12 ምጣኔ)፣ ሊፍትና መጸዳጃ ቤቶች በሁሉም የህዝብ አገልግሎት ህንፃዎች ውስጥ ማካተት አስገዳጅ ነው።",
                "የዲዛይን አዘጋጅ፣ አማካሪ፣ ተቋራጭና ባለቤት ለህንፃው መዋቅራዊ ጥንካሬና ደህንነት የጋራና የግል የህግ ኃላፊነት (Joint and Several Liability) አለባቸው።",
                "የግንባታ ስራ ተጠናቆ የመጠቀሚያ ፈቃድ (Occupancy Permit) ሳይገኝ ህንፃውን ለአገልግሎት ማዋል በጥብቅ የተከለከለ ነው።"
            ),
            officeWorksGuidelines = listOf(
                "የአርክቴክቸር፣ ስትራክቸር፣ ሳኒታሪ፣ ኤሌክትሪካልና የእሳት ደህንነት ዲዛይኖች በአዲሱ የEBCS/ES EN ኮድ መሠረት መዘጋጀታቸውን ማረጋገጥ።",
                "የአፈር ምርመራ (Geotechnical Investigation Report) ለምድብ ለ እና ሐ ህንፃዎች በባለስልጣኑ ማረጋገጫ ማግኘት።",
                "ዲጂታል ፕላን ማጽደቂያ ሰነዶችንና የባለሙያዎችን የታደሰ ፈቃድና የCPD ማስረጃ ማዘጋጀት።",
                "የሙያ ዋስትና መድን (Professional Indemnity Insurance) ፖሊሲ ማቅረብ።"
            ),
            siteWorksGuidelines = listOf(
                "በአዋጅ 1356/2017 የተደነገጉትን 7 የሳይት ፍተሻ ደረጃዎች (ቁፋሮ፣ መሰረት፣ ምሶሶ፣ ስላብ፣ መዋቅር፣ አገልግሎቶች፣ ማጠናቀቂያ) ማካሄድ።",
                "የሳይት ማጠርያ (Hoarding fence) ቁመት ቢያንስ 2.5 ሜትር መሆኑንና የደህንነት ማስጠንቀቂያ ቦርዶች መለጠፋቸውን ማረጋገጥ።",
                "ኮንክሪት ከመሞላቱ በፊት የቅድመ-ኮንክሪት ፈቃድ (Pre-pour Inspection Permit) በጽሁፍ መፈረም አለበት።",
                "ለሰራተኞች ሙሉ የደህንነት አልባሳት (PPE - ቆብ፣ ጫማ፣ አንጸባራቂ ልብስ፣ ማሰሪያ) ማቅረብ አስገዳጅ ነው።"
            ),
            penaltyOrRisk = "ያለፈቃድ የተጀመረ ግንባታ ወዲያውኑ ይታገዳል፣ እንዲፈርስ ይደረጋል፤ ጥፋተኛውም እስከ 1,000,000 ብር የገንዘብ መቀጮ እና ከ5 እስከ 20 ዓመት ፅኑ እስራት ይጠብቀዋል።",
            internationalEquivalent = "International Building Code (IBC 2024 / 2021) & UK Building Safety Act 2022"
        ),

        // 2. FIDIC Conditions of Contract Suite
        LawRegulationItem(
            id = "fidic-suite-contracts",
            codeOrRef = "FIDIC Suite 2017 / 1999 (Red, Yellow, Silver Books)",
            titleAm = "ዓለም አቀፍ የኮንስትራክሽን ውል ስምምነቶች (FIDIC Contracts)",
            titleEn = "FIDIC Conditions of Contract for Construction (Red, Yellow, Silver Books)",
            jurisdiction = JurisdictionScope.INTERNATIONAL_GLOBAL,
            workDomain = WorkDomain.CONTRACT_LEGAL,
            category = "CONTRACT",
            summaryAm = "በአለም አቀፍ የኢንጂነሮች ፌዴሬሽን (FIDIC) የተዘጋጀ የኮንስትራክሽን ስራዎች መደበኛ የውል ማዕቀፍ ሲሆን፣ በአሰሪ፣ አማካሪ (The Engineer) እና ተቋራጭ መካከል ያለውን መብት፣ ኃላፊነትና የይገባኛል ጥያቄዎች ይገዛል።",
            summaryEn = "The global standard suite of construction contracts published by the International Federation of Consulting Engineers (FIDIC), governing rights, variations, interim payments, claims, and dispute adjudication.",
            mandatoryRequirements = listOf(
                "Clause 4.2: የቅድመ-ክፍያና የስራ አፈፃፀም ዋስትና (Performance Security 10%) በ28 ቀናት ውስጥ መቅረብ አለበት።",
                "Clause 6.9: ቁልፍ የሰው ኃይል (Key Personnel) ያለ አማካሪው ፈቃድ መቀየር የተከለከለ ነው።",
                "Clause 13: የስራ ለውጥ ትዕዛዞች (Variation Orders) በጽሁፍ ብቻ የሚሰጡና የዋጋ ማስተካከያ የሚደረግላቸው መሆኑ።",
                "Clause 14: ጊዜያዊ የክፍያ ሰርተፍኬት (Interim Payment Certificate - IPC) በየወሩ በዝርዝር መለካት አለበት።",
                "Clause 20/21: የይገባኛል ጥያቄ (Claims) ከተፈጠረበት ቀን ጀምሮ በ28 ቀናት ውስጥ በጽሁፍ ካልቀረበ ተቀባይነት ያጣል።"
            ),
            officeWorksGuidelines = listOf(
                "የውል ሰነዶችን ቅደም ተከተል (Order of Priority of Contract Documents) ማክበር።",
                "የጊዜ ማራዘሚያ (Extension of Time - EOT) እና የዋጋ ጭማሪ ግምገማዎችን በFIDIC መመሪያ ማዘጋጀት።",
                "የክፍያ ሰነዶች (IPC) በቦታው ላይ ከተሰራው ስራ (BOQ Measurement) ጋር መመሳከራቸውን ማረጋገጥ።"
            ),
            siteWorksGuidelines = listOf(
                "የዕለት ተዕለት የሳይት ማስታወሻ ደብተር (Daily Site Diary & Weather Record) በየቀኑ በሁለቱም ወገን መፈረም።",
                "የእቃዎች ጥራት ምርመራ ሰርተፍኬት (Material Test Certificates) ወደ ሳይት ከመግባታቸው በፊት ማረጋገጥ።",
                "የደህንነት፣ የጤናና የአካባቢ ጥበቃ (HSE) እቅድ በሳይቱ ላይ በየቀኑ መተግበሩን መከታተል።"
            ),
            penaltyOrRisk = "የ28 ቀናት የማሳወቂያ የጊዜ ገደብ ካለፈ የይገባኛል መብት ይሰረዛል (Time-barred claim)፤ የዘገየ ስራ ላይ የቀን የቅጣት ኪሳራ (Liquidated Delay Damages) ይጣላል።",
            internationalEquivalent = "World Bank SBDW, PPA Standard Bidding Documents, NEC4 Contracts"
        ),

        // 3. International Building Code (IBC 2024 / 2021)
        LawRegulationItem(
            id = "int-ibc-2024",
            codeOrRef = "IBC 2024 / 2021 (International Building Code)",
            titleAm = "ዓለም አቀፍ የህንፃ ኮድ (International Building Code - IBC)",
            titleEn = "International Building Code (IBC 2024 / 2021 Edition)",
            jurisdiction = JurisdictionScope.INTERNATIONAL_GLOBAL,
            workDomain = WorkDomain.OFFICE_WORKS,
            category = "CODE_STANDARD",
            summaryAm = "በአሜሪካና በበርካታ ዓለም አቀፍ ሀገራት ተቀባይነት ያለው ዋና የህንፃ ዲዛይንና ደህንነት ኮድ ሲሆን፣ የእሳት ጥበቃ፣ የመውጫ መንገዶች (Means of Egress) እና መዋቅራዊ ሸክሞችን ይገዛል።",
            summaryEn = "Model building code developed by the International Code Council (ICC), widely referenced globally for fire protection, life safety, egress width, structural loads, and accessibility.",
            mandatoryRequirements = listOf(
                "ምዕራፍ 3 እና 5፡ የህንፃ አገልግሎት ምድብ (Assembly, Business, Educational, Factory, Hazardous, Residential)።",
                "ምዕራፍ 7 እና 9፡ የእሳት መቋቋም ሰዓታት (1 to 4 hour Fire-resistance rating) እና አውቶማቲክ ስፕሪንክለር ስርዓት።",
                "ምዕራፍ 10፡ የአደጋ ጊዜ መውጫ በሮች፣ ደረጃዎችና ኮሪደሮች ስፋት በሰው ብዛት ተሰልቶ መዘጋጀት አለበት።",
                "ምዕራፍ 16፡ መዋቅራዊ ሸክሞች (Dead, Live, Roof Live, Wind, Earthquake, Soil Lateral Pressures)።"
            ),
            officeWorksGuidelines = listOf(
                "የአደጋ መውጫ ደረጃዎች ርቀት (Travel Distance to Exit) ከገደቡ (≤60m ወይም በስፕሪንክለር ≤75m) አለመብለጡን ማረጋገጥ።",
                "የህንፃ ከፍታና የወለል ስፋት ገደብ (Allowable Building Height and Floor Area) ስሌት።",
                "የመስታወትና የፊት ለፊት ክላዲንግ (Glazing & Cladding) ንፋስ መቋቋም ስሌት ማካተት።"
            ),
            siteWorksGuidelines = listOf(
                "የእሳት መከላከያ ቀለምና ፕላስተር (Fireproofing coating on steel & joints) ውፍረት መፈተሽ።",
                "የአደጋ ጊዜ መውጫ በሮች ወደ ውጭ የሚከፈቱና በፓኒክ ባር (Panic hardware) የተገጠሙ መሆናቸውን ማረጋገጥ።"
            ),
            penaltyOrRisk = "የአደጋ መውጫና የእሳት መስፈርት ካልተሟላ ህንፃው የመጠቀሚያ ፈቃድ (Certificate of Occupancy) አይሰጠውም።",
            internationalEquivalent = "NFPA 101 Life Safety Code, Eurocode Fire Parts"
        ),

        // 4. Eurocodes (ES EN 1990 - 1999) & EBCS Suite
        LawRegulationItem(
            id = "eurocodes-esen-ebcs",
            codeOrRef = "ES EN 1990 - 1999 / EBCS 1 - 11",
            titleAm = "የኢትዮጵያና አውሮፓ ህንፃ ዲዛይን ኮዶች (Eurocodes / EBCS)",
            titleEn = "Ethiopian & European Structural Eurocodes (ES EN 1990-1999 & EBCS 1-11)",
            jurisdiction = JurisdictionScope.ETHIOPIA_NATIONAL,
            workDomain = WorkDomain.ALL,
            category = "CODE_STANDARD",
            summaryAm = "በኢትዮጵያ ደረጃዎች ኤጀንሲ የተተገበረው የአውሮፓና የኢትዮጵያ ጥምር ኮድ ሲሆን፤ መዋቅራዊ ኮንክሪት (EBCS-2/EN 1992)፣ ብረት (EBCS-3/EN 1993)፣ አፈር መካኒክስ (EBCS-7/EN 1997) እና የመሬት መንቀጥቀጥ (EBCS-8/EN 1998) ያጠቃልላል።",
            summaryEn = "Unified structural design standards combining Ethiopian Building Code Standards and Eurocodes for structural concrete, steel, geotechnics, and earthquake-resistant detailing.",
            mandatoryRequirements = listOf(
                "ES EN 1990: የዲዛይን ዘመን ገደብ (Design working life: 50 years for buildings, 100 years for bridges/monumental)።",
                "ES EN 1992 (EBCS-2): የኮንክሪት ሽፋን ውፍረት (Cover: 25mm for slabs, 35-40mm for beams/columns, 50-75mm for foundations)።",
                "ES EN 1997 (EBCS-7): የአፈር የመሸከም አቅም (Bearing Capacity) እና ማረጋገጫ ፋክተሮች (Partial safety factors)።",
                "ES EN 1998 (EBCS-8): የመሬት መንቀጥቀጥ ጥበቃና የብረት መቆላለፊያ (Seismic zones 1-4, Ductile detailing with 135° hooks)።"
            ),
            officeWorksGuidelines = listOf(
                "የኮንክሪት ጥንካሬ ክፍል C25/30 ወይም C30/37 በዲዛይን ስሌት ላይ መለየት።",
                "የብረት ዲያሜትርና የመተላለፊያ ርዝመት (Lap length = 40 to 50 x bar diameter) ማስላት።",
                "የንፋስና የሴይስሚክ ሸክሞች በ3D ሞዴሊንግ ሶፍትዌር (ETABS/SAP2000) ማረጋገጥ።"
            ),
            siteWorksGuidelines = listOf(
                "የኮንክሪት ሽፋን መደገፊያ ስፔሰሮች (Concrete spacer blocks) በየ 1 ሜትር መዘርጋታቸውን ማረጋገጥ።",
                "የምሶሶ ብረት ማሰሪያዎች (Stirrups) በጫፎች ላይ በ 10 ሳ.ሜ መቀራረባቸውንና 135 ዲግሪ መታጠፋቸውን ማረጋገጥ።",
                "ኮንክሪት ከተሞላ በኋላ ቢያንስ ለ 7 ቀናት የማያቋርጥ የውሃ እርጥበት ማግኘት አለበት (Curing)።"
            ),
            penaltyOrRisk = "መዋቅራዊ ስህተቶች የህንፃ ስንጥቅ፣ ማዘንበል ወይም ሙሉ በሙሉ የመፍረስ አደጋ ያስከትላሉ።",
            internationalEquivalent = "ACI 318-19, BS 8110, ASCE 7-22"
        ),

        // 5. OSHA 1926 & ISO 45001 Site Safety
        LawRegulationItem(
            id = "hse-osha-iso45001",
            codeOrRef = "OSHA 29 CFR 1926 & ISO 45001:2018",
            titleAm = "የኮንስትራክሽን ሳይት ጤና፣ ደህንነትና አካባቢ ጥበቃ (OSHA & ISO 45001)",
            titleEn = "Construction Health, Safety & Environment Standards (OSHA 1926 & ISO 45001)",
            jurisdiction = JurisdictionScope.INTERNATIONAL_GLOBAL,
            workDomain = WorkDomain.SITE_WORKS,
            category = "HSE",
            summaryAm = "በግንባታ ሳይቶች ላይ የሰራተኞችን ሕይወት ከአደጋ ለመጠበቅ፣ ከመውደቅ መከላከያ (Fall Protection)፣ የመቆፈሪያ አፈር መደርመስ ጥበቃ (Trench Shoring)፣ የክሬንና ስካፎልዲንግ ደህንነትን የሚደነግግ አለም አቀፍ ስታንዳርድ ነው።",
            summaryEn = "Global construction safety and occupational health requirements governing fall protection over 1.8m/2m, excavation trench shoring, certified scaffolding, personal protective gear, and incident logs.",
            mandatoryRequirements = listOf(
                "ከ 1.8 ሜትር (6 ጫማ) በላይ ከፍታ ላይ ለሚሰራ ማንኛውም ሰው የመውደቅ መከላከያ ማሰሪያ (Full-body safety harness) ግዴታ ነው።",
                "ከ 1.5 ሜትር በላይ ጥልቅ ቁፋሮዎች የአፈር መደርመስ መከላከያ ድጋፍ (Shoring or benching) ሊኖራቸው ይገባል።",
                "ስካፎልዲንግ በሰለጠነ ባለሙያ መፈተሽና አረንጓዴ የፍተሻ ካርድ (Green Safety Tag) መለጠፍ አለበት።",
                "የአደጋ መከላከያ የመጀመሪያ እርዳታ ሳጥን (First Aid Box) እና የሰለጠነ የጤና መኮንን በሳይት መኖር አለበት።"
            ),
            officeWorksGuidelines = listOf(
                "የሳይት ደህንነት እቅድ (Site Specific HSE Plan) አዘጋጅቶ ለአማካሪው ማቅረብ።",
                "የአደጋ ስጋት ትንተና (Job Safety Analysis - JSA & Risk Assessment Matrix) ማዘጋጀት።",
                "የአደጋ መድን (Workmen's Compensation Insurance) ፖሊሲ ለሁሉም ሰራተኞች ማስገባት።"
            ),
            siteWorksGuidelines = listOf(
                "በየማለዳው የ10 ደቂቃ የደህንነት ውይይት (Daily Tool-Box Talk) ለሁሉም ሰራተኞች መስጠት።",
                "የኤሌክትሪክ ኬብሎችና ማሽነሪዎች የኤሌክትሪክ ፍንጣቂ መከላከያ (GFCI/ELCB) የተገጠመላቸው መሆኑን መፈተሽ።",
                "የአደጋ ክስተት ምዝገባ ደብተር (Accident/Near-Miss Incident Log) መያዝ።"
            ),
            penaltyOrRisk = "የሰራተኛ ሞት ወይም ከባድ ጉዳት በወንጀል ሕግ የሚያስጠይቅ ከመሆኑም በላይ ፕሮጀክቱ ወዲያውኑ በባለስልጣን ይታገዳል።",
            internationalEquivalent = "Ethiopian Labour Proclamation No. 1156/2011 & UK CDM 2015"
        ),

        // 6. Quality Control & ASTM/EN Material Testing
        LawRegulationItem(
            id = "qc-astm-testing",
            codeOrRef = "ASTM C39 / EN 12390 & ISO 9001:2015",
            titleAm = "የግንባታ እቃዎች የጥራት ቁጥጥርና የላቦራቶሪ ምርመራ (ASTM & ISO 9001)",
            titleEn = "Construction Materials Testing & Quality Assurance (ASTM, EN & ISO 9001)",
            jurisdiction = JurisdictionScope.INTERNATIONAL_GLOBAL,
            workDomain = WorkDomain.SITE_WORKS,
            category = "CODE_STANDARD",
            summaryAm = "የኮንክሪት የኩብ ጥንካሬ (Cube/Cylinder Test)፣ የብረት የመለጠጥ አቅም (Tensile Test)፣ የአፈር ሙላት ድግግሞሽ (Compaction Test) እና የሲሚንቶ ጥራትን በሳይንሳዊ ላቦራቶሪ መመርመርያ አለም አቀፍ ደንብ ነው።",
            summaryEn = "Statutory testing protocols for concrete compressive strength at 7 and 28 days, steel reinforcement tensile yield tests, soil compaction field density tests, and slump tests.",
            mandatoryRequirements = listOf(
                "ለእያንዳንዱ 50 ኪዩቢክ ሜትር ኮንክሪት ወይም ለእያንዳንዱ ወለል ቢያንስ 6 ናሙና ኩቦች (Cubes) መወሰድ አለባቸው (3ቱ ለ7 ቀን፣ 3ቱ ለ28 ቀን)።",
                "የ28ኛ ቀን የኮንክሪት ጥንካሬ ከዲዛይን ጥንካሬው (fck) በታች መሆን የለበትም።",
                "የብረት ጥንካሬ (Yield Strength) በቤተ-ሙከራ ተፈትሾ ቢያንስ 500 MPa (Grade 60) መሆኑ በሰርተፍኬት መረጋገጥ አለበት።",
                "የመሰረት አፈር ሙላት (Backfill Compaction) ቢያንስ 95% Modified Proctor Density መድረስ አለበት።"
            ),
            officeWorksGuidelines = listOf(
                "የጥራት ቁጥጥር እቅድ (Quality Assurance Plan & Inspection Test Plan - ITP) ማዘጋጀት።",
                "የተፈቀዱ የኮንክሪት ማቀነባበሪያ ሬሾዎች (Concrete Mix Design Approvals) ማጽደቅ።",
                "የላቦራቶሪ ውጤቶችን መዝግቦ የስታቲስቲክስ ግምገማ ማዘጋጀት።"
            ),
            siteWorksGuidelines = listOf(
                "ኮንክሪት ከመሞላቱ በፊት የስለምፕ ፍተሻ (Slump Test: 80 - 120 mm) በቦታው ላይ ማከናወን።",
                "የኮንክሪት ናሙና ኩቦችን በጥላ ስር ለ24 ሰዓት ካቆዩ በኋላ በውሃ ገንዳ ውስጥ በ20°C ሙቀት ማቆየት።",
                "የተሰባበሩ፣ የዛጉ ወይም የተጣመሙ ብረቶችን ከሳይት ማስወገድ።"
            ),
            penaltyOrRisk = "የላቦራቶሪ ፍተሻ ያላለፈ ኮንክሪት እንዲፈርስ ይደረጋል (Rejection & Demolition)፤ ተቋራጩም ወጪውን ይሸፍናል።",
            internationalEquivalent = "ES ISO 12390, ACI 301, BS EN 206"
        ),

        // 7. Ethiopian Building Regulation 243/2003 & 2017 Amendments
        LawRegulationItem(
            id = "eth-reg-243-2003",
            codeOrRef = "ደንብ ቁጥር 243/2003 ዓ.ም (Council of Ministers Regulation No. 243/2011)",
            titleAm = "የኢትዮጵያ የህንፃ የሚኒስትሮች ምክር ቤት ደንብ ቁጥር 243/2003",
            titleEn = "Ethiopian Building Regulation No. 243/2011 and 2017 Directives",
            jurisdiction = JurisdictionScope.ETHIOPIA_NATIONAL,
            workDomain = WorkDomain.ALL,
            category = "PERMIT",
            summaryAm = "የህንፃ አዋጅን ለማስፈጸም የወጣ ዝርዝር ደንብ ሲሆን፤ የቦታ ሽንሻኖ (Setbacks)፣ የህንፃ ከፍታ (FAR/Building Height)፣ የመኪና ማቆሚያ ምጣኔ እና የፈቃድ አሰጣጥ የጊዜ ገደቦችን ይደነግጋል።",
            summaryEn = "Detailed ministerial regulation governing building setbacks, parking ratio allocations, floor area ratio (FAR), design review timelines, and permit fees across Ethiopian municipalities.",
            mandatoryRequirements = listOf(
                "የግንባታ ርቀት ከጎረቤት ይዞታ ድንበር ቢያንስ 2 ሜትር (ለዝቅተኛ ፎቆች) እና ለከፍተኛ ፎቆች በስሌት መራቅ አለበት።",
                "የመኪና ማቆሚያ ስፋት፡ ለእያንዳንዱ አፓርታማ ወይም ለተወሰነ የቢሮ ወለል ስፋት የተመደበ ፓርኪንግ መኖር ግዴታ ነው።",
                "የዝናብ ውሃ ማፋሰሻና የሳኒታሪ ፍሳሽ ከከተማው የቦይ መስመር ጋር በደንብ መያያዝ አለበት።",
                "የግንባታ ፈቃድ ማመልከቻ በ21 የሥራ ቀናት ውስጥ ምላሽ ማግኘት አለበት።"
            ),
            officeWorksGuidelines = listOf(
                "የከተማ ማስተር ፕላን እና የዞኒንግ (Zoning regulations) መስፈርቶችን በዲዛይን መከተል።",
                "የህንፃ አጠቃላይ የወለል ስፋት ምጣኔ (FAR) እና የይዞታ ሽፋን (Ground Coverage ≤70%) ማረጋገጥ።",
                "የኤሌክትሪክ ትራንስፎርመርና የጄኔሬተር ቦታ በፕላኑ ላይ መለየት።"
            ),
            siteWorksGuidelines = listOf(
                "የግንባታ አቅጣጫና ወሰን (Setback & Pegging verification) ከከተማው ቀያሽ ጋር በጋራ መፈተሽ።",
                "የህዝብ መንገድና የእግረኛ መሄጃን ባለመዝጋት የግንባታ እቃዎችን በይዞታው ውስጥ ብቻ ማከማቸት።"
            ),
            penaltyOrRisk = "የድንበር ርቀት የጣሰ ግንባታ ያለ ምንም ማካካሻ እንዲስተካከል ወይም እንዲፈርስ ይደረጋል።",
            internationalEquivalent = "Municipal Zoning Codes & International Planning Directives"
        ),

        // 8. Public Procurement & Asset Disposal Proclamation 649/2001 (PPA)
        LawRegulationItem(
            id = "eth-proc-ppa-649",
            codeOrRef = "አዋጅ ቁጥር 649/2001 (Public Procurement Proclamation 649/2009)",
            titleAm = "የመንግስት ግዥና ንብረት አስተዳደር አዋጅና መመሪያ (PPA Rules)",
            titleEn = "Public Procurement and Property Administration Directives (PPA)",
            jurisdiction = JurisdictionScope.ETHIOPIA_NATIONAL,
            workDomain = WorkDomain.CONTRACT_LEGAL,
            category = "CONTRACT",
            summaryAm = "በመንግስትና በህዝብ በጀት ለሚገነቡ ፕሮጀክቶች የጨረታ ሂደት፣ የውል አሰጣጥ፣ የተቋራጭ ክፍያ፣ የይገባኛል ክርክርና የውል ማቋረጥ ስርዓትን የሚመራ ዋና የፋይናንስና ህግ አዋጅ ነው።",
            summaryEn = "National procurement statute governing competitive bidding, standard contract conditions, milestone disbursement, retention money, and blacklisting of non-performing contractors.",
            mandatoryRequirements = listOf(
                "የጨረታ ማስከበሪያ ዋስትና (CPO / Bid Bond) ከ1-2% ማቅረብ።",
                "የስራ አፈፃፀም ዋስትና 10% (Performance Guarantee) በባንክ የተረጋገጠ መሆን አለበት።",
                "የቅድመ ክፍያ ዋስትና 100% ያልተገደበ የባንክ ዋስትና (Advance Payment Guarantee) ያስፈልጋል።",
                "የማቆያ ገንዘብ (Retention Money) 5% ተቀንሶ ግንባታው ተጠናቆ ከዋስትና ዘመን (Defects Liability Period) በኋላ ይመለሳል።"
            ),
            officeWorksGuidelines = listOf(
                "የጨረታ ሰነድ (Standard Bidding Document - SBD) ማዘጋጀት።",
                "የዋጋ ንረት ስሌት (Price Escalation Formula) በውሉ መሰረት በትክክል ማስላት።",
                "የማጠናቀቂያ ሰርተፍኬት (Substantial Completion Certificate) መረጃዎችን ማረጋገጥ።"
            ),
            siteWorksGuidelines = listOf(
                "የተጠናቀቁ ስራዎችን የጋራ መለካት (Joint Measurement) ከአሰሪው መሀንዲስ ጋር በየወሩ ማከናወን።",
                "የጉድለት ማረጋገጫ ዝርዝር (Snag List / Punch List) ሰርቶ ማጠናቀቅ።"
            ),
            penaltyOrRisk = "ውልን ያጓተተ ተቋራጭ በPPA ጥቁር መዝገብ ውስጥ ተካቶ ለዓመታት ከመንግስት ስራ ይታገዳል፤ ዋስትናውም ይወረሳል።",
            internationalEquivalent = "UNCITRAL Model Law on Public Procurement"
        )
    )

    // Master Checklist for Office Works
    val OFFICE_WORKS_CHECKLIST: List<ComplianceCheckItem> = listOf(
        ComplianceCheckItem(
            id = "off-1",
            taskTitleAm = "የአዲሱ ህንፃ አዋጅ 1356/2017 ምድብ ማረጋገጫ (Category A, B, C, Mega)",
            taskTitleEn = "Building Category Classification Verification",
            governingLawRef = "አዋጅ 1356/2017 ዓ.ም አንቀጽ 5",
            domain = WorkDomain.OFFICE_WORKS,
            descriptionAm = "የህንፃው ፎቅ ብዛት፣ የስፓን ርዝመት እና የታለመለት አገልግሎት ተፈትሾ ትክክለኛው ምድብ ተሰጥቶታል።",
            penaltyIfIgnored = "የፕላን ውድቅ መሆንና የፈቃድ መዘግየት።"
        ),
        ComplianceCheckItem(
            id = "off-2",
            taskTitleAm = "የመዋቅር ስሌት በEBCS-1/2 እና ES EN 1990/1992 መዘጋጀቱ",
            taskTitleEn = "Structural Calculations Audit per Eurocodes/EBCS",
            governingLawRef = "EBCS 1 & 2 / ES EN 1990-1992",
            domain = WorkDomain.OFFICE_WORKS,
            descriptionAm = "የሙት፣ ህያው፣ ንፋስና የመሬት መንቀጥቀጥ ሸክሞች በ3D ሞዴሊንግ ተሰልተው የመሸከም አቅም ማረጋገጫ ተሰጥቷል።",
            penaltyIfIgnored = "መዋቅራዊ ድክመትና የመፍረስ አደጋ።"
        ),
        ComplianceCheckItem(
            id = "off-3",
            taskTitleAm = "የአፈር ምርመራ (Geotechnical Soil Report) ማረጋገጫ",
            taskTitleEn = "Geotechnical Borehole & Bearing Capacity Report",
            governingLawRef = "EBCS 7 / አዋጅ 1356/2017",
            domain = WorkDomain.OFFICE_WORKS,
            descriptionAm = "የቦርሆል ቁፋሮ፣ የአፈር ጥንካሬ (Bearing Capacity) እና የከርሰ-ምድር ውሃ መጠን ተረጋግጧል።",
            penaltyIfIgnored = "የመሰረት መስመጥና ያልተስተካከለ ስንጥቅ (Differential Settlement)።"
        ),
        ComplianceCheckItem(
            id = "off-4",
            taskTitleAm = "የአካል ጉዳተኞች ተደራሽነትና የእሳት ደህንነት ፕላን",
            taskTitleEn = "Accessibility (1:12 Ramps, Lifts) & Fire Egress Plan",
            governingLawRef = "አዋጅ 1356/2017 አንቀጽ 28 & IBC Ch. 10",
            domain = WorkDomain.OFFICE_WORKS,
            descriptionAm = "የአካል ጉዳተኞች መወጣጫ ራምፕ፣ ሊፍት፣ የአደጋ መውጫ ደረጃዎችና የእሳት ማጥፊያ ስፕሪንክለር ተካተዋል።",
            penaltyIfIgnored = "የመጠቀሚያ ፈቃድ እገዳና የህግ ተጠያቂነት።"
        ),
        ComplianceCheckItem(
            id = "off-5",
            taskTitleAm = "የባለሙያዎች የታደሰ ፈቃድ፣ CPD ነጥብና የሙያ መድን (Indemnity Insurance)",
            taskTitleEn = "Professional License, CPD (30 hrs) & Indemnity Insurance",
            governingLawRef = "የከተማና መሰረተ ልማት መመሪያ & አዋጅ 1356/2017",
            domain = WorkDomain.OFFICE_WORKS,
            descriptionAm = "የዲዛይን አዘጋጅና አማካሪ መሀንዲሶች የታደሰ ፈቃድ፣ አመታዊ 30 የCPD ሰአታት እና የሙያ ዋስትና መድን አላቸው።",
            penaltyIfIgnored = "የፕላን ውድቅ መሆንና የዲሲፕሊን ቅጣት።"
        ),
        ComplianceCheckItem(
            id = "off-6",
            taskTitleAm = "የውል አስተዳደርና የዋስትና ሰነዶች (FIDIC Clause 4.2 / PPA)",
            taskTitleEn = "Performance & Advance Guarantees Verification",
            governingLawRef = "FIDIC Red Book / PPA 649",
            domain = WorkDomain.CONTRACT_LEGAL,
            descriptionAm = "የ10% የስራ አፈፃፀም ዋስትና እና የቅድመ ክፍያ የባንክ ዋስትናዎች በህጋዊ ባንክ ተረጋግጠው ተቀምጠዋል።",
            penaltyIfIgnored = "የፋይናንስ ኪሳራና የውል መቋረጥ አደጋ።"
        )
    )

    // Master Checklist for Site Works
    val SITE_WORKS_CHECKLIST: List<ComplianceCheckItem> = listOf(
        ComplianceCheckItem(
            id = "site-1",
            taskTitleAm = "የሳይት ማጠርያ (Hoarding ≥2.5m) እና የደህንነት ማስጠንቀቂያ ሰሌዳ",
            taskTitleEn = "Site Hoarding (≥2.5m) & Safety Signage Installation",
            governingLawRef = "አዋጅ 1356/2017 አንቀጽ 35 & OSHA 1926",
            domain = WorkDomain.SITE_WORKS,
            descriptionAm = "የሳይት አጥር ሙሉ በሙሉ ተዘግቷል፣ የህዝብ ደህንነት ተጠብቋል፣ የፕሮጀክቱ መረጃና ማስጠንቀቂያ ሰሌዳ ተለጥፏል።",
            penaltyIfIgnored = "የሳይት ስራ እገዳና የከተማ አስተዳደር ቅጣት።"
        ),
        ComplianceCheckItem(
            id = "site-2",
            taskTitleAm = "የሰራተኞች ሙሉ የደህንነት አልባሳት (PPE) እና የጠዋት ገለጻ (Toolbox Talk)",
            taskTitleEn = "Mandatory PPE Enforcement & Daily Safety Briefing",
            governingLawRef = "OSHA 1926 Subpart E / ISO 45001 / አዋጅ 1156/2011",
            domain = WorkDomain.SITE_WORKS,
            descriptionAm = "ሁሉም ሰራተኞች ቆብ፣ ጫማ፣ አንጸባራቂ ጃኬት ለብሰዋል፤ ከ1.8m በላይ ለሚሰሩ የደህንነት ገመድ (Harness) ታስሯል።",
            penaltyIfIgnored = "የአካል ጉዳት፣ ሞትና የወንጀል ክስ።"
        ),
        ComplianceCheckItem(
            id = "site-3",
            taskTitleAm = "የመሰረት ቁፋሮና የበለበሎ ጥራት ፍተሻ (Foundation Stage Audit)",
            taskTitleEn = "Foundation Excavation & Blinding Concrete Inspection",
            governingLawRef = "አዋጅ 1356/2017 አንቀጽ 32 (ደረጃ 1)",
            domain = WorkDomain.SITE_WORKS,
            descriptionAm = "የቁፋሮ ጥልቀት የተፈቀደው አፈር ላይ መድረሱና የበለበሎ ኮንክሪት በቅድሚያ መሞላቱ ተረጋግጧል።",
            penaltyIfIgnored = "ግንባታው እንዲቆም ትእዛዝ ይሰጣል።"
        ),
        ComplianceCheckItem(
            id = "site-4",
            taskTitleAm = "የቅድመ-ኮንክሪት ፍተሻና ፈቃድ (Pre-pour Concrete Casting Permit)",
            taskTitleEn = "Pre-Pour Reinforcement, Formwork & Cover Inspection",
            governingLawRef = "EBCS 2 / ES EN 1992 / አዋጅ 1356/2017",
            domain = WorkDomain.SITE_WORKS,
            descriptionAm = "የብረት መጠንና አሰካክ፣ የስፔሰር ብሎኮች (Cover 25-40mm)፣ የፎርምወርክ ጥንካሬና ጽዳት በአማካሪው ተፈርሟል።",
            penaltyIfIgnored = "ያለፈቃድ የተሞላ ኮንክሪት እንዲፈርስ ይደረጋል።"
        ),
        ComplianceCheckItem(
            id = "site-5",
            taskTitleAm = "የኮንክሪት ስለምፕና የኩብ ናሙና አወሳሰድ (ASTM C39 / EN 12390)",
            taskTitleEn = "Slump Test (80-120mm) & Compressive Cube Sampling",
            governingLawRef = "ASTM C39 / ES ISO 12390",
            domain = WorkDomain.SITE_WORKS,
            descriptionAm = "የስለምፕ ፍተሻ ተካሂዷል፣ 6 ናሙና ኩቦች ተወስደው ለ7 እና 28 ቀን ምርመራ በውሃ ገንዳ ተቀምጠዋል።",
            penaltyIfIgnored = "የጥራት አለመታወቅና የክፍያ እገዳ።"
        ),
        ComplianceCheckItem(
            id = "site-6",
            taskTitleAm = "የቀን ሳይት ማስታወሻ ደብተር (Daily Site Diary & Weather Record)",
            taskTitleEn = "Daily Site Diary, Weather, Manpower & Plant Records",
            governingLawRef = "FIDIC Clause 4.21 / አዋጅ 1356/2017",
            domain = WorkDomain.SITE_WORKS,
            descriptionAm = "የአየር ሁኔታ፣ የሰራተኛ ብዛት፣ የገቡ እቃዎች፣ የተሰሩ ስራዎችና የአማካሪው ትዕዛዞች በየቀኑ ተመዝግበዋል።",
            penaltyIfIgnored = "የይገባኛል ጥያቄ ውድቅ መሆንና የውል ክርክር።"
        )
    )
}
