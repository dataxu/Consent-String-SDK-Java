package com.iab.gdpr.utils;

import com.iab.gdpr.ConsentStringConstants;

import java.time.Instant;
import java.util.*;

/**
 * This class implements a builder of IAB consent string as specified in
 * @see <a href="https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Consent%20string%20and%20vendor%20list%20formats%20v1.1%20Final.md#vendor-consent-string-format">Vendor Consent String Format</a>
 */
public class ConsentStringBuilder implements ConsentStringConstants {
    private static final int CHAR_SIZE = 6;
    private static final char MIN_CHAR = 'A';
    private static final char MAX_CHAR = 'A' + (1 << CHAR_SIZE) - 1;

    public static final int CONSENT_LANGUAGE_LENGTH = CONSENT_LANGUAGE_SIZE / CHAR_SIZE;

    public static final int MIN_PURPOSE_INDEX = 1;
    public static final int MAX_PURPOSE_INDEX = PURPOSES_SIZE;

    public static final int MIN_VENDOR_ID = 1;
    public static final int MAX_VENDOR_ID = (1 << VENDOR_ID_SIZE) - 1;

    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();

    private Integer version;
    private Instant created;
    private Instant lastUpdated;
    private Integer cmpId;
    private Integer cmpVersion;
    private Integer consentScreen;
    private String consentLanguage;
    private Integer vendorListVersion;
    private final BitSet alowedPurposes = new BitSet(MAX_PURPOSE_INDEX);
    private final SortedSet<Integer> consentedVendorIds = new TreeSet<>();

    public ConsentStringBuilder() {
    }

    public ConsentStringBuilder(final ConsentStringBuilder consentStringBuilder) {
        Objects.nonNull(consentStringBuilder);
        this.version = consentStringBuilder.version;
        this.created = consentStringBuilder.created;
        this.lastUpdated = consentStringBuilder.lastUpdated;
        this.cmpId = consentStringBuilder.cmpId;
        this.cmpVersion = consentStringBuilder.cmpVersion;
        this.consentScreen = consentStringBuilder.consentScreen;
        this.consentLanguage = consentStringBuilder.consentLanguage;
        this.vendorListVersion = consentStringBuilder.vendorListVersion;
        this.alowedPurposes.or(consentStringBuilder.alowedPurposes);
        this.consentedVendorIds.addAll(consentStringBuilder.consentedVendorIds);
    }

    public ConsentStringBuilder setVersion(final int version) {
        this.version = version;

        return this;
    }

    public ConsentStringBuilder clearVersion() {
        version = null;

        return this;
    }

    public ConsentStringBuilder setCreated(final Instant created) {
        Objects.requireNonNull(created);
        this.created = created;

        return this;
    }

    public ConsentStringBuilder clearCreated() {
        created = null;

        return this;
    }

    public ConsentStringBuilder setLastUpdated(final Instant lastUpdated) {
        Objects.requireNonNull(lastUpdated);
        this.lastUpdated = lastUpdated;

        return this;
    }

    public ConsentStringBuilder clearLastUpdated() {
        lastUpdated = null;

        return this;
    }

    public ConsentStringBuilder setCmpId(final int cmpId) {
        this.cmpId = cmpId;

        return this;
    }

    public ConsentStringBuilder clearCmpId() {
        cmpId = null;

        return this;
    }

    public ConsentStringBuilder setCmpVersion(final int cmpVersion) {
        this.cmpVersion = cmpVersion;

        return this;
    }

    public ConsentStringBuilder clearCmpVersion() {
        cmpVersion = null;

        return this;
    }

    public ConsentStringBuilder setConsentScreen(final int consentScreen) {
        this.consentScreen = consentScreen;

        return this;
    }

    public ConsentStringBuilder clearConsentScreen() {
        consentScreen = null;

        return this;
    }

    public ConsentStringBuilder setConsentLanguage(final String consentLanguage) {
        if (consentLanguage == null || consentLanguage.length() != CONSENT_LANGUAGE_LENGTH) {
            throw new IllegalArgumentException(String.format("Incorrect consent language '%s'! Should be with length %d.",
                    consentLanguage, CONSENT_LANGUAGE_SIZE));
        }
        checkString(consentLanguage);

        this.consentLanguage = consentLanguage;

        return this;
    }

    public ConsentStringBuilder clearConsentLanguage() {
        consentLanguage = null;

        return this;
    }

    public ConsentStringBuilder setAllowedPurpose(final int purposeIndex, final boolean allowed) {
        if (purposeIndex < MIN_PURPOSE_INDEX || purposeIndex > MAX_PURPOSE_INDEX) {
            throw new IllegalArgumentException(String.format("Incorrect purpose index id %d! Should be in range [%d, %d].",
                    purposeIndex, MIN_PURPOSE_INDEX, MAX_PURPOSE_INDEX));
        }

        alowedPurposes.set(purposeIndex - 1, allowed);

        return this;
    }

    public ConsentStringBuilder clearAllowedPurposes() {
        alowedPurposes.clear();

        return this;
    }

    public ConsentStringBuilder setVendorListVersion(final int vendorListVersion) {
        this.vendorListVersion = vendorListVersion;

        return this;
    }

    public ConsentStringBuilder clearVendorListVersion() {
        vendorListVersion = null;

        return this;
    }

