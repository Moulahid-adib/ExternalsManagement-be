package ma.nttdata.externals.module.cv.controller;

import ma.nttdata.externals.module.candidate.dto.CandidateDTO;
import ma.nttdata.externals.module.candidate.service.CandidateSrv;
import ma.nttdata.externals.module.cv.dto.CvFileDTO;
import ma.nttdata.externals.module.cv.service.CvSrv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cv")
public class CvController {

    private static final Logger log = LoggerFactory.getLogger(CvController.class);

    private final CvSrv cvSrv;
    private final CandidateSrv candidateSrv;

    public CvController(CvSrv cvSrv , CandidateSrv candidateSrv) {
        this.cvSrv = cvSrv;
        this.candidateSrv = candidateSrv;
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extractCandidateInfo(@RequestBody CvFileDTO cvFileDTO) {
        log.info("Received request on /api/v1/cv/extract. File size: {} characters",
                (cvFileDTO != null && cvFileDTO.b64EFile() != null) ? cvFileDTO.b64EFile().length() : 0);
        try {
            var extractedData = cvSrv.extractCandidateInfo(cvFileDTO);
            log.info("Successfully extracted candidate info from CV");
            return ResponseEntity.ok(extractedData);
        } catch (Exception e) {
            log.error("Error during CV extraction in controller: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveCandidateFromCv(@RequestBody CandidateDTO candidateDTO) {
        log.info("Received request on /api/v1/cv/save for candidate: {}", 
                candidateDTO != null ? candidateDTO.fullName() : "null");
        try {
            CandidateDTO savedCandidate = candidateSrv.save(candidateDTO);
            log.info("Successfully saved candidate. ID: {}", savedCandidate.id());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedCandidate);
        } catch (Exception e) {
            log.error("Error saving candidate in controller: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving candidate: " + e.getMessage());
        }
    }

}

