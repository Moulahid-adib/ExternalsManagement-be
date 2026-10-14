package ma.nttdata.externals.module.interview.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EvaluationsAIResponseDTO(
        Double score,
        String feedback,
        @JsonAlias({"evaluationType", "evaluationTypeDescription"})
        String evaluationTypeDescription
) {
}
