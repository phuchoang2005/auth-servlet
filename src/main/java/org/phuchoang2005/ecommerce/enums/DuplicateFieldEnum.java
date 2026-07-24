package org.phuchoang2005.ecommerce.enums;

public enum DuplicateFieldEnum {
    USERNAME("uk_username", "Username", "username"),
    EMAIL("uk_profile_email", "Email", "email"),
    PHONE("uk_profile_phone", "Phone", "phone");

    private final String constraintName;
    private final String friendlyName;
    private final String fieldName;

    DuplicateFieldEnum(String constraintName, String friendlyName, String fieldName) {
        this.constraintName = constraintName;
        this.friendlyName = friendlyName;
        this.fieldName = fieldName;
    }

    public static DuplicateFieldEnum fromErrorMessage(String errorMessage) {
        // Case-insensitive: H2 uppercases unquoted constraint names in its
        // violation message (e.g. UK_PROFILE_EMAIL), while our names are lowercase.
        String upper = errorMessage.toUpperCase();
        for (DuplicateFieldEnum field : values()) {
            if (upper.contains(field.constraintName.toUpperCase())) {
                return field;
            }
        }
        return null;
    }

    public String getConstraintName(){return this.constraintName;}
    public String getFriendlyName(){return this.friendlyName;}
    public String getFieldName(){return this.fieldName;}
}