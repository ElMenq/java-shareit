package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class ItemRequestShortDto {

    private long id;

    @NotBlank
    @Size
    private String description;

}