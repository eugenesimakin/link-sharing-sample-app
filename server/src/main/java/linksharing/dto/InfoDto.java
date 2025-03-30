package linksharing.dto;

import java.util.List;

public record InfoDto(String email, String firstName, String lastName, String imageUrl, List<LinkDto> links) {
}
