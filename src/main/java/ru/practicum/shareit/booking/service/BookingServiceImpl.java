package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFromUserDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.enums.BookingState;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final ItemService itemService;
    private final UserService userService;


    private final BookingMapper bookingMapper;

    private final ItemMapper itemMapper;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public BookingDto addNewBooking(long userId, BookingFromUserDto bookingFromUser) {
        UserDto userDto = userService.getUser(userId);
        validateBookingDate(userId, bookingFromUser);
        ItemDto itemDto = getValidatedBookingItem(userId, bookingFromUser);
        LocalDateTime start = bookingFromUser.getStart();
        LocalDateTime end = bookingFromUser.getEnd();

        User booker = userMapper.toUser(userDto);
        Item item = itemMapper.toItem(itemDto);

        Booking booking = bookingMapper.toBooking(booker, item, start, end, BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Добавлено новое бронирование {} от user id={}", savedBooking, userId);

        return bookingMapper.toBookingDto(savedBooking);
    }

    private void validateBookingDate(long userId, BookingFromUserDto booking) {
        LocalDateTime start = booking.getStart();
        LocalDateTime end = booking.getEnd();
        if (start == null || end == null) {
            log.info("У бронирования {} от user id={} не указаны даты бронирования", booking, userId);
            throw new ValidationException();
        }
        if (start.equals(end)) {
            log.info("У бронирования {} от user id={} дата начала бронирования равна дате окончания", booking, userId);
            throw new ValidationException();
        }
        if (start.isAfter(end)) {
            log.info("У бронирования {} от user id={} дата начала бронирования позже даты окончания", booking, userId);
            throw new ValidationException();
        }
        if (start.isBefore(LocalDateTime.now())) {
            log.info("У бронирования {} от user id={} дата начала бронирования раньше текущей даты", booking, userId);
            throw new ValidationException();
        }
        if (end.isBefore(LocalDateTime.now())) {
            log.info("У бронирования {} от user id={} дата окончания бронирования раньше текущей даты", booking, userId);
            throw new ValidationException();
        }
    }

    private ItemDto getValidatedBookingItem(long userId, BookingFromUserDto booking) {
        Long itemId = booking.getItemId();
        if (itemId == null) {
            log.info("У бронирования {} от user id={} указан неверный item id", booking, userId);
            throw new NotFoundException();
        }
        ItemDto item = itemService.getItem(userId, itemId);
        if (!item.getAvailable()) {
            log.info("В бронировании {} от user id={} указана недоступная вещь с id={}", booking, userId, booking.getItemId());
            throw new ValidationException();
        }
        long ownerId = itemService.findItem(itemId).getOwner().getId();
        if (ownerId == userId) {
            log.info("В бронировании {} user id={} пытается забронировать свою вещь id={}", booking, userId, booking.getItemId());
            throw new NotFoundException();
        }
        return itemService.getItem(userId, itemId);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(long userId, long bookingId, String approved) {
        Booking booking = findBooking(bookingId);
        validateBookingUpdate(userId, booking);
        switch (approved) {
            case "true":
                if (booking.getStatus().equals(BookingStatus.APPROVED)) {
                    log.info("Бронирование {} от user id={} уже находится в статусе {}", booking, userId, booking.getStatus());
                    throw new ValidationException();
                }
                booking.setStatus(BookingStatus.APPROVED);
                break;
            case "false":
                if (booking.getStatus().equals(BookingStatus.REJECTED)) {
                    log.info("Бронирование {} от user id={} уже находится в статусе {}", booking, userId, booking.getStatus());
                    throw new ValidationException();
                }
                booking.setStatus(BookingStatus.REJECTED);
                break;
            default:
                throw new RuntimeException();
        }
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Изменено бронирование {} от user id={}", savedBooking, userId);
        return bookingMapper.toBookingDto(savedBooking);
    }

    private Booking findBooking(long bookingId) {
        if (bookingId == 0) {
            throw new ValidationException();
        }
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new NotFoundException();
        }
        return booking.get();
    }

    private void validateBookingUpdate(long userId, Booking booking) {
        UserDto userDto = userService.getUser(userId);
        if (userDto.getId() != booking.getItem().getOwner().getId()) {
            log.info("У позиции {} в бронировании {} указан другой владелец {}, обращается user с id={}",
                    booking.getItem(),
                    booking,
                    booking.getItem().getOwner(),
                    userDto.getId());
            throw new NotFoundException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBooking(long userId, long bookingId) {
        User user = userMapper.toUser(userService.getUser(userId));
        Booking booking = findBooking(bookingId);
        if (!(user.equals(booking.getBooker()) || user.equals(booking.getItem().getOwner()))) {
            log.info("Различаются user id={}, кто ищет бронирование, и автор бронирования id={}" +
                            "или владелец вещи id={}",
                    user.getId(),
                    booking.getBooker().getId(),
                    booking.getItem().getOwner().getId());
            throw new NotFoundException();
        }
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(long userId, BookingState state) {
        User user = userMapper.toUser(userService.getUser(userId));
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBooker(user, Sort.by("end").descending());
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerAndStartIsBeforeAndEndIsAfter(
                        user,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        Sort.by("end").descending());
                break;
            case PAST:
                bookings = bookingRepository.findByBookerAndEndIsBefore(
                        user,
                        LocalDateTime.now(),
                        Sort.by("end").descending());
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerAndStartIsAfter(
                        user,
                        LocalDateTime.now(),
                        Sort.by("end").descending());
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerAndStatusIs(
                        user,
                        BookingStatus.WAITING,
                        Sort.by("end").descending());
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerAndStatusIs(
                        user,
                        BookingStatus.REJECTED,
                        Sort.by("end").descending());
                break;
        }
        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getItemsOwnerBookings(long userId, BookingState state) {
        User user = userMapper.toUser(userService.getUser(userId));
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerIs(user, Sort.by("end").descending());
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerAndStartIsBeforeAndEndIsAfter(
                        user,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        Sort.by("end").descending());
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerAndEndIsBefore(
                        user,
                        LocalDateTime.now(),
                        Sort.by("end").descending());
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerAndStartIsAfter(
                        user,
                        LocalDateTime.now(),
                        Sort.by("end").descending());
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerAndStatusIs(
                        user,
                        BookingStatus.WAITING,
                        Sort.by("end").descending());
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerAndStatusIs(
                        user,
                        BookingStatus.REJECTED,
                        Sort.by("end").descending());
                break;
        }
        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

}
