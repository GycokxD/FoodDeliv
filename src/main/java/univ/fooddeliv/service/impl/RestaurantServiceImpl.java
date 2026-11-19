package univ.fooddeliv.service.impl;

import jakarta.persistence.EntityNotFoundException;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.dto.RestaurantDto;
import univ.fooddeliv.dto.SearchHitDto;
import univ.fooddeliv.models.Dish;
import univ.fooddeliv.models.Restaurant;
import univ.fooddeliv.repository.DishRepository;
import univ.fooddeliv.repository.RestaurantRepository;
import univ.fooddeliv.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;

    @Override
    public Page<RestaurantDto> getAllRestaurants(int page, int size) {
        Page<Restaurant> restaurants = restaurantRepository.findAll(
                PageRequest.of(page, size, Sort.by("name").ascending())
        );
        return restaurants.map(this::convertToDto);
    }

    @Override
    public RestaurantDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findWithDishesById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        return convertToDto(restaurant);
    }

    @Override
    public List<SearchHitDto> search(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        String q = query.trim();
        List<SearchHitDto> hits = new ArrayList<>();
        for (Restaurant restaurant : restaurantRepository.search(q, PageRequest.of(0, 6))) {
            hits.add(SearchHitDto.builder()
                    .type("restaurant")
                    .id(restaurant.getId())
                    .title(restaurant.getName())
                    .subtitle(restaurant.getAddress())
                    .image(toStaticImagePath(restaurant.getImage()))
                    .url("/restaurants/" + restaurant.getId())
                    .build());
        }
        for (Dish dish : dishRepository.search(q, PageRequest.of(0, 8))) {
            String price = dish.getPrice() == null ? "" : dish.getPrice().intValue() + " сом";
            hits.add(SearchHitDto.builder()
                    .type("dish")
                    .id(dish.getId())
                    .title(dish.getName())
                    .subtitle(dish.getRestaurant().getName() + " · " + price)
                    .image(toStaticImagePath(dish.getImage()))
                    .url("/restaurants/" + dish.getRestaurant().getId() + "#dish-" + dish.getId())
                    .build());
        }
        return hits;
    }

    private RestaurantDto convertToDto(Restaurant restaurant) {
        return RestaurantDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .imageUrl(toStaticImagePath(restaurant.getImage()))
                .createdAt(restaurant.getCreatedAt())
                .dishes(restaurant.getDishes().stream()
                        .map(dish -> DishDto.builder()
                                .id(dish.getId())
                                .name(dish.getName())
                                .description(dish.getDescription())
                                .price(dish.getPrice())
                                .image(toStaticImagePath(dish.getImage()))
                                .restaurantId(restaurant.getId())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private String toStaticImagePath(String image) {
        if (image == null || image.isBlank()) {
            return "/static/images/restaurant-placeholder.svg";
        }
        if (image.startsWith("/static/") || image.startsWith("/uploads/") || image.startsWith("http")) {
            return image;
        }
        return "/static/" + image;
    }
}
