package com.mb.springfileupload.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.springfileupload.domain.File;
import com.mb.springfileupload.dto.FileCreationDto;
import com.mb.springfileupload.repository.FileRepository;
import com.mb.springfileupload.service.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author mb
 */
@SpringBootTest
public class FileResourceTest {

    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper jsonMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();

        fileRepository.deleteAll();
        fileRepository.flush();
    }

    @Test
    public void create_withName_OK() throws Exception {
        final FileCreationDto dto = FileCreationDto.builder()
                .name("test.png")
                .username("user@email.com")
                .build();

        final MockMultipartFile model = getFileModel(dto);
        final MockMultipartFile file = createDummyFile();

        final MvcResult result = mvc.perform(
                MockMvcRequestBuilders.multipart("/api/files")
                        .file(model)
                        .file(file))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        final File parsed = jsonMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {
        });
        Assertions.assertNotNull(parsed);
        Assertions.assertNull(parsed.getData());
        Assertions.assertEquals("test", parsed.getName());
        Assertions.assertEquals("png", parsed.getType());
        Assertions.assertNotNull(parsed.getId());

        final Optional<File> fileOpt = fileRepository.findById(parsed.getId());
        Assertions.assertTrue(fileOpt.isPresent());
        Assertions.assertNotNull(fileOpt.get().getData());
    }

    @Test
    public void create_emptyProperties() throws Exception {
        final FileCreationDto dto = FileCreationDto.builder()
                .name("test.png")
                .build();

        final MockMultipartFile model = getFileModel(dto);
        final MockMultipartFile file = createDummyFile();

        mvc.perform(
                MockMvcRequestBuilders.multipart("/api/files")
                        .file(model)
                        .file(file))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void create_noFile() throws Exception {
        final FileCreationDto dto = FileCreationDto.builder()
                .name("test.png")
                .username("user@email.com")
                .description("description")
                .build();

        final MockMultipartFile model = getFileModel(dto);

        mvc.perform(
                MockMvcRequestBuilders.multipart("/api/files")
                        .file(model))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void read() throws Exception {
        final File toCreate = File.builder()
                .name("test")
                .username("user@email.com")
                .type("pdf")
                .createdAt(LocalDateTime.now())
                .data(new byte[]{})
                .build();
        final File file = fileRepository.save(toCreate);

        final MvcResult result = mvc.perform(
                get("/api/files/{id}", file.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        final File parsed = jsonMapper.readValue(result.getResponse().getContentAsString(), File.class);
        Assertions.assertEquals(file.getId(), parsed.getId());
        Assertions.assertEquals(file.getName(), parsed.getName());
        Assertions.assertEquals(file.getDescription(), parsed.getDescription());
        Assertions.assertEquals(file.getUsername(), parsed.getUsername());
        Assertions.assertEquals(file.getType(), parsed.getType());
        Assertions.assertNotNull(parsed.getCreatedAt());
        Assertions.assertNull(parsed.getData());
    }

    @Test
    public void read_notFound() throws Exception {
        final String id = "NoSuchId";

        mvc.perform(
                get("/api/files/{id}", id))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_OK() throws Exception {
        final File toCreate = File.builder()
                .name("test")
                .username("user@email.com")
                .type("pdf")
                .data(new byte[]{})
                .build();
        final File file = fileRepository.save(toCreate);

        mvc.perform(
                delete("/api/files/{id}", file.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        final Optional<File> fileOpt = fileService.findById(file.getId());
        Assertions.assertFalse(fileOpt.isPresent());
    }

    @Test
    public void download() throws Exception {
        final java.io.File testFile = ResourceUtils.getFile("classpath:dummy-image.png");

        final File toCreate = File.builder()
                .name("test-image")
                .username("user@email.com")
                .type("png")
                .data(Files.readAllBytes(testFile.toPath()))
                .build();
        final File file = fileRepository.save(toCreate);

        final MvcResult result = mvc.perform(
                get("/api/files/{id}/download", file.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    private MockMultipartFile getFileModel(final FileCreationDto dto) throws JsonProcessingException {
        return new MockMultipartFile("model", "", "application/json", jsonMapper.writeValueAsString(dto).getBytes());
    }

    private MockMultipartFile createDummyFile() throws IOException {
        ClassPathResource dummyAttachment = new ClassPathResource("dummy-image.png");
        Assertions.assertTrue(dummyAttachment.isReadable());
        return new MockMultipartFile("file", "dummy-image.png", MimeTypeUtils.IMAGE_PNG_VALUE, FileCopyUtils.copyToByteArray(dummyAttachment.getInputStream()));
    }

}
