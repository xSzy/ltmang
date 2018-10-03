package demo.Audio;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

public class PlayAudioFromFileDemo
{
    public static void main(String[] args)
    {
        try
        {
            //play using clip
            //clip = sound loaded to memory
            
            //get input stream from file
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File("test.wav"));
            
            //get the clip
            Clip clip = AudioSystem.getClip();
            
            //load to memory
            clip.open(ais);
            
            //start
            clip.start();
            
            //make the thread sleep when the sound plays
            Thread.sleep(clip.getMicrosecondLength()/1000);
        }
        catch (UnsupportedAudioFileException ex)
        {
            Logger.getLogger(PlayAudioFromFileDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(PlayAudioFromFileDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (LineUnavailableException ex)
        {
            Logger.getLogger(PlayAudioFromFileDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(PlayAudioFromFileDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
