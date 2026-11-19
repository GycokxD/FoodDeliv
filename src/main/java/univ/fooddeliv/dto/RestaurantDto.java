package univ.fooddeliv.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDto {
    private Long id;
    private String name;
    private String address;
    private String imageUrl;
    private LocalDateTime createdAt;
    private List<DishDto> dishes;
}