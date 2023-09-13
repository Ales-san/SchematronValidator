package itworks.group.models;

public class ValidationResult {

    public ValidationResult(boolean isValid, String[] validationMessages) {
        this.isValid = isValid;
        this.validationMessages = validationMessages;
    }

    public ValidationResult(boolean isValid) {
        this.isValid = isValid;
        this.validationMessages = new String[] {};
    }

    private final boolean isValid;

    private final String[] validationMessages;

}
