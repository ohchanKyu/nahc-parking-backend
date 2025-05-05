package kr.ac.dankook.parkingApplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRequest {

    @NotBlank(message = "Region Code is Required.")
    private String regionCode;
    private String type;
    @JsonProperty("isOpen")
    private boolean isOpen;
    @JsonProperty("isCurrent")
    private boolean isCurrent;
    @JsonProperty("isFree")
    private boolean isFree;
}
