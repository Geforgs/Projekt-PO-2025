package po25;

public enum Language {
    JAVA   ("Java",   "java"),
    CPP    ("C++",    "cpp"),
    PYTHON ("Python", "py");

    private final String displayName;
    private final String fileExtension;

    Language(String displayName, String fileExtension) {
        this.displayName   = displayName;
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() { return fileExtension; }

    @Override public String toString() {
        return displayName;
    }
}
