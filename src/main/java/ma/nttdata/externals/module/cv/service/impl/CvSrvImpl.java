package ma.nttdata.externals.module.cv.service.impl;

import ma.nttdata.externals.commons.constants.JsonExtractionPromptConstants;
import ma.nttdata.externals.commons.exception.BadRequestException;
import ma.nttdata.externals.commons.exception.InternalServerException;
import ma.nttdata.externals.module.cv.dto.CvFileDTO;
import ma.nttdata.externals.module.cv.dto.FileDTO;
import ma.nttdata.externals.module.cv.service.CvSrv;
import ma.nttdata.externals.module.prompt.constants.PromptEnum;
import ma.nttdata.externals.module.prompt.repository.PromptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CvSrvImpl implements CvSrv {
    private static final Logger log = LoggerFactory.getLogger(CvSrvImpl.class);

    private final boolean mockFlag;
    private final RestClient aiRestClient;
    private final PromptRepository promptRepository;

    public CvSrvImpl(@Value("${app.mock.flag}") boolean mockFlag,
                     @Qualifier("aiServiceClient") RestClient aiRestClient,
                     PromptRepository promptRepository) {
        this.mockFlag = mockFlag;
        this.aiRestClient = aiRestClient;
        this.promptRepository = promptRepository;
    }

    @Override
    public String extractCandidateInfo(CvFileDTO cvFileDTO) {
        log.info("extractCandidateInfo called. mockFlag={}", mockFlag);
        try {
            // Validate input
            if (cvFileDTO == null || cvFileDTO.b64EFile() == null || cvFileDTO.b64EFile().isEmpty()) {
                log.warn("Validation failed: CvFileDTO or its base64 file content is null/empty");
                throw new BadRequestException("CV file data is required");
            }

            if (mockFlag) {
                log.info("mockFlag is true, returning mock JSON extraction data");
                return JsonExtractionPromptConstants.jsonMock;
            } else {
                log.info("mockFlag is false, querying real AI endpoint");
                String extracted = getExtractedData(cvFileDTO);
                log.info("Successfully fetched AI extraction. Result length: {} characters", extracted != null ? extracted.length() : 0);
                return extracted;
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception in extractCandidateInfo: {}", e.getMessage(), e);
            throw new InternalServerException("Error processing CV file", e);
        }
    }

    private String getExtractedData(CvFileDTO cvFileDTO) {
        log.info("Looking up prompt for code: {}", PromptEnum.CV_EXTRACTION.name());
        var prompt = promptRepository.findByPromptCode(PromptEnum.CV_EXTRACTION.name())
                .orElseThrow(() -> {
                    log.error("Prompt with code '{}' not found in database", PromptEnum.CV_EXTRACTION.name());
                    return new InternalServerException("Prompt with code '" + PromptEnum.CV_EXTRACTION.name() + "' not found");
                });

        var promptText = prompt.getPromptDesc();
        var promptSchema = prompt.getSchema();
        log.info("Found prompt. Description length: {}, Schema length: {}", 
                promptText != null ? promptText.length() : 0, 
                promptSchema != null ? promptSchema.length() : 0);

        var fileDTO = new FileDTO(promptText, cvFileDTO.b64EFile(),  promptSchema);
        log.info("Sending POST request to general-ai service endpoint /extract...");
        try {
            String response = aiRestClient.post()
                    .uri("/extract")
                    .body(fileDTO)
                    .retrieve()
                    .body(String.class);
            log.info("Received successful response from general-ai");
            return response;
        } catch (Exception e) {
            log.error("HTTP request to general-ai service failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}

