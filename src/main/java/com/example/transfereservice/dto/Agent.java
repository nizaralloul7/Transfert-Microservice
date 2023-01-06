package com.example.transfereservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Agent
{
    private Long id;
    private String nom;
    private String prenom;
    private String cin;
    private String telephone;
    private Date dateCreation;
    private double plafond;
}
