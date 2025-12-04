package br.com.brisabr.helpdesk_api.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSimpleDTO {
    private Long id;
    private String nome;
}