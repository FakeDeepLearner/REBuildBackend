package com.rebuild.backend.model.entities.forum_entities;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String title;

    @NonNull
    private String content;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Resume resume;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private User creatingUser;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            mappedBy = "associatedPost")
    private List<Comment> comments = new ArrayList<>();

}
