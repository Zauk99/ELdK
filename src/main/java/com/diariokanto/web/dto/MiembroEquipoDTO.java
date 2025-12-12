package com.diariokanto.web.dto;

import lombok.Data;

@Data
public class MiembroEquipoDTO {
    private Long id;
    private String nombrePokemon; // "Charizard"
    private String mote;          // "Fuego"
    private String objeto;        // "Restos"
    private String habilidad;     // "Mar Llamas"
    private String naturaleza;    // "Firme"
    
    // Los 4 movimientos
    private String movimiento1;
    private String movimiento2;
    private String movimiento3;
    private String movimiento4;

    private int hpEv;
    private int attackEv;
    private int defenseEv;
    private int spAttackEv;
    private int spDefenseEv;
    private int speedEv;
}