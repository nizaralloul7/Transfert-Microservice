package com.example.transfereservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransfereRequest
{
    private double montant;
    private String referenceAgent;
    private String referenceClientDonneur;
    private String referenceClientBeneficiaire;

}
