package univ.fooddeliv.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String image;
    private Long restaurantId;
    private String restaurantName;
    private Double totalPrice;
    private Integer quantity;
}