/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.solclient.installer;

/**
 *
 * @author maks
 */
public class InstallerException extends Exception{
    public InstallerException(int reason, Throwable cause) {
        super("A launcher exception has occured",cause);
    }
}
