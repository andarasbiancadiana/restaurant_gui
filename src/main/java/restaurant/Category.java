package restaurant;

public enum Category {
    APERITIVE,
    FEL_PRINCIPAL,
    DESERT,
    BAUTURI_RACORITOARE,
    BAUTURI_ALCOOLICE;

    public String getDisplayName() {
        return switch (this) {
            case APERITIVE -> "Aperitive";
            case FEL_PRINCIPAL -> "Fel Principal";
            case DESERT -> "Desert";
            case BAUTURI_RACORITOARE -> "Bauturi Racoritoare";
            case BAUTURI_ALCOOLICE -> "Bauturi Alcoolice";
        };
    }
}
