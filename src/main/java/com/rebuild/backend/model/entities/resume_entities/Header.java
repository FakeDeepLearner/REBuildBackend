package com.rebuild.backend.model.entities.resume_entities;


import com.rebuild.backend.model.entities.superclasses.SuperclassHeader;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "headers")
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Header extends SuperclassHeader implements ResumeProperty{

    public Header(PhoneNumber number, String firstName, String lastName, String email){
        super(number, firstName, lastName, email);
    }

    @OneToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", referencedColumnName = "id",
    foreignKey = @ForeignKey(name = "head_fk_resume_id"))
    private Resume resume;


    @Override
    public String toString() {
        return "HEADER:\n" +
                "\tPhone Number: " + number.fullNumber() + "\n" +
                "\tName: " + firstName + " " + lastName + "\n" +
                "\tEmail: " + email + "\n\n\n";
    }


}
