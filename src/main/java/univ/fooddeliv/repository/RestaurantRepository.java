package univ.fooddeliv.repository;

import univ.fooddeliv.models.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.dishes WHERE r.id = :id")
    Optional<Restaurant> findWithDishesById(@Param("id") Long id);

    @Query("SELECT r FROM Restaurant r")
    Page<Restaurant> findAll(Pageable pageable);

    @Query("""
            SELECT r FROM Restaurant r
            WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(r.address) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY r.name
            """)
    List<Restaurant> search(@Param("q") String query, Pageable pageable);
}