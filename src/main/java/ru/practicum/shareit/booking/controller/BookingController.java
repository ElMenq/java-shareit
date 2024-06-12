package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFromUserDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.enums.BookingState;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> add(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                          @RequestBody BookingFromUserDto bookingFromUser) {
        log.info("Received POST-request at /bookings endpoint from user id={}", userId);
        return ResponseEntity.ok().body(bookingService.addNewBooking(userId, bookingFromUser));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> update(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                             @PathVariable long bookingId,
                                             @RequestParam String approved) {
        log.info("Received PATCH-request at /bookings/{} endpoint from user id={}", bookingId, userId);
        return ResponseEntity.ok().body(bookingService.updateBooking(userId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> get(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                          @PathVariable long bookingId) {
        log.info("Received GET-request at /bookings/{} endpoint from user id={}", userId, bookingId);
        return ResponseEntity.ok().body(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getUserBookings(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                                            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Received GET-request at /bookings endpoint from user id={} with state={}", userId, state);
        BookingState bookingState = resolveBookingState(state);
        List<BookingDto> bookings = bookingService.getUserBookings(userId, bookingState);
        return ResponseEntity.ok().body(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getItemsOwnerBookings(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                                                  @RequestParam(defaultValue = "ALL") String state) {
        log.info("Received GET-request at /bookings/owner endpoint from user id={} with state={}", userId, state);
        BookingState bookingState = resolveBookingState(state);
        List<BookingDto> bookings = bookingService.getItemsOwnerBookings(userId, bookingState);
        return ResponseEntity.ok().body(bookings);
    }

    private BookingState resolveBookingState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

}
