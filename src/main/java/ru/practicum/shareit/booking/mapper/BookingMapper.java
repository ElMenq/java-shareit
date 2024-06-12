package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.BookingFromUserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toBookingDto(Booking booking);

    Booking toBooking(BookingDto bookingDto);

    @Mapping(target = "id", source = "booking.id")
    @Mapping(target = "bookerId", source = "booking.booker.id")
    BookingForItemDto toBookingForItemDto(Booking booking);

    @Mappings({
            @Mapping(target = "booker", source = "userDto"),
            @Mapping(target = "item", source = "itemDto"),
            @Mapping(target = "start", source = "bookingFromUser.start"),
            @Mapping(target = "end", source = "bookingFromUser.end"),
            @Mapping(target = "status", constant = "WAITING")
    })
    Booking toBooking(BookingFromUserDto bookingFromUser, UserDto userDto, ItemDto itemDto);

}
