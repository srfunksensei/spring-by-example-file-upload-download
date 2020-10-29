package com.mb.springfileupload.service;

import com.mb.springfileupload.domain.File;
import com.mb.springfileupload.dto.FileCreationDto;
import com.mb.springfileupload.repository.FileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author mb
 */
@SpringBootTest
public class FileServiceTest {

    @Autowired
    private FileService underTest;

    @Autowired
    private FileRepository fileRepository;

    @BeforeEach
    public void setup() {
        fileRepository.deleteAll();
        fileRepository.flush();
    }

    @Test
    public void create_withName() {
        final String name = "test", type = "png";
        final FileCreationDto dto = FileCreationDto.builder()
                .username("user@email.com")
                .name(name + "." + type)
                .description("description")
                .data(new byte[]{})
                .build();

        final File file = underTest.create(dto, "name.zip");
        Assertions.assertEquals(dto.getUsername(), file.getUsername());
        Assertions.assertEquals(dto.getDescription(), file.getDescription());
        Assertions.assertEquals(dto.getData(), file.getData());
        Assertions.assertEquals(name, file.getName());
        Assertions.assertEquals(type, file.getType());
        Assertions.assertNotNull(file.getCreatedAt());
    }

    @Test
    public void create_withFileName() {
        final String name = "name", type = "zip";
        final FileCreationDto dto = FileCreationDto.builder()
                .username("user@email.com")
                .description("description")
                .data(new byte[]{})
                .build();

        final File file = underTest.create(dto, name + "." + type);
        Assertions.assertEquals(dto.getUsername(), file.getUsername());
        Assertions.assertEquals(dto.getDescription(), file.getDescription());
        Assertions.assertEquals(dto.getData(), file.getData());
        Assertions.assertEquals(name, file.getName());
        Assertions.assertEquals(type, file.getType());
        Assertions.assertNotNull(file.getCreatedAt());
    }
}
