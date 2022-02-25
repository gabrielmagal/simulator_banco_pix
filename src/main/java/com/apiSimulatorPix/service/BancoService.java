package com.apiSimulatorPix.service;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BancoService extends PanacheEntity {
    
    @Column(length = 30, unique = true)
    private String email;

    @Column(length = 30, unique = true)
    private String name;
    
    @Column(length = 10)
    private Float money;

}
