package ma.nttdata.externals.module.offer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ma.nttdata.externals.module.interview.dto.InterviewDTO;

import java.util.List;
import java.util.UUID;

public record OfferDTO(
        UUID id,
        String title,
        String description ,
        @JsonProperty(defaultValue = "null")
        String formattedDescription,
        List<InterviewDTO> interviews

) {
    @Override
    public String toString() {
        // Exclude 'interviews' to keep AI prompt small
        return "OfferDTO[" +
                "id=" + id +
                ", title=" + title +
                ", description=" + description +
                ", formattedDescription=" + formattedDescription +
                "]";
    }
}