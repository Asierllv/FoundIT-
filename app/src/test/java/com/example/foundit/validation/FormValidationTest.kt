package com.example.foundit.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormValidationTest {
    @Test fun requiredRejectsBlank() { assertEquals("This field is required", FormValidation.required("  ")) }
    @Test fun requiredAcceptsValue() { assertNull(FormValidation.required("Wallet")) }
    @Test fun emailRejectsInvalid() { assertEquals("Enter a valid email address", FormValidation.email("not-an-email")) }
    @Test fun emailAcceptsValid() { assertNull(FormValidation.email("person@example.com")) }
    @Test fun passwordRequiresEightCharacters() { assertEquals("Password must contain at least 8 characters", FormValidation.password("short")) }
    @Test fun passwordAcceptsEightCharacters() { assertNull(FormValidation.password("password")) }
}
