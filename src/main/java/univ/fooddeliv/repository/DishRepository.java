package univ.fooddeliv.repository;

import univ.fooddeliv.models.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {
    Page<Dish> findByRestaurantId(Long restaurantId, Pageable pageable);

    @Query("""
            SELECT d FROM Dish d
            WHERE d.restaurant.id = :restaurantId
              AND (:minPrice IS NULL OR d.price >= :minPrice)
              AND (:maxPrice IS NULL OR d.price <= :maxPrice)
            """)
    Page<Dish> findMenu(@Param("restaurantId") Long restaurantId,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        Pageable pageable);

    long countByRestaurantId(Long restaurantId);

    List<Dish> findAllByOrderByNameAsc();

    @Query("""
            SELECT d FROM Dish d
            JOIN FETCH d.restaurant
            WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(CAST(d.description AS string)) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY d.name
            """)
    List<Dish> search(@Param("q") String query, Pageable pageable);

    @Query("""
            SELECT d FROM Dish d
            WHERE d.restaurant.id = :restaurantId
              AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(CAST(d.description AS string)) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY d.name
            """)
    List<Dish> searchByRestaurant(@Param("restaurantId") Long restaurantId, @Param("q") String query);

    @Query("""
            SELECT d FROM Dish d
            WHERE d.restaurant.id = :restaurantId
              AND (:minPrice IS NULL OR d.price >= :minPrice)
              AND (:maxPrice IS NULL OR d.price <= :maxPrice)
              AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(CAST(d.description AS string)) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY d.name
            """)
    List<Dish> searchMenu(@Param("restaurantId") Long restaurantId,
                          @Param("q") String query,
                          @Param("minPrice") Double minPrice,
                          @Param("maxPrice") Double maxPrice);
}
