/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.mcblueparrot.client.installer;

/**
 *
 * @author maks
 */
public interface InstallStatusCallback {
    void setTextStatus(String status);
    void setProgressBarValues(int max, int cur);
    void setProgressBarIndeterminate(boolean indeterminate);
    void onDone(boolean okay);
}
