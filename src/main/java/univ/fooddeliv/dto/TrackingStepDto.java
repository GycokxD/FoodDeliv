package univ.fooddeliv.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackingStepDto {
    private final String label;
    private final String state;
}
