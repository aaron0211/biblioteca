package com.aaron.actividad.domain;

public class Usuarios {
    private String usuario;
    private String pass;

    public Usuarios(String usuario, String pass){
        this.usuario = usuario;
        this.pass = pass;
    }

    public Usuarios(){

    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
