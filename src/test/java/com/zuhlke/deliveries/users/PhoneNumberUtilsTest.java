package com.zuhlke.deliveries.users;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PhoneNumberUtilsTest {
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Test
    void testCountryCode() {
        assertEquals(382, phoneNumberUtil.getCountryCodeForRegion("ME"));
    }

    @Test
    void testPhoneNumbers() throws NumberParseException {
        format("+382 69 654321");
        format("+381 69 654321");
        format("00382 69 654321");
        format("069 654321");
        format("069654321");
        format("069/654321");
        format("069-654321");
        format("069/654-321");
    }

    @Test
    void testPhoneNumberEquality() throws NumberParseException {
        assertAreSame("069 654433", "069/654433");
        assertAreSame("069 654433", "+382 069/654433");
        assertAreNotSame("069 654433", "+381 069/654433");
    }

    private void assertAreSame(String phone1, String phone2) throws NumberParseException {
        assertEquals(format(phone1), format(phone2));
    }

    private void assertAreNotSame(String phone1, String phone2) throws NumberParseException {
        assertNotEquals(format(phone1), format(phone2));
    }

    private String format(String phone1) throws NumberParseException {
        return phoneNumberUtil.format(phoneNumberUtil.parse(phone1, "ME"), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }
}
