package com.example.transfereservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client
{
    private Long id;
    private String cin;
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private String phoneNumber;
    private String email;
}
