package ma.nttdata.externals.module.candidate.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ma.nttdata.externals.commons.config.NullSafeLocalDateDeserializer;
import ma.nttdata.externals.module.candidate.constants.GenderEnum;
import ma.nttdata.externals.module.cv.dto.CvFileDTO;
import ma.nttdata.externals.module.interview.dto.InterviewDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CandidateDTO(
        UUID id,
        String fullName,
        @JsonDeserialize(using = NullSafeLocalDateDeserializer.class)
        LocalDate birthDate,
        int yearsOfExperience,
        GenderEnum gender,
        String mainTech,
        String summary,
        List<ContactDTO> contacts,
        List<ExperienceDTO> experiences,
        List<SkillDTO> skills,
        List<EducationDTO> educations,
        List<CvFileDTO> cvFiles,
        AddressDTO address,
        List<LanguageDTO> naturalLanguages,
        List<InterviewDTO> interviews

) {
    @Override
    public String toString() {
        // Exclude 'interviews' (previous transcriptions) and raw b64 CV files to keep AI prompt small
        return "CandidateDTO[" +
                "id=" + id +
                ", fullName=" + fullName +
                ", birthDate=" + birthDate +
                ", yearsOfExperience=" + yearsOfExperience +
                ", gender=" + gender +
                ", mainTech=" + mainTech +
                ", summary=" + summary +
                ", contacts=" + contacts +
                ", experiences=" + experiences +
                ", skills=" + skills +
                ", educations=" + educations +
                ", address=" + address +
                ", naturalLanguages=" + naturalLanguages +
                "]";
    }
}
