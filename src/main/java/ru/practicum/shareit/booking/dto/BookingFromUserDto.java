package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingFromUserDto {

    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;

}
