package com.mb.springfileupload.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author mb
 */
@Entity
@Table(name = "file")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"createdAt"})
public class File {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id")
    private String id;

    @NotBlank
    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "description")
    private String description;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "type", nullable = false)
    private String type;

    @JsonIgnore
    @Column(name = "data", nullable = false)
    @Lob
    private byte[] data;

    @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
