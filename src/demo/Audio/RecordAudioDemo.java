package demo.Audio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

public class RecordAudioDemo extends JFrame
{
    boolean isRecording;
    JButton btnRecord;
    
    public static void main(String[] args)
    {
        RecordAudioDemo rad = new RecordAudioDemo();
        rad.setVisible(true);
    }
    
    public RecordAudioDemo()
    {
        //set initial state to not recording
        isRecording = false;
        
        //frame settings
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Record Audio");
        this.setSize(300, 300);
        
        //add record button
        btnRecord = new JButton("Record");
        
        //add action listener to that button
        btnRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                recordButtonPressed();
            }
        });
        
        //add this button to the frame
        this.add(btnRecord);
    }
    
    /**
     * This function starts when record button is pressed
     */
    public void recordButtonPressed()
    {
        //if program is recording, stop it
        if(isRecording)
        {
            isRecording = false;
            btnRecord.setText("Stopped");
        }
        //otherwise start the recording
        else
        {
            try {
                isRecording = true;
                btnRecord.setText("Record");
                
                //audio settings
                float sampleRate = 8000;
                int sampleSizeInBits = 8;
                int channel = 1;
                boolean signed = true;
                boolean bigEndian = true;
                
                //create an audio format for the recording
                AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
                
                //get the input dataline
                DataLine.Info tdi = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(tdi);
                
                //get the output dataline
                DataLine.Info sdi = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine sline = (SourceDataLine) AudioSystem.getLine(sdi);
                
                //open and start both line
                line.open(format);
                line.start();
                sline.open(format);
                sline.start();
                
                //initialize buffer
                int bufferSize = (int) (format.getSampleRate()*format.getFrameSize());
                byte buffer[] = new byte[bufferSize];
                byte previousBuffer[] = new byte[bufferSize];
                
                //create a new thread
                SwingWorker recordThread = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        while(isRecording)
                        {
                            for(int i = 0; i < bufferSize; i++)
                                previousBuffer[i] = buffer[i];
                            
                            //read data from input line
                            int count = line.read(buffer, 0, buffer.length);
                            
                            //write data to output line
                            if(count > 0)
                            {
                                sline.write(buffer, 0, buffer.length);
                                sline.write(previousBuffer, 0, buffer.length);
                            }
                        }
                        
                        //drain the rest of output line
                        sline.drain();
                        
                        //close the lines
                        sline.close();
                        line.close();
                        return null;
                    }
                };
                
                //start the thread
                recordThread.execute();
                
                //test
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("test.wav"));
                AudioFormat musicFormat = ais.getFormat();
                SourceDataLine musicOut = AudioSystem.getSourceDataLine(musicFormat);
                musicOut.open();
                musicOut.start();
                byte testbuffer[] = new byte[(int)musicFormat.getSampleRate()*musicFormat.getFrameSize()];
                
                //create another thread
                SwingWorker playThread = new SwingWorker<Void, Void>()
                {
                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        
                        while(true)
                        {
                            int c = ais.read(testbuffer, 0, testbuffer.length);
                            
                            if(c > 0)
                                musicOut.write(testbuffer, 0, testbuffer.length);
                            else
                                break;
                        }
                        System.out.println("Music finished");
                        return null;
                    }
                };
                
                //start the thread
                playThread.execute();
            }
            catch (LineUnavailableException ex) {
                Logger.getLogger(RecordAudioDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch(UnsupportedAudioFileException ex)
            {
                Logger.getLogger(RecordAudioDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch(IOException ex)
            {
                Logger.getLogger(RecordAudioDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
