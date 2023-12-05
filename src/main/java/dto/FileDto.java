package dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDto {

    @JsonProperty("filename")
    private String fileName;
    private String fileType;
    private byte[] fileData;
    private String hash;
    private Long size;
}