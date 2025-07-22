package com.rebuild.backend.model.entities.resume_entities;


import com.rebuild.backend.model.entities.superclasses.SuperclassHeader;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "headers")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class Header extends SuperclassHeader implements ResumeProperty{

    public Header(String number, String firstName, String lastName, String email){
        super(number, firstName, lastName, email);
    }



    @Override
    public String toString() {
        return "HEADER:\n" +
                "\tPhone Number: " + number + "\n" +
                "\tName: " + firstName + " " + lastName + "\n" +
                "\tEmail: " + email + "\n\n\n";
    }


}
