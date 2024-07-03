package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ItemService itemService;

    @MockBean
    ItemMapper itemMapper;

    @MockBean
    CommentMapper commentMapper;

    @Autowired
    ObjectMapper mapper;

    private static DateTimeFormatter formatter;
    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private List<ItemDto> itemsDto;
    private CommentDto commentDto;

    @BeforeAll
    static void beforeAll() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    }

    @BeforeEach
    void setUp() {
        User user1 = User.builder()
                .id(1L)
                .name("userName1")
                .email("user1@ya.ru")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("userName2")
                .email("user2@ya.ru")
                .build();
        Item item1 = Item.builder()
                .id(1L)
                .name("Товар1")
                .description("Описание товара1")
                .available(false)
                .owner(user1)
                .build();
        Item item2 = Item.builder()
                .id(2L)
                .name("Товар2")
                .description("Описание товара2")
                .available(false)
                .owner(user1)
                .build();
        itemDto1 = ItemDto.builder()
                .id(1L)
                .name("Товар1")
                .description("Описание товара1")
                .available(false)
                .build();
        itemDto2 = ItemDto.builder()
                .id(2L)
                .name("Товар2")
                .description("Описание товара2")
                .available(false)
                .build();
        itemsDto = List.of(itemDto1, itemDto2);

        Comment comment = Comment.builder()
                .id(1L)
                .text("Отзыв1")
                .item(item1)
                .author(user2)
                .created(LocalDateTime.of(2023, 5, 1, 15, 0, 0))
                .build();
        commentDto = CommentDto.builder()
                .id(1L)
                .text("Отзыв1")
                .authorName("userName2")
                .created(LocalDateTime.of(2023, 5, 1, 15, 0, 0))
                .build();

        // Мокируем поведение мапперов
        when(itemMapper.toItemDto(item1)).thenReturn(itemDto1);
        when(itemMapper.toItemDto(item2)).thenReturn(itemDto2);
        when(commentMapper.toCommentDto(comment)).thenReturn(commentDto);
    }

    @Test
    void getAll() throws Exception {
        when(itemService.getOwnerItems(anyLong(), anyInt(), anyInt())).thenReturn(itemsDto);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto1.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto1.getAvailable())))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDto2.getAvailable())));
    }

    @Test

    void search() throws Exception {
        when(itemService.search(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(itemsDto);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "Товар")
                        .param("from", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto1.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto1.getAvailable())))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDto2.getAvailable())));
    }

    @Test
    void getItem() throws Exception {
        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemDto1);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto1.getName())))
                .andExpect(jsonPath("$.description", is(itemDto1.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto1.getAvailable())));
    }

    @Test
    void create() throws Exception {
        when(itemService.addNewItem(anyLong(), any())).thenReturn(itemDto1);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto1.getName())))
                .andExpect(jsonPath("$.description", is(itemDto1.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto1.getAvailable())));
    }

    @Test
    void addComment() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any())).thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated().format(formatter))));
    }

    @Test
    void update() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any())).thenReturn(itemDto2);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto2.getName())))
                .andExpect(jsonPath("$.description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto2.getAvailable())));
    }
}