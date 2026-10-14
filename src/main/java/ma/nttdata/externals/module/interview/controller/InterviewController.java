package ma.nttdata.externals.module.interview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.nttdata.externals.commons.services.EmailContentBuilder;
import ma.nttdata.externals.commons.services.impl.EmailServiceImpl;
import ma.nttdata.externals.module.candidate.dto.CandidateDTO;
import ma.nttdata.externals.module.interview.dto.*;
import ma.nttdata.externals.module.interview.entity.Evaluation;
import ma.nttdata.externals.module.interview.service.*;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
@Tag(name = "Interview Management API", description = "This API exposes endpoints to manage interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewServ interviewServ;
    private final InterviewTokenServ interviewTokenServ;
    private final EmailServiceImpl emailServiceImpl;
    private final EmailContentBuilder emailContentBuilder;
    private final QuestionServ questionServ;

    @Operation(summary = "Create a new interview", description = "Creates and returns a new interview based on the provided InterviewDTO")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<InterviewDTO> createInterview(@RequestBody InterviewDTO interviewDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewServ.createInterview(interviewDTO));
    }

    @Operation(summary = "Get all interviews", description = "Returns a list of all interviews in the system")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping()
    public ResponseEntity<List<InterviewListDTO>> getAllInterviews() {
        return ResponseEntity.ok(interviewServ.getAllInterviewList());
    }

    @Operation(summary = "Get interview by ID", description = "Fetches an interview by its unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<InterviewDTO> getInterviewById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(interviewServ.getInterviewById(id));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found", e);
        }
    }

    @Operation(summary = "Update interview", description = "Updates an existing interview by ID using provided data")
    @PutMapping("/{id}")
    public ResponseEntity<InterviewDTO> updateInterview(
            @PathVariable UUID id,
            @RequestBody InterviewDTO interviewDTO) {
        try {
            return ResponseEntity.ok(interviewServ.updateInterview(id, interviewDTO));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found", e);
        }
    }

    @Operation(summary = "Add a comment to an interview", description = "Updates the comment field of an interview by ID")
    @PutMapping("/{id}/add-comment")
    public ResponseEntity<InterviewDTO> addComment(
            @PathVariable UUID id,
            @RequestBody CommentRequestDTO commentRequestDTO) {
        try {
            InterviewDTO updatedInterview = interviewServ.addCommentToInterview(id, commentRequestDTO.comment());
            return ResponseEntity.ok(updatedInterview);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found", e);
        }
    }

    @Operation(summary = "Delete interview", description = "Deletes the interview with the specified ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable UUID id) {
        try {
            interviewServ.deleteInterview(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found", e);
        }
    }

    @Operation(summary = "Get interviews by offer ID", description = "Returns a list of interviews associated with a specific offer ID")
    @GetMapping("/offer/{offerId}")
    public ResponseEntity<List<InterviewDTO>> getInterviewsByOfferId(@PathVariable UUID offerId) {
        return ResponseEntity.ok(interviewServ.getInterviewsByOfferId(offerId));
    }

    @Operation(summary = "Get answer of a question", description = "Returns the answer associated with a given question ID")
    @GetMapping("/questions/{questionId}/answer")
    public ResponseEntity<AnswerDTO> getAnswerByQuestion(@PathVariable UUID questionId) {
        AnswerDTO answer = interviewServ.getAnswerByQuestionId(questionId);
        return ResponseEntity.ok(answer);
    }

    @Operation(summary = "Get candidate by interview ID", description = "Returns the candidate linked to a specific interview")
    @GetMapping("/{interviewId}/candidate")
    public ResponseEntity<CandidateDTO> getCandidateByInterviewId(@PathVariable UUID interviewId)
            throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(interviewServ.getCandidateByInterviewId(interviewId));
    }

    @Operation(summary = "Get evaluation type by evaluation ID", description = "Returns the evaluation type associated with a specific evaluation ID")
    @GetMapping("/{id}/type")
    public ResponseEntity<EvaluationTypeDTO> getEvaluationType(@PathVariable UUID id) {
        return ResponseEntity.ok(interviewServ.getEvaluationTypeOfEvaluation(id));
    }

    @Operation(summary = "Generate and save interview link", description = "Generates a secure interview token, saves the interview link, and returns the full URL for the given interview ID")
    @PostMapping("/{interviewId}/generateAndSaveLink")
    public ResponseEntity<String> generateAndSaveInterviewLink(@PathVariable UUID interviewId) {
        LocalDateTime scheduledAt = interviewServ.getInterviewById(interviewId).scheduledAt();
        // Updated to pass interviewId to the token generation
        String token = interviewTokenServ.generateToken(scheduledAt, interviewId);
        String interviewLink = interviewServ.saveInterviewLink(token, interviewId);
        return ResponseEntity.ok(interviewLink);
    }

    @Operation(summary = "Get interview ID from token", description = "Extracts and returns the interview ID from a valid interview token")
    @GetMapping("/token/{token}/interview-id")
    public ResponseEntity<UUID> getInterviewIdFromToken(@PathVariable String token) {
        UUID interviewId = interviewTokenServ.getInterviewIdFromValidToken(token);
        return ResponseEntity.ok(interviewId);
    }

    @Operation(summary = "Get interview details by token", description = "Retrieves complete interview information using a valid interview token")
    @GetMapping("/token/{token}")
    public ResponseEntity<InterviewDTO> getInterviewByToken(@PathVariable String token) {
        InterviewDTO interview = interviewTokenServ.getInterviewByValidToken(token);
        return ResponseEntity.ok(interview);
    }

    @Operation(summary = "Validate interview token", description = "Validates an interview token and returns true if the token is valid and not expired, false otherwise")
    @GetMapping("/token/{token}/validate")
    public ResponseEntity<Boolean> validateInterviewToken(@PathVariable String token) {
        boolean isValid = interviewTokenServ.isValidAndNotExpired(token);
        return ResponseEntity.ok(isValid);
    }

    @Operation(summary = "Send interview invitation email", description = "Sends an email to the candidate containing the interview link and scheduled date.")
    @PostMapping("/{interviewId}/sendEmail")
    public ResponseEntity<String> sendEmail(@PathVariable UUID interviewId) {

        SendEmailDTO payload = interviewServ.getEmailInfo(interviewId);

        String html = emailContentBuilder.buildInterviewEmail(
                payload.candidateFullName(),
                payload.offerTitle(),
                payload.link(),
                payload.scheduledDate());

        emailServiceImpl.sendEmail(payload.email(), "Your Interview at NTT DATA", html);
        return ResponseEntity.ok("Email sent successfully!");
    }

    @Operation(summary = "Generate Interview questions by interview Id", description = "It sends a prompt to the AI and then get The question")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @PostMapping("/{interviewId}/generateQuestions")
    public ResponseEntity<List<QuestionDTO>> generateInterviewQuestions(@PathVariable UUID interviewId,
            @RequestBody GenerateInterviewQuestionsRequest generateInterviewQuestionsRequest) {
        List<QuestionDTO> savedQuestions = interviewServ.generateInterviewQuestions(interviewId,
                generateInterviewQuestionsRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedQuestions);
    }

    @Operation(summary = "Get interview questions by interview Id", description = "Return the DTO of questions of that interview")
    @GetMapping("/{interviewId}/questions")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByInterviewId(@PathVariable UUID interviewId) {

        return ResponseEntity.ok(questionServ.findAllQuestionsDTOSByInterviewId(interviewId));
    }

    @Operation(summary = "Generates interview evaluations using AI ", description = """
            This endpoint receives a list of questions and candidate answers for a specific interview,
            and calls an AI service to generate evaluations based on multiple criteria
            (e.g., time management, technical accuracy, job alignment).

            The AI returns scores and feedback for each evaluation type defined in the interview context.
            These evaluations are then persisted to the database.
            """)
    @PostMapping("/{interviewId}/evaluations")
    public ResponseEntity<?> prepareInterviewEvaluations(@PathVariable UUID interviewId,
            @RequestBody InterviewEvaluationsRequestDTO interviewEvaluationsRequest) {
        List<Evaluation> savedEvaluations = interviewServ.prepareInterviewEvaluations(interviewId,
                interviewEvaluationsRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Evaluation is created and saved");
    }

    @Operation(summary = "Get all the evaluations of an interview", description = "Return the evaluations of an interview alongside with their evaluationTypes")
    @GetMapping("/{interviewId}/evaluations")
    public ResponseEntity<InterviewEvaluationDTO> getInterviewEvaluations(@PathVariable UUID interviewId) {
        return ResponseEntity.ok(interviewServ.getInterviewEvaluations(interviewId));
    }

    @Operation(summary = "Get all the audios of the questions of an interview", description = "Returns the zip of the list of the audios")
    @GetMapping("/{interviewId}/generateQuestionsAudios")
    public ResponseEntity<byte[]> getInterviewQuestionsAudios(@PathVariable UUID interviewId) throws IOException {
        return interviewServ.generateInterviewQuestionsAudios(interviewId);
    }

    @Operation(summary = "Get the transcription of an interview", description = "Retrieves the full transcription of a specific interview identified by its ID. "
            +
            "The transcription includes all questions asked by the AI and the corresponding " +
            "answers provided by the candidate, with timestamps relative to the start of the interview.")
    @GetMapping("/{interviewId}/transcription")
    public String getInterviewTranscription(@PathVariable UUID interviewId) throws IOException {
        return interviewServ.getTanscription(interviewId);
    }

    @Operation(summary = "Update the transcription of an interview", description = "Update the full transcription of a specific interview identified by its ID. "
            +
            "The transcription includes all questions asked by the AI and the corresponding " +
            "answers provided by the candidate, with timestamps relative to the start of the interview.")
    @PostMapping("/{interviewId}/transcription")
    public InterviewDTO updateInterviewTranscription(@PathVariable UUID interviewId, @RequestBody String transcription)
            throws IOException {
        return interviewServ.updateTranscription(interviewId, transcription);
    }

}
