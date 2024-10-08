/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fpsjframe;

/**
 *
 * @author Dillon
 */
// Java program to play an Audio 
// file using Clip Object 
import java.io.File; 
import java.io.IOException; 
import java.util.Scanner; 

import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.Clip; 
import javax.sound.sampled.LineUnavailableException; 
import javax.sound.sampled.UnsupportedAudioFileException; 

public class Audio {
    private Clip clip;

    // No-argument constructor
    public Audio() {
        // No initialization here
    }

    // Method to load the audio file
    public void loadAudio(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = new File(filePath);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        clip = AudioSystem.getClip();
        clip.open(audioStream);
    }

    // Method to play the sound
    public void playSound() {
        if (clip != null) {
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            System.out.println("Audio not loaded. Call loadAudio() first.");
        }
    }

    // Method to stop the sound
    public void stopSound() {
        if (clip != null) {
            clip.stop();
        } else {
            System.out.println("Audio not loaded. Call loadAudio() first.");
        }
    }
}