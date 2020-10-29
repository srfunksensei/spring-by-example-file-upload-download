package com.mb.springfileupload.service;

import com.mb.springfileupload.domain.File;
import com.mb.springfileupload.dto.FileCreationDto;
import com.mb.springfileupload.repository.FileRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author mb
 */
@Service
@AllArgsConstructor
public class FileService {

    private FileRepository fileRepository;

    @Transactional
    public File create(@Valid final FileCreationDto dto, final String fileName) {
        final String actualName = StringUtils.isNotBlank(dto.getName()) ? dto.getName() : fileName;
        final Pair<String, String> nameAndExtension = extractNameAndExtension(actualName);

        final File toCreate = File.builder()
                .username(dto.getUsername())
                .description(StringUtils.trimToNull(dto.getDescription()))
                .name(nameAndExtension.getLeft())
                .type(nameAndExtension.getRight())
                .data(dto.getData())
                .createdAt(LocalDateTime.now())
                .build();

        return fileRepository.save(toCreate);
    }

    private Pair<String, String> extractNameAndExtension(final String fileName) {
        final int lastIndexOf = fileName.lastIndexOf(".");
        return new ImmutablePair<>(fileName.substring(0, lastIndexOf), fileName.substring(lastIndexOf + 1));
    }

    public Optional<File> findById(final String id) {
        return fileRepository.findById(id);
    }

    public void delete(final String id) {
        fileRepository.deleteById(id);
    }
}
