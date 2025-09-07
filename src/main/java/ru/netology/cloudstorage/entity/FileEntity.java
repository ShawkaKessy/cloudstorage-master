package ru.netology.cloudstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "files",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "filename"}))
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String filename;

    @Lob
    @Column(nullable = false)
    private byte[] content;
}
