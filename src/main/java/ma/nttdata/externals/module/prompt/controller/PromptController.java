package ma.nttdata.externals.module.prompt.controller;

import jakarta.validation.Valid;
import ma.nttdata.externals.module.prompt.dto.PromptDTO;
import ma.nttdata.externals.module.prompt.service.PromptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prompts")
@PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping
    public ResponseEntity<PromptDTO> createPrompt(@Valid @RequestBody PromptDTO promptDTO) {
        PromptDTO createdPrompt = promptService.createPrompt(promptDTO);
        return new ResponseEntity<>(createdPrompt, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDTO> getPromptById(@PathVariable UUID id) {
        return promptService.getPromptById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PromptDTO>> getAllPrompts() {
        List<PromptDTO> prompts = promptService.getAllPrompts();
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<PromptDTO>> getAllPromptsPaginated(@PageableDefault(size = 10) Pageable pageable) {
        Page<PromptDTO> prompts = promptService.getAllPromptsPaginated(pageable);
        return ResponseEntity.ok(prompts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptDTO> updatePrompt(@Valid @PathVariable UUID id, @RequestBody PromptDTO promptDTO) {
        if (!promptService.getPromptById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        promptDTO = promptService.updatePrompt(id, promptDTO);
        return ResponseEntity.ok(promptDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable UUID id) {
        if (!promptService.getPromptById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        promptService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }
}