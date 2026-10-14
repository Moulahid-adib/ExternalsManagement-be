package ma.nttdata.externals.module.interview.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.nttdata.externals.commons.constants.InterviewEvaluationPromptConstants;
import ma.nttdata.externals.commons.exception.InternalServerException;
import ma.nttdata.externals.commons.exception.ResourceNotFoundException;
import ma.nttdata.externals.module.interview.dto.EvaluationsAIResponseDTO;
import ma.nttdata.externals.module.interview.dto.EvaluationDTO;
import ma.nttdata.externals.module.interview.dto.InterviewEvaluationsRequestDTO;
import ma.nttdata.externals.module.interview.dto.PlaceholdersForInterviewEvaluationPromptDTO;
import ma.nttdata.externals.module.interview.entity.Evaluation;
import ma.nttdata.externals.module.interview.entity.EvaluationType;
import ma.nttdata.externals.module.interview.entity.Interview;
import ma.nttdata.externals.module.interview.mapper.EvaluationMapper;
import ma.nttdata.externals.module.interview.repository.EvaluationRepository;
import ma.nttdata.externals.module.interview.repository.EvaluationTypeRepository;
import ma.nttdata.externals.module.interview.repository.InterviewRepository;
import ma.nttdata.externals.module.interview.service.EvaluationServ;
import ma.nttdata.externals.module.prompt.dto.PromptDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Service
public class EvaluationServImpl implements EvaluationServ {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationMapper evaluationMapper;
    private final InterviewRepository interviewRepository;
    private final EvaluationTypeRepository evaluationTypeRepository;
    private final boolean mockFlag;
    private final RestClient aiRestClient;

    public EvaluationServImpl(EvaluationRepository evaluationRepository,
                              EvaluationMapper evaluationMapper,
                              InterviewRepository interviewRepository,
                              EvaluationTypeRepository evaluationTypeRepository,
                              @Value("${app.mock.flag}") boolean mockFlag,
                              @Qualifier("aiServiceClient") RestClient aiRestClient) {
        this.evaluationRepository = evaluationRepository;
        this.evaluationMapper = evaluationMapper;
        this.interviewRepository = interviewRepository;
        this.evaluationTypeRepository = evaluationTypeRepository;
        this.mockFlag = mockFlag;
        this.aiRestClient = aiRestClient;
    }

    @Override
    public List<EvaluationDTO> getAllEvaluations() {
        return evaluationMapper.toDtoList(evaluationRepository.findAll());
    }

    @Override
    public EvaluationDTO getEvaluationById(UUID id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found with id: " + id));
        return evaluationMapper.toDto(evaluation);
    }

    @Override
    public EvaluationDTO createEvaluation(EvaluationDTO evaluationDTO) {
        Evaluation evaluation = evaluationMapper.toEntity(evaluationDTO);

        Interview interview = interviewRepository.findById(evaluationDTO.interviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + evaluationDTO.interviewId()));
        evaluation.setInterview(interview);

        EvaluationType evaluationType = evaluationTypeRepository.findById(evaluationDTO.evaluationTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationType not found with id: " + evaluationDTO.evaluationTypeId()));
        evaluation.setEvaluationType(evaluationType);

        Evaluation saved = evaluationRepository.save(evaluation);
        return evaluationMapper.toDto(saved);
    }

    @Override
    public EvaluationDTO updateEvaluation(UUID id, EvaluationDTO evaluationDTO) {
        Evaluation existing = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found with id: " + id));

        existing.setFeedback(evaluationDTO.feedback());
        existing.setScore(evaluationDTO.score());

        Interview interview = interviewRepository.findById(evaluationDTO.interviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + evaluationDTO.interviewId()));
        existing.setInterview(interview);

        EvaluationType evaluationType = evaluationTypeRepository.findById(evaluationDTO.evaluationTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationType not found with id: " + evaluationDTO.evaluationTypeId()));
        existing.setEvaluationType(evaluationType);

        Evaluation updated = evaluationRepository.save(existing);
        return evaluationMapper.toDto(updated);
    }

    @Override
    public void deleteEvaluation(UUID id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found with id: " + id));
        evaluationRepository.delete(evaluation);
    }

    @Override
    public List<EvaluationsAIResponseDTO> prepareEvaluationsDTOFromAiResponse(InterviewEvaluationsRequestDTO interviewEvaluationsRequest, PlaceholdersForInterviewEvaluationPromptDTO  placeholders, PromptDTO prompt){
        try{
            String generatedEvaluation = mockFlag ? InterviewEvaluationPromptConstants.JSON_MOCK
                    :getInterviewsEvaluationsFromAiByPrompt(interviewEvaluationsRequest,placeholders,prompt);

            ObjectMapper objectMapper = new ObjectMapper();
            List<EvaluationsAIResponseDTO> evaluation = objectMapper.readValue(generatedEvaluation, new TypeReference<List<EvaluationsAIResponseDTO>>() {});

            return evaluation;
        }catch (Exception e){
            throw new InternalServerException("Failed to parse questions JSON", e);
        }

    }

    @Override
    public String getInterviewsEvaluationsFromAiByPrompt(InterviewEvaluationsRequestDTO interviewEvaluationsRequest, PlaceholdersForInterviewEvaluationPromptDTO placeholders,PromptDTO prompt){
        String promptDesc  = prompt.promptDesc();

        String candidateData = placeholders.candidate().toString()
                .replaceAll("b64EFile=[^,)]+", "b64EFile=REDACTED");

        promptDesc = promptDesc.replace(InterviewEvaluationPromptConstants.CANDIDATE_PLACEHOLDER, candidateData)
                .replace(InterviewEvaluationPromptConstants.OFFER_PLACEHOLDER,placeholders.offer().toString())
                .replace(InterviewEvaluationPromptConstants.JSON_SCHEMA_PLACEHOLDER,prompt.schema())
                .replace(InterviewEvaluationPromptConstants.QUESTION_ANSWER_DTO_PLACEHOLDER,interviewEvaluationsRequest.questionsAndAnswersForEvaluation().toString())
                .replace(InterviewEvaluationPromptConstants.EVALUATION_TYPES_PLACEHOLDER,placeholders.evaluationType().toString())
                .replace(InterviewEvaluationPromptConstants.JSON_MOCK_PLACEHOLDER,InterviewEvaluationPromptConstants.JSON_MOCK);

        return aiRestClient.post()
                .uri("/evaluationInterview")
                .body(promptDesc)
                .retrieve()
                .body(String.class);
    }

    @Override
    public List<Evaluation> saveAIEvaluationResponse(List<EvaluationsAIResponseDTO> aiEvaluationsResponse, List<Evaluation> evaluations){
        List<Evaluation> evaluationsWithFeedbackAndScore = evaluationMapper.mapAIEvaluationResponsesToEvaluations(aiEvaluationsResponse,evaluations);

        return evaluationRepository.saveAll(evaluationsWithFeedbackAndScore);
    }

    @Override
    public List<Evaluation> getAllEvaluationsByInterviewID(UUID interviewId){

        return evaluationRepository.findByInterviewId(interviewId);
    }

}