    public ConsentStringBuilder addConsentedVendorId(final Integer consentedVendorId) {
        if (consentedVendorId == null || consentedVendorId < MIN_VENDOR_ID || consentedVendorId > MAX_VENDOR_ID) {
            throw new IllegalArgumentException(String.format("Incorrect consented vendor id %s! Should be in range [%d, %d].",
                    consentedVendorId, MIN_VENDOR_ID, MAX_VENDOR_ID));
        }

        consentedVendorIds.add(consentedVendorId);

        return this;
    }

    public ConsentStringBuilder removeConsentedVendorId(final Integer consentedVendorId) {
        consentedVendorIds.remove(consentedVendorId);

        return this;
    }

    public ConsentStringBuilder clearConsentedVendorIds() {
        consentedVendorIds.clear();

        return this;
    }

    public ConsentStringBuilder clear() {
        clearVersion();
        clearCreated();
        clearLastUpdated();
        clearCmpId();
        clearCmpVersion();
        clearConsentScreen();
        clearConsentLanguage();
        clearVendorListVersion();
        clearAllowedPurposes();
        clearConsentedVendorIds();

        return this;
    }

    // Note: at the moment consent string is building by using simplest BitField encoding method. It could be not the most
    // optimal (depends from consented vendor ids values), but it is not critical if builder is used only for testing.
    // Support for Range encoding (and automatic using most optimal method for the given consented vendor ids values)
    // will be added soon.
    public String build() {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(created, "created");
        Objects.requireNonNull(lastUpdated, "lastUpdated");
        Objects.requireNonNull(cmpId, "cmpId");
        Objects.requireNonNull(cmpVersion, "cmpVersion");
        Objects.requireNonNull(consentScreen, "consentScreen");
        Objects.requireNonNull(consentLanguage, "consentLanguage");
        Objects.requireNonNull(vendorListVersion, "vendorListVersion");

        final int maxVendorId = !consentedVendorIds.isEmpty() ? consentedVendorIds.last() : 0;

        final int totalSize = VERSION_BIT_SIZE + CREATED_BIT_SIZE + UPDATED_BIT_SIZE + CMP_ID_SIZE + CMP_VERSION_SIZE +
                CONSENT_SCREEN_SIZE + CONSENT_LANGUAGE_SIZE + VENDOR_LIST_VERSION_SIZE + PURPOSES_SIZE + MAX_VENDOR_ID_SIZE + ENCODING_TYPE_SIZE + maxVendorId;

        final byte[] consentStringBytes = new byte[totalSize / 8 + ((totalSize % 8) != 0 ? 1 : 0)];

        setBits(consentStringBytes, VERSION_BIT_OFFSET, VERSION_BIT_SIZE, version);

        setBits(consentStringBytes, CREATED_BIT_OFFSET, created);
        setBits(consentStringBytes, UPDATED_BIT_OFFSET, lastUpdated);

        setBits(consentStringBytes, CMP_ID_OFFSET, CMP_ID_SIZE, cmpId);
        setBits(consentStringBytes, CMP_VERSION_OFFSET, CMP_VERSION_SIZE, cmpVersion);

        setBits(consentStringBytes, CONSENT_SCREEN_SIZE_OFFSET, CONSENT_SCREEN_SIZE, consentScreen);
        setBits(consentStringBytes, CONSENT_LANGUAGE_OFFSET, consentLanguage);

        setBits(consentStringBytes, VENDOR_LIST_VERSION_OFFSET, VENDOR_LIST_VERSION_SIZE, vendorListVersion);

        for(int purposeIndex = MIN_PURPOSE_INDEX; purposeIndex <= MAX_PURPOSE_INDEX; purposeIndex++) {
            setBit(consentStringBytes, PURPOSES_OFFSET + purposeIndex - 1, alowedPurposes.get(purposeIndex - 1));
        }

        setBits(consentStringBytes, MAX_VENDOR_ID_OFFSET, VENDOR_ID_SIZE, maxVendorId);

        setBit(consentStringBytes, ENCODING_TYPE_OFFSET, false);

        for(int vendorId = 1; vendorId <= maxVendorId; vendorId++) {
            setBit(consentStringBytes, VENDOR_BITFIELD_OFFSET + vendorId - 1, consentedVendorIds.contains(vendorId));
        }

        return BASE64_ENCODER.encodeToString(consentStringBytes);
    }

    private void checkString(final String value) {
        for(int index = 0; index != value.length(); index++) {
            final char c = value.charAt(index);
            if (c < MIN_CHAR || c > MAX_CHAR) {
                throw new IllegalArgumentException(String.format("Incorrect character '%c' at index %d! Should be in range ['%c', '%c']",
                        c, index, MIN_CHAR, MAX_CHAR));
            }
        }
    }

    private void setBits(final byte[] bytes, final int offset, final Instant value) {
        setBits(bytes, offset, 36, value.toEpochMilli() / 100);
    }

    private void setBits(final byte[] bytes, final int offset, final String value) {
        for(int index = 0; index != value.length(); index++) {
            setBits(bytes, offset + index * CHAR_SIZE, CHAR_SIZE, value.charAt(index) - MIN_CHAR);
        }
    }

    private void setBits(final byte[] bytes, final int offset, final int size, final long value) {
        for(int index = 0; index != size; index++) {
            setBit(bytes, offset + index, ((value >>> (size - index - 1)) & 1) == 1);
        }
    }

    private void setBit(final byte[] bytes, final int offset, final boolean value) {
        final int byteIndex = offset / 8;
        final int bitIndex = 7 - (offset % 8);

        final byte bitMask = (byte)(1 << bitIndex);

        bytes[byteIndex] &= ~bitMask;
        if (value) {
            bytes[byteIndex] |= bitMask;
        }
    }
}
