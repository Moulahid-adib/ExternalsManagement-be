package ma.nttdata.externals.module.interview.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ma.nttdata.externals.commons.constants.InterviewEvaluationPromptConstants;
import ma.nttdata.externals.commons.constants.InterviewPromptConstants;
import ma.nttdata.externals.commons.exception.ResourceNotFoundException;
import ma.nttdata.externals.module.candidate.dto.CandidateDTO;
import ma.nttdata.externals.module.candidate.entity.Candidate;
import ma.nttdata.externals.module.candidate.entity.Contact;
import ma.nttdata.externals.module.candidate.mapper.CandidateMapper;
import ma.nttdata.externals.module.interview.dto.*;
import ma.nttdata.externals.module.interview.entity.*;
import ma.nttdata.externals.module.interview.mapper.*;
import ma.nttdata.externals.module.interview.mapper.EvaluationTypeMapper;
import ma.nttdata.externals.module.interview.repository.AnswerRepository;
import ma.nttdata.externals.module.interview.repository.EvaluationRepository;
import ma.nttdata.externals.module.interview.repository.InterviewRepository;
import ma.nttdata.externals.module.interview.repository.QuestionRepository;
import ma.nttdata.externals.module.interview.service.*;

import ma.nttdata.externals.module.offer.entity.Offer;
import ma.nttdata.externals.module.prompt.dto.PromptDTO;
import ma.nttdata.externals.module.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Value;
import ma.nttdata.externals.module.offer.dto.OfferDTO;
import ma.nttdata.externals.module.offer.mapper.OfferMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class InterviewServImpl implements InterviewServ {

    private final InterviewMapper interviewMapper;
    private final InterviewRepository interviewRepository;

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    private final AnswerMapper answerMapper;
    private final AnswerRepository answerRepository;

    private final EvaluationRepository evaluationRepository ;
    private final EvaluationMapper evaluationMapper ;

    private final EvaluationTypeMapper evaluationTypeMapper;

    private final CandidateMapper candidateMapper ;


    private final String interviewBaseLink;

    private final OfferMapper offerMapper;

    private final EvaluationServ evaluationServ;
    private final PromptService promptServ;
    private final EvaluationTypeServ evaluationTypeServ;
    private final QuestionServ questionServ;
    private final TextToSpeechServ textToSpeechServ;

    public InterviewServImpl(
            InterviewMapper interviewMapper,
            InterviewRepository interviewRepository ,
            QuestionRepository questionRepository ,
            QuestionMapper questionMapper,
            AnswerRepository answerRepository  ,
            AnswerMapper answerMapper ,
            EvaluationRepository evaluationRepository ,
            EvaluationMapper evaluationMapper ,
            EvaluationTypeMapper evaluationTypeMapper ,
            CandidateMapper candidateMapper,
            @Value("${interview.baseLink}") String interviewBaseLink,
            OfferMapper offerMapper, EvaluationServ evaluationServ, PromptService promptServ, EvaluationTypeServ evaluationTypeServ, QuestionServ questionServ, TextToSpeechServ textToSpeechServ
    ) {
        this.interviewMapper = interviewMapper;
        this.interviewRepository = interviewRepository;
        this.questionRepository = questionRepository ;
        this.questionMapper = questionMapper;
        this.answerRepository = answerRepository ;
        this.answerMapper = answerMapper ;
        this.evaluationMapper = evaluationMapper ;
        this.evaluationRepository = evaluationRepository;
        this.evaluationTypeMapper= evaluationTypeMapper;
        this.candidateMapper = candidateMapper;
        this.interviewBaseLink = interviewBaseLink;
        this.offerMapper = offerMapper;
        this.evaluationServ = evaluationServ;
        this.promptServ = promptServ;
        this.evaluationTypeServ = evaluationTypeServ;
        this.questionServ = questionServ;
        this.textToSpeechServ = textToSpeechServ;
    }

    // new interview
    @Override
    public InterviewDTO createInterview(InterviewDTO interviewDTO) {
        Interview interview = interviewMapper.toEntity(interviewDTO);
        Interview savedInterview = interviewRepository.save(interview);
        return interviewMapper.toDto(savedInterview);
    }

    @Override
    public List<InterviewDTO> getAllInterviews() {
        List<Interview> interviews = interviewRepository.findAll();
        return interviewMapper.toDtoList(interviews);
    }

    // get interview by offer id
    @Override
    public List<InterviewDTO> getInterviewsByOfferId(UUID offerId) {
        List<Interview> interviews = interviewRepository.findByOfferId(offerId);
        return interviewMapper.toDtoList(interviews);
    }

    // get interview by id
    @Override
    public InterviewDTO getInterviewById(UUID id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found with Id " + id));
        return interviewMapper.toDto(interview);
    }

    @Override
    public InterviewDTO updateInterview(UUID id, InterviewDTO interviewDTO) {
        Interview existingInterview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found with Id " + id));

        interviewMapper.updateInterviewFromDto(interviewDTO, existingInterview);

        Interview updatedInterview = interviewRepository.save(existingInterview);
        return interviewMapper.toDto(updatedInterview);
    }


    // delete
    @Override
    public void deleteInterview(UUID id) {
        if (!interviewRepository.existsById(id)) {
            throw new RuntimeException("Interview not found with ID " + id);
        }
        interviewRepository.deleteById(id);
    }

    // get questions of an interview
    @Override
    public List<QuestionDTO> getQuestionsByInterviewId(UUID interviewId) {
        List<Question> questions = questionRepository.findByInterviewId(interviewId);
        return questionMapper.toDtoList(questions);
    }

    @Override
    public AnswerDTO getAnswerOfQuestion(UUID questionId) {
        return null;
    }



    // get the answer of question id
    @Override
    public  AnswerDTO getAnswerByQuestionId(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));

        Answer answer = question.getAnswer();

        return answerMapper.toDto(answer);
    }
    @Override
    public CandidateDTO getCandidateByInterviewId(UUID interviewId) throws ChangeSetPersister.NotFoundException {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        Candidate candidate = interview.getCandidate();
        if (candidate == null) {
            throw new ChangeSetPersister.NotFoundException();
        }

        return candidateMapper.candidateToCandidateDTO(
                candidate);
    }

    @Override
    public List<EvaluationDTO> getEvaluationsOfInterview(UUID interviewId) {
        List<Evaluation> evaluations = evaluationRepository.findByInterviewId(interviewId);
        return evaluationMapper.toDtoList(evaluations);
    }

    @Override
    public EvaluationTypeDTO getEvaluationTypeOfEvaluation(UUID id) {
        Optional<Evaluation> evaluationOptional = evaluationRepository.findTypeById(id);

        if (evaluationOptional.isEmpty()) {
            throw new EntityNotFoundException("Evaluation not found with id: " + id);
        }

        Evaluation evaluation = evaluationOptional.get();

        EvaluationType evaluationType = evaluation.getEvaluationType(); // assuming this is the entity

        return evaluationTypeMapper.toDto(evaluationType);
    }
    //add comment
    @Override
    public InterviewDTO addCommentToInterview(UUID id, String comment) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interview not found with ID: " + id));

        interview.setComment(comment); // Assuming 'comment' is a field in the Interview entity
        Interview updatedInterview = interviewRepository.save(interview);

        return interviewMapper.toDto(updatedInterview);
    }

    @Override
    public String saveInterviewLink(String token, UUID interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview",interviewId));


        String interviewLink = interviewBaseLink+token;
        interview.setLink(interviewLink);
        interviewRepository.save(interview);
        return interviewLink;
    }
    @Override
    public placeholdersForInterviewQuestionsPromptDTO getPlaceholdersForInterviewQuestionsPrompt(UUID interviewId){
        Interview interview = interviewRepository.findWithCandidateWithoutContactsAndOfferById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview" + interviewId));

        CandidateDTO candidate = candidateMapper.candidateToCandidateDTO(interview.getCandidate());

        OfferDTO offer = offerMapper.toDto(interview.getOffer());

        placeholdersForInterviewQuestionsPromptDTO  placeholders = new placeholdersForInterviewQuestionsPromptDTO(
                candidate,
                offer,
                interview.getNumberOfQuestions(),
                interview.getEstimatedDuration()
        );

        return  placeholders;
    }

    @Override
    public SendEmailDTO getEmailInfo(UUID interviewId){
        Interview interview = interviewRepository.findWithCandidateAndOfferById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview",interviewId));;

        Candidate candidate = interview.getCandidate();
        Offer offer = interview.getOffer();

        String email = candidate.getContacts().stream()
                .filter(c -> {
                    String type = c.getContactType();
                    return type != null && (type.equalsIgnoreCase("email") || type.equalsIgnoreCase("mail"));
                })
                .map(Contact::getContactValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Candidate email not found"));

        return new SendEmailDTO(
                candidate.getFullName(),
                offer.getTitle(),
                email,
                interview.getScheduledAt(),
                interview.getLink()
        );
    }

    @Override
    public PlaceholdersForInterviewEvaluationPromptDTO getInterviewEvaluationPlaceholders(UUID interviewId){
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview",interviewId));

        CandidateDTO candidate = candidateMapper.candidateToCandidateDTO(interview.getCandidate());
        OfferDTO offer = offerMapper.toDto(interview.getOffer());
        List<EvaluationType> evaluationTypes = interview.getEvaluations()
                .stream()
                .map(evaluation -> evaluation.getEvaluationType())
                .toList();

        return new PlaceholdersForInterviewEvaluationPromptDTO(
                candidate,
                offer,
                evaluationTypes
        );

    }

    public List<InterviewListDTO> getAllInterviewList(){
        List<Interview> interviews = interviewRepository.findAll();

        List<InterviewListDTO> interviewList = interviews.stream()
                .map(interviewMapper::fromInterviewToInterviewListDTO)
                .collect(Collectors.toList());

        return interviewList;
    }

    public List<QuestionDTO> generateInterviewQuestions(UUID interviewId,
                                                        GenerateInterviewQuestionsRequest generateInterviewQuestionsRequest){
        placeholdersForInterviewQuestionsPromptDTO placeholders = getPlaceholdersForInterviewQuestionsPrompt(interviewId);
        List<EvaluationTypeDTO> evaluationTypes = evaluationTypeServ.findAllById(generateInterviewQuestionsRequest.evaluationTypesIds());
        
        // --- Initialize empty evaluations so AI can fill them later ---
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found: " + interviewId));
        List<Evaluation> emptyEvaluations = evaluationTypes.stream().map(typeDto -> {
            Evaluation eval = new Evaluation();
            eval.setInterview(interview);
            eval.setEvaluationType(evaluationTypeMapper.toEntity(typeDto));
            return eval;
        }).collect(Collectors.toList());
        evaluationRepository.saveAll(emptyEvaluations);
        // -------------------------------------------------------------

        PromptDTO prompt = promptServ.findByPromptCode(InterviewPromptConstants.INTERVIEW_GENERATE_QUESTIONS_PROMPT_CODE);
        List<QuestionDTO> generatedQuestions = questionServ.prepareQuestionsFromAIResponse( placeholders, evaluationTypes,prompt);
        List<QuestionDTO> generatedQuestionsWithInterviewId = generatedQuestions.stream()
                .map(q -> new QuestionDTO(
                        null,
                        q.description(),
                        q.durationInMinutes(),
                        interviewId,
                        null
                ))
                .toList();
        List<QuestionDTO> savedQuestions = questionServ.saveAllQuestions(generatedQuestionsWithInterviewId);
        return savedQuestions;
    }

    @Override
    public List<Evaluation> prepareInterviewEvaluations(UUID interviewId, InterviewEvaluationsRequestDTO interviewEvaluationsRequest) {
        PlaceholdersForInterviewEvaluationPromptDTO placeholders = getInterviewEvaluationPlaceholders(interviewId);
        PromptDTO prompt = promptServ.findByPromptCode(InterviewEvaluationPromptConstants.INTERVIEW_EVALUATION_PROMPT_CODE);
        List<EvaluationsAIResponseDTO> aiEvaluationResponse = evaluationServ.prepareEvaluationsDTOFromAiResponse(interviewEvaluationsRequest,placeholders,prompt);
        List<Evaluation> evaluations = evaluationServ.getAllEvaluationsByInterviewID(interviewId);
        List<Evaluation> savedEvaluations = evaluationServ.saveAIEvaluationResponse(aiEvaluationResponse,evaluations);

        Interview interview = interviewRepository.findById(interviewId).orElse(null);
        if (interview != null && interview.getEndTime() == null) {
            interview.setEndTime(java.time.LocalDateTime.now());
            interviewRepository.save(interview);
        }

        return savedEvaluations;
    }

    @Override
    public InterviewEvaluationDTO getInterviewEvaluations(UUID interviewId){
        List<Evaluation> evaluations = evaluationServ.getAllEvaluationsByInterviewID(interviewId);
        return evaluationMapper.mapEvaluationToInterviewEvaluation(evaluations);
    }

    @Override
    public  ResponseEntity<byte[]> generateInterviewQuestionsAudios(UUID interviewId) throws IOException {
        List<QuestionDTO> questions = questionServ.findAllQuestionsDTOSByInterviewId(interviewId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < questions.size(); i++) {
                byte[] audio = textToSpeechServ.speak(questions.get(i).description());

                ZipEntry entry = new ZipEntry("question_" + (i + 1) + ".mp3");
                zos.putNextEntry(entry);
                zos.write(audio);
                zos.closeEntry();
            }
        }

        byte[] audiosZipped = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(audiosZipped.length);

        return ResponseEntity.ok().headers(headers).body(audiosZipped);
    }

    public String getTanscription(UUID interviewId){
        Interview interview = interviewRepository.findById(interviewId).orElseThrow(() -> new EntityNotFoundException("Interview not found with ID: " + interviewId));
        return interview.getTranscription();
    }

    public InterviewDTO updateTranscription(UUID interviewId, String transcription){
        Interview interview = interviewRepository.findById(interviewId).orElseThrow(() -> new EntityNotFoundException("Interview not found with ID: " + interviewId));
        interview.setTranscription(transcription);
        Interview updatedInterview = interviewRepository.save(interview);
        return interviewMapper.toDto(updatedInterview);
    }

}


