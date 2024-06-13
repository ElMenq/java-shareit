package ru.practicum.shareit.item.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerId(long userId);

    @Query(" select i from Item i " +
            "where i.available = true and (upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%')))")
    List<Item> search(String text);

    @Query("SELECT i FROM Item i " +
            "LEFT JOIN FETCH i.bookings b " +
            "LEFT JOIN FETCH i.comments c " +
            "WHERE i.owner.id = :userId")
    List<Item> findAllByOwnerIdWithBookingsAndComments(@Param("userId") long userId);

}
