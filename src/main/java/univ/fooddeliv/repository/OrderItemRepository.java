package univ.fooddeliv.repository;

import univ.fooddeliv.models.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByDish_Id(Long dishId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from OrderItem oi where oi.order.id in (select o.id from Order o where o.user.id = :userId)")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("""
            select r.name, coalesce(sum(oi.price), 0)
            from OrderItem oi
            join oi.dish d
            join d.restaurant r
            join oi.order o
            where o.status <> univ.fooddeliv.models.OrderStatus.CANCELLED
            group by r.id, r.name
            order by sum(oi.price) desc
            """)
    List<Object[]> revenueByRestaurant(Pageable pageable);

    @Query("""
            select d.name, r.name, coalesce(sum(oi.quantity), 0), coalesce(sum(oi.price), 0)
            from OrderItem oi
            join oi.dish d
            join d.restaurant r
            join oi.order o
            where o.status <> univ.fooddeliv.models.OrderStatus.CANCELLED
            group by d.id, d.name, r.name
            order by sum(oi.quantity) desc
            """)
    List<Object[]> topDishes(Pageable pageable);
}
