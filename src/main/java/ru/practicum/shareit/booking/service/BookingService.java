package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFromUserDto;
import ru.practicum.shareit.enums.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto addNewBooking(long userId, BookingFromUserDto bookingFromUser);

    BookingDto updateBooking(long userId, long bookingId, String approved);

    BookingDto getBooking(long userId, long bookingId);

    List<BookingDto> getUserBookings(long userId, String state);

    List<BookingDto> getItemsOwnerBookings(long userId, String state);

    List<BookingDto> getBookings(long userId, BookingState state);

}
