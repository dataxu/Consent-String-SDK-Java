package com.iab.gdpr.utils;

import com.iab.gdpr.ConsentStringParser;
import com.iab.gdpr.utils.ConsentStringBuilder;
import org.junit.Test;

import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class ConsentStringBuilderTest {
    private static final int VERSION = 0x13;
    private static final Instant CREATED = Instant.ofEpochMilli(((System.currentTimeMillis() - 10 * 1000) / 100) * 100);
    private static final Instant LAST_UPDATED = Instant.ofEpochMilli((System.currentTimeMillis() / 100) * 100);
    private static final int CMP_ID = 0x456;
    private static final int CMP_VERSION = 0x789;
    private static final int CONSENT_SCREEN = 0x21;
    private static final String CONSENT_LANGUAGE = "UA";
    private static final int VENDOR_LIST_VERSION = 0x654;
    private static final boolean[] ALLOWED_PURPOSES = new boolean[]{
            true, false, true, false,
            false, true, false, true,
            true, true, false, false,
            false, false, true, true,
            false, false, false, false,
            true, true, true, true
    };
    private static final SortedSet<Integer> CONSENTED_VENDOR_IDS = new TreeSet<>(Arrays.asList(
            2, 5, 3, 9, 35, 20, 71, 60, 100, 54, 78, 90, 100
    ));

    @Test
    public void testBuild() throws ParseException {
        final ConsentStringBuilder csb = new ConsentStringBuilder();

        csb
                .setVersion(VERSION)
                .setCreated(CREATED)
                .setLastUpdated(LAST_UPDATED)
                .setCmpId(CMP_ID)
                .setCmpVersion(CMP_VERSION)
                .setConsentScreen(CONSENT_SCREEN)
                .setConsentLanguage(CONSENT_LANGUAGE)
                .setVendorListVersion(VENDOR_LIST_VERSION);
        for(int purposeIndex = 1; purposeIndex <= 24; purposeIndex++) {
            csb.setAllowedPurpose(purposeIndex, ALLOWED_PURPOSES[purposeIndex - 1]);
        }
        CONSENTED_VENDOR_IDS.forEach(csb::addConsentedVendorId);

        final String consentString = csb.build();

        final ConsentStringParser csp = new ConsentStringParser(consentString);

        assertEquals(VERSION, csp.getVersion());
        assertEquals(CREATED, csp.getConsentRecordCreated());
        assertEquals(LAST_UPDATED, csp.getConsentRecordLastUpdated());
        assertEquals(CMP_ID, csp.getCmpId());
        assertEquals(CMP_VERSION, csp.getCmpVersion());
        assertEquals(CONSENT_SCREEN, csp.getConsentScreen());
        assertEquals(CONSENT_LANGUAGE, csp.getConsentLanguage());
        assertEquals(VENDOR_LIST_VERSION, csp.getVendorListVersion());
        for(int purposeIndex = 1; purposeIndex <= 24; purposeIndex++) {
            assertEquals(ALLOWED_PURPOSES[purposeIndex - 1], csp.isPurposeAllowed(purposeIndex));
        }
        final int maxVendorId = CONSENTED_VENDOR_IDS.last();
        for(int vendorId = 1; vendorId <= maxVendorId; vendorId++) {
            assertEquals(CONSENTED_VENDOR_IDS.contains(vendorId), csp.isVendorAllowed(vendorId));
        }
    }
}
