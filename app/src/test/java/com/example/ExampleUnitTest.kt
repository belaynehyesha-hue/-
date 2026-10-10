package com.example

import com.example.ui.screens.IndustryFormatValidators
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for IndustryFormatValidators validating Ethiopian and international
 * construction regulatory formats.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFullNameValidation() {
    assertTrue(IndustryFormatValidators.validateFullName("ዳዊት ሃይሉ").isValid)
    assertTrue(IndustryFormatValidators.validateFullName("Eng. Dawit Hailu Woldemariam").isValid)
    assertFalse(IndustryFormatValidators.validateFullName("ዳዊት").isValid)
    assertFalse(IndustryFormatValidators.validateFullName("").isValid)
  }

  @Test
  fun testPhoneValidation() {
    assertTrue(IndustryFormatValidators.validatePhone("0911234567").isValid)
    assertTrue(IndustryFormatValidators.validatePhone("+251911234567").isValid)
    assertTrue(IndustryFormatValidators.validatePhone("0712345678").isValid)
    assertFalse(IndustryFormatValidators.validatePhone("12345").isValid)
    assertFalse(IndustryFormatValidators.validatePhone("0811234567").isValid)
  }

  @Test
  fun testEmailValidation() {
    assertTrue(IndustryFormatValidators.validateEmail("engineer@construction.et").isValid)
    assertTrue(IndustryFormatValidators.validateEmail("").isValid) // Optional field
    assertFalse(IndustryFormatValidators.validateEmail("invalid-email").isValid)
  }

  @Test
  fun testNationalFaydaIdValidation() {
    assertTrue(IndustryFormatValidators.validateNationalId("ET-FAN-98410294").isValid)
    assertTrue(IndustryFormatValidators.validateNationalId("FAN-1029-3847-5610").isValid)
    assertFalse(IndustryFormatValidators.validateNationalId("123").isValid)
    assertFalse(IndustryFormatValidators.validateNationalId("").isValid)
  }

  @Test
  fun testLicenseNumberValidation() {
    assertTrue(IndustryFormatValidators.validateLicenseNumber("MUI/PPE/2026/04821").isValid)
    assertTrue(IndustryFormatValidators.validateLicenseNumber("ECA/PE/2025/10492").isValid)
    assertTrue(IndustryFormatValidators.validateLicenseNumber("ETH-ENG-78291").isValid)
    assertFalse(IndustryFormatValidators.validateLicenseNumber("invalid-license").isValid)
    assertFalse(IndustryFormatValidators.validateLicenseNumber("").isValid)
  }

  @Test
  fun testInsurancePolicyValidation() {
    assertTrue(IndustryFormatValidators.validateInsurancePolicy("NYALA-PI-2026-10492").isValid)
    assertTrue(IndustryFormatValidators.validateInsurancePolicy("INS-ETH-PI-9902").isValid)
    assertFalse(IndustryFormatValidators.validateInsurancePolicy("policy123").isValid)
    assertFalse(IndustryFormatValidators.validateInsurancePolicy("").isValid)
  }

  @Test
  fun testCpdPointsValidation() {
    assertTrue(IndustryFormatValidators.validateCpdPoints("35").isValid)
    assertTrue(IndustryFormatValidators.validateCpdPoints("20").isValid) // Valid with note
    assertFalse(IndustryFormatValidators.validateCpdPoints("abc").isValid)
    assertFalse(IndustryFormatValidators.validateCpdPoints("150").isValid)
  }

  @Test
  fun testExperienceVsGradeValidation() {
    // PPE Grade 1 requires 8+ years
    assertTrue(IndustryFormatValidators.validateExperienceVsGrade("ልምድ ያለው መሪ መሃንዲስ (PPE Grade 1)", 10).isValid)
    assertFalse(IndustryFormatValidators.validateExperienceVsGrade("ልምድ ያለው መሪ መሃንዲስ (PPE Grade 1)", 3).isValid)

    // PE Grade 2 requires 4+ years
    assertTrue(IndustryFormatValidators.validateExperienceVsGrade("ተግባራዊ መሃንዲስ (PE Grade 2)", 5).isValid)
    assertFalse(IndustryFormatValidators.validateExperienceVsGrade("ተግባራዊ መሃንዲስ (PE Grade 2)", 2).isValid)
  }

  @Test
  fun testSpecializedSkillsValidation() {
    assertTrue(IndustryFormatValidators.validateSkills(listOf("ETABS 3D Seismic Modeling", "FIDIC Red Book")).isValid)
    assertFalse(IndustryFormatValidators.validateSkills(emptyList()).isValid)
  }
}

