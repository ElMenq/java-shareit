package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

import java.util.List;

@Data
@Builder
public class ItemDto {

    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private List<CommentDto> comments;
    private List<BookingDto> bookings; // Добавляем поле для списка бронирований

    // Метод для установки списка бронирований
    public void setBookingsList(List<BookingDto> bookings) {
        this.bookings = bookings;
    }
}
