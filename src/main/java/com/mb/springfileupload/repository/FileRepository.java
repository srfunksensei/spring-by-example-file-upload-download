package com.mb.springfileupload.repository;

import com.mb.springfileupload.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author mb
 */
public interface FileRepository extends JpaRepository<File, String> {
}
