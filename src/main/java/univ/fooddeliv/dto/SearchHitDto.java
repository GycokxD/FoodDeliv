package univ.fooddeliv.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchHitDto {
    private String type;
    private Long id;
    private String title;
    private String subtitle;
    private String image;
    private String url;
}
