import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class MusicPlayerGUI extends JFrame {

    private Clip clip;

    public MusicPlayerGUI(String musicPath) {
        setTitle("Music Player");
        setSize(300, 100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Playing: " + new File(musicPath).getName(), SwingConstants.CENTER);
        add(label);

        playMusic(musicPath);
        setVisible(true);
    }

    private void playMusic(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();  // Plays once; use clip.loop(Clip.LOOP_CONTINUOUSLY) for looping

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            JOptionPane.showMessageDialog(this, "Error playing audio: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Replace with your own path to a .wav file
        String musicFilePath = "C:\\path\\to\\your\\music.wav";
        new MusicPlayerGUI(musicFilePath);
    }
}