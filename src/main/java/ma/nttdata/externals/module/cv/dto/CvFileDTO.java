package ma.nttdata.externals.module.cv.dto;

public record CvFileDTO(
        String promptCode,
        String b64EFile,
        String mimeType,
        String extractedData, // json
        String filePath
) {
    @Override
    public String toString() {
        return "CvFileDTO[" +
                "promptCode=" + promptCode +
                ", b64EFile=REDACTED_TO_SAVE_TOKENS" +
                ", mimeType=" + mimeType +
                ", extractedData=" + extractedData +
                ", filePath=" + filePath + ']';
    }
}
