package ma.nttdata.externals.module.candidate.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ma.nttdata.externals.commons.exception.BadRequestException;
import ma.nttdata.externals.commons.exception.InternalServerException;
import ma.nttdata.externals.commons.exception.ResourceNotFoundException;
import ma.nttdata.externals.module.candidate.constants.LanguageLevel;
import ma.nttdata.externals.module.candidate.dto.CandidateDTO;
import ma.nttdata.externals.module.candidate.entity.*;
import ma.nttdata.externals.module.candidate.mapper.CandidateMapper;
import ma.nttdata.externals.module.candidate.repository.CandidateRepository;
import ma.nttdata.externals.module.candidate.repository.CityRepository;
import ma.nttdata.externals.module.candidate.repository.CountryRepository;
import ma.nttdata.externals.module.candidate.repository.LanguageRepository;
import ma.nttdata.externals.module.candidate.service.CandidateSrv;
import ma.nttdata.externals.module.offer.dto.OfferCandidatesDTO;
import ma.nttdata.externals.module.offer.dto.OfferFormattedDescriptionDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidateSrvImpl implements CandidateSrv {
    private final CandidateMapper mapper;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final LanguageRepository languageRepository;

    public CandidateSrvImpl(CandidateMapper candidateMapper,
                            CandidateRepository candidateRepository,
                            CountryRepository countryRepository,
                            CityRepository cityRepository,
                            LanguageRepository languageRepository) {
        this.mapper = candidateMapper;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.languageRepository = languageRepository;
    }

    @Override
    public CandidateDTO save(CandidateDTO candidateDTO) {
        try {
            if (candidateDTO == null) {
                throw new BadRequestException("Candidate data is required");
            }

            Candidate candidate = mapper.candidateDTOToCandidate(candidateDTO);
            if (candidate.getAddress() != null && candidate.getAddress().getCountry() != null) {
                Country reqCountry = candidate.getAddress().getCountry();
                Optional<Country> existingCountry = Optional.empty();
                
                if (reqCountry.getEnglishName() != null) {
                    existingCountry = countryRepository.findByEnglishName(reqCountry.getEnglishName());
                }
                if (existingCountry.isEmpty() && reqCountry.getName() != null) {
                    existingCountry = countryRepository.findByName(reqCountry.getName());
                }

                if (existingCountry.isPresent()) {
                    candidate.getAddress().setCountry(existingCountry.get());
                    if (candidate.getAddress().getCity() != null) {
                        candidate.getAddress().getCity().setCountry(existingCountry.get());
                    }
                } else {
                    if (reqCountry.getEnglishName() == null) {
                        reqCountry.setEnglishName(reqCountry.getName());
                    }
                }
            }
            
            if (candidate.getAddress() != null && candidate.getAddress().getCity() != null) {
                Optional<City> existingCity = cityRepository.findByName(candidate.getAddress().getCity().getName());
                existingCity.ifPresent(candidate.getAddress()::setCity);
            }

            Candidate savedCandidate = candidateRepository.save(candidate);
            return mapper.candidateToCandidateDTO(savedCandidate);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Error creating candidate", e);
        }
    }

    @Override
    @Transactional
    public CandidateDTO update(UUID id, CandidateDTO candidateDTO) {
        try {
            if (id == null) {
                throw new BadRequestException("Candidate ID is required");
            }

            if (candidateDTO == null) {
                throw new BadRequestException("Candidate data is required");
            }

            Optional<Candidate> existingCandidateOpt = candidateRepository.findById(id);
            if (existingCandidateOpt.isEmpty()) {
                throw new ResourceNotFoundException("Candidate", id);
            }

            Candidate updatedCandidate = mapper.candidateDTOToCandidate(candidateDTO);
            Candidate savedCandidate = candidateRepository.save(updatedCandidate);
            return mapper.candidateToCandidateDTO(savedCandidate);
        } catch (ResourceNotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Error updating candidate", e);
        }
    }

    @Override
    public List<CandidateDTO> getAllCandidates() {
        try {
            List<Candidate> candidates = candidateRepository.findAll();

            if (candidates.isEmpty()) {
                throw new ResourceNotFoundException("No candidates found");
            }

            return candidates.stream()
                    .map(mapper::candidateToCandidateDTO)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Error retrieving candidates", e);
        }
    }

    @Override
    public CandidateDTO getById(UUID id) {
        try {
            if (id == null) {
                throw new BadRequestException("Candidate ID is required");
            }

            Optional<Candidate> candidate = candidateRepository.findById(id);
            if (candidate.isEmpty()) {
                throw new EntityNotFoundException("Candidate id not found : " + id);
            }

            return mapper.candidateToCandidateDTO(candidate.get());
        } catch (EntityNotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Error retrieving candidate", e);
        }
    }


    @Override
    public boolean delete(UUID id) {
        try {
            if (id == null) {
                throw new BadRequestException("Candidate ID is required");
            }

            Optional<Candidate> candidate = candidateRepository.findById(id);
            if (candidate.isEmpty()) {
                throw new ResourceNotFoundException("Candidate", id);
            }

            candidateRepository.deleteById(id);
            return true;
        } catch (ResourceNotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Error deleting candidate", e);
        }
    }

    @Override
    public List<CandidateDTO> getCandidates() {
        try {
            List<Candidate> candidates = candidateRepository.findAll();
            return candidates.stream()
                    .map(mapper::candidateToCandidateDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new InternalServerException("Error retrieving candidates", e);
        }
    }

    @Override
    public Map<String, Long> getCandidatesByLanguage() {
        try {
            List<Object[]> results = languageRepository.countCandidatesByLanguage();
            Map<String, Long> languageCountMap = new HashMap<>();
            
            for (Object[] result : results) {
                String language = (String) result[0];
                long count = (Long) result[1];
                languageCountMap.put(language, count);
            }
            
            return languageCountMap;
        } catch (Exception e) {
            throw new InternalServerException("Error retrieving candidates by language", e);
        }
    }

    @Override
    public Map<String, Long> getCandidatesBySkill() {
        try {
                        return candidateRepository.findAll().stream()
                    .flatMap(candidate -> candidate.getSkills().stream())
                    .collect(Collectors.groupingBy(Skill::getSkillName, Collectors.counting()));
        } catch (Exception e) {
            throw new InternalServerException("Error retrieving candidates by skill", e);
        }
    }

    @Override
    public Long getTotalCandidates() {
        try {
            return candidateRepository.count();
        } catch (Exception e) {
            throw new InternalServerException("Error retrieving total candidates count", e);
        }
    }
    @Override
    public List<String> getDistinctMainTechs() {
        return candidateRepository.findDistinctMainTechs();
    }


    @Override
    public List<OfferCandidatesDTO> findRecommendedCandidates(OfferFormattedDescriptionDTO offerDTO) {
        List<String> offerLanguageNames = offerDTO.languages() == null ? List.of() :
                offerDTO.languages().stream()
                        .map(l -> l.languageName().toLowerCase(Locale.ROOT).trim())
                        .toList();

        List<Candidate> roughMatches;
        if (offerLanguageNames.isEmpty()) {
            roughMatches = candidateRepository.findCandidatesRoughMatchNoLang(
                    offerDTO.mainTech(),
                    offerDTO.yearsOfExperience()
            );
        } else {
            roughMatches = candidateRepository.findCandidatesRoughMatch(
                    offerDTO.mainTech(),
                    offerDTO.yearsOfExperience(),
                    offerLanguageNames
            );
        }
        List<OfferCandidatesDTO> candidateDTOs = roughMatches.stream()
                .map(mapper::toOfferCandidatesDTO)
                .filter(c -> matchLanguagesWithLevel(c, offerDTO))
                .toList();
        return candidateDTOs;
    }

    private boolean matchLanguagesWithLevel(OfferCandidatesDTO candidateDTO, OfferFormattedDescriptionDTO offerDTO) {
        if (offerDTO.languages() == null || offerDTO.languages().isEmpty()) {
            return true; // no language requirement
        }
        if (candidateDTO.languages() == null || candidateDTO.languages().isEmpty()) {
            return false; // candidate has no languages
        }

        for (var offerLang : offerDTO.languages()) {
            String offerName = offerLang.languageName().toLowerCase(Locale.ROOT).trim();
            LanguageLevel offerLevel = offerLang.level();

            for (var candidateLang : candidateDTO.languages()) {
                String candidateName = candidateLang.languageInEnglish().toLowerCase(Locale.ROOT).trim();
                LanguageLevel candidateLevel = candidateLang.level();

                if (candidateName.equals(offerName)) {
                    if (isLevelSufficient(candidateLevel, offerLevel)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isLevelSufficient(LanguageLevel candidateLevel, LanguageLevel offerLevel) {
        // relies on the enum order (BEGINNER < INTERMEDIATE < ADVANCED < NATIVE)
        return candidateLevel.ordinal() >= offerLevel.ordinal();
    }

}
