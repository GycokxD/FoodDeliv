package univ.fooddeliv.repository;

import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByLiveTrackingTrueAndStatusIn(Collection<OrderStatus> statuses);

    Optional<Order> findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(Long userId, Collection<OrderStatus> statuses);

    Optional<Order> findFirstByUser_IdAndAddressIsNotNullOrderByCreatedAtDesc(Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("select o.status, count(o) from Order o group by o.status")
    List<Object[]> countByStatus();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Order o where o.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
