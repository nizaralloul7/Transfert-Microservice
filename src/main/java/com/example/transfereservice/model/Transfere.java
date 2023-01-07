package com.example.transfereservice.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import com.example.transfereservice.enums.StatusTransfere;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transfere
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @JsonIgnore
    private Long id;
    private String reference;
    private double montant;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateExpiration;
    private String referenceAgent;
    private String referenceClientDonneur;
    private String referenceClientBeneficiaire;
    private StatusTransfere status;
    @JsonIgnore
    private String codePinTransfere;
    private String moyenDeTransfert;
}
