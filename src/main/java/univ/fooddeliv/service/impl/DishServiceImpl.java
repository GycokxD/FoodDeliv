package univ.fooddeliv.service.impl;

import jakarta.servlet.http.Cookie;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.models.Dish;
import univ.fooddeliv.repository.DishRepository;
import univ.fooddeliv.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;

    @Override
    public Page<DishDto> getDishesByRestaurantId(Long restaurantId, int page, int size) {
        return getMenu(restaurantId, page, size, "name", null, null);
    }

    @Override
    public Page<DishDto> getMenu(Long restaurantId, int page, int size, String sort, Double minPrice, Double maxPrice) {
        Sort order = switch (sort == null ? "" : sort) {
            case "price_asc" -> Sort.by("price").ascending().and(Sort.by("name").ascending());
            case "price_desc" -> Sort.by("price").descending().and(Sort.by("name").ascending());
            default -> Sort.by("name").ascending();
        };
        Page<Dish> dishes = dishRepository.findMenu(
                restaurantId,
                minPrice,
                maxPrice,
                PageRequest.of(page, size, order)
        );
        return dishes.map(this::convertToDto);
    }

    @Override
    public List<DishDto> searchByRestaurant(Long restaurantId, String query) {
        return searchMenu(restaurantId, query, null, null);
    }

    @Override
    public List<DishDto> searchMenu(Long restaurantId, String query, Double minPrice, Double maxPrice) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return dishRepository.searchMenu(restaurantId, query.trim(), minPrice, maxPrice).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<DishDto> getDishesFromCookie(Cookie[] cookie) {
        String dishesFromCookie = "";
        if (cookie != null) {
            for (Cookie c : cookie) {
                if (c.getName().equalsIgnoreCase("cart")) {
                    dishesFromCookie = c.getValue();
                    break;
                }
            }
        }
        if (!dishesFromCookie.isBlank()) {
            String[] dishes = dishesFromCookie.split("!");
            List<DishDto> dishDtos = new ArrayList<>();
            for (String d : dishes) {
                Dish dish = dishRepository.findById(Long.parseLong(d.substring(0, d.indexOf("#")))).orElse(null);
                if (dish != null) {
                    DishDto dto = convertToDto(dish);
                    int quantity = Integer.parseInt(d.substring(d.indexOf("#") + 1));
                    if (quantity < 0) {
                        quantity = 1;
                    }
                    dto.setQuantity(quantity);
                    dto.setTotalPrice(getTotalPrice(dto));
                    dishDtos.add(dto);
                }
            }
            return dishDtos;
        }
        return null;
    }

    private DishDto convertToDto(Dish dish) {
        return DishDto.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .image(toStaticImagePath(dish.getImage()))
                .restaurantId(dish.getRestaurant().getId())
                .restaurantName(dish.getRestaurant().getName())
                .quantity(1)
                .build();
    }

    private String toStaticImagePath(String image) {
        if (image == null || image.isBlank()) {
            return "/static/images/dish-placeholder.svg";
        }
        if (image.startsWith("/static/") || image.startsWith("/uploads/") || image.startsWith("http")) {
            return image;
        }
        return "/static/" + image;
    }

    private Double getTotalPrice(DishDto dishDto) {
        return dishDto.getPrice() * dishDto.getQuantity();
    }

    @Override
    public Dish findDishById(Long id) {
        return dishRepository.findById(id).orElse(null);
    }

}
