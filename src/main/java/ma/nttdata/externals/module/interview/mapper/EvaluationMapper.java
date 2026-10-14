package ma.nttdata.externals.module.interview.mapper;

import ma.nttdata.externals.module.interview.dto.EvaluationDTO;
import ma.nttdata.externals.module.interview.dto.EvaluationsAIResponseDTO;
import ma.nttdata.externals.module.interview.dto.FullEvaluationDTO;
import ma.nttdata.externals.module.interview.dto.InterviewEvaluationDTO;
import ma.nttdata.externals.module.interview.entity.Evaluation;
import ma.nttdata.externals.module.interview.entity.Interview;
import ma.nttdata.externals.module.interview.entity.EvaluationType;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = { EvaluationTypeMapper.class })
public interface EvaluationMapper {

    // to Dto
    @Mapping(target = "id", source = "id")
    @Mapping(target = "score", source = "score")
    @Mapping(target = "feedback", source = "feedback")
    @Mapping(target = "interviewId", source = "interview.id")
    @Mapping(target = "evaluationTypeId", source = "evaluationType.id")
    EvaluationDTO toDto(Evaluation evaluation);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "score", source = "score")
    @Mapping(target = "feedback", source = "feedback")
    @Mapping(target = "interview", ignore = true)
    @Mapping(target = "evaluationType", ignore = true)
    Evaluation toEntity(EvaluationDTO dto);

    List<EvaluationDTO> toDtoList(List<Evaluation> list);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "feedback", source = "feedback")
    @Mapping(target = "interviewId", source = "interview.id")
    FullEvaluationDTO fromEvaluationToFullEvaluationDTO(Evaluation evaluation);


    @AfterMapping
    default void setInterviewAndType(EvaluationDTO dto, @MappingTarget Evaluation evaluation) {
        if (dto.interviewId() != null) {
            Interview interview = new Interview();
            interview.setId(dto.interviewId());
            evaluation.setInterview(interview);
        }
        if (dto.evaluationTypeId() != null) {
            EvaluationType type = new EvaluationType();
            type.setId(dto.evaluationTypeId());
            evaluation.setEvaluationType(type);
        }
    }

    default List<Evaluation> mapAIEvaluationResponsesToEvaluations(
            List<EvaluationsAIResponseDTO> aiResponses,
            List<Evaluation> evaluations) {

        for (Evaluation evaluation : evaluations) {
            String evaluationTypeDescription = evaluation.getEvaluationType().getDescription();

            // Strategy 1: exact match (case-insensitive)
            EvaluationsAIResponseDTO bestMatch = null;
            for (EvaluationsAIResponseDTO aiResponse : aiResponses) {
                if (aiResponse.evaluationTypeDescription() != null &&
                    aiResponse.evaluationTypeDescription().trim().equalsIgnoreCase(evaluationTypeDescription.trim())) {
                    bestMatch = aiResponse;
                    break;
                }
            }

            // Strategy 2: contains match (e.g. "Technical" matches "Technical Skills" and "Technical Knowledge")
            if (bestMatch == null) {
                String[] dbWords = evaluationTypeDescription.toLowerCase().split("\\s+");
                int bestScore = 0;
                for (EvaluationsAIResponseDTO aiResponse : aiResponses) {
                    if (aiResponse.evaluationTypeDescription() == null) continue;
                    String[] aiWords = aiResponse.evaluationTypeDescription().toLowerCase().split("\\s+");
                    int overlap = 0;
                    for (String dbWord : dbWords) {
                        for (String aiWord : aiWords) {
                            if (dbWord.equals(aiWord) && dbWord.length() > 3) overlap++;
                        }
                    }
                    if (overlap > bestScore) {
                        bestScore = overlap;
                        bestMatch = aiResponse;
                    }
                }
                // Only use fuzzy if there was at least 1 word in common
                if (bestScore == 0) bestMatch = null;
            }

            if (bestMatch != null) {
                evaluation.setFeedback(bestMatch.feedback());
                evaluation.setScore(bestMatch.score());
            }
        }

        return evaluations;
    }

    default InterviewEvaluationDTO mapEvaluationToInterviewEvaluation(
            List<Evaluation> evaluations
    ) {
        if (evaluations == null || evaluations.isEmpty()) {
            return null;
        }

        UUID interviewId = evaluations.get(0).getInterview().getId();
        String candidateFullName = evaluations.get(0).getInterview().getCandidate().getFullName();
        String offerTitle = evaluations.get(0).getInterview().getOffer().getTitle();
        LocalDateTime scheduledAt = evaluations.get(0).getInterview().getScheduledAt();
        int estimatedDuration = evaluations.get(0).getInterview().getEstimatedDuration();

        List<FullEvaluationDTO> fullEvaluationDTOs =
                evaluations.stream()
                        .map(this::fromEvaluationToFullEvaluationDTO)
                        .toList();

        InterviewEvaluationDTO interviewEvaluation =
                new InterviewEvaluationDTO(
                        interviewId,
                        candidateFullName,
                        offerTitle,
                        scheduledAt,
                        estimatedDuration,
                        fullEvaluationDTOs
                );


        return interviewEvaluation;
    }


}
