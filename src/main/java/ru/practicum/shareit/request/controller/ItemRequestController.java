package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestBody ItemRequestShortDto itemRequestShortDto) {
        log.info("Received POST-request at /requests endpoint from user id={} with request body: {}", userId, itemRequestShortDto);
        return ResponseEntity.ok().body(itemRequestService.create(userId, itemRequestShortDto));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received GET-request at /requests endpoint from user id={}", userId);
        return ResponseEntity.ok().body(itemRequestService.getAll(userId));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @PathVariable long requestId) {
        log.info("Received GET-request at /requests/{} endpoint from user id={}", requestId, userId);
        return ResponseEntity.ok().body(itemRequestService.getItemRequest(userId, requestId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                                        @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                        @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Received GET-request at /requests/all?from={}&size={} endpoint from user id={}", from, size, userId);
        return ResponseEntity.ok().body(itemRequestService.search(userId, from, size));
    }

}