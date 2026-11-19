package univ.fooddeliv.service;

import univ.fooddeliv.dto.RestaurantDto;
import univ.fooddeliv.dto.SearchHitDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RestaurantService {
    Page<RestaurantDto> getAllRestaurants(int page, int size);

    RestaurantDto getRestaurantById(Long id);

    List<SearchHitDto> search(String query);
}
