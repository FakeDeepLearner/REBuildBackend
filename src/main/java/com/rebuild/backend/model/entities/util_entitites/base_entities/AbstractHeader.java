package com.rebuild.backend.model.entities.util_entitites.base_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.responses.resume_responses.HeaderResponse;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractHeader extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(name = "phone_number")
    @Convert(converter = DatabaseEncryptor.class)
    protected String number;

    @Column(name = "name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    protected String name;

    @Column(name = "email", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    protected String email;

    @ElementCollection
    @CollectionTable(name = "header_links",
            joinColumns = {@JoinColumn(name = "header_id", referencedColumnName = "id")})
    @NonNull
    protected List<String> links;

    public HeaderResponse toResponse(){
        return new HeaderResponse(this.number, this.name, this.email, this.links);
    }
}
